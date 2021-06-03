package com.example.findspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.findspot.adapter.PositionGroupListAdapter;
import com.example.findspot.data.PositionItemInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import static com.example.findspot.SelectWhomActivity.ghistory;
import static com.example.findspot.SelectWhomActivity.list_g_users;

public class ChoiceGPSGroupActivity extends AppCompatActivity {
    public static ArrayList<PositionItemInfo> position_tmp;    //주소를 받는 타이밍을 겹치지 못하게 순차처리 될 수 있게 하기 위해 사용하는 리스트
    public static ArrayList<PositionItemInfo> list_group;      //중간지점을 찾을 결론적 위치 (이름, 도로명주소, 위도, 경도)로 구성된 리스트

    TextView tv_applyhistory;
    Button btn_search_time, btn_search_distance;

    PositionGroupListAdapter listAdapter;       //위치 리스트 어댑터(UI 구현)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_gps_group);

        String getExtraH = getIntent().getStringExtra("history");

        position_tmp = new ArrayList<>();
        list_group = new ArrayList<>();         //TODO: ShowMiddleActivity에서 뒤로가기를 할 때 clear해줘야 함
        for (PositionItemInfo pi : list_g_users) {
            list_group.add(new PositionItemInfo(pi.getUserName(), "", 0.0, 0.0));
        }

        //위치 기록을 위한 그룹 리스트 초기설정
        listAdapter = new PositionGroupListAdapter(this, R.layout.positionrow_group, list_group);
        ListView lv = (ListView) findViewById(R.id.choice_gps_g_lv_position);
        lv.setAdapter(listAdapter);

        tv_applyhistory = findViewById(R.id.choice_gps_g_applyhistory);     //최근 기록을 볼 수 있는 지 tv 보여줌
        if (getExtraH.equals("noHistory"))
            tv_applyhistory.setVisibility(View.INVISIBLE);
        else
            tv_applyhistory.setText(getExtraH);     //최근 기록 확인 [시간/거리 기준]  >>
        btn_search_time = (Button) findViewById(R.id.choice_gps_g_bt_time);
        btn_search_distance = (Button) findViewById(R.id.choice_gps_g_bt_distance);

        choice_tv_clickListener();         //사용자 GPS 설정 화면의 텍스트뷰에 해당하는 onClickListener를 정의한 함수를 호출함
        choice_btn_clickListener();        //사용자 GPS 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void choice_tv_clickListener() {
        tv_applyhistory.setOnClickListener(v -> {       //"최근에 [시간/거리] 찾은 위치 보기" 텍스트 클릭 시
            //위치 기록으로 중간지점 찾기가 보이도록 함
            if (ghistory.getStandard().equals("T")) {  //최근에 이 그룹으로 시간 기준 중간지점을 찾은 적이 있음
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("activity_tag", "group");   //어떤 Activity인지(random / group)
                it_showmiddle.putExtra("standard_tag", "time");    //시간 기준
                it_showmiddle.putExtra("isHistory", "true");       //이전 history 결과를 보여줘야 함
                startActivity(it_showmiddle);
            }
            else {  //최근에 이 그룹으로 거리 기준 중간지점을 찾은 적이 있음(D)
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("activity_tag", "group");    //어떤 Activity인지(random / group)
                it_showmiddle.putExtra("standard_tag", "distance"); //거리 기준
                it_showmiddle.putExtra("isHistory", "true");        //이전 history 결과를 보여줘야 함
                startActivity(it_showmiddle);
            }
        });
    }

    void choice_btn_clickListener() {
        btn_search_time.setOnClickListener(v -> {   //"시간 기준" 버튼 클릭시,
            boolean isNext = true;
            for (PositionItemInfo pi : list_group) {
                if (pi.getRoadName().equals("")) {      //입력한 도로명이 없을 시
                    isNext = false;
                    Toast.makeText(getApplicationContext(), "입력하지 않은 위치가 있습니다.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if (isNext) {       //다음 화면으로 넘어갈 수 있음
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("standard_tag", "time");    //시간 기준
                it_showmiddle.putExtra("activity_tag", "group");   //어떤 Activity인지(random / group)
                it_showmiddle.putExtra("isHistory", "false");      //history 결과 보여주는 것이 아님
                startActivity(it_showmiddle);
            }
        });

        btn_search_distance.setOnClickListener(v -> {   //"거리 기준" 버튼 클릭시,
            //activity_showmiddle로 화면 이동하고 거리 기준임을 intent로 전달
            Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
            it_showmiddle.putExtra("standard_tag", "distance");     //거리 기준
            it_showmiddle.putExtra("activity_tag", "group");        //어떤 Activity인지(random / group)
            it_showmiddle.putExtra("isHistory", "false");           //history 결과 보여주는 것이 아님
            startActivity(it_showmiddle);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 101 && resultCode == 1) {
            String resultText = "값이없음";
            try {
                resultText = new ChoiceGPSGroupActivity.Task().execute().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            String[] resultXY = geojsonParser(resultText);  //JSON 형식에서 x,y값 추출하기

            position_tmp.get(0).setLongitude(Double.parseDouble(resultXY[0]));
            position_tmp.get(0).setLatitude(Double.parseDouble(resultXY[1]));

            for (int i = 0 ; i < list_group.size(); i++) {
                if (list_group.get(i).getUserName().equals(position_tmp.get(0).getUserName())) {
                    list_group.set(i, position_tmp.get(0));
                    break;
                }
            }

            position_tmp.remove(0); //0번째 아이템 삭제
            listAdapter.notifyDataSetChanged();     //리스트 갱신
        }
    }

    //KaKao Geocode API 통해 주소 검색 결과 받기
    @SuppressLint("StaticFieldLeak")
    public class Task extends AsyncTask<String, Void, String> {
        String receiveMsg = "";
        String KAKAO_KEY = getString(R.string.kakao_key);  //KAKAO REST API 키
        String auth = "KakaoAK " + KAKAO_KEY;
        URL link = null;
        HttpsURLConnection hc = null;

        Task() { super(); }

        @Override
        protected String doInBackground(String... params) {
            try {
                link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(position_tmp.get(0).getRoadName(), "UTF-8")); //한글을 URL용으로 인코딩

                HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);

                hc = (HttpsURLConnection) link.openConnection();
                hc.setRequestMethod("GET");
                hc.setRequestProperty("User-Agent", "Java-Client");   //https 호출시 user-agent 필요
                hc.setRequestProperty("X-Requested-With", "curl");
                hc.setRequestProperty("Authorization", auth);

                //String 형태로 결과 받기
                if (hc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(hc.getInputStream(), StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder buffer = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    Log.i("receiveMsg : ", receiveMsg);

                    reader.close();
                } else {
                    Log.i("통신 결과", hc.getResponseCode() + "에러");
                }
            } catch (IOException e) { e.printStackTrace(); }

            return receiveMsg;
        }
    }

    //API 문자열 결과 JSON으로 파싱하기
    public String[] geojsonParser(String jsonString) {
        String[] geo_array = new String[2]; //API 결과 중 필요한 값(x,y)

        try {
            JSONArray jarray = new JSONObject(jsonString).getJSONArray("documents");
            JSONObject jObject = jarray.getJSONObject(0).getJSONObject("road_address");

            geo_array[0] = (String) jObject.optString("x");  //x좌표
            geo_array[1] = (String) jObject.optString("y");  //y좌표
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return geo_array;
    }
}
