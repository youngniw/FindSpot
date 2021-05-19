package com.example.findspot;

public class StationInfo {
    private String station = "";   //지하철역 이름
    private double stationX = 0.0;       //지하철역의 경도
    private double stationY = 0.0;       //지하철역의 위도

    StationInfo(String station, double stationX, double stationY) {
        this.station = station;
        this.stationX = stationX;
        this.stationY = stationY;
    }

    public String getStation() { return station; }
    public double getStationX() { return stationX; }
    public double getStationY() { return stationY; }
}
