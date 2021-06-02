package com.example.findspot.data;

//사용자 위치 리스트를 구성하는 아이템 (닉네임(생략가능), 도로명주소, 위도, 경도를 저장)
public class PositionItemInfo {
    private String userName = "";
    private String roadName;
    private double latitude, longitude;

    public PositionItemInfo(String roadName, double latitude, double longitude) {
        super();
        this.roadName = roadName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PositionItemInfo(String userName, String roadName, double latitude, double longitude) {
        super();
        this.userName = userName;
        this.roadName = roadName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserName() { return userName; }
    public String getRoadName() { return roadName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
