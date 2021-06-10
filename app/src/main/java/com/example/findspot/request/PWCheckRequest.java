package com.example.findspot.request;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PWCheckRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/checkpassword.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> checkPWInfo;        //전달할 정보(닉네임, 비밀번호 -> 비밀번호 변경 전 현재 비밀번호가 맞는 지 확인)

    public PWCheckRequest(String userNickName, String userPassword, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        checkPWInfo = new HashMap<>();
        checkPWInfo.put("nickName", userNickName);
        checkPWInfo.put("password", userPassword);
    }

    @Override
    protected Map<String, String> getParams() { return checkPWInfo; }
}
