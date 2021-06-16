package com.example.findspot.algorithm;

import com.example.findspot.data.PositionItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class MiddleDPointAlgorithm {
    HashSet<Position> setPositions = new HashSet<>();

    public MiddleDPointAlgorithm(ArrayList<PositionItemInfo> list) {
        for (PositionItemInfo user : list) {
            setPositions.add(new Position(user.getLatitude(), user.getLongitude()));
        }
    }

    public double[] calMiddlePoint() {
        double middleDLat = 0.0, middleDLong = 0.0;
        HashMap<Integer, Position> checkedIndex = new HashMap<>();          //결합된 적이 있는지 체크하는 변수 (인덱스, 위치)
        ArrayList<CombineCandidate> combineCandidates = new ArrayList<>();  //combine 할 수 있는 후보

        boolean completedflag = false;
        while (!completedflag) {
            //loop 돌때마다 새로 초기화
            checkedIndex.clear();
            combineCandidates.clear();
            ArrayList<Position> allPosition = new ArrayList<>(setPositions);

            /* 알고리즘 시작 */
            //모든 위치간의 거리 구하기
            for (int i = 0; i < allPosition.size(); i++) {
                for (int j = 0; j < i; j++) {
                    double dist = calDistance(allPosition.get(i).getLatitude(), allPosition.get(i).getLongitude(), allPosition.get(j).getLatitude(), allPosition.get(j).getLongitude());
                    if (dist < 1) {   //1km는 임의로 정한 것임(전체 크기를 보고 상대적으로 정해야 되지 않을까..?)
                        combineCandidates.add(new CombineCandidate(dist, i, allPosition.get(i), j, allPosition.get(j)));    //일정 거리 이내이면 결합 후보 리스트에 추가
                    }
                }
            }
            Collections.sort(combineCandidates);    //combineCandidates 거리 기준으로 오름차순 정렬

            if (combineCandidates.size() > 0) {
                for (int i = 0; i < combineCandidates.size(); i++) {
                    Position candidatePos1 = combineCandidates.get(i).getPos1();
                    Position candidatePos2 = combineCandidates.get(i).getPos2();
                    int candidateIndex1 = combineCandidates.get(i).getIndex1();
                    int candidateIndex2 = combineCandidates.get(i).getIndex2();
                    //checkedIndex에 Index1,Index2가 모두 없다면 combine
                    if (!checkedIndex.containsKey(candidateIndex1) && !checkedIndex.containsKey(candidateIndex2)) {
                        //checkedIndex에 추가
                        checkedIndex.put(candidateIndex1, candidatePos1);
                        checkedIndex.put(candidateIndex2, candidatePos2);
                        //setPosition에서 삭제
                        setPositions.remove(candidatePos1);
                        setPositions.remove(candidatePos2);
                        //combine된 새로운 위치 생성
                        double newLat = (candidatePos1.getLatitude() + candidatePos2.getLatitude()) / 2;
                        double newLong = (candidatePos1.getLongitude() + candidatePos2.getLongitude()) / 2;
                        setPositions.add(new Position(newLat, newLong)); //setPosition에 새로운 위치 추가
                    }
                }
            }
            else
                completedflag = true;   //결합된게 없으면 종료
        }
        for(Position pos : setPositions) {
            middleDLat = middleDLat + pos.getLatitude();
            middleDLong = middleDLong + pos.getLongitude();
        }
        middleDLat = middleDLat / setPositions.size();
        middleDLong = middleDLong / setPositions.size();

        return new double[]{middleDLat, middleDLong};
    }

    //위치 간의 거리 구하는 함수(반환 단위: km)
    public static double calDistance(double lat1, double long1, double lat2, double long2) {        //위도와 경도 간의 거리를 km로 변환함
        double theta = long1 - long2;
        double distance = Math.sin((double)(lat1*Math.PI/180.0)) * Math.sin((double)(lat2*Math.PI/180.0)) + Math.cos((double)(lat1*Math.PI/180.0)) * Math.cos((double)(lat2*Math.PI/180.0)) * Math.cos((double)(theta*Math.PI/180.0));

        distance = Math.acos(distance);
        distance = (double)(distance*180.0/Math.PI);
        distance = distance * 60 * 1.1515;      //mile단위
        distance = distance * 1.609344;         //mile단위->km단위

        return Math.abs(distance);
    }

    public static class Position {
        private final double latitude;
        private final double longitude;

        Position(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        @Override
        public boolean equals(Object pos) {
            if(this == pos) return true;
            if(pos == null || getClass() != pos.getClass()) return false;

            Position other = (Position) pos;
            return other.latitude == latitude && other.longitude == longitude;
        }

        @Override
        public int hashCode() {
            long lat = Double.doubleToLongBits(latitude);
            int result = (int)(lat ^ (lat >>> 32));

            long longit = Double.doubleToLongBits(longitude);
            result = 31 * result + (int)(longit ^ (longit >>> 32));

            return result;
        }
    }

    public static class CombineCandidate implements Comparable<CombineCandidate> {
        private final double distance;    //결합 후보군과의 거리
        private final int index1;         //결합 후보군1의 인덱스 (pos1/2/3..)
        private final Position pos1;      //결합 후보군1의 위치
        private final int index2;         //결합 후보군2의 인덱스 (pos1/2/3..)
        private final Position pos2;      //결합 후보군2의 위치

        //생성자
        public CombineCandidate(double distance, int index1, Position pos1, int index2, Position pos2) {
            this.distance = distance;
            this.index1 = index1;
            this.pos1 = pos1;
            this.index2 = index2;
            this.pos2 = pos2;
        }

        public double getDistance() { return distance; }
        public int getIndex1() { return index1; }
        public Position getPos1() { return pos1; }
        public int getIndex2() { return index2; }
        public Position getPos2() { return pos2; }

        //거리 기준으로 정렬(오름차순)
        public int compareTo(CombineCandidate c) {
            return Double.compare(this.distance, c.getDistance());
        }
    }
}
