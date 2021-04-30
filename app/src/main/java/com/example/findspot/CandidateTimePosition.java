package com.example.findspot;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import static com.example.findspot.ShowMiddleActivity.countFinishLoop;
import static com.example.findspot.ShowMiddleActivity.current;
import static com.example.findspot.ShowMiddleActivity.minTimeGapS;
import static com.example.findspot.ShowMiddleActivity.resultTPositions;
import static com.example.findspot.ShowMiddleActivity.searchTPositions;

public class CandidateTimePosition implements Comparable<CandidateTimePosition> {
    private ShowMiddleActivity activity;
    private int whichStation = 0;
    private int timeGap = 0;    //소요시간이 제일 큰 위치와 작은 위치의 소요시간 차
    private int size = 0;       //사용자 수
    private String stationName = "";
    private double resultPositionX = 0.0;       //endX값
    private double resultPositionY = 0.0;       //endY값
    private ArrayList<Integer> mt = null;       //사용자별 소요시간

    CandidateTimePosition (ShowMiddleActivity activity, Context context, int whichStation, ArrayList<PositionItem> list, String stationName, double resultPositionX, double resultPositionY) {
        this.activity = activity;
        this.whichStation = whichStation;

        mt = new ArrayList<>();

        this.stationName = stationName;
        this.resultPositionX = resultPositionX;        //임시로 계산할 중간지점 x값(경도)
        this.resultPositionY = resultPositionY;        //임시로 계산할 중간지점 y값(위도)
        this.size = list.size();

        for (int i=0; i<list.size(); i++) {     //중간지점을 찾는 사람들의 거리상의 중간지점과의 시간 오차를 얻어냄
            new MiddleTime(context, this, list.get(i).getLongitude(), list.get(i).getLatitude(), resultPositionX, resultPositionY);
        }
    }
    CandidateTimePosition () {  //거리상 중간지점을 제외한 소요시간 차가 가장 작은 지하철역을 저장하기 위해 초기 값을 가장 큰 숫자로 해서 추후에 주변 리스트의 값 중 하나가 저장되게 함
        this.timeGap = Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(CandidateTimePosition ctp) {   //timeGap값에 대해 오름차순 정렬이 가능하게 함 TODO: 오름차순인지 확인해야 함
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
        for (int i=0; i<mt.size(); i++) {     //중간지점을 찾는 사람들의 거리상의 중간지점과의 시간 오차를 얻어냄
            if (minTime > mt.get(i))      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최소값보다 작은 경우
                minTime = mt.get(i);

            if (maxTime < mt.get(i))      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최대값보다 작은 경우
                maxTime = mt.get(i);
        }
        timeGap = maxTime - minTime;
        Log.i("stationGap+Name", String.valueOf(timeGap)+": "+ stationName);

        switch (whichStation) {
            case 0: {   //거리상 중간 지점에 해당하는 역이 객체일 때
                Log.i("stationSwitch", "case0");
                if (timeGap <= 10)      //거리상 중간 지점의 사용자들의 소요 시간 오차가 10보다 작은 경우
                    resultTPositions.add(this);       //결과 후보 중에 하나로 저장함

                activity.searchNext();      //timeGap 값이 10보다 작거나 같은 경우에는 결과 후보로 가능하므로 resultPosition에 추가됨
                break;
            }
            case 1: {   //거리상 중간 지점 근처 역에 해당하는 객체일 때
                Log.i("stationSwitch", "case1");
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
                    activity.searchNextNext();

                Log.i("stationCase1Name", stationName);
                break;
            }
            case 2: {   //거리 상 중간 지점의 근처 역중 가장 소요시간이 작은 역의 근처역 or SearchTPositions의 역을 기준으로 하는 근처 역에 해당하는 객체일 때
                Log.i("stationSwitch", "case2");
                activity.countFinishLoop--;

                if (timeGap < minTimeGapS.getTimeGap())
                    minTimeGapS = this;   //주변 역들 중 소요시간 최소로 저장함

                if (timeGap <= 10)  //해당 역의 시간 소요 오차 시간의 최대와 최소가 10보다 작거나 같을 때
                    resultTPositions.add(this);   //10보다 작거나 같으므로 결과 후보로 추가

                Log.i("stationCase2Name", stationName);

                if (countFinishLoop == 0)
                    activity.searchNextNextNextNext();
                break;
            }
        }
    }

    public int getTimeGap() { return timeGap; }
    public int getSize() { return size; }

    public String getStationName() { return stationName; }
    public double getResultPositionX() { return resultPositionX; }
    public double getResultPositionY() { return resultPositionY; }
    public ArrayList<Integer> getMt() { return mt; }
}
