package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ChangeNickNameRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/changenickname.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> changeNickNameInfo;        //전달할 정보(변경할 닉네임)

    //해당 친구를 추가해주세요!
    public ChangeNickNameRequest(String pastNickName, String newNickName, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        changeNickNameInfo = new HashMap<>();
        changeNickNameInfo.put("pastNickName", pastNickName);
        changeNickNameInfo.put("newNickName", newNickName);
    }

    @Override
    protected Map<String, String> getParams() { return changeNickNameInfo; }
}
