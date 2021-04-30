package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GetTMiddleRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/timemiddlestation.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> timeMiddleInfo;        //전달할 정보(가까운 역을 받기 위한 중심 위치 정보)

    //middleX와 middleY를 위치로 하는 지역의 radius 반경 내 지하철 역을 알려주세요!
    public GetTMiddleRequest(boolean isDistanceMiddle, String middlex, String middley, int radius, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        timeMiddleInfo = new HashMap<>();
        if (isDistanceMiddle)   //거리상 중간지점인 위치를 전달해 해당 위치와 가장 가까운 지하철역과 그 역 주변 반경 radius내의 지하철역 리스트를 받기 위한 경우
            timeMiddleInfo.put("isDistanceMiddle", "true");
        else                    //전달하는 경도와 위도 값이 지하철 역인 경우(즉, 거리상 중간지점이 아닌 이외의 위치)
            timeMiddleInfo.put("isDistanceMiddle", "false");
        timeMiddleInfo.put("middleX", middlex);
        timeMiddleInfo.put("middleY", middley);
        timeMiddleInfo.put("radius", String.valueOf(radius));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return timeMiddleInfo;
    }
}
