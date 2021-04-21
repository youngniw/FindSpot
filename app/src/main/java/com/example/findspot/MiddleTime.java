package com.example.findspot;

import android.content.Context;
import android.util.Log;

import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MiddleTime {

    MiddleTime(Context context, double startX, double startY, double endX, double endY) {
        ODsayService odsayService = ODsayService.init(context, context.getString(R.string.odsay_key));      //key값을 통해 OKsayService 객체 생성

        odsayService.setReadTimeout(5000);  //서버 연결 제한시간 5초로 설정
        odsayService.setConnectionTimeout(5000);    //데이터 획득 제한시간 5초로 설정

        // 콜백 함수
        OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener() {
            @Override
            public void onSuccess(ODsayData odsayData, API api) {
                try {
                    // API Value 는 API 호출 메소드 명을 따라갑니다.
                    if (api == API.SEARCH_PUB_TRANS_PATH) {
                        int test1 = odsayData.getJson().getJSONObject("result").getJSONArray("path").getJSONObject(0).getJSONObject("info").getInt("totalTime");
                        Log.i("totalTime", String.valueOf(test1));
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int code, String message, API api) {
                if (api == API.SEARCH_PUB_TRANS_PATH) {}
            }
        };
        // 호출 성공 시 실행
        odsayService.requestSearchPubTransPath(String.valueOf(startX), String.valueOf(startY), String.valueOf(endX), String.valueOf(endY), "0","0","0", onResultCallbackListener);
    }
}
