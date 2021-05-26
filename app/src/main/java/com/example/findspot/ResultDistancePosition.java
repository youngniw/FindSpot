package com.example.findspot;

import java.util.ArrayList;

public class ResultDistancePosition implements Comparable<ResultDistancePosition> {
    private int size = 0;               //사용자 수
    private double distanceGap = 0.0;        //지하철까지 거리가 제일 먼 사용자의 위치와 가까운 사용자의 위치의 거리차(km단위)
    private String stationName = "";    //지하철 역 이름
    private double resultPositionX = 0.0;       //역의 x값
    private double resultPositionY = 0.0;       //역의 y값
    private ArrayList<Double> distanceList = null;    //사용자별 해당 역까지의 거리(km단위)

    ResultDistancePosition(int size, String stationName, double resultPositionX, double resultPositionY, ArrayList<Double> distanceList){
        this.size = size;
        this.stationName = stationName;
        this.resultPositionX = resultPositionX;
        this.resultPositionY = resultPositionY;
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
    public double getResultPositionX() { return resultPositionX; }
    public double getResultPositionY() { return resultPositionY; }
    public ArrayList<Double> getDistanceList() { return distanceList; }

    @Override
    public int compareTo(ResultDistancePosition ctp) {   //distanceGap값에 대해 오름차순 정렬이 가능하게 함
        if (this.distanceGap < ctp.getDistanceGap()) {
            return -1;
        } else if (this.distanceGap > ctp.getDistanceGap()) {
            return 1;
        }
        return 0;
    }
}
