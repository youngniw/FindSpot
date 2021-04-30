package com.example.findspot;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.request.GetDMiddleRequest;
import com.example.findspot.request.GetTMiddleRequest;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static com.example.findspot.ChoiceGPSRandomActivity.list_random;
import static com.example.findspot.ChoiceGPSGroupActivity.list_group;

public class ShowMiddleActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    MapView mapView;
    ArrayList<PositionItem> list;   //실제 중간지점을 찾을 사람들의 설정 위치 등의 정보
    StationInfo currentStation;     //거리상 중간지점과 가장 가까운 지하철역 정보(고정됨)
    ArrayList<StationInfo> nearStationList;     //현재 기준이 되는 지하철역의 반경 _km 내에 있는 지하철역 리스트(가변적)
    ArrayList<ArrayList<StationInfo>> nextSearchList;     //거리상 중간 지점 이후 그 근처의 지하철 역을 기준으로 하는 그 근처의 리스트들 목록
    static int countFinishLoop = 0;
    static CandidateTimePosition current;
    static CandidateTimePosition minTimeGapS;
    static ArrayList<CandidateTimePosition> resultTPositions;  //소요시간 최대 및 최소 오차가 10 이하인 역들
    static ArrayList<CandidateTimePosition> searchTPositions;  //(더 조사해야하는 역들)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmiddle);

        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.shownmiddle_mapview);
        mapViewContainer.addView(mapView);

        ImageButton wholegps = findViewById(R.id.shownmiddle_viewAllUser);
        wholegps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.fitMapViewAreaToShowAllPOIItems();
            }
        });


        //이전 Activity에 따라 사용할 list 설정
        String getExtra_activity = getIntent().getStringExtra("activity_tag");
        list = new ArrayList<>();
        if (getExtra_activity.equals("random")) list = list_random;
        else if (getExtra_activity.equals("group")) list = list_group;

        //전체 사용자 ping으로 나타내기
        for (int i = 0; i < list.size(); i++) {
            MapPOIItem user_ping = new MapPOIItem();     //핑에 대한 객체(속성인 itemname은 핑 이름)
            if (getExtra_activity.equals("random")) user_ping.setItemName(String.valueOf(i+1));   //ping 선택 후 말풍선에 보여질 내용(random: 순번) ex. 1, 2,...
            else if (getExtra_activity.equals("group")) user_ping.setItemName(list.get(i).getUserName());    //ping 선택 후 말풍선에 보여질 내용(group: 닉네임)
            user_ping.setMapPoint(MapPoint.mapPointWithGeoCoord(list.get(i).getLatitude(), list.get(i).getLongitude()));     //ping 위치 지정(위도 y, 경도 x)
            user_ping.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            user_ping.setCustomImageResourceId(R.drawable.peoplepin_50);
            //user_ping.setShowDisclosureButtonOnCalloutBalloon(false); // 말풍선 옆에 꺽쇠 표시 안함
            mapView.addPOIItem(user_ping);   //지도에 ping 추가
        }
        mapView.setPOIItemEventListener(this);

        //거리 기준 중간지점 계산히기
        double avgX = 0.0, avgY = 0.0;

        for (PositionItem pi : list) {
            avgX += pi.getLongitude();
            avgY += pi.getLatitude();
        }
        avgX /= list.size();
        avgY /= list.size();

        //거리 기준에 따른 중간 지점의 가장 근접한 지하철역과 그 역의 주변 지하철역 서버로부터 받기
        nearStationList = new ArrayList<StationInfo>();     //기준이 되는 지하철역의 반경 내의 지하철 역 리스트

        //거리/시간 기준에 따라 중간지점 보여주기
        String getExtra_standard = getIntent().getStringExtra("standard_tag");
        if (getExtra_standard.equals("distance")) {  //거리기준일 경우
            try {   //(avgX와 avgY)의 근처 5개 지하철 역 리스트를 요청 및 전달받음(서버에서 5개를 전달함) -> 핑으로 주변 5개의 역을 보여줌
                new ShowMiddleActivity.GetDStationTask(avgX, avgY).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

            MapPOIItem middle_d = new MapPOIItem();
            middle_d.setItemName("거리상 중간지점");   //ping 선택 후 말풍선에 보여질 내용
            middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(avgY, avgX));     //ping 위치 지정
            middle_d.setMarkerType(MapPOIItem.MarkerType.YellowPin);    //기본 마커 타입
            middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            middle_d.setCustomImageResourceId(R.drawable.middlepin_75);
            middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            mapView.addPOIItem(middle_d);   //지도에 ping 추가

            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(avgY, avgX), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
        }
        else if (getExtra_standard.equals("time")) {       //시간기준일 경우
            searchTPositions = new ArrayList<>();    //현재 기준이 되는 지하철역보다 시간 오차가 작은 지하철역들을 저장(더 조사해야하는 역들)
            resultTPositions = new ArrayList<>();    //찾아본 지하철역 중 시간 오차가 10보다 작거나 같은 지하철역들이 저장됨
            minTimeGapS = new CandidateTimePosition();   //이후에 주변역들 중 소요시간 최소인 곳을 저장함

            //위의 try문을 통해 거리상 중간 지점 바로 근처의 지하철역과 그 지하철 역의 반경 2km 내의 지하철역 리스트를 서버로부터 받음
            try {   //(avgX와 avgY)와 가까운 지하철 역과 그 역의 반경 2km내의 지하철 역 리스트를 요청 및 전달받음
                new ShowMiddleActivity.GetTStationTask(true, avgX, avgY, 2).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

            //response에서 조사를 하는 함수를 호출함
            //후보 3개까지 나오고 모든 수행은 search함수에서 함
            //이후의 모든 작업은 아래의 함수들을 통해 수행함
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        //지하철역이면 추천스팟 보여주는 Activity로 intent 전달
        //나머지는 포커싱 및 zoom 4레벨
        mapView.setMapCenterPointAndZoomLevel(mapPOIItem.getMapPoint(), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
    }
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        //지하철역이면 추천스팟 보여주는 Activity로 intent 전달
        //나머지는 포커싱 및 zoom 4레벨
        mapView.setMapCenterPointAndZoomLevel(mapPOIItem.getMapPoint(), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
    }
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) { }
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) { }

    //입력한 값들에 대한 거리 기준 중간지점을 위한 작업 수행함
    public class GetDStationTask extends AsyncTask<String, Void, String> {
        double x = 0.0;
        double y = 0.0;

        GetDStationTask(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);   //전달한 위치 근처의 5개 지하철 역 정보(이름, 경도, 위도)를 받음

                        //5개의 지하철역의 핑 설정
                        MapPOIItem station = new MapPOIItem();
                        station.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        station.setCustomImageResourceId(R.drawable.subwaypin_75);
                        station.setSelectedMarkerType(null);                       //선택 효과 마커 타입

                        //근처 역 정보 리스트에 저장(5개 값)
                        JSONArray nearStationArray = jsonObject.getJSONArray("nearStation");
                        for (int i=0; i<nearStationArray.length(); i++) {
                            String nearStationName = nearStationArray.getJSONObject(i).getString("station");
                            double nearX = nearStationArray.getJSONObject(i).getDouble("x");
                            double nearY = nearStationArray.getJSONObject(i).getDouble("y");
                            StationInfo nearStation = new StationInfo(nearStationName, nearX, nearY);
                            nearStationList.add(nearStation);
                            Log.i("nearStation", nearStationName);

                            //핑 찍기
                            station.setItemName(nearStationName+"역");   //ping 선택 후 말풍선에 보여질 내용
                            station.setMapPoint(MapPoint.mapPointWithGeoCoord(nearY, nearX));     //ping 위치 지정
                            mapView.addPOIItem(station);   //지도에 ping 추가
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetDMiddleRequest getDMiddleRequest = new GetDMiddleRequest(String.valueOf(x), String.valueOf(y), responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueD = Volley.newRequestQueue(ShowMiddleActivity.this);
            queueD.add(getDMiddleRequest);

            return null;
        }
    }


    //입력한 값들에 대한 시간 기준 중간지점을 위한 작업 수행함
    public class GetTStationTask extends AsyncTask<String, Void, String> {
        boolean isDistanceMiddle;
        int index = -1;
        double x = 0.0;
        double y = 0.0;
        int radius = 0;

        GetTStationTask(boolean isDistanceMiddle, double x, double y, int radius) {
            this.isDistanceMiddle = isDistanceMiddle;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        GetTStationTask(boolean isDistanceMiddle, int index, double x, double y, int radius) {
            this.isDistanceMiddle = isDistanceMiddle;
            this.index = index;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);   //(거리상 중간지점 가장 근처의 역 or 중심으로 하고자하는 역)과 그 역 반경 radius(km)이내의 지하철 역 정보들을 받음
                        if (jsonObject.getBoolean("isDistanceMiddle")) {   //거리상 중간지점인 위치를 전달해 해당 위치와 가장 가까운 지하철역과 그 역 주변 반경 radius내의 지하철역 리스트를 받기 위한 경우
                            //전달한 위치인 거리상 중간 지점과 가장 가까운 역 정보 저장
                            String simularStationName = jsonObject.getJSONObject("simularStation").getString("station");
                            double simularX = jsonObject.getJSONObject("simularStation").getDouble("x");
                            double simularY = jsonObject.getJSONObject("simularStation").getDouble("y");
                            currentStation = new StationInfo(simularStationName, simularX, simularY);
                        }   //포함되지 않는 경우: 인자로 전달한 값이 역과 값이 일치할 경우(-> 거리상 중간지점이 아닌 실제 역 주변의 지하철 역을 리스트로 받기 위한 경우)

                        //중간 지점과 가장 가까운 근처 반경이내의 역 정보 리스트에 저장
                        JSONArray nearStationArray = jsonObject.getJSONArray("nearStation");
                        for (int i=0; i<nearStationArray.length(); i++) {
                            String nearStationName = nearStationArray.getJSONObject(i).getString("station");
                            double nearX = nearStationArray.getJSONObject(i).getDouble("x");
                            double nearY = nearStationArray.getJSONObject(i).getDouble("y");
                            StationInfo nearStation = new StationInfo(nearStationName, nearX, nearY);

                            if (index == -1)    //거리상 중간 지점에 대한 근처 역 리스트 or minTimeGaps를 기준으로 하는 근처 역 리스트에 추가
                                nearStationList.add(nearStation);
                            else    //조사할 대상 리스트(searchTPositions)에 지하철 역이 포함되어 있는 경우
                                nextSearchList.get(index).add(nearStation);

                            Log.i("stationName", nearStationName);
                        }

                        if (jsonObject.getBoolean("isDistanceMiddle"))
                            search();   //거리상 중간 지점와 가장 가까운 역과 그 역의 근처 지하철 역들에 대한 소요시간을 구해 최적화된 중간지점을 찾음
                        else {
                            if (index == -1)
                                searchNextNextNext(false);
                            else
                                searchNextNextNext(true);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetTMiddleRequest getTMiddleRequest = new GetTMiddleRequest(isDistanceMiddle, String.valueOf(x), String.valueOf(y), radius, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueT = Volley.newRequestQueue(ShowMiddleActivity.this);
            queueT.add(getTMiddleRequest);

            return null;
        }
    }

    //거리 상 중간지점(초기 기준 위치)과 가장 가까운 지하철역의 사용자들 소요시간 정보 저장하는 current 생성 (가변적)
    public void search() {
        current = new CandidateTimePosition(this, this, 0, list, currentStation.getStation(), currentStation.getStationX(), currentStation.getStationY());
    }

    //거리 상 중간지점 반경 2km내의 지하철 역들 정보 계산
    void searchNext() {
        countFinishLoop = nearStationList.size();       //동기화를 맞추기 위한 사이즈 확인

        for (StationInfo nearS : nearStationList) {     //거리상 중간지점과 가장 가까운 역을 중심으로 반경 2km이내에 있는 역에 대해
            CandidateTimePosition tmpStation = new CandidateTimePosition(this, this, 1, list, nearS.getStation(), nearS.getStationX(), nearS.getStationY());
        }
    }

    //searchTPositions에 속한 역을 기준으로 다시 주변 역 검사
    void searchNextNext() {
        //거리기준 중간지점 반경 2km내의 지하철역들 확인 완료 이후
        for (CandidateTimePosition i : searchTPositions)
            Log.i("stationIn", String.valueOf(i.getTimeGap()));


        //주변 역들의 소요시간 차가 10보다 크고, 거리상 중간지점보다 큰 경우(주변 역들 중 가장 소요시간 차가 작았던 지하철역을 중심으로 다시 조사함)
        if (searchTPositions.size()==0 && nearStationList.size()!=0) {
            //minTimeGapS를 중심으로 다시 반경 2km내에 주변 지하철 역을 조사함(try문을 다시 써야함)

            try {
                new ShowMiddleActivity.GetTStationTask(false, minTimeGapS.getResultPositionX(), minTimeGapS.getResultPositionY(), 2).execute().get();
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }

        }
        else {      //조사할 대상 리스트에 지하철 역이 포함되어 있는 경우
            nextSearchList = new ArrayList<>();

            for (int i=0; i<searchTPositions.size(); i++) {    //조사할 대상 리스트에 포함된 지역을 중심으로 하여 반경 2km이내의 지하철 역을 조사함
                nextSearchList.add(new ArrayList<StationInfo>());
                current = searchTPositions.get(i);

                try {
                    new GetTStationTask(false, i, current.getResultPositionX(), current.getResultPositionY(), 2).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
            }
        }
    }

    void searchNextNextNext(boolean isNextSearch) {
        if (!isNextSearch) {    //searchTPositions에 값이 존재하지 않으므로 minTimeGaps로 조사함
            Log.i("stationNoIsNextSearch", "--------->_<---------");
            countFinishLoop = nearStationList.size();       //동기화를 맞추기 위한 사이즈 확인

            //minTimGapS를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
            for (StationInfo nextNearS : nearStationList) {
                CandidateTimePosition tmpStationIn = new CandidateTimePosition(this, this, 2, list, nextNearS.getStation(), nextNearS.getStationX(), nextNearS.getStationY());
            }
        }

        else {
            Log.i("stationIsNextSearch", "--------->_<---------");
            for (ArrayList<StationInfo> nextNearList : nextSearchList) {
                countFinishLoop += nextNearList.size();
            }

            //current(조사 대상 리스트의 지하철 역 중 하나)를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
            boolean isTooMuch = false;
            for (ArrayList<StationInfo> nextNearList : nextSearchList) {
                if (resultTPositions.size() >= 8) {
                    isTooMuch = true;
                    break;
                }

                for (StationInfo nextNearS: nextNearList) {
                    CandidateTimePosition tmpStationIn = new CandidateTimePosition(this, this, 2, list, nextNearS.getStation(), nextNearS.getStationX(), nextNearS.getStationY());
                }
            }

            if (isTooMuch)
                searchNextNextNextNext();
        }
    }

    void searchNextNextNextNext() {
        Log.i("stationNextNextNextNext", "--------->_<---------");
        if (resultTPositions.size() == 0) {     //minTimeGaps의 주변 역까지 조사를 했지만 result후보 역이 없을 때
            if (minTimeGapS.getTimeGap() > current.getTimeGap())
                resultTPositions.add(current);
            else
                resultTPositions.add(minTimeGapS);
        }

        //resultTPositions리스트에 최대 및 최소 소요시간의 오차가 10보다 작거나 같은 지하철 역들이 포함됨
        Collections.sort(resultTPositions);     //오름차순 정렬함


        for (int i=0; i<resultTPositions.size(); i++) {
            Log.i("stationResult", resultTPositions.get(i).getStationName());
        }

        //화면에 결과로 포함된 지하철 역 핑 출력
        MapPOIItem station = new MapPOIItem();
        station.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        station.setCustomImageResourceId(R.drawable.middlepin_75);
        station.setSelectedMarkerType(null);                       //선택 효과 마커 타입
        station.setItemName(resultTPositions.get(0).getStationName() + "역");   //ping 선택 후 말풍선에 보여질 내용
        station.setMapPoint(MapPoint.mapPointWithGeoCoord(resultTPositions.get(0).getResultPositionY(), resultTPositions.get(0).getResultPositionX()));     //ping 위치 지정
        mapView.addPOIItem(station);

        //가장 소요시간 차가 작은 위치를 기준으로 화면의 중심점 및 줌레벨 설정
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(resultTPositions.get(0).getResultPositionY(), resultTPositions.get(0).getResultPositionX()), 4, false);

        station.setCustomImageResourceId(R.drawable.subwaypin_75);  //가장 짧은 지하철 역 이외의 나머지 역들
        //결과로 보일 역의 개수가 5보다 작을 때(TODO: 소은이--> 5개까지는 보이게 하자!)
        int size = resultTPositions.size() < 5 ? resultTPositions.size() : 5;
        for (int i=1; i<size; i++) {
            //거리 기준 중간 지점 역 출력
            station.setItemName(resultTPositions.get(i).getStationName() + "역");   //ping 선택 후 말풍선에 보여질 내용
            station.setMapPoint(MapPoint.mapPointWithGeoCoord(resultTPositions.get(i).getResultPositionY(), resultTPositions.get(i).getResultPositionX()));     //ping 위치 지정
            mapView.addPOIItem(station);   //지도에 ping 추가
        }
    }
}
