package com.example.findspot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.request.GroupInfoRequest;

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

import static com.example.findspot.SelectWhomActivity.selectedGroup;

public class ChoiceGPSGroupActivity extends AppCompatActivity {
    static ArrayList<PositionItem> position_tmp;    //주소를 받는 타이밍을 겹치지 못하게 순차처리 될 수 있게 하기 위해 사용하는 리스트
    static ArrayList<PositionItem> list_group;      //중간지점을 찾을 결론적 위치 (이름, 도로명주소, 위도, 경도)로 구성된 리스트

    TextView tv_applyhistory;
    Button btn_search_time, btn_search_distance;

    ArrayList<PositionItem> list_g_users;       //(그룹에 속한 사용자 닉네임, 데이터베이스에 저장된 위치의 위도 및 경도)로 구성된 리스트
    PositionGroupListAdapter listAdapter;       //위치 리스트 어댑터(UI 구현)
    ArrayList<String> ghistory;     //이전에 해당 그룹이 중간지점을 찾았다면 그에 대한 정보(인덱스 순서대로 중간지점을 시간(T)기준인지 거리(D)기준인지, 경도, 위도, 도로명)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_gps_group);

        ghistory = new ArrayList<>();
        position_tmp = new ArrayList<PositionItem>();

        list_group = new ArrayList<PositionItem>();
        list_group.add(new PositionItem("위영은", "수내역", 37.378672170554, 127.11416562491435));
        list_group.add(new PositionItem("김소은", "솔샘역", 37.62051242223699, 127.01358864212412));
        list_group.add(new PositionItem("아야여", "잠실역", 37.31345217500308, 126.79569135036883));
        list_group.add(new PositionItem("헤로우", "숙명여자대학교", 37.54658554768764, 126.96476672491916));

        list_g_users = new ArrayList<PositionItem>();

        //**********************************(DB로부터 PositionGroupUserItem를 그룹의 인원수만큼 받아와야함)********************************************************
        //TODO: 앞으로 알고리즘 짜야됨
        try {
            new ChoiceGPSGroupActivity.GetGroupInfoTask().execute().get();      //DB로부터 그룹의 속한 사용자들의 닉네임, 도로명, 지정한 x, y값을 받아옴, 또한 최근에 찾은 위치를 불러옴
        } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

        //TODO: 위에 짜면 이 부분은 삭제:)
        list_g_users.add(new PositionItem("위영은", "수내역", 10.002012031123, 20.2321454135827893));
        list_g_users.add(new PositionItem("김소은", "목내역", 8.00201203123, 30.232124135827893));
        list_g_users.add(new PositionItem("아야여", "금내역", 70.00201203123, 40.232124135827893));
        list_g_users.add(new PositionItem("헤로우", "토내역", 16.00201203123, 50.232124135827893));

        //위치 기록을 위한 그룹 리스트 초기설정
        listAdapter = new PositionGroupListAdapter(this, R.layout.positionrow_group, list_g_users);
        ListView lv = (ListView) findViewById(R.id.choice_gps_g_lv_position);
        lv.setAdapter(listAdapter);

        tv_applyhistory = findViewById(R.id.choice_gps_g_applyhistory);     //최근 기록 불러오기
        btn_search_time = (Button) findViewById(R.id.choice_gps_g_bt_time);
        btn_search_distance = (Button) findViewById(R.id.choice_gps_g_bt_distance);

        choice_tv_clickListener();         //사용자 GPS 설정 화면의 텍스트뷰에 해당하는 onClickListener를 정의한 함수를 호출함
        choice_btn_clickListener();        //사용자 GPS 설정 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void choice_tv_clickListener() {
        tv_applyhistory.setOnClickListener(new View.OnClickListener() {     //"최근에 [시간/거리] 찾은 위치 보기" 텍스트 클릭 시
            @Override
            public void onClick(View v) {
                //TODO: 위치 기록으로 중간지점 찾기가 보이도록 함(만약에 이전에 시간이었다면, 시간(standard)으로 보여주자) -> tag로 기록 보여주는 것을 알려줘야함(Showmiddle에)
                if (ghistory.get(0).equals("T")) {  //최근에 이 그룹으로 시간 기준 중간지점을 찾은 적이 있음
                    //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                    Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                    it_showmiddle.putExtra("standard_tag", "time");    //시간 기준
                    //TODO: it_showmiddle.putExtra("isHistory", "true");
                    it_showmiddle.putExtra("activity_tag", "group");   //어떤 Activity인지(random / group)
                    startActivity(it_showmiddle);
                }
                else {  //최근에 이 그룹으로 거리 기준 중간지점을 찾은 적이 있음(D)
                    //activity_showmiddle로 화면 이동하고 시간 기준임을 intent로 전달
                    Intent it_showmiddle = new Intent(ChoiceGPSGroupActivity.this, ShowMiddleActivity.class);
                    it_showmiddle.putExtra("standard_tag", "distance");    //거리 기준
                    //TODO: it_showmiddle.putExtra("isHistory", "true");
                    it_showmiddle.putExtra("activity_tag", "group");   //어떤 Activity인지(random / group)
                    startActivity(it_showmiddle);
                }
            }
        });
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
                //TODO: DB에 최근으로 중간 지점 위치 전달하기
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
                //TODO: DB에 최근으로 중간 지점 위치 전달하기
            }
        });
    }

    //그룹에 속한 사용자 정보를 받고 이 그룹에서 이전에 중간지점을 찾은 기록을 얻기 위한 작업 수행함
    public class GetGroupInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스에 그룹에 속한 사용자의 정보(닉네임, 도로명, 경도, 위도)와 최근에 찾은 위치를 얻음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        //TODO: 확인 요망
                        if (jsonObject.getJSONObject("group").getString("historyTF").equals("T")) {     //이전에 이 그룹에서 중간 지점 찾기를 했었음(기록이 있음)
                            ghistory.add(jsonObject.getJSONObject("group").getString("standard"));  //시간(T) or 거리(D) 기준
                                if (jsonObject.getJSONObject("group").getString("standard").equals("T"))    //최근 기록이 시간기준으로 중간지점을 찾은 기록일 때
                                    tv_applyhistory.setText("최근 기록 확인 [시간 기준]  >>");
                                else        //최근 기록이 거리기준으로 중간지점을 찾은 기록일 때
                                    tv_applyhistory.setText("최근 기록 확인 [거리 기준]  >>");
                            ghistory.add(String.valueOf(jsonObject.getJSONObject("group").getDouble("x")));     //최근 중간지점의 경도
                            ghistory.add(String.valueOf(jsonObject.getJSONObject("group").getDouble("y")));     //최근 중간지점의 위도
                            ghistory.add(jsonObject.getJSONObject("group").getString("roadName"));      //최근 중간지점의 도로명주로
                        }
                        else    //그룹의 이전 중간 지점 찾기 기록이 없음    TODO: 안보이게 하든지 아예 클릭이 안먹게 하든지 설정
                            tv_applyhistory.setVisibility(View.INVISIBLE);

                        JSONArray members = jsonObject.getJSONArray("members");
                        for (int i=0; i<members.length(); i++) {    //사용자 정보를 list_g_users에 저장함
                            PositionItem memberInfo = new PositionItem(members.getJSONObject(i).getString("nickName"), members.getJSONObject(i).getString("roadName"),
                                    members.getJSONObject(i).getDouble("x"), members.getJSONObject(i).getDouble("y"));
                            list_g_users.add(memberInfo);       //TODO: null일때는 추가가 안됨(x와 y값이 없을 시)
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };

            //서버로 Volley를 이용해서 요청을 함.(그룹에 속한 사용자들의 정보를 받기 위한 전달)
            GroupInfoRequest gUserInfoRequest = new GroupInfoRequest(selectedGroup, responseListener);
            RequestQueue queue = Volley.newRequestQueue(ChoiceGPSGroupActivity.this);
            queue.add(gUserInfoRequest);

            return null;
        }
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

        String KAKAO_KEY = getString(R.string.kakao_key);  //KAKAO REST API 키
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
