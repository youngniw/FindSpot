package com.example.findspot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class ChoiceGPSActivity extends AppCompatActivity {
    static String address;  // 주소(도로명주소/지번주소)

    EditText et_position;
    Button btn_add, btn_search_time, btn_search_distance;
    ArrayList<PositionItem> list;       //(도로명주소,위도,경도)로 구성된 리스트
    PositionListAdapter listAdapter;    //위치 리스트 어댑터(UI 구현)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_gps);

        list = new ArrayList<PositionItem>();

        //위치 기록을 위한 리스트 초기설정
        listAdapter = new PositionListAdapter(this, R.layout.positionrow, list);
        ListView lv = (ListView) findViewById(R.id.choice_gps_lv_position);
        lv.setAdapter(listAdapter);

        //테스트를 위해 항목추가
        PositionItem item1 = new PositionItem("서울특별시 용산구 임정로 7 (효창동, 숙명여자대학교 동문회관)", 0.0, 0.0);
        list.add(item1);             //리스트에 PositionItem 추가
        item1 = new PositionItem("서울특별시 서초구 남부순환로 2406(서초동)", 0.0, 0.0);
        list.add(item1);             //리스트에 PositionItem 추가
        listAdapter.notifyDataSetChanged(); //리스트 갱신

        et_position = (EditText) findViewById(R.id.choice_gps_et_inputPosition);    //위치를 입력하는 editText
        et_position.setFocusable(false);

        btn_add = (Button) findViewById(R.id.choice_gps_bt_add);
        btn_search_time = (Button) findViewById(R.id.choice_gps_bt_time);
        btn_search_distance = (Button) findViewById(R.id.choice_gps_bt_distance);

        choice_et_clickListener();         //사용자 위치 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
        choice_btn_clickListener();        //사용자 GPS 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void choice_et_clickListener() {
        et_position.setOnClickListener(new View.OnClickListener() {        //위치 설정 텍스트 클릭
            @Override
            public void onClick(View v) {
                //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
                Intent it_address = new Intent(ChoiceGPSActivity.this, DaumWebViewActivity.class);
                startActivityForResult(it_address, 100);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 100 && resultCode == 1) {
            et_position.setText(String.format("%s", address));
        }
    }


    void choice_btn_clickListener() {
        //사용자 위치 항목 추가하기 (이벤트)
        btn_add.setOnClickListener(new View.OnClickListener() {        //위치 추가 버튼 클릭
            @Override
            public void onClick(View v) {
                String name = et_position.getText().toString();
                double longitude = 0.0, latitude = 0.0;

                //좌표제공API로 위도경도 알아내고, PositionItem에 값 넣기
                if ((name.equals(""))) {    //도로명주소가 입력되지 않았는데 '추가'버튼을 클릭할 경우
                    Toast.makeText(ChoiceGPSActivity.this.getApplicationContext(), "주소가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ChoiceGPSActivity.this.getApplicationContext(), "주소가 입력되었습니다.", Toast.LENGTH_SHORT).show();

                    String resultText = "값이없음";

                    try {
                        resultText = new Task().execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    String[] resultXY = geojsonParser(resultText);

                    longitude = Double.parseDouble(resultXY[0]);
                    latitude = Double.parseDouble(resultXY[1]);

                    PositionItem item = new PositionItem(name, latitude, longitude);    //PositionItem 생성
                    list.add(item);             //리스트에 PositionItem 추가
                    et_position.setText("");    //et_position 초기화
                    listAdapter.notifyDataSetChanged(); //리스트 갱신

                    Log.i("결과확인","name: "+ item.getName()+ "latitude: "+ String.valueOf(item.getLatitude())+ "longitude: "+ String.valueOf(item.getLongitude()));
                }
            }
        });

        btn_search_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSActivity.this, ShowMiddleActivity.class);

                // 넘겨줄 데이터 Bundle 형태로 만들기
                Bundle extras = new Bundle();
                extras.putString("standard_tag", "time");    //시간 기준
                it_showmiddle.putExtras(extras);

                startActivity(it_showmiddle);
            }
        });

        btn_search_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 거리 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSActivity.this, ShowMiddleActivity.class);

                // 넘겨줄 데이터 Bundle 형태로 만들기
                Bundle extras = new Bundle();
                extras.putString("standard_tag", "distance");    //거리 기준
                it_showmiddle.putExtras(extras);

                startActivity(it_showmiddle);
            }
        });
    }

    public class Task extends AsyncTask<String, Void, String> {

        String receiveMsg = "";

        String KAKAO_KEY = "dc90ecf7e13bbcfd5d02a7a41ed33464";
        String auth = "KakaoAK " + KAKAO_KEY;
        URL link= null;
        HttpsURLConnection hc = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address, "UTF-8"));

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });

                hc = (HttpsURLConnection)link.openConnection();
                hc.setRequestMethod("GET");
                hc.setRequestProperty("User-Agent", "Java-Client");   // https 호출시 user-agent 필요
                hc.setRequestProperty("X-Requested-With", "curl");
                hc.setRequestProperty("Authorization", auth);

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

    public String[] geojsonParser(String jsonString) {
        String x = null;
        String y = null;
        String[] arraysum = new String[2];
        try {
            JSONArray jarray = new JSONObject(jsonString).getJSONArray("documents");
            JSONObject jObject = jarray.getJSONObject(0).getJSONObject("road_address");
            x = (String) jObject.optString("x");
            y = (String) jObject.optString("y");
            arraysum[0] = x;
            arraysum[1] = y;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraysum;
    }
}