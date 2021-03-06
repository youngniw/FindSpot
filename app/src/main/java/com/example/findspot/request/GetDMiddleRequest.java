package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GetDMiddleRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/distancemiddlestation.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> distanceMiddleInfo;        //전달할 정보(가까운 역을 받기 위한 중심 위치 정보)

    //middleLat와 middleLong를 위치로 하는 지역의 근처 지하철역 5개를 주세요!
    public GetDMiddleRequest(String middleLat, String middleLong, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        distanceMiddleInfo = new HashMap<>();
        distanceMiddleInfo.put("middleY", middleLat);
        distanceMiddleInfo.put("middleX", middleLong);
    }

    @Override
    protected Map<String, String> getParams() { return distanceMiddleInfo; }
}
