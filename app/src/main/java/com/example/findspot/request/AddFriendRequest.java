package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AddFriendRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/addfriend.php";       //php파일 연동을 위한 서버 URL을 설정
    private Map<String, String> addFriendInfo;        //전달할 정보(추가할 친구)

    //해당 친구를 추가해주세요!
    public AddFriendRequest(String userNickName, String addFriend, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        addFriendInfo = new HashMap<>();
        addFriendInfo.put("userNickName", userNickName);
        addFriendInfo.put("friendNickName", addFriend);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError { return addFriendInfo; }
}
