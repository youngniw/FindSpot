package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class NickNameCheckRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/nickNameCheck.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> checkIDInfo;        //전달할 정보

    public NickNameCheckRequest(String userNickName, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        checkIDInfo = new HashMap<>();
        checkIDInfo.put("nickName", userNickName);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return checkIDInfo;
    }
}
