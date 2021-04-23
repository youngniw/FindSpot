package com.example.findspot.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.example.findspot.GroupInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupInfoRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/groupinfo.php";       //php파일 연돌을 위한 서버 URL을 설정
    private Map<String, String> groupUserInfo;        //전달할 정보(중간지점을 찾기 위한 그룹정보 전달)

    public GroupInfoRequest(GroupInfo groupInfo, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        groupUserInfo = new HashMap<>();
        groupUserInfo.put("groupName", groupInfo.getGroupName());       //그룹명 전달
        groupUserInfo.put("groupHostName", groupInfo.getGHostName());   //그룹 호스트이름 전달
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return groupUserInfo;
    }
}
