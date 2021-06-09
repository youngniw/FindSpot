package com.example.findspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.request.GetUserInfoRequest;
import com.example.findspot.request.SetUserInfoRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import static com.example.findspot.ChoiceGPSRandomActivity.address_r;
import static com.example.findspot.LoginActivity.nickName;

public class EditProfileActivity extends AppCompatActivity {
    String history_roadName;
    double longitude = 0, latitude = 0;

    EditText et_birthYear, et_gps;
    Button btn_ok;
    Switch sw_editProfileGender;
    ImageView iv_currentGps, iv_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        try {
            new GetUserInfoTask().execute().get();  //사용자의 정보 가져오기
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        et_birthYear = (EditText) findViewById(R.id.edit_profile_birthYear);
        et_gps = (EditText) findViewById(R.id.edit_profile_gps);
        btn_ok = (Button) findViewById(R.id.edit_profile_ok);
        sw_editProfileGender = (Switch) findViewById(R.id.join_gender);
        iv_currentGps = (ImageView) findViewById(R.id.edit_profile_current_gps);
        iv_Back = (ImageView) findViewById(R.id.edit_profile_back);

        editProfile_clickListener();
    }

    void editProfile_clickListener() {
        //태어난 연도를 입력하기 위한 EditText 클릭
        et_birthYear.setOnClickListener(v -> {
            final NumberPicker yearPick = new NumberPicker(getApplicationContext());    //연도를 선택할 수 있는 다이얼로그 생성
            yearPick.setMinValue(1900);
            yearPick.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));    //올해를 Max값으로 설정
            if (et_birthYear.getText().toString().equals(""))
                yearPick.setValue(Calendar.getInstance().get(Calendar.YEAR));   //선택한 연도가 없으므로 올해를 설정
            else
                yearPick.setValue(Integer.parseInt(et_birthYear.getText().toString()));     //선택한 연도가 이전에 있었으므로 그 값으로 설정

            AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());    //다이얼로그 설정
            dialog.setTitle("출생년도 입력");     //다이얼로그 제목 설정
            dialog.setView(yearPick);           //내용으로 숫자 선택할 수 있게 보여줌
            //버튼 클릭시 동작
            dialog.setPositiveButton("확인", (dialog1, which) -> et_birthYear.setText(String.valueOf(yearPick.getValue())));
            dialog.setNegativeButton("취소", (dialog12, which) -> dialog12.dismiss());
            dialog.show();
        });

        //위치 설정 EditText 클릭
        et_gps.setOnClickListener(v -> {
            //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
            Intent it_address = new Intent(getApplicationContext(), DaumWebViewActivity.class);
            it_address.putExtra("name", "");    //name에 빈 문자열을 줌으로써 address_r에 값을 받아옴
            startActivityForResult(it_address, 200);
        });

        //현재 위치 반환 아이콘 클릭
        iv_currentGps.setOnClickListener(v -> {
            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    //위치 관리자 객체 참조
            //권한 없을 시 클릭 이벤트 종료
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);  //가장 최근 위치정보 가져오기
            final LocationListener gpsLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    String provider = location.getProvider();
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    //kakao Rest API를 이용해여 roadName 구하기

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL a = new URL("https://dapi.kakao.com/v2/local/geo/coord2address.json?x=" + latitude + "&y=" + longitude);
                                HttpsURLConnection myConn = (HttpsURLConnection) a.openConnection();
                                myConn.setRequestProperty("Authorization", "KakaoAK " + getString(R.string.kakao_restAPI_key));

                            } catch (IOException e) { e.printStackTrace(); }

                        }
                    });
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) { }
                @Override
                public void onProviderDisabled(@NonNull String provider) { }
            };
        });

        //확인 버튼 클릭
        btn_ok.setOnClickListener(v -> {
            if (!et_gps.equals(history_roadName)) { //DB기록과 같지 않다면 해당 값의 위도 경도 구하기
                //설정한 roadName에 대해 좌표제공API로 위도경도 알아내기
                String resultText = "";
                try {
                    resultText = new GetGeocodeTask().execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
                String[] resultXY = geojsonParser(resultText);  //JSON 형식에서 x,y값 추출하기
                longitude = Double.parseDouble(resultXY[0]);    //string에서 double로 형변환
                latitude = Double.parseDouble(resultXY[1]);     //string에서 double로 형변환
            }

            try {
                new SetUserInfoTask(sw_editProfileGender.isChecked()?"F":"M", Integer.valueOf(et_birthYear.getText().toString()), et_gps.getText().toString(), longitude, latitude).execute().get();  //사용자의 정보 DB에 저장하기
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }


        });

        //뒤로가기 버튼 클릭
        iv_Back.setOnClickListener(v -> {
            finish();
        });
    }

    //startActivityForResult()에 대한 수신용 메소드
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 200 && resultCode == 1) {
            et_gps.setText(String.format("%s", address_r));
        }
    }

    //사용자 정보 가져오기
    @SuppressLint("StaticFieldLeak")
    public class GetUserInfoTask extends AsyncTask<String, Void, String> {
        GetUserInfoTask() { }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getString("gender").equals("M")) sw_editProfileGender.setChecked(false); //switch에서 false는 남자를 의미
                    else sw_editProfileGender.setChecked(true); //switch에서 true는 여자를 의미

                    et_birthYear.setText(jsonObject.getInt("birthYear"));   //생년월일 설정

                    history_roadName = jsonObject.getString("roadName");
                    et_gps.setText(history_roadName);   //주소 설정
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };

            //서버로 Volley를 이용해서 요청을 함
            GetUserInfoRequest getUserInfoRequest = new GetUserInfoRequest(nickName, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(getUserInfoRequest);

            return null;
        }
    }

    //사용자 정보 DB에 저장하기
    @SuppressLint("StaticFieldLeak")
    public class SetUserInfoTask extends AsyncTask<String, Void, String> {
        String gender, roadName;
        int birthYear;
        double latitude, longitude;

        SetUserInfoTask(String gender, int birthYear, String roadName, double latitude, double longitude) {
            this.gender = gender;
            this.birthYear = birthYear;
            this.roadName = roadName;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("accept")) Toast.makeText(getApplicationContext(), "변경이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getApplicationContext(), "오류로 인해 변경이 완료되지 않았습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };

            //서버로 Volley를 이용해서 요청을 함
            SetUserInfoRequest setUserInfoRequest = new SetUserInfoRequest(nickName, gender, birthYear, roadName, latitude, longitude, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(setUserInfoRequest);

            return null;
        }
    }

    //KaKao Geocode API 통해 주소 검색 결과 받기
    @SuppressLint("StaticFieldLeak")
    public class GetGeocodeTask extends AsyncTask<String, Void, String> {
        String receiveMsg = "";

        String KAKAO_KEY = getString(R.string.kakao_key);  //KAKAO REST API 키
        String auth = "KakaoAK " + KAKAO_KEY;
        URL link= null;
        HttpsURLConnection hc = null;

        GetGeocodeTask() { super(); }

        @Override
        protected String doInBackground(String... params) {
            try {
                link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address_r, "UTF-8")); //한글을 URL용으로 인코딩

                HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);

                hc = (HttpsURLConnection)link.openConnection();
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
