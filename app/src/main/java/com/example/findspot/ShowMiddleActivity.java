package com.example.findspot;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;

import java.util.ArrayList;

import static com.example.findspot.ChoiceGPSRandomActivity.list_random;
import static com.example.findspot.ChoiceGPSGroupActivity.list_group;

public class ShowMiddleActivity extends AppCompatActivity {
    ArrayList<PositionItem> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmiddle);

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.shownmiddle_mapview);
        mapViewContainer.addView(mapView);

        //이전 ACtivity에 따라 사용할 list 설정
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
            mapView.addPOIItem(user_ping);   //지도에 ping 추가
        }

        //시간/거리 기준에 따라 중간지점 보여주기
        String getExtra_standard = getIntent().getStringExtra("standard_tag");
        if (getExtra_standard.equals("time")) { //시간기준일 경우
            //*************************************
            //앞으로 알고리즘 짜야됨
        } else if (getExtra_standard.equals("distance")) {  //거리기준일 경우
            double avgX = 0.0, avgY = 0.0;
            for (int i = 0; i < list.size(); i++) {
                avgX += list.get(i).getLongitude();
                avgY += list.get(i).getLatitude();
            }
            avgX /= list.size();
            avgY /= list.size();

            MapPOIItem middle_d = new MapPOIItem();
            middle_d.setItemName("거리 중간지점");   //ping 선택 후 말풍선에 보여질 내용
            middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(avgY, avgX));     //ping 위치 지정
            middle_d.setMarkerType(MapPOIItem.MarkerType.YellowPin);    //기본 마커 타입
            middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            middle_d.setCustomImageResourceId(R.drawable.middlepin_64);
            middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            //middle_d.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            //middle_d.setCustomSelectedImageResourceId(int imageResourceId);   //hdpi 기준으로 작업된 이미지를 지정하면 다른 dpi 단말에서는 이미지가 자동으로 적절한 크기로 scaling 되어 나타남
            mapView.addPOIItem(middle_d);   //지도에 ping 추가

            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(avgY, avgX), 4, false); //중간지점을 기준으로 화면의 중심점 및 줌레벨 설정
        }
        //************* 핑을 누르면 이벤트로 그곳 setMapCenerPointAndZoomLevel
    }
}