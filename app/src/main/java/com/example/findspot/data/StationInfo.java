package com.example.findspot.data;

public class StationInfo {
    private final String station;           //지하철역 이름
    private final double stationLat;        //지하철역의 위도
    private final double stationLong;       //지하철역의 경도

    public StationInfo(String station, double stationLat, double stationLong) {
        this.station = station;
        this.stationLat = stationLat;
        this.stationLong = stationLong;
    }

    public String getStation() { return station; }
    public double getStationLat() { return stationLat; }
    public double getStationLong() { return stationLong; }
}
