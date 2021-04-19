package com.example.findspot;

import java.util.ArrayList;

public class GroupInfo {
    private String gName = "";
    private ArrayList<String> gUsers;

    public GroupInfo(String gName) {
        super();
        this.gName = gName;
        gUsers = new ArrayList<String>();
    }

    public String getGroupName() { return gName; }

    public ArrayList<String> getGroupUsers() { return gUsers; }

    public void setGroupName(String gName) { this.gName = gName; }

    public void setGroupUsers(ArrayList<String> gUsers) { this.gUsers = gUsers; }
}
