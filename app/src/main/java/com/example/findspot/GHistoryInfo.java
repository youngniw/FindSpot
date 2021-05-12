package com.example.findspot;

import java.util.ArrayList;

public class GHistoryInfo {
    private String standard = "N";      //history가 거리(D)기준인지 시간(T)기준인지 / history 없음(N)
    private double whereY = 360.0;      //history의 중간지점 위도(시간일 때는 timeGaps가 가장 작은 역의 위도)
    private double whereX = 360.0;      //history의 중간지점 경도(시간일 때는 timeGaps가 가장 작은 역의 경도)
    private ArrayList<PositionItem> usersPick = null;       //history에서 사용자의 위치 정보
    private ArrayList<StationInfo> hisStations = null;      //시간 기준일 때는 timeGaps가 가장 작은 역을 제외한 나머지 결과 역들 정보 / 거리 기준일 때는 중간지점 근처 5개의 역을 저장함

    GHistoryInfo() {
        usersPick = new ArrayList<PositionItem>();
        hisStations = new ArrayList<StationInfo>();
    }

    public String getStandard() { return standard; }
    public double getWhereY() { return whereY; }
    public double getWhereX() { return whereX; }
    public ArrayList<PositionItem> getUsersPick() { return usersPick; }
    public ArrayList<StationInfo> getHisStations() { return hisStations; }

    public void setStandard(String standard) { this.standard = standard; }
    public void setWhereY(double whereY) { this.whereY = whereY; }
    public void setWhereX(double whereX) { this.whereX = whereX; }

    public void historyClear() {    //기록 삭제(뒤로가기 시 누적될 수 있으므로)
        usersPick.clear();
        hisStations.clear();
    }
}
