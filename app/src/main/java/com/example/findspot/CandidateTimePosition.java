package com.example.findspot;

import android.content.Context;

import java.util.ArrayList;

public class CandidateTimePosition implements Comparable<CandidateTimePosition> {
    int maxIndex = 0;        //(endX, endY)를 위치로 갖는 지점과의 소요시간이 가장 큰 위치의 인덱스 번호      TODO: 안씀
    int minIndex = 0;        //(endX, endY)를 위치로 갖는 지점과의 소요시간이 가장 작은 위치의 인덱스 번호    TODO: 안씀
    int timeGap = 0;    //소요시간이 제일 큰 위치와 작은 위치의 소요시간 차
    double resultPositionX = 0.0;      //endX값
    double resultPositionY = 0.0;      //endY값

    CandidateTimePosition (Context context, ArrayList<PositionItem> list, double resultPositionX, double resultPositionY) {
        this.resultPositionX = resultPositionX;        //임시로 계산할 중간지점 x값(경도)
        this.resultPositionY = resultPositionY;        //임시로 계산할 중간지점 y값(위도)

        //리스트의 순서대로 실행되게 함
        int maxTime = 0;
        int minTime = Integer.MAX_VALUE;
        for (int i=0; i<list.size(); i++) {     //중간지점을 찾는 사람들의 거리상의 중간지점과의 시간 오차를 얻어냄
            MiddleTime mt = new MiddleTime(context, list.get(i).getLongitude(), list.get(i).getLatitude(), resultPositionX, resultPositionY);
            if (minTime > mt.getTotalTime()) {      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최소값보다 작은 경우
                minIndex = i;
                minTime = mt.getTotalTime();
            }
            if (maxTime < mt.getTotalTime()) {      //얻어낸 소요시간이 이전까지 구한 소요시간 중 최대값보다 작은 경우
                maxIndex = i;
                maxTime = mt.getTotalTime();
            }
        }
        timeGap = maxTime - minTime;
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

    public int getTimeGap() { return timeGap; }
    public int getMaxIndex() { return maxIndex; }
    public int getMinIndex() { return minIndex; }
    public double getResultPositionX() { return resultPositionX; }
    public double getResultPositionY() { return resultPositionY; }
}
