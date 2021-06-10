package com.example.findspot.request;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PWChangeRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/changepassword.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> changePWInfo;

    public PWChangeRequest(String userNickName, String changePassword, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        changePWInfo = new HashMap<>();
        changePWInfo.put("nickName", userNickName);
        changePWInfo.put("password", changePassword);
    }

    @Override
    protected Map<String, String> getParams() { return changePWInfo; }
}
