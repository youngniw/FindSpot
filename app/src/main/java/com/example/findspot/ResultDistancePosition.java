package com.example.findspot;

import java.util.ArrayList;

public class ResultDistancePosition implements Comparable<ResultDistancePosition> {
    private final int size;                   //사용자 수
    private final double distanceGap;         //지하철까지 거리가 제일 먼 사용자의 위치와 가까운 사용자의 위치의 거리차(km단위)
    private final String stationName;         //지하철 역 이름
    private final double resultPositionLat;   //역의 위도값
    private final double resultPositionLong;  //역의 경도값
    private final ArrayList<Double> distanceList; //사용자별 해당 역까지의 거리(km단위)

    ResultDistancePosition(int size, String stationName, double resultPositionLat, double resultPositionLong, ArrayList<Double> distanceList){
        this.size = size;
        this.stationName = stationName;
        this.resultPositionLat = resultPositionLat;
        this.resultPositionLong = resultPositionLong;
        this.distanceList = distanceList;

        //distanceList를 통해 distanceGap구함
        Double min = distanceList.get(0);
        Double max = distanceList.get(0);
        for (int i=1; i<distanceList.size(); i++){
            if (distanceList.get(i)>max)
                max = distanceList.get(i);

            if (distanceList.get(i)<min)
                min = distanceList.get(i);
        }
        distanceGap = Math.round((max-min)*100)/100.0;
    }

    public int getSize() { return size; }
    public double getDistanceGap() { return distanceGap; }
    public String getStationName() { return stationName; }
    public double getResultPositionLat() { return resultPositionLat; }
    public double getResultPositionLong() { return resultPositionLong; }
    public ArrayList<Double> getDistanceList() { return distanceList; }

    @Override
    public int compareTo(ResultDistancePosition rdp) {   //distanceGap값에 대해 오름차순 정렬이 가능하게 함
        if (this.distanceGap < rdp.getDistanceGap()) {
            return -1;
        } else if (this.distanceGap > rdp.getDistanceGap()) {
            return 1;
        }
        return 0;
    }
}
