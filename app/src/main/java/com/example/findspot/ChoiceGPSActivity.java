package com.example.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


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

        //*****************
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

                    String APIKey = "dc90ecf7e13bbcfd5d02a7a41ed33464";

                    PositionResource bodyJson = null;
                    try {
                        String apiURL = "https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address, "UTF-8");

                        HttpResponse<JsonNode> response = Unirest.get(apiURL).header("Authorization", APIKey).asJson();

                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

                        bodyJson = objectMapper.readValue(response.getBody().toString(), PositionResource.class);
                    } catch (UnsupportedEncodingException | UnirestException | JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    longitude = bodyJson.getDocuments().get(0).getX();
                    latitude = bodyJson.getDocuments().get(0).getY();

                    PositionItem item = new PositionItem(name, latitude, longitude);    //PositionItem 생성
                    list.add(item);             //리스트에 PositionItem 추가
                    et_position.setText("");    //et_position 초기화
                    listAdapter.notifyDataSetChanged(); //리스트 갱신
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
}