package com.example.findspot.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.example.findspot.data.GroupInfo;

import java.util.HashMap;
import java.util.Map;

public class DelGroupRequest extends StringRequest {
    final static private String URL = "http://222.111.4.158/wheremiddle/deletegroup.php";       //php파일 연동을 위한 서버 URL을 설정
    private final Map<String, String> delGroupInfo;        //전달할 정보(삭제할 그룹)

    //해당 그룹을 삭제해주세요!
    public DelGroupRequest(GroupInfo delGroup, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        delGroupInfo = new HashMap<>();
        delGroupInfo.put("groupName", delGroup.getGroupName());
        delGroupInfo.put("groupHostNickName", delGroup.getGHostName());
    }

    @Override
    protected Map<String, String> getParams() { return delGroupInfo; }
}
