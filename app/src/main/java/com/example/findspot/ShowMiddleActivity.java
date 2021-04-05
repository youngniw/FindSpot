package com.example.findspot;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapPOIItem;
import static com.example.findspot.ChoiceGPSRandomActivity.list;

public class ShowMiddleActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmiddle);

        MapView mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.shownmiddle_mapview);
        mapViewContainer.addView(mapView);
        //전체 사용자 ping으로 나타내기
        for (int i = 0; i < list.size(); i++) {
            MapPOIItem middle_d = new MapPOIItem();
            middle_d.setItemName(String.valueOf(i+1));   //ping 선택 후 말풍선에 보여질 내용
            middle_d.setMapPoint(MapPoint.mapPointWithGeoCoord(list.get(i).getLatitude(), list.get(i).getLongitude()));     //ping 위치 지정
            mapView.addPOIItem(middle_d);   //지도에 ping 추가
        }

        //시간/거리 기준에 따라 중간지점 보여주기
        String getString = getIntent().getStringExtra("standard_tag");
        if (getString.equals("time")) { //시간기준일 경우
            //*************************************
            //앞으로 알고리즘 짜야됨
        } else if (getString.equals("distance")) {  //거리기준일 경우
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
            //middle_d.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            //middle_d.setCustomImageResourceId(int imageResourceId);
            middle_d.setSelectedMarkerType(null);                       //선택 효과 마커 타입
            //middle_d.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            //middle_d.setCustomSelectedImageResourceId(int imageResourceId);   // hdpi 기준으로 작업된 이미지를 지정하면 다른 dpi 단말에서는 이미지가 자동으로 적절한 크기로 scaling 되어 나타남
            mapView.addPOIItem(middle_d);   //지도에 ping 추가
        }
    }
}