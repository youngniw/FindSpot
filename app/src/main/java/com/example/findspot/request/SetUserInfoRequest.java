package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SetUserInfoRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/setuserinfo.php";       //php파일 연돌을 위한 서버 URL을 설정
    private final Map<String, String> userInfo;        //전달할 정보(회원탈퇴를 위한 닉네임 정보)

    public SetUserInfoRequest(String userNickName, String gender, int birthYear, String roadName, double latitude, double longitude, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userInfo = new HashMap<>();
        userInfo.put("nickName", userNickName);
        userInfo.put("gender", gender);
        userInfo.put("birthYear", String.valueOf(birthYear));
        userInfo.put("roadName", roadName);
        userInfo.put("latitude", String.valueOf(latitude)); //latitude와 longitude가 모두 0이면 roadName이 바뀌지 않았음을 의미함
        userInfo.put("longitude", String.valueOf(longitude));
    }

    @Override
    protected Map<String, String> getParams() { return userInfo; }
}
