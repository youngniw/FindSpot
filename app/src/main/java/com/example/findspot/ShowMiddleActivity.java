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
import com.example.findspot.request.GetTimeMiddleRequest;

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
    ArrayList<PositionItem> list;   //실제 중간지점을 찾을 사람들의 설정 위치 등의 정보
    StationInfo currentStation;     //거리상 중간지점과 가장 가까운 지하철역 정보(고정됨)
    ArrayList<StationInfo> nearStationList;     //현재 기준이 되는 지하철역의 반경 _km 내에 있는 지하철역 리스트(가변적)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmiddle);

        final MapView mapView = new MapView(this);
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

        for (PositionItem pi : list) {      //TODO: for문 바꿨으니 확인바람
            avgX += pi.getLongitude();
            avgY += pi.getLatitude();
        }
        /*
        for (int i = 0; i < list.size(); i++) {
            avgX += list.get(i).getLongitude();
            avgY += list.get(i).getLatitude();
        }
         */
        avgX /= list.size();
        avgY /= list.size();

        //시간/거리 기준에 따라 중간지점 보여주기
        String getExtra_standard = getIntent().getStringExtra("standard_tag");
        if (getExtra_standard.equals("time")) {       //시간기준일 경우
            nearStationList = new ArrayList<StationInfo>();     //현재 기준이 되는 지하철역의 반경 내의 지하철 역 리스트
            ArrayList<CandidateTimePosition> searchTPositions = new ArrayList<>();    //현재 기준이 되는 지하철역보다 시간 오차가 작은 지하철역들을 저장(더 조사해야하는 역들)
            ArrayList<CandidateTimePosition> resultTPositions = new ArrayList<>();    //찾아본 지하철역 중 시간 오차가 10보다 작거나 같은 지하철역들이 저장됨

            //TODO: 앞으로 알고리즘 짜야됨

            GetStationThread getStationThread = new GetStationThread(true, avgX, avgY, 2);
            getStationThread.start();
            try {
                getStationThread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
            /*
            try {   //(avgX와 avgY)와 가까운 지하철 역과 그 역의 반경 2km내의 지하철 역 리스트를 요청 및 전달받음
                new ShowMiddleActivity.GetStationTask(true, avgX, avgY, 2).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

             */
            //TODO: 싱크를 맞춰야함. try문을 수행해 response를 받기 전에 아래의 코드를 수행하기에 nullPointer오류가 발생함
            CandidateTimePosition current = new CandidateTimePosition(this, list, currentStation.getStationX(), currentStation.getStationY());  //거리 상 중간지점(초기 기준 위치)과 가장 가까운 지하철역의 사용자들 소요시간 정보 저장(가변적)
            CandidateTimePosition minTimeGapS = new CandidateTimePosition();   //이후에 주변역들 중 소요시간 최소인 곳을 저장함

            if (current.getTimeGap() <= 10)  //거리상 중간 지점의 사용자들의 소요 시간 오차가 10보다 작은 경우
                resultTPositions.add(current);       //결과 후보 중에 하나로 저장함

            for (StationInfo nearS : nearStationList) {     //거리상 중간지점과 가장 가까운 역을 중심으로 반경 2km이내에 있는 역에 대해 (TODO: for문 바꿨으니 확인바람)
                CandidateTimePosition tmpStation = new CandidateTimePosition(this, list, nearS.getStationX(), nearS.getStationY());

                if (tmpStation.getTimeGap() < minTimeGapS.getTimeGap())
                    minTimeGapS = tmpStation;   //주변 역들 중 소요시간 최소로 저장함

                if (tmpStation.getTimeGap() <= 10) { //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                    resultTPositions.add(tmpStation);   //10보다 작거나 같으므로 결과 후보로 추가
                    searchTPositions.add(tmpStation);   //이후에 이 tmpStation역을 중심으로 반경 내에 이 역보다 최대/최소 소요시간의 차가 더 작은 역이 있을 수 있으므로 검색후보로 추가
                }
                else if (tmpStation.getTimeGap() < current.getTimeGap())
                    searchTPositions.add(tmpStation);   //거리기준 중간지점역보다 소요시간 오차가 더 작으므로 결과 후보는 아니더라도 기준 검색 후보로 추가됨
            }

            //주변 역들의 소요시간 차가 10보다 크고, 거리상 중간지점보다 큰 경우(주변 역들 중 가장 소요시간 차가 작았던 지하철역을 중심으로 다시 조사함)
            if (searchTPositions.size()==0 && nearStationList.size()!=0) {
                //minTimeGapS를 중심으로 다시 반경 2km내에 주변 지하철 역을 조사함(try문을 다시 써야함)
                //TODO: 이 코드 역시 동기를 맞춰야함
                GetStationThread getStationThread2 = new GetStationThread(false, minTimeGapS.getResultPositionX(), minTimeGapS.getResultPositionY(), 2);
                getStationThread2.start();
                try {
                    getStationThread2.join();
                } catch (InterruptedException e) { e.printStackTrace(); }

                //minTimGapS를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
                for (StationInfo nextNearS : nearStationList) {     //TODO: for문 바꿨으니 확인바람
                    CandidateTimePosition tmpStationIn = new CandidateTimePosition(this, list, nextNearS.getStationX(), nextNearS.getStationY());

                    if (tmpStationIn.getTimeGap() <= 10)    //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                        resultTPositions.add(tmpStationIn);   //10보다 작거나 같으므로 결과 후보로 추가
                }
            }
            else {      //조사할 대상 리스트에 지하철 역이 포함되어 있는 경우(TODO: for문 바꿨으니 확인바람)
                for (CandidateTimePosition searchS : searchTPositions) {    //조사할 대상 리스트에 포함된 지역을 중심으로 하여 반경 2km이내의 지하철 역을 조사함
                    current = searchS;

                    //TODO: 이 코드 역시 동기를 맞춰야함
                    GetStationThread getStationThread3 = new GetStationThread(false, current.getResultPositionX(), current.getResultPositionY(), 2);
                    getStationThread3.start();
                    try {
                        getStationThread3.join();
                    } catch (InterruptedException e) { e.printStackTrace(); }

                    //current(조사 대상 리스트의 지하철 역 중 하나)를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
                    for (StationInfo nextNearS : nearStationList) {     //TODO: for문 바꿨으니 확인바람
                        CandidateTimePosition tmpStationIn = new CandidateTimePosition(this, list, nextNearS.getStationX(), nextNearS.getStationY());

                        if (tmpStationIn.getTimeGap() <= 10)    //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                            resultTPositions.add(tmpStationIn);   //10보다 작거나 같으므로 결과 후보로 추가
                    }
                }
            }

            //resultTPositions리스트에 최대 및 최소 소요시간의 오차가 10보다 작거나 같은 지하철 역들이 포함됨(TODO: 추가 코드 필요)
            Collections.sort(resultTPositions);     //오름차순 정렬함
            //TODO: 출력되게 함

                /*
                //콜백함수의 순서가 달라질 수 있으므로 (endX, endY)와의 직선거리를 저장함
                Location end = new Location("point end");   //종점에 대한 위치를 설정
                end.setLatitude(tmpPositionY);
                end.setLongitude(tmpPositionX);

                ArrayList<Integer> distance = new ArrayList<>();
                for (int i=0; i<list.size(); i++) {
                    Location tmp = new Location("point tmp");
                    tmp.setLatitude(list.get(i).getLatitude());
                    tmp.setLongitude(list.get(i).getLongitude());
                    distance.add(Math.round(end.distanceTo(tmp)));      //각 위치와 종점과의 거리를 계산함
                }

                 */

        }
        else if (getExtra_standard.equals("distance")) {  //거리기준일 경우
            MapPOIItem middle_d = new MapPOIItem();
            middle_d.setItemName("거리 중간지점");   //ping 선택 후 말풍선에 보여질 내용
            middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(avgY, avgX));     //ping 위치 지정
            middle_d.setMarkerType(MapPOIItem.MarkerType.YellowPin);    //기본 마커 타입
            middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            middle_d.setCustomImageResourceId(R.drawable.middlepin_64);
            middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            mapView.addPOIItem(middle_d);   //지도에 ping 추가

            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(avgY, avgX), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
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

    /*
    //입력한 값들에 대한 회원가입을 위한 작업 수행함
    public class GetStationTask extends AsyncTask<String, Void, String> {
        boolean isDistanceMiddle;
        double x = 0.0;
        double y = 0.0;
        int radius = 0;

        GetStationTask(boolean isDistanceMiddle, double x, double y, int radius) {
            this.isDistanceMiddle = isDistanceMiddle;
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
                            nearStationList.add(nearStation);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetTimeMiddleRequest getMiddleRequest = new GetTimeMiddleRequest(isDistanceMiddle, String.valueOf(x), String.valueOf(y), radius, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queue = Volley.newRequestQueue(ShowMiddleActivity.this);
            queue.add(getMiddleRequest);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
    */
    public class GetStationThread extends Thread {
        boolean isDistanceMiddle = true;
        double x = 0.0;
        double y = 0.0;
        int radius = 0;

        GetStationThread(boolean isDistanceMiddle, double x, double y, int radius) {
            this.isDistanceMiddle = isDistanceMiddle;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        @Override
        public void run() {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);       //중간지점 가장 근처의 가까운 역과 그 역 반경 1km이내의 지하철 역 정보를 받음
                        //중간 지점과 가장 가까운 역 정보 저장
                        String simularStationName = jsonObject.getJSONObject("simularStation").getString("station");
                        double simularX = jsonObject.getJSONObject("simularStation").getDouble("x");
                        double simularY = jsonObject.getJSONObject("simularStation").getDouble("y");
                        currentStation = new StationInfo(simularStationName, simularX, simularY);

                        //중간 지점과 가장 가까운 근처 반경이내의 역 정보 리스트에 저장
                        JSONArray nearStationArray = jsonObject.getJSONArray("nearStation");
                        for (int i=0; i<nearStationArray.length(); i++) {
                            String nearStationName = nearStationArray.getJSONObject(i).getString("station");
                            double nearX = nearStationArray.getJSONObject(i).getDouble("x");
                            double nearY = nearStationArray.getJSONObject(i).getDouble("y");
                            StationInfo nearStation = new StationInfo(nearStationName, nearX, nearY);
                            nearStationList.add(nearStation);
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetTimeMiddleRequest getMiddleRequest = new GetTimeMiddleRequest(isDistanceMiddle, String.valueOf(x), String.valueOf(y), 2, responseListener);    //반경을 1km로 줌
            RequestQueue queue = Volley.newRequestQueue(ShowMiddleActivity.this);
            queue.add(getMiddleRequest);
        }
    }
}
