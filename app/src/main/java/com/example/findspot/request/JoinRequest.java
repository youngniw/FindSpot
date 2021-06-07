package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/join.php";       //php파일 연돌을 위한 서버 URL을 설정
    private final Map<String, String> userInfo;        //전달할 정보(회원가입을 위한 사용자 정보)

    public JoinRequest(String userID, String userPW, String userNickName, String gender, String birthYear, String socialLogin, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        userInfo = new HashMap<>();
        userInfo.put("ID", userID);     //소셜 로그인에서는 회원 구글 고유 번호임
        userInfo.put("PW", userPW);     //소셜 로그인에서 Email 계정임
        userInfo.put("nickName", userNickName);
        userInfo.put("gender", gender);
        userInfo.put("birthYear", birthYear);
        userInfo.put("socialLogin", socialLogin);
    }

    @Override
    protected Map<String, String> getParams() { return userInfo; }
}
