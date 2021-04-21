package com.example.findspot;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import java.util.ArrayList;

import static com.example.findspot.ChoiceGPSRandomActivity.list_random;
import static com.example.findspot.ChoiceGPSGroupActivity.list_group;

public class ShowMiddleActivity extends AppCompatActivity implements MapView.POIItemEventListener {
    ArrayList<PositionItem> list;

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
        if (getExtra_standard.equals("time")) { //시간기준일 경우
            //*************************************
            //앞으로 알고리즘 짜야됨
            MiddleTime mt = new MiddleTime(ShowMiddleActivity.this, list.get(0).getLongitude(), list.get(0).getLatitude(), avgX, avgY);

        } else if (getExtra_standard.equals("distance")) {  //거리기준일 경우
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
}
