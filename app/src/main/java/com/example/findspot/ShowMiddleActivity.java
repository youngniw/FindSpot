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
import java.util.concurrent.ExecutionException;

import static com.example.findspot.ChoiceGPSRandomActivity.list_random;
import static com.example.findspot.ChoiceGPSGroupActivity.list_group;

public class ShowMiddleActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    ArrayList<PositionItem> list;   //실제 중간지점을 찾을 사람들의 설정 위치 등의 정보
    StationInfo currentStation;
    ArrayList<StationInfo> nearStationList;

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
        for (int i = 0; i < list.size(); i++) {
            avgX += list.get(i).getLongitude();
            avgY += list.get(i).getLatitude();
        }
        avgX /= list.size();
        avgY /= list.size();

        //시간/거리 기준에 따라 중간지점 보여주기
        String getExtra_standard = getIntent().getStringExtra("standard_tag");
        if (getExtra_standard.equals("time")) {             //시간기준일 경우
            nearStationList = new ArrayList<StationInfo>();
            //*************************************
            //TODO: 앞으로 알고리즘 짜야됨
            try {
                new ShowMiddleActivity.GetStationTask(avgX, avgY).execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

            //TODO: 반경을 초기에는 2km로 해서 nearStationList를 받았고, 그것들을 계산한 결과 최대와 최소 소요시간의 차가 10보다 크다면 2km를 더 늘려 계산함
            CandidateTimePosition current = new CandidateTimePosition(this, list, currentStation.getStationX(), currentStation.getStationY());  //거리 상 중간지점과 가장 가까운 지하철 역에 대한 정보

            ArrayList<CandidateTimePosition> tmpTimePosition = new ArrayList<>();       //중간지점의 반경을 기준으로 그 주위의 지하철 역에 대한 정보
            //if (current.getTimeGap() > 10)
                //tmpTimePosition.        //반경 2km 내에
            //while (current.getTimeGap() > 10) {

            //}

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

    //입력한 값들에 대한 회원가입을 위한 작업 수행함
    public class GetStationTask extends AsyncTask<String, Void, String> {
        double x = 0.0;
        double y = 0.0;

        GetStationTask(double x, double y) {
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
            GetTimeMiddleRequest getMiddleRequest = new GetTimeMiddleRequest(String.valueOf(x), String.valueOf(y), 2, responseListener);    //반경을 2km로 줌
            RequestQueue queue = Volley.newRequestQueue(ShowMiddleActivity.this);
            queue.add(getMiddleRequest);

            return null;
        }
    }
}
