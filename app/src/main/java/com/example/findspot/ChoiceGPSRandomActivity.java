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

public class ChoiceGPSRandomActivity extends AppCompatActivity {
    static String address_r;  // 주소(도로명주소/지번주소)

    EditText et_position;
    Button btn_add, btn_search_time, btn_search_distance;
    static ArrayList<PositionItem> list_random;       //(도로명주소,위도,경도)로 구성된 리스트
    PositionListAdapter listAdapter;    //위치 리스트 어댑터(UI 구현)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_gps_random);

        list_random = new ArrayList<PositionItem>();

        //위치 기록을 위한 리스트 초기설정
        listAdapter = new PositionListAdapter(this, R.layout.positionrow, list_random);
        ListView lv = (ListView) findViewById(R.id.choice_gps_r_lv_position);
        lv.setAdapter(listAdapter);

        et_position = (EditText) findViewById(R.id.choice_gps_r_et_inputPosition);    //위치를 입력하는 editText

        btn_add = (Button) findViewById(R.id.choice_gps_r_bt_add);
        btn_search_time = (Button) findViewById(R.id.choice_gps_r_bt_time);
        btn_search_distance = (Button) findViewById(R.id.choice_gps_r_bt_distance);

        choice_et_clickListener();         //사용자 위치 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
        choice_btn_clickListener();        //사용자 GPS 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void choice_et_clickListener() {
        et_position.setOnClickListener(new View.OnClickListener() {        //위치 설정 텍스트 클릭
            @Override
            public void onClick(View v) {
                //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
                Intent it_address = new Intent(ChoiceGPSRandomActivity.this, DaumWebViewActivity.class);
                it_address.putExtra("name", "");
                startActivityForResult(it_address, 100);
            }
        });
    }
    //startActivityForResult()에 대한 수신용 메소드
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 100 && resultCode == 1) {
            et_position.setText(String.format("%s", address_r));
        }
    }

    void choice_btn_clickListener() {
        btn_add.setOnClickListener(new View.OnClickListener() { //"추가" 버튼 클릭시,
            @Override
            public void onClick(View v) {   //"추가" 버튼을 클릭함으로써 위치를 추가하고 싶은 경우
                String name = et_position.getText().toString();

                //좌표제공API로 위도경도 알아내고, PositionItem에 값 넣기
                double longitude = 0.0, latitude = 0.0;
                if ((name.equals(""))) {    //도로명주소가 입력되지 않았는데 '추가'버튼을 클릭할 경우
                    Toast.makeText(ChoiceGPSRandomActivity.this.getApplicationContext(), "주소가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    String resultText = "값이없음";
                    try {
                        resultText = new Task().execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    String[] resultXY = geojsonParser(resultText);  //JSON 형식에서 x,y값 추출하기

                    longitude = Double.parseDouble(resultXY[0]);    //string에서 double로 형변환
                    latitude = Double.parseDouble(resultXY[1]);     //string에서 double로 형변환

                    PositionItem item = new PositionItem(name, latitude, longitude);    //PositionItem 생성
                    list_random.add(item);      //리스트에 PositionItem 추가
                    et_position.setText("");    //et_position 초기화
                    listAdapter.notifyDataSetChanged(); //리스트 갱신
                }
            }
        });

        btn_search_time.setOnClickListener(new View.OnClickListener() { //"시간 기준" 버튼 클릭시,
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSRandomActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("standard_tag", "time");    //시간 기준
                it_showmiddle.putExtra("activity_tag", "random");  //어떤 Activity인지(random / group)
                it_showmiddle.putExtra("isHistory", "false");      //history 결과 보여주는 것이 아님
                startActivity(it_showmiddle);
            }
        });

        btn_search_distance.setOnClickListener(new View.OnClickListener() { //"거리 기준" 버튼 클릭시,
            @Override
            public void onClick(View v) {
                //activity_showmiddle로 화면 이동하고 거리 기준임을 intent로 전달
                Intent it_showmiddle = new Intent(ChoiceGPSRandomActivity.this, ShowMiddleActivity.class);
                it_showmiddle.putExtra("standard_tag", "distance"); //거리 기준
                it_showmiddle.putExtra("activity_tag", "random");   //어떤 Activity인지(random / group)
                it_showmiddle.putExtra("isHistory", "false");       //history 결과 보여주는 것이 아님
                startActivity(it_showmiddle);
            }
        });
    }

    //KaKao Geocode API 통해 주소 검색 결과 받기
    public class Task extends AsyncTask<String, Void, String> {
        String receiveMsg = "";

        String KAKAO_KEY = getString(R.string.kakao_key);  //KAKAO REST API 키
        String auth = "KakaoAK " + KAKAO_KEY;
        URL link= null;
        HttpsURLConnection hc = null;

        @Override
        protected String doInBackground(String... params) {
            try {
                link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address_r, "UTF-8")); //한글을 URL용으로 인코딩

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });

                hc = (HttpsURLConnection)link.openConnection();
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
