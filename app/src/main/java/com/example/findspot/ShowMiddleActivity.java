package com.example.findspot;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.adapter.PositionPagerAdapter;
import com.example.findspot.algorithm.MiddleDPointAlgorithm;
import com.example.findspot.data.PositionItemInfo;
import com.example.findspot.data.RouteInfo;
import com.example.findspot.data.StationInfo;
import com.example.findspot.request.GetDMiddleRequest;
import com.example.findspot.request.GetTMiddleRequest;
import com.example.findspot.request.SaveHistoryRequest;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import me.relex.circleindicator.CircleIndicator;

import static com.example.findspot.ChoiceGPSRandomActivity.list_random;
import static com.example.findspot.ChoiceGPSGroupActivity.list_group;
import static com.example.findspot.SelectWhomActivity.ghistory;
import static com.example.findspot.SelectWhomActivity.selectedGroup;
import static com.example.findspot.algorithm.MiddleDPointAlgorithm.calDistance;

public class ShowMiddleActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    static int countFinishLoop = 0;
    static CandidateTimePosition current;
    static CandidateTimePosition minTimeGapS;
    static ArrayList<CandidateTimePosition> resultTPositions;  //소요시간 최대 및 최소 오차가 10 이하인 역들
    static ArrayList<CandidateTimePosition> searchTPositions;  //(더 조사해야하는 역들)

    boolean isChangedHistory = false;
    ArrayList<PositionItemInfo> list;   //실제 중간지점을 찾을 사람들의 설정 위치 등의 정보
    StationInfo currentStation;     //거리상 중간지점과 가장 가까운 지하철역 정보(고정됨)
    ArrayList<StationInfo> nearStationList;     //현재 기준이 되는 지하철역의 반경 _km 내에 있는 지하철역 리스트(가변적) cf. 거리기준일 때는 결과 주변 지하철 역 리스트들
    ArrayList <ResultDistancePosition> resultDPositions;
    ArrayList<ArrayList<StationInfo>> nextSearchList;     //거리상 중간 지점 이후 그 근처의 지하철 역을 기준으로 하는 그 근처의 리스트들 목록
    HashMap<String, Boolean> visitedStations;   //지하철 역 중복 계산 방지

    MapView mapView;
    ViewPager pager;
    PositionPagerAdapter adapter;
    CircleIndicator indicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmiddle);

        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.shownmiddle_mapview);
        mapViewContainer.addView(mapView);

        ImageButton wholegps = findViewById(R.id.shownmiddle_viewAllUser);
        wholegps.setOnClickListener(v -> mapView.fitMapViewAreaToShowAllPOIItems());


        //이전 Activity에 따라 사용할 list 설정
        String getExtra_activity = getIntent().getStringExtra("activity_tag");
        list = new ArrayList<>();
        if (getIntent().getStringExtra("isHistory").equals("true")) {       //history가 있을 시, 사용자가 이전 history에서 선택한 위치를 저장함(사용자 픽)
            list = ghistory.getUsersPick();
        }
        else {      //사용자가 선택한 위치 정보를 list에 저장
            if (getExtra_activity.equals("random")) list = list_random;
            else if (getExtra_activity.equals("group")) list = list_group;
        }

        searchTPositions = new ArrayList<>();    //현재 기준이 되는 지하철역보다 시간 오차가 작은 지하철역들을 저장(더 조사해야하는 역들)
        resultTPositions = new ArrayList<>();    //찾아본 지하철역 중 시간 오차가 10보다 작거나 같은 지하철역들이 저장됨

        pager = (ViewPager)findViewById(R.id.showmiddle_viewPager);
        indicator = (CircleIndicator) findViewById(R.id.indicator);

        //전체 사용자 ping으로 나타내기
        for (int i = 0; i < list.size(); i++) {
            MapPOIItem user_ping = new MapPOIItem();    //핑에 대한 객체(속성인 itemname은 핑 이름)
            if (getExtra_activity.equals("random"))     //ping 선택 후 말풍선에 보여질 내용(random: 순번) ex. 사람1, 사람2,...
                user_ping.setItemName("사람"+(i+1));
            else if (getExtra_activity.equals("group")) //ping 선택 후 말풍선에 보여질 내용(group: 닉네임)
                user_ping.setItemName(list.get(i).getUserName());
            user_ping.setMapPoint(MapPoint.mapPointWithGeoCoord(list.get(i).getLatitude(), list.get(i).getLongitude()));     //ping 위치 지정(위도 y, 경도 x)
            user_ping.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            user_ping.setCustomImageResourceId(R.drawable.peoplepin_50);
            mapView.addPOIItem(user_ping);   //지도에 ping 추가
        }
        mapView.setPOIItemEventListener(this);

        //중간지점 구하기
        MiddleDPointAlgorithm middleDPointAlgorithm = new MiddleDPointAlgorithm(list);
        double[] middle = middleDPointAlgorithm.calMiddlePoint();
        double middleDLat = middle[0];
        double middleDLong = middle[1];

        //거리 기준에 따른 중간 지점의 가장 근접한 지하철역과 그 역의 주변 지하철역 서버로부터 받기
        nearStationList = new ArrayList<>();     //기준이 되는 지하철역의 반경 내의 지하철 역 리스트

        //거리/시간 기준에 따라 중간지점 보여주기
        String getExtra_standard = getIntent().getStringExtra("standard_tag");
        if (getExtra_standard.equals("distance")) {     //거리기준일 경우(거리상 중간지점)
            MapPOIItem middle_d = new MapPOIItem();
            resultDPositions = new ArrayList<>();

            if (getIntent().getStringExtra("isHistory").equals("true")) {
                //결과를 출력하기 위해 resultDistancePositions 수동 생성
                ResultDistancePosition middleDistancePosition = new ResultDistancePosition(ghistory.getUsersPick().size(), "거리상 중간지점",
                        ghistory.getMiddleLat(), ghistory.getMiddleLong(), ghistory.getMiddleTakeTOrD());

                for (int i=0; i<ghistory.getHisStations().size(); i++) {      //근처 역 정보 저장(5개) -> 중간지점에 해당하는 역이 없고 근처 5개의 역만 있음
                    ResultDistancePosition nearDistancePosition = new ResultDistancePosition(ghistory.getUsersPick().size(), ghistory.getHisStations().get(i).getStation(),
                            ghistory.getHisStations().get(i).getStationLat(), ghistory.getHisStations().get(i).getStationLong(), ghistory.getNearTakeTOrD().get(i).getUsersTakeTOrD());
                    resultDPositions.add(nearDistancePosition);
                }
                Collections.sort(resultDPositions);     //오차거리에 따른 오름차순으로 정렬
                resultDPositions.add(0, middleDistancePosition);    //중간지점역 0번째로 수동 저장

                adapter = new PositionPagerAdapter(this, list, resultDPositions);
                pager.setAdapter(adapter);
                indicator.setViewPager(pager);
                adapter.registerDataSetObserver(indicator.getDataSetObserver());
                adapter.notifyDataSetChanged();

                middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(ghistory.getMiddleLat(), ghistory.getMiddleLong()));     //ping 위치 지정
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(ghistory.getMiddleLat(), ghistory.getMiddleLong()), 4, false);   //history의 위치 기준으로 화면의 중심점 및 줌레벨 설정

                //중간지점의 근처 지하철 역 출력
                for (int i=1; i<resultDPositions.size(); i++) {
                    MapPOIItem stationPing = new MapPOIItem();     //핑에 대한 객체(속성인 itemname은 핑 이름)
                    stationPing.setItemName(resultDPositions.get(i).getStationName()+"역");    //ping 선택 후 말풍선에 보여질 내용(지하철역 이름)
                    stationPing.setMapPoint(MapPoint.mapPointWithGeoCoord(resultDPositions.get(i).getResultPositionLat(), resultDPositions.get(i).getResultPositionLong()));     //ping 위치 지정(위도 y, 경도 x)
                    stationPing.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    stationPing.setCustomImageResourceId(R.drawable.subwaypin_75);
                    mapView.addPOIItem(stationPing);   //지도에 ping 추가
                }
            }
            else {   //실제로 계산(평균)을 통해 구한 거리상 중간지점으로 출력되게 함
                ResultDistancePosition middleDistancePosition = new ResultDistancePosition(list.size(), "거리상 중간지점", middleDLat, middleDLong, calTakeDistance(new StationInfo("", middleDLat, middleDLong)));

                try {   //(middleDLong와 middleDLat)의 근처 5개 지하철 역 리스트를 요청 및 전달받음(서버에서 5개를 전달함) -> 핑으로 주변 5개의 역을 보여줌
                    new GetDStationTask(middleDistancePosition, middleDLat, middleDLong).execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

                middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(middleDLat, middleDLong));     //ping 위치 지정
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(middleDLat, middleDLong), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
            }
            middle_d.setItemName("거리상 중간지점");   //ping 선택 후 말풍선에 보여질 내용
            middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            middle_d.setCustomImageResourceId(R.drawable.middlepin_75);
            middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            mapView.addPOIItem(middle_d);   //지도에 ping 추가
        }
        else if (getExtra_standard.equals("time")) {       //시간기준일 경우
            if (getIntent().getStringExtra("isHistory").equals("true")){    //이전에 사용한 기록을 화면으로 보여줘야 함
                CandidateTimePosition middleTimePosition = new CandidateTimePosition(ghistory.getUsersPick().size(), ghistory.getMiddleTakeTOrD(), ghistory.getMiddleSName());
                resultTPositions.add(middleTimePosition);    //중간지점역 수동 저장

                for (int i=0; i<ghistory.getHisStations().size(); i++) {      //근처 역 정보 저장
                    CandidateTimePosition tmpNearTimePosition = new CandidateTimePosition(ghistory.getUsersPick().size(), ghistory.getNearTakeTOrD().get(i).getUsersTakeTOrD(), ghistory.getHisStations().get(i).getStation());
                    resultTPositions.add(tmpNearTimePosition);
                }

                adapter = new PositionPagerAdapter(this, true, list, resultTPositions);
                pager.setAdapter(adapter);
                indicator.setViewPager(pager);
                adapter.registerDataSetObserver(indicator.getDataSetObserver());
                adapter.notifyDataSetChanged();

                //가장 추천하는 시간 중점 위치 핑으로 보여줌
                MapPOIItem middle_d = new MapPOIItem();
                middle_d.setItemName("시간상 중간지점");   //ping 선택 후 말풍선에 보여질 내용
                middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(ghistory.getMiddleLat(), ghistory.getMiddleLong()));     //ping 위치 지정
                middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                middle_d.setCustomImageResourceId(R.drawable.middlepin_75);
                middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
                mapView.addPOIItem(middle_d);   //지도에 ping 추가
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(ghistory.getMiddleLat(), ghistory.getMiddleLong()), 4, false);   //history의 위치 기준으로 화면의 중심점 및 줌레벨 설정

                //그 외의 나머지 지하철 역 출력
                for (int i=0; i < ghistory.getHisStations().size(); i++) {
                    MapPOIItem stationPing = new MapPOIItem();     //핑에 대한 객체(속성인 itemname은 핑 이름)
                    stationPing.setItemName(ghistory.getHisStations().get(i).getStation()+"역");    //ping 선택 후 말풍선에 보여질 내용(지하철역 이름)
                    stationPing.setMapPoint(MapPoint.mapPointWithGeoCoord(ghistory.getHisStations().get(i).getStationLat(), ghistory.getHisStations().get(i).getStationLong()));     //ping 위치 지정(위도 y, 경도 x)
                    stationPing.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    stationPing.setCustomImageResourceId(R.drawable.subwaypin_75);
                    mapView.addPOIItem(stationPing);   //지도에 ping 추가
                }
            }
            else {  //기록을 보여주는 것이 아닌 실제로 알고리즘 수행해야함
                visitedStations = new HashMap<>();              //지하철역에 대해 timeGap계산을 했는 지를 저장함
                minTimeGapS = new CandidateTimePosition();      //이후에 주변역들 중 소요시간 최소인 곳을 저장함

                adapter = new PositionPagerAdapter(this, false, list, resultTPositions);

                //위의 try문을 통해 거리상 중간 지점 바로 근처의 지하철역과 그 지하철 역의 반경 2km 내의 지하철역 리스트를 서버로부터 받음
                try {   //(middleDLong와 middleDLat)와 가까운 지하철 역과 그 역의 반경 2km내의 지하철 역 리스트를 요청 및 전달받음
                    new ShowMiddleActivity.GetTStationTask(true, middleDLat, middleDLong, 2).execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

                //response에서 조사를 하는 함수를 호출함
                //후보 3개까지 나오고 모든 수행은 search함수에서 함
                //이후의 모든 작업은 아래의 함수들을 통해 수행함
            }
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
    @SuppressLint("StaticFieldLeak")
    public class GetDStationTask extends AsyncTask<String, Void, String> {
        ResultDistancePosition middleDistancePosition;
        double latitude, longitude;

        GetDStationTask(ResultDistancePosition middleDistancePosition, double latitude, double longitude) {
            super();
            this.middleDistancePosition = middleDistancePosition;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);   //전달한 위치 근처의 5개 지하철 역 정보(이름, 경도, 위도)를 받음

                    //근처 역 정보 리스트에 저장(5개 값)
                    JSONArray nearStationArray = jsonObject.getJSONArray("nearStation");
                    for (int i=0; i<nearStationArray.length(); i++) {
                        String nearStationName = nearStationArray.getJSONObject(i).getString("station");
                        double nearLat = nearStationArray.getJSONObject(i).getDouble("y");
                        double nearLong = nearStationArray.getJSONObject(i).getDouble("x");
                        StationInfo nearStation = new StationInfo(nearStationName, nearLat, nearLong);
                        nearStationList.add(nearStation);
                        Log.d("nearStation", nearStationName);

                        //5개의 지하철역의 핑 설정
                        MapPOIItem station = new MapPOIItem();
                        station.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        station.setCustomImageResourceId(R.drawable.subwaypin_75);
                        station.setSelectedMarkerType(null);                       //선택 효과 마커 타입
                        station.setItemName(nearStationName+"역");   //ping 선택 후 말풍선에 보여질 내용
                        station.setMapPoint(MapPoint.mapPointWithGeoCoord(nearLat, nearLong));     //ping 위치 지정
                        mapView.addPOIItem(station);   //지도에 ping 추가

                        //근처 역에 대한 middleTakeD 저장
                        ResultDistancePosition nearDistancePosition = new ResultDistancePosition(list.size(), nearStationName, nearLat, nearLong, calTakeDistance(new StationInfo(nearStationName, nearLat, nearLong)));
                        resultDPositions.add(nearDistancePosition);
                    }
                    Collections.sort(resultDPositions);     //오차거리에 따른 오름차순으로 정렬
                    resultDPositions.add(0, middleDistancePosition);    //중간지점역 0번째로 수동 저장

                    adapter = new PositionPagerAdapter(ShowMiddleActivity.this, list, resultDPositions);
                    pager.setAdapter(adapter);
                    indicator.setViewPager(pager);
                    adapter.registerDataSetObserver(indicator.getDataSetObserver());
                    adapter.notifyDataSetChanged();

                    //그룹으로 결과를 찾은 경우에는, 중간 지점 결과를 DB에 보내 history에 저장함
                    if (getIntent().getStringExtra("activity_tag").equals("group"))
                        saveHistory("D", latitude, longitude);

                } catch (JSONException e) { e.printStackTrace(); }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetDMiddleRequest getDMiddleRequest = new GetDMiddleRequest(String.valueOf(latitude), String.valueOf(longitude), responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueD = Volley.newRequestQueue(ShowMiddleActivity.this);
            queueD.add(getDMiddleRequest);

            return null;
        }
    }

    //입력한 값들에 대한 시간 기준 중간지점을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class GetTStationTask extends AsyncTask<String, Void, String> {
        boolean isDistanceMiddle;
        int index = -1;
        double latitude, longitude;
        int radius;

        GetTStationTask(boolean isDistanceMiddle, double latitude, double longitude, int radius) {
            super();
            this.isDistanceMiddle = isDistanceMiddle;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        GetTStationTask(boolean isDistanceMiddle, int index, double latitude, double longitude, int radius) {
            super();
            this.isDistanceMiddle = isDistanceMiddle;
            this.index = index;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);   //(거리상 중간지점 가장 근처의 역 or 중심으로 하고자하는 역)과 그 역 반경 radius(km)이내의 지하철 역 정보들을 받음
                    if (jsonObject.getBoolean("isDistanceMiddle")) {   //거리상 중간지점인 위치를 전달해 해당 위치와 가장 가까운 지하철역과 그 역 주변 반경 radius내의 지하철역 리스트를 받기 위한 경우
                        //전달한 위치인 거리상 중간 지점과 가장 가까운 역 정보 저장
                        String simularStationName = jsonObject.getJSONObject("simularStation").getString("station");
                        double simularLat = jsonObject.getJSONObject("simularStation").getDouble("y");
                        double simularLong = jsonObject.getJSONObject("simularStation").getDouble("x");
                        currentStation = new StationInfo(simularStationName, simularLat, simularLong);
                    }   //포함되지 않는 경우: 인자로 전달한 값이 역과 값이 일치할 경우(-> 거리상 중간지점이 아닌 실제 역 주변의 지하철 역을 리스트로 받기 위한 경우)

                    //중간 지점과 가장 가까운 근처 반경이내의 역 정보 리스트에 저장
                    JSONArray nearStationArray = jsonObject.getJSONArray("nearStation");
                    for (int i=0; i<nearStationArray.length(); i++) {
                        String nearStationName = nearStationArray.getJSONObject(i).getString("station");
                        double nearLat = nearStationArray.getJSONObject(i).getDouble("y");
                        double nearLong = nearStationArray.getJSONObject(i).getDouble("x");
                        StationInfo nearStation = new StationInfo(nearStationName, nearLat, nearLong);

                        if (index == -1)    //거리상 중간 지점에 대한 근처 역 리스트 or minTimeGaps를 기준으로 하는 근처 역 리스트에 추가
                            nearStationList.add(nearStation);
                        else    //조사할 대상 리스트(searchTPositions)에 지하철 역이 포함되어 있는 경우
                            nextSearchList.get(index).add(nearStation);
                    }

                    if (jsonObject.getBoolean("isDistanceMiddle"))
                        getDMiddleTime();   //거리상 중간 지점와 가장 가까운 역과 그 역의 근처 지하철 역들에 대한 소요시간을 구해 최적화된 중간지점을 찾음
                    else {
                        //searchTPositions 항목의 개수가 0일 때
                        //searchTPositions 항목의 개수가 0이 아닐 때(위와 다른 수행)
                        getNearNeighborTime(index != -1);
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };
            // 서버로 Volley를 이용해서 요청을 함
            GetTMiddleRequest getTMiddleRequest = new GetTMiddleRequest(isDistanceMiddle, String.valueOf(latitude), String.valueOf(longitude), radius, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueT = Volley.newRequestQueue(ShowMiddleActivity.this);
            queueT.add(getTMiddleRequest);

            return null;
        }
    }

    //거리 상 중간지점(초기 기준 위치)과 가장 가까운 지하철역의 사용자들 소요시간 정보 저장하는 current 생성 (가변적)
    public void getDMiddleTime() {
        visitedStations.put(currentStation.getStation(), true);     //거리상 중간 지점 계산함을 저장
        current = new CandidateTimePosition(this, 0, list, currentStation.getStation(), currentStation.getStationLat(), currentStation.getStationLong());
    }

    //거리 상 중간지점 반경 2km내의 지하철 역들 정보(TimeGap) 계산 - API 사용
    void getDMiddleNearTime() {
        countFinishLoop = nearStationList.size();       //동기화를 맞추기 위한 사이즈 확인

        for (StationInfo nearS : nearStationList) {     //거리상 중간지점과 가장 가까운 역을 중심으로 반경 2km이내에 있는 역에 대해
            visitedStations.put(nearS.getStation(), true);     //이 위치를 계산함을 저장
            new CandidateTimePosition(this, 1, list, nearS.getStation(), nearS.getStationLat(), nearS.getStationLong());
        }
    }

    //searchTPositions에 속한 역을 기준으로 다시 주변 역 검사
    void getNearInfoByS() {
        //이때 거리상 중간지점의 주변 역들을 계산해봤을 때 resultTPositions에서 timeGap가 3보다 작거나 같은 것이 3개 이상 존재한다면 searchTPositions 검사하지 않고 출력
        Collections.sort(resultTPositions);
        if (resultTPositions.size()>=5 && resultTPositions.get(4).getTimeGap()<=5)    //timeGap이 5보다 작거나 같은 위치 5개의 결과가 이미 존재함
            resultTMiddle();    //결과를 출력함

        else {  //timeGap이 5보다 작거나 같은 위치가 5개 이상 존재하지 않을 시(추가 검사)
            //거리기준 중간지점 반경 2km내의 지하철역들 확인 완료 이후
            //주변 역들의 소요시간 차가 10보다 크고, 거리상 중간지점보다 큰 경우(주변 역들 중 가장 소요시간 차가 작았던 지하철역을 중심으로 다시 조사함)
            if (searchTPositions.size()==0 && nearStationList.size()!=0) {
                //minTimeGapS를 중심으로 다시 반경 2km내에 주변 지하철 역을 조사함(try문을 다시 써야함)
                try {
                    new GetTStationTask(false, minTimeGapS.getResultPositionLat(), minTimeGapS.getResultPositionLong(), 2).execute().get();
                } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }

            }
            else {      //조사할 대상 리스트에 지하철 역이 포함되어 있는 경우
                nextSearchList = new ArrayList<>();

                Collections.sort(searchTPositions);     //searchTPositions의 TimeGap으로 오름차순함(여기서 앞의 3개만 사용함)

                int searchTsize = Math.min(searchTPositions.size(), 3);

                for (int i=0; i<searchTsize; i++) {    //조사할 대상 리스트에 포함된 지역을 중심으로 하여 반경 2km이내의 지하철 역을 조사함
                    nextSearchList.add(new ArrayList<>());
                    current = searchTPositions.get(i);

                    try {
                        new GetTStationTask(false, i, current.getResultPositionLat(), current.getResultPositionLong(), 2).execute().get();
                    } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
                }
            }
        }
    }

    //nearStationList의 각 원소를 기준으로 반경 2km내의 지하철 역들 정보(TimeGap) 계산 - API 사용
    void getNearNeighborTime(boolean isNextSearch) {
        if (!isNextSearch) {    //searchTPositions에 값이 존재하지 않으므로 minTimeGaps로 조사함
            Log.d("stationNoIsNextSearch", "--------->_<---------");
            countFinishLoop = nearStationList.size();       //동기화를 맞추기 위한 사이즈 확인

            //minTimGapS를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
            for (StationInfo nextNearS : nearStationList) {
                if (visitedStations.containsKey(nextNearS.getStation())) {
                    countFinishLoop--;
                    if (countFinishLoop == 0)
                        resultTMiddle();     //결과 출력
                }
                else {
                    visitedStations.put(nextNearS.getStation(),true);     //이 위치를 계산함을 저장
                    new CandidateTimePosition(this, 2, list, nextNearS.getStation(), nextNearS.getStationLat(), nextNearS.getStationLong());
                }
            }
        }

        else {
            Log.d("stationIsNextSearch", "--------->_<---------");
            for (ArrayList<StationInfo> nextNearList : nextSearchList) {
                countFinishLoop += nextNearList.size();
            }

            //current(조사 대상 리스트의 지하철 역 중 하나)를 중심으로 하여 반경 2km내의 지하철 역에 대해서 조사함
            for (ArrayList<StationInfo> nextNearList : nextSearchList) {
                for (StationInfo nextNearS: nextNearList) {
                    if (visitedStations.containsKey(nextNearS.getStation())) {
                        countFinishLoop--;
                        if (countFinishLoop == 0)
                            resultTMiddle();     //결과 출력
                    }
                    else {
                        visitedStations.put(nextNearS.getStation(),true);     //이 위치를 계산함을 저장
                        new CandidateTimePosition(this, 2, list, nextNearS.getStation(), nextNearS.getStationLat(), nextNearS.getStationLong());
                    }
                }
            }
        }
    }

    //결과를 뽑아내고 출력함
    void resultTMiddle() {
        Log.d("stationNextNextNextNext", "--------->_<---------");
        if (resultTPositions.size() == 0) {     //minTimeGaps의 주변 역까지 조사를 했지만 result후보 역이 없을 때
            if (minTimeGapS.getTimeGap() > current.getTimeGap())
                resultTPositions.add(current);
            else
                resultTPositions.add(minTimeGapS);
        }

        //resultTPositions리스트에 최대 및 최소 소요시간의 오차가 10보다 작거나 같은 지하철 역들이 포함됨
        Collections.sort(resultTPositions);     //오름차순 정렬함

        for (int i=0; i<resultTPositions.size(); i++) {
            Log.d("stationResult", resultTPositions.get(i).getStationName());
        }

        //화면에 결과로 포함된 지하철 역 핑 출력
        MapPOIItem stationM = new MapPOIItem();
        stationM.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        stationM.setCustomImageResourceId(R.drawable.middlepin_75);
        stationM.setSelectedMarkerType(null);                       //선택 효과 마커 타입
        stationM.setItemName(resultTPositions.get(0).getStationName() + "역");   //ping 선택 후 말풍선에 보여질 내용
        stationM.setMapPoint(MapPoint.mapPointWithGeoCoord(resultTPositions.get(0).getResultPositionLat(), resultTPositions.get(0).getResultPositionLong()));     //ping 위치 지정
        mapView.addPOIItem(stationM);

        //가장 소요시간 차가 작은 위치를 기준으로 화면의 중심점 및 줌레벨 설정
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(resultTPositions.get(0).getResultPositionLat(), resultTPositions.get(0).getResultPositionLong()), 4, false);

        //결과 핑 출력
        int size = Math.min(resultTPositions.size(), 5);
        for (int i=1; i<size; i++) {
            //거리 기준 중간 지점 역 출력
            MapPOIItem station = new MapPOIItem();
            station.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            station.setCustomImageResourceId(R.drawable.subwaypin_75);  //가장 짧은 지하철 역 이외의 나머지 역들
            station.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            station.setItemName(resultTPositions.get(i).getStationName() + "역");   //ping 선택 후 말풍선에 보여질 내용
            station.setMapPoint(MapPoint.mapPointWithGeoCoord(resultTPositions.get(i).getResultPositionLat(), resultTPositions.get(i).getResultPositionLong()));     //ping 위치 지정
            mapView.addPOIItem(station);   //지도에 ping 추가
        }

        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        adapter.registerDataSetObserver(indicator.getDataSetObserver());

        adapter.notifyDataSetChanged();

        //그룹으로 결과를 찾은 경우에는, 중간 지점 결과를 DB에 보내 history에 저장함
        if (getIntent().getStringExtra("activity_tag").equals("group"))
            saveHistory("T", resultTPositions.get(0).getResultPositionLat(), resultTPositions.get(0).getResultPositionLong());
    }

    //DB에 중간 지점을 찾은 결과를 기록으로 저장함
    public void saveHistory(String standard, double resultLat, double resultLong){
        try {
            new saveHistoryTask(standard, resultLat, resultLong).execute().get();
        } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
    }

    //입력한 값들에 대한 거리 기준 중간지점을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class saveHistoryTask extends AsyncTask<String, Void, String> {
        String standard;
        String resultSName = "";
        double resultLat, resultLong;
        String usersPick = "";
        String resultStations = "";
        String takeTOrD = "";

        saveHistoryTask(String standard, double resultLat, double resultLong) {
            super();
            this.standard = standard;
            this.resultLat = resultLat;
            this.resultLong = resultLong;

            for (PositionItemInfo pi : list_group) {    //사용자의 핑 정보 저장(ex. 바나나,37.23423424,127.232332;사과,_______,________)
                usersPick = usersPick.concat(pi.getUserName()+","+pi.getLatitude()+","+pi.getLongitude()+";");
            }
            usersPick = usersPick.substring(0, usersPick.length()-1);   //마지막 문자 ; 삭제하기

            //주변 지하철 역 이름 저장 -> ex.강남,논현,강남구청
            if (standard.equals("D")) {   //거리 기준으로 중간지점 결과를 찾은 경우
                for (ResultDistancePosition tmpDPosition : resultDPositions) {
                    if (!tmpDPosition.getStationName().equals("거리상 중간지점"))
                        resultStations = resultStations.concat(tmpDPosition.getStationName()+",");

                    for (double distance : tmpDPosition.getDistanceList())
                        takeTOrD = takeTOrD.concat(distance+",");
                    takeTOrD = takeTOrD.substring(0, takeTOrD.length()-1);   //마지막 문자 , 삭제하기
                    takeTOrD = takeTOrD.concat(";");
                }

                resultStations = resultStations.substring(0, resultStations.length()-1);   //마지막 문자 , 삭제하기
            }
            else {      //시간 기준으로 중간지점 결과를 찾은 경우
                int size = Math.min(resultTPositions.size(), 5);
                if (size > 0) {
                    resultSName = resultTPositions.get(0).getStationName();

                    for (int i=0; i<size; i++) {    //이외의 역들
                        if (i!=0)
                            resultStations = resultStations.concat(resultTPositions.get(i).getStationName()+",");

                        for (RouteInfo personRoute : resultTPositions.get(i).getRouteInfo())
                            takeTOrD = takeTOrD.concat(personRoute.getTotalTime()+",");
                        takeTOrD = takeTOrD.substring(0, takeTOrD.length()-1);   //마지막 문자 , 삭제하기
                        takeTOrD = takeTOrD.concat(";");
                    }
                    resultStations = resultStations.substring(0, resultStations.length()-1);   //마지막 문자 , 삭제하기
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = response -> {
                //TODO: history 기록 변경
            };

            //서버로 Volley를 이용해서 요청을 함
            SaveHistoryRequest sHistoryRequest = new SaveHistoryRequest(selectedGroup.getGroupName(), selectedGroup.getGHostName(),
                    standard, resultSName, resultLat, resultLong, usersPick, resultStations, takeTOrD, responseListener);
            RequestQueue queueSave = Volley.newRequestQueue(ShowMiddleActivity.this);
            queueSave.add(sHistoryRequest);

            return null;
        }
    }

    private ArrayList<Double> calTakeDistance(StationInfo station) {
        ArrayList<Double> takeD = new ArrayList<>();

        for (PositionItemInfo pi : list) {
            takeD.add(Math.round(calDistance(station.getStationLat(), station.getStationLong(), pi.getLatitude(), pi.getLongitude())*100)/100.0);
        }

        return takeD;
    }
}
