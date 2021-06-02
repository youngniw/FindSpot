package com.example.findspot.data;

import java.util.ArrayList;

public class GHistoryInfo {
    private String standard = "N";          //history가 거리(D)기준인지 시간(T)기준인지 / history 없음(N)
    private double middleLat = 360.0;       //history의 중간지점 위도(시간일 때는 timeGaps가 가장 작은 역의 위도)
    private double middleLong = 360.0;      //history의 중간지점 경도(시간일 때는 timeGaps가 가장 작은 역의 경도)
    private String middleSName = "";
    private final ArrayList<Double> middleTakeTOrD;        //중간지점 위도 경도의 소요 시간 혹은 소요 거리
    private final ArrayList<PositionItemInfo> usersPick;   //history에서 사용자의 위치 정보
    private final ArrayList<StationInfo> hisStations;      //시간 기준일 때는 timeGaps가 가장 작은 역을 제외한 나머지 결과 역들 정보 / 거리 기준일 때는 중간지점 근처 5개의 역을 저장함
    private final ArrayList<NearStationTakeTOrD> nearTakeTOrD;

    public GHistoryInfo() {
        middleTakeTOrD = new ArrayList<>();
        usersPick = new ArrayList<>();
        hisStations = new ArrayList<>();
        nearTakeTOrD = new ArrayList<>();
    }

    public String getStandard() { return standard; }
    public double getMiddleLat() { return middleLat; }
    public double getMiddleLong() { return middleLong; }
    public String getMiddleSName() { return middleSName; }
    public ArrayList<PositionItemInfo> getUsersPick() { return usersPick; }
    public ArrayList<StationInfo> getHisStations() { return hisStations; }
    public ArrayList<Double> getMiddleTakeTOrD() { return middleTakeTOrD; }
    public ArrayList<NearStationTakeTOrD> getNearTakeTOrD() { return nearTakeTOrD; }

    public void setStandard(String standard) { this.standard = standard; }
    public void setMiddleLat(double whereLat) { this.middleLat = whereLat; }
    public void setMiddleLong(double whereLong) { this.middleLong = whereLong; }
    public void setMiddleSName(String middleSName) { this.middleSName = middleSName; }

    public void historyClear() {    //기록 삭제(뒤로가기 시 누적될 수 있으므로)
        middleTakeTOrD.clear();
        usersPick.clear();
        hisStations.clear();
        nearTakeTOrD.clear();
    }

    public static class NearStationTakeTOrD {
        private final ArrayList<Double> usersTakeTOrD;

        public NearStationTakeTOrD(ArrayList<Double> usersTakeTOrD) {
            this.usersTakeTOrD = usersTakeTOrD;
        }

        public ArrayList<Double> getUsersTakeTOrD() { return usersTakeTOrD; }
    }
}
