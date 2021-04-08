package com.example.findspot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class ChoiceGPSGroupActivity extends AppCompatActivity {
    static ArrayList<PositionItem> position_tmp;

    Button btn_search_time, btn_search_distance;
    static ArrayList<PositionItem> list_group;  //중간지점을 찾을 결론적 위치 (이름, 도로명주소, 위도, 경도)로 구성된 리스트
    ArrayList<PositionItem> list_g_users;       //(그룹에 속한 사용자 닉네임, 지정한 위치의 위도,경도)로 구성된 리스트
    PositionGroupListAdapter listAdapter;       //위치 리스트 어댑터(UI 구현)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_gps_group);

        position_tmp = new ArrayList<PositionItem>();

        list_group = new ArrayList<PositionItem>();

        list_g_users = new ArrayList<PositionItem>();
        //**********************************(DB로부터 PositionGroupUserItem를 그룹의 인원수만큼 받아와야함)********************************************************
        list_g_users.add(new PositionItem("위영은", "수내역", 10.00201203123, 20.2321454135827893));
        list_g_users.add(new PositionItem("김소은", "목내역", 8.00201203123, 30.232124135827893));
        list_g_users.add(new PositionItem("아야여", "금내역", 70.00201203123, 40.232124135827893));
        list_g_users.add(new PositionItem("헤로우", "토내역", 16.00201203123, 50.232124135827893));

        //위치 기록을 위한 그룹 리스트 초기설정
        listAdapter = new PositionGroupListAdapter(this, R.layout.positionrow_group, list_g_users);
        ListView lv = (ListView) findViewById(R.id.choice_gps_g_lv_position);
        lv.setAdapter(listAdapter);

        btn_search_time = (Button) findViewById(R.id.choice_gps_g_bt_time);
        btn_search_distance = (Button) findViewById(R.id.choice_gps_g_bt_distance);

        choice_btn_clickListener();        //사용자 GPS 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void choice_btn_clickListener() {
        btn_search_time.setOnClickListener(new View.OnClickListener() { //"시간 기준" 버튼 클릭시,
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("standard_tag", "time");    //시간 기준
                it_showmiddle.putExtra("activity_tag", "group");   //어떤 Activity인지(random / group)
                startActivity(it_showmiddle);
            }
        });

        btn_search_distance.setOnClickListener(new View.OnClickListener() { //"거리 기준" 버튼 클릭시,
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 거리 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("standard_tag", "distance");     //거리 기준
                it_showmiddle.putExtra("activity_tag", "group");        //어떤 Activity인지(random / group)
                startActivity(it_showmiddle);
            }
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

            //이미 한번 위치를 추가한 적이 있다면 바꾸고, 아니라면 위치 결과들을 모아놓은 list_group에 추가함
            boolean ischange_flag = false;
            for (int i = 0 ; i < list_group.size(); i++) {
                if (list_group.get(i).getUserName().equals(position_tmp.get(0).getUserName())) {
                    list_group.set(i, position_tmp.get(0));
                    ischange_flag = true;
                    break;
                }
            }
            if (!ischange_flag) list_group.add(position_tmp.get(0));

            position_tmp.remove(0); //0번째 아이템 삭제
            listAdapter.notifyDataSetChanged();     //리스트 갱신
        }
    }

    //KaKao Geocode API 통해 주소 검색 결과 받기
    public class Task extends AsyncTask<String, Void, String> {
        String receiveMsg = "";

        String KAKAO_KEY = "dc90ecf7e13bbcfd5d02a7a41ed33464";  //KAKAO REST API 키
        String auth = "KakaoAK " + KAKAO_KEY;
        URL link = null;
        HttpsURLConnection hc = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(position_tmp.get(0).getRoadName(), "UTF-8")); //한글을 URL용으로 인코딩

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });

                hc = (HttpsURLConnection) link.openConnection();
                hc.setRequestMethod("GET");
                hc.setRequestProperty("User-Agent", "Java-Client");   //https 호출시 user-agent 필요
                hc.setRequestProperty("X-Requested-With", "curl");
                hc.setRequestProperty("Authorization", auth);

                //String 형태로 결과 받기
                if (hc.getResponseCode() == hc.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(hc.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

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