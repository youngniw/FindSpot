package com.example.findspot;

//사용자 위치 리스트를 구성하는 아이템 (도로명주소, 위도, 경도를 저장)
public class PositionItem {
    private String name;
    private double latitude, longitude;

    public PositionItem(String name, double latitude, double longitude) {
        super();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public void setName(String name) { this.name = name; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
}