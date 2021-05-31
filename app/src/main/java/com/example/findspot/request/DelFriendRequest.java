package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DelFriendRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/deletefriend.php";       //php파일 연동을 위한 서버 URL을 설정
    private Map<String, String> delFriendInfo;        //전달할 정보(삭제할 친구)

    //해당 친구를 삭제해주세요!
    public DelFriendRequest(String userNickName, String delFriend, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        delFriendInfo = new HashMap<>();
        delFriendInfo.put("userNickName", userNickName);
        delFriendInfo.put("friendNickName", delFriend);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError { return delFriendInfo; }
}
