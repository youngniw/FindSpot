package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GetTimeMiddleRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/timemiddlestation.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> timeMiddleInfo;        //전달할 정보(가까운 역을 받기 위한 중심 위치 정보)

    public GetTimeMiddleRequest(String middlex, String middley, int radius, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        timeMiddleInfo = new HashMap<>();
        timeMiddleInfo.put("middleX", middlex);
        timeMiddleInfo.put("middleY", middley);
        timeMiddleInfo.put("radius", String.valueOf(radius));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return timeMiddleInfo;
    }
}
