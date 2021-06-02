package com.example.findspot;

import android.content.Context;
import android.util.Log;

import com.example.findspot.data.PositionItemInfo;
import com.example.findspot.data.RouteInfo;

import java.util.ArrayList;

import static com.example.findspot.ShowMiddleActivity.countFinishLoop;
import static com.example.findspot.ShowMiddleActivity.current;
import static com.example.findspot.ShowMiddleActivity.minTimeGapS;
import static com.example.findspot.ShowMiddleActivity.resultTPositions;
import static com.example.findspot.ShowMiddleActivity.searchTPositions;

public class CandidateTimePosition implements Comparable<CandidateTimePosition> {
    private ShowMiddleActivity activity;
    private int whichStation = 0;
    private int size = 0;           //사용자 수
    private int saveCompleteN = 0;  //사용자의 결과가 도출된 개수
    private int timeGap = 0;        //소요시간이 제일 큰 위치와 작은 위치의 소요시간 차
    private String stationName = "";
    private double resultPositionLat = 0.0;         //endLat값(위도)
    private double resultPositionLong = 0.0;        //endLong값(경도)
    private ArrayList<RouteInfo> routes = null;     //사용자별 경로 정보

    CandidateTimePosition(Context context, int whichStation, ArrayList<PositionItemInfo> list, String stationName, double resultPositionLat, double resultPositionLong) {
        this.activity = (ShowMiddleActivity) context;
        this.whichStation = whichStation;

        routes = new ArrayList<RouteInfo>();

        this.stationName = stationName;
        this.resultPositionLat = resultPositionLat;        //임시로 계산할 중간지점 위도값
        this.resultPositionLong = resultPositionLong;      //임시로 계산할 중간지점 경도값
        this.size = list.size();

        for (int i=0; i<list.size(); i++) {     //중간지점을 찾는 사람들의 거리상의 중간지점과의 시간 오차를 얻어냄
            new MiddleTime(context, this, list.get(i).getLongitude(), list.get(i).getLatitude(), resultPositionLong, resultPositionLat);
        }
    }
    CandidateTimePosition() {  //거리상 중간지점을 제외한 소요시간 차가 가장 작은 지하철역을 저장하기 위해 초기 값을 가장 큰 숫자로 해서 추후에 주변 리스트의 값 중 하나가 저장되게 함
        this.timeGap = Integer.MAX_VALUE;
    }

    //시간 기준 history를 위해 사용됨
    CandidateTimePosition(int size, ArrayList<Double> takeTimeList, String stationName) {
        this.size = size;
        this.stationName = stationName;

        //takeTimeList를 통해 timeGap 구함
        Double min = takeTimeList.get(0);
        Double max = takeTimeList.get(0);
        for (int i=1; i<takeTimeList.size(); i++){
            if (takeTimeList.get(i)>max)
                max = takeTimeList.get(i);

            if (takeTimeList.get(i)<min)
                min = takeTimeList.get(i);
        }
        timeGap = max.intValue()-min.intValue();

        routes = new ArrayList<RouteInfo>();
        for (Double take : takeTimeList) {
            routes.add(new RouteInfo(4, take.intValue(), 0));
        }
    }


    @Override
    public int compareTo(CandidateTimePosition ctp) {   //timeGap값에 대해 오름차순 정렬이 가능하게 함
        if (this.timeGap < ctp.getTimeGap()) {
            return -1;
        } else if (this.timeGap > ctp.getTimeGap()) {
            return 1;
        }
        return 0;
    }

    public void calTimeGap() {
        //리스트의 순서대로 실행되게 함
        int maxTime = 0;
        int minTime = Integer.MAX_VALUE;
        for (int i=0; i<routes.size(); i++) {     //중간지점을 찾는 사람들의 거리상의 중간지점과의 시간 오차를 얻어냄
            if (minTime > routes.get(i).getTotalTime())      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최소값보다 작은 경우
                minTime = routes.get(i).getTotalTime();

            if (maxTime < routes.get(i).getTotalTime())      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최대값보다 작은 경우
                maxTime = routes.get(i).getTotalTime();
        }
        timeGap = maxTime - minTime;
        Log.i("CTP_stationGap+Name", timeGap+": "+ stationName);

        switch (whichStation) {
            case 0: {   //거리상 중간 지점에 해당하는 역이 객체일 때
                if (timeGap <= 10)      //거리상 중간 지점의 사용자들의 소요 시간 오차가 10보다 작은 경우
                    resultTPositions.add(this);       //결과 후보 중에 하나로 저장함

                activity.getDMiddleNearTime();
                break;
            }
            case 1: {   //거리상 중간 지점 근처 역에 해당하는 객체일 때
                activity.countFinishLoop--;

                if (timeGap < minTimeGapS.getTimeGap())
                    minTimeGapS = this;   //주변 역들 중 소요시간 최소로 저장함

                if (timeGap <= 10) { //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                    resultTPositions.add(this);   //10보다 작거나 같으므로 결과 후보로 추가
                    searchTPositions.add(this);   //이후에 이 tmpStation역을 중심으로 반경 내에 이 역보다 최대/최소 소요시간의 차가 더 작은 역이 있을 수 있으므로 검색후보로 추가
                }
                else if (timeGap < current.getTimeGap())
                    searchTPositions.add(this);   //거리기준 중간지점역보다 소요시간 오차가 더 작으므로 결과 후보는 아니더라도 기준 검색 후보로 추가됨

                if (countFinishLoop == 0)
                    activity.getNearInfoByS();    //서버(DB)로부터 근처 역을 기준으로 한 그 근처의 지하철역들을 받아옴

                break;
            }
            case 2: {   //거리 상 중간 지점의 근처 역중 가장 소요시간이 작은 역의 근처역 or SearchTPositions의 역을 기준으로 하는 근처 역에 해당하는 객체일 때
                activity.countFinishLoop--;

                if (timeGap < minTimeGapS.getTimeGap())
                    minTimeGapS = this;   //주변 역들 중 소요시간 최소로 저장함

                if (timeGap <= 10)  //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                    resultTPositions.add(this);   //10보다 작거나 같으므로 결과 후보로 추가

                if (countFinishLoop == 0)
                    activity.resultTMiddle();     //결과 출력
                break;
            }
        }
    }

    public int getTimeGap() { return timeGap; }
    public int getSize() { return size; }
    public int getSaveCompleteN() { return saveCompleteN; }
    public void addSaveN() { saveCompleteN++; }
    public String getStationName() { return stationName; }
    public double getResultPositionLat() { return resultPositionLat; }
    public double getResultPositionLong() { return resultPositionLong; }
    public ArrayList<RouteInfo> getRouteInfo() { return routes; }
}
