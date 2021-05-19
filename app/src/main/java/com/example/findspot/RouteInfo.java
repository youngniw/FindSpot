package com.example.findspot;

import android.util.Log;

import java.util.ArrayList;

public class RouteInfo {
    private int pathType = 0;     //결과 종류(1: 지하철, 2: 버스, 3: 지하철+버스, 4: history임)
    private int totalTime = 0;
    private int transitNum = 0;     //총 환승횟수(pathType가 4일 때는 사용되지 않음)
    private ArrayList<SubPath> paths;       //(pathType가 4일 때는 사용되지 않음)

    RouteInfo(int pathType, int totalTime, int transitNum) {
        this.pathType = pathType;
        this.totalTime = totalTime;
        this.transitNum = transitNum;
        paths = new ArrayList<SubPath>();
    }

    public int getPathType() { return pathType; }
    public int getTotalTime() { return totalTime; }
    public int getTransitNum() { return transitNum; }
    public ArrayList<SubPath> getPaths() { return paths; }

    public static class SubPath {
        private int trafficType = 0;        //이동수단 종류(1: 지하철, 2: 버스, 3: 도보)
        private int subTime = 0;            //이동수단의 소요시간
        private String boardName = "";      //승차 정류장/역 이름
        private String arriveName = "";     //하차 정류장/역 이름
        private String subwayName = "";     //지하철일 때, 노선명
        private String busName = "";        //버스일 때, 버스명

        SubPath(int trafficType, int subTime, String boardName, String arriveName, String subwayName, String busName){
            this.trafficType = trafficType;
            this.subTime = subTime;
            this.boardName = boardName;
            this.arriveName = arriveName;
            this.subwayName = subwayName;
            this.busName = busName;

            Log.i("PResult서브경로", trafficType+" / "+subTime+" / "+boardName+" / "+arriveName+" / "+subwayName+" / "+busName);
        }

        SubPath(int trafficType, int subTime) {     //도보일 때
            this.trafficType = trafficType;
            this.subTime = subTime;
            Log.i("PResult도보", trafficType+" / "+subTime+" / ");
        }

        public int getSubTime() { return subTime; }
        public int getTrafficType() { return trafficType; }
        public String getBoardName() { return boardName; }
        public String getArriveName() { return arriveName; }
        public String getSubwayName() { return subwayName; }
        public String getBusName() { return busName; }
    }
}
