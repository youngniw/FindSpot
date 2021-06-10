package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GetUserInfoRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/getuserinfo.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> userInfo;        //전달할 정보(회원탈퇴를 위한 닉네임 정보)

    public GetUserInfoRequest(String userNickName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userInfo = new HashMap<>();
        userInfo.put("nickName", userNickName);
    }

    @Override
    protected Map<String, String> getParams() { return userInfo; }
}
