package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/join.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> userInfo;        //전달할 정보

    public JoinRequest(String userID, String userPW, String gender, String birthYear, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userInfo = new HashMap<>();
        userInfo.put("ID", userID);
        userInfo.put("PW", userPW);
        userInfo.put("gender", gender);
        userInfo.put("birthYear", birthYear);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return userInfo;
    }
}
