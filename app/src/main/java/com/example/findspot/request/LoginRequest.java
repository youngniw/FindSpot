package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/login.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> userLoginInfo;        //전달할 정보(로그인을 위한 ID와 PW 정보)

    public LoginRequest(String userID, String userPW, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userLoginInfo = new HashMap<>();
        userLoginInfo.put("ID", userID);
        userLoginInfo.put("PW", userPW);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return userLoginInfo;
    }
}
