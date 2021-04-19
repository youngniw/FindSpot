package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/join.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> userJoinInfo;        //전달할 정보(회원가입을 위한 사용자 정보)

    public JoinRequest(String userID, String userPW, String userNickName, String gender, String birthYear, String socialLogin, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userJoinInfo = new HashMap<>();
        userJoinInfo.put("ID", userID);
        userJoinInfo.put("PW", userPW);
        userJoinInfo.put("nickName", userNickName);
        userJoinInfo.put("gender", gender);
        userJoinInfo.put("birthYear", birthYear);
        userJoinInfo.put("socialLogin", socialLogin);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return userJoinInfo;
    }
}