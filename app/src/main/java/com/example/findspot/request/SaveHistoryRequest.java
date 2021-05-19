package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SaveHistoryRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/savehistory.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> historyInfo;        //전달할 정보(history로 전달할 정보)

    public SaveHistoryRequest(String gName, String gHostName, String standard, String resultSName, double resultLat, double resultLong, String usersPick, String resultStations, String takeTOrD, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        historyInfo = new HashMap<>();
        historyInfo.put("groupName", gName);
        historyInfo.put("groupHostName", gHostName);
        historyInfo.put("standard", standard);  //무슨 기준인지(T / D)
        historyInfo.put("resultSName", resultSName);    //거리상 중간 지점 지하철역
        historyInfo.put("resultLat", String.valueOf(resultLat));    //시간 기준이라면 가장 소요시간 간격이 짧은 위치의 위도, 거리 기준이면 중간 지점의 위도
        historyInfo.put("resultLong", String.valueOf(resultLong));
        historyInfo.put("usersPick", usersPick);                //사용자 설정 위치 정보(이름1: 위치, 이름2: 위치, ...)
        historyInfo.put("resultStations", resultStations);      //근처 지하철 역(거리 기준에서만 전달함)
        historyInfo.put("takeTOrD", takeTOrD);                  //결과로 나온 지하철 역마다 그룹에 속한 사용자들이 걸리는 시간 or 거리(ex. 사1, 사2, 사3, 사1, 사2, 사3...)
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return historyInfo;
    }
}
