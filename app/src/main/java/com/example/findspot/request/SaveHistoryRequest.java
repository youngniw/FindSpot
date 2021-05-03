package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SaveHistoryRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/savehistory.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> historyInfo;        //전달할 정보(history로 전달할 정보)

    public SaveHistoryRequest(String gName, String gHostName, String standard, double resultLat, double resultLong, String usersPick, String resultStations, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        historyInfo = new HashMap<>();
        historyInfo.put("groupName", gName);
        historyInfo.put("groupHostName", gHostName);
        historyInfo.put("standard", standard);  //무슨 기준인지(T / D)
        historyInfo.put("resultLat", String.valueOf(resultLat));    //시간 기준이라면 가장 소요시간 간격이 짧은 위치의 위도 / 거리 기준이면 중간 지점의 위도
        historyInfo.put("resultLong", String.valueOf(resultLong));
        historyInfo.put("usersPick", usersPick);
        historyInfo.put("resultStations", resultStations);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return historyInfo;
    }
}
