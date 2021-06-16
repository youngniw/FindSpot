package com.example.findspot;

import android.content.Context;
import android.widget.Toast;

import com.example.findspot.data.RouteInfo;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MiddleTime {
    MiddleTime(Context context, final CandidateTimePosition result, double startX, double startY, double endX, double endY) {
        ODsayService odsayService = ODsayService.init(context, context.getString(R.string.odsay_key));      //key값을 통해 ODsayService 객체 생성

        odsayService.setReadTimeout(5000);          //서버 연결 제한시간 5초로 설정
        odsayService.setConnectionTimeout(5000);    //데이터 획득 제한시간 5초로 설정

        //API 결과 콜백 함수
        OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
            @Override
            public void onSuccess(ODsayData odsayData, API api) {
                try {
                    //API Value는 API 호출 메소드 명을 따라감
                    if (api == API.SEARCH_PUB_TRANS_PATH) {
                        JSONObject optimal_route = odsayData.getJson().getJSONObject("result").getJSONArray("path").getJSONObject(0);
                        JSONObject info = optimal_route.getJSONObject("info");
                        JSONArray subPath = optimal_route.getJSONArray("subPath");

                        RouteInfo tmp = new RouteInfo(optimal_route.getInt("pathType"), info.getInt("totalTime"),
                                info.getInt("busTransitCount")+info.getInt("subwayTransitCount")-1);
                        for (int i=0; i<subPath.length(); i++){     //중간경로 저장
                            JSONObject subP = subPath.getJSONObject(i);
                            if (subP.getInt("trafficType") == 3)      //도보일 경우
                                tmp.getPaths().add(new RouteInfo.SubPath(3, subP.getInt("sectionTime")));
                            else {
                                String type = (subP.getInt("trafficType") == 1) ? "name" : "busNo";

                                String lane = "";
                                for (int j=0; j<subP.getJSONArray("lane").length(); j++){
                                    lane = lane.concat(subP.getJSONArray("lane").getJSONObject(j).getString(type)+" / ");
                                }
                                lane = lane.substring(0, lane.length()-3);   //마지막 3문자 " / "삭제하기

                                if (subP.getInt("trafficType") == 1) {  //지하철
                                    tmp.getPaths().add(new RouteInfo.SubPath(1, subP.getInt("sectionTime"), subP.getString("startName"),
                                            subP.getString("endName"), lane, ""));
                                }
                                else if (subP.getInt("trafficType") == 2) {  //버스일 경우
                                    tmp.getPaths().add(new RouteInfo.SubPath(2, subP.getInt("sectionTime"), subP.getString("startName"),
                                            subP.getString("endName"), "", lane));
                                }
                            }
                        }

                        result.getRouteInfo().add(tmp);
                        result.addSaveN();

                        if (result.getSize() == result.getSaveCompleteN())
                            result.calTimeGap();    //모든 사용자의 소요시간을 구했으므로, 최대 소요시간 차이 등 계산을 수행함
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }

            @Override
            public void onError(int code, String message, API api) {
                if (api == API.SEARCH_PUB_TRANS_PATH) {
                    Toast.makeText(context, "경로가 확인되지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        odsayService.requestSearchPubTransPath(String.valueOf(startX), String.valueOf(startY),
                String.valueOf(endX), String.valueOf(endY), "0", "0", "0", onResultCallbackListener);
    }
}
