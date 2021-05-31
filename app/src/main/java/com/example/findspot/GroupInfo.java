package com.example.findspot;

import java.util.ArrayList;

public class GroupInfo {
    private String gName = "";          //그룹이름
    private String gHostName = "";      //그룹방장이름
    private ArrayList<String> gUsers;   //그룹에 속한 사용자 이름

    public GroupInfo(String gName, String gHostName) {
        super();
        this.gName = gName;
        this.gHostName = gHostName;
        gUsers = new ArrayList<String>();
    }

    public GroupInfo(String gName, String gHostName, ArrayList<String> gUsers) {
        super();
        this.gName = gName;
        this.gHostName = gHostName;
        this.gUsers = gUsers;
    }

    public String getGroupName() { return gName; }
    public String getGHostName() { return gHostName; }
    public ArrayList<String> getGroupUsers() { return gUsers; }

    public void setGroupName(String gName) { this.gName = gName; }
    public void setGHostName(String gHostName) { this.gHostName = gHostName; }
    public void setGroupUsers(ArrayList<String> gUsers) { this.gUsers = gUsers; }
}
