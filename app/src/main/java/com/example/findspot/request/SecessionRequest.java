package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SecessionRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/secession.php";       //php파일 연돌을 위한 서버 URL을 설정
    private final Map<String, String> secessionInfo;        //전달할 정보(회원탈퇴를 위한 닉네임 정보)

    public SecessionRequest(String userNickName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        secessionInfo = new HashMap<>();
        secessionInfo.put("nickName", userNickName);
    }

    @Override
    protected Map<String, String> getParams() { return secessionInfo; }
}
