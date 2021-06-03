package com.example.findspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.data.GroupInfo;
import com.example.findspot.request.LoginRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.HomeActivity.groupList;

public class LoginActivity extends AppCompatActivity {
    static String nickName;
    EditText et_loginID, et_loginPW;
    Button btn_login, btn_join;
    ImageButton imgbtn_login_kakao, imgbtn_login_naver, imgbtn_login_google, imgbtn_login_facebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        friendList = new ArrayList<>();       //사용자의 친구목록 리스트 생성
        groupList = new ArrayList<>();     //사용자의 그룹목록 리스트 생성

        et_loginID = (EditText) findViewById(R.id.login_ID_input);      //id를 입력하는 editText
        et_loginPW = (EditText) findViewById(R.id.login_PW_input);      //비밀번호를 입력하는 editText

        btn_login = (Button) findViewById(R.id.login_login_btn);        //로그인 버튼
        btn_join = (Button) findViewById(R.id.login_join_btn);          //회원가입 버튼

        imgbtn_login_kakao = (ImageButton) findViewById(R.id.login_kakao);          //카카오로 로그인하는 이미지버튼
        imgbtn_login_naver = (ImageButton) findViewById(R.id.login_naver);          //네이버로 로그인하는 이미지버튼
        imgbtn_login_google = (ImageButton) findViewById(R.id.login_google);        //구글로 로그인하는 이미지버튼
        imgbtn_login_facebook = (ImageButton) findViewById(R.id.login_facebook);    //페이스북으로 로그인하는 이미지버튼

        login_btn_clickListener();        //로그인 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
        login_socialbtn_clickListener();  //로그인 화면의 소셜로그인 버튼(4개)에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void login_btn_clickListener() {
        //로그인버튼을 클릭했을 때
        btn_login.setOnClickListener(v -> {
            InputMethodManager inputMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);      //키보드 안보이게 하기 위한 InputMethodManager객체
            inputMM.hideSoftInputFromWindow(et_loginID.getWindowToken(), 0);
            inputMM.hideSoftInputFromWindow(et_loginPW.getWindowToken(), 0);

            //로그인을 위해 ID와 PW를 모두 입력했는지를 확인함
            if (et_loginID.getText().toString().equals("")) {    //비밀번호 입력했는지를 점검
                Toast.makeText(getApplicationContext(), "ID를 입력해주세요:)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (et_loginPW.getText().toString().equals("")) {    //비밀번호 입력했는지를 점검
                Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요:)", Toast.LENGTH_SHORT).show();
                return;
            }

            //로그인을 위해 입력한 id와 비밀번호가 유효한 지를 체크함
            try {
                new LoginCheckTask().execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        });

        //회원가입버튼을 클릭했을 때
        btn_join.setOnClickListener(v -> {
            Intent it_join = new Intent(LoginActivity.this, JoinActivity.class);        //회원가입 창으로 화면이 전환됨
            startActivity(it_join);     //이때 finish하지 않아서 로그인 창으로 회원가입때 뒤로가기로 돌아올 수 있음
        });
    }

    void login_socialbtn_clickListener() {
        //소셜로그인 가능한 4개의 버튼을 클릭했을 때에 해당하는 onClickListener를 정의함
        //*****************************************************************************************************
    }

    //로그인을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class LoginCheckTask extends AsyncTask<String, Void, String> {
        LoginCheckTask() { super(); }
        @Override
        protected String doInBackground(String... strings) {
            String userID = et_loginID.getText().toString();     //EditText에 현재 입력된 id값을 가져옴
            String userPW = et_loginPW.getText().toString();     //EditText에 현재 입력된 비밀번호값을 가져옴

            //데이터베이스에 사용자의 ID와 PW를 입력한 ID와 PW를 비교했을 때 존재한다면 입력한 ID와 PW로 로그인함
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean canLogin = jsonObject.getBoolean("canLogin");   //입력한 ID와 PW로 로그인 가능한 지 여부
                    boolean existID = jsonObject.getBoolean("existID");     //입력한 ID값이 DB에 있는지 여부(로그인 안될 시 원인 파악)
                    nickName = jsonObject.getString("nickName");     //해당 ID의 닉네임을 받음

                    if (canLogin) {     //로그인 가능
                        friendList.clear();     //로그인 시 계속해서 add될 수 있으므로 친구목록 초기화
                        JSONArray jsonArrayFriends = jsonObject.getJSONArray("friends");    //친구목록을 받아옴
                        for (int i=0; i < jsonArrayFriends.length(); i++) {
                            JSONObject subJsonObject = jsonArrayFriends.getJSONObject(i);
                            String fName = subJsonObject.getString("fName");

                            friendList.add(fName);      //사용자의 친구목록인 friendlist에 추가함
                        }

                        groupList.clear();      //로그인 시 계속해서 add될 수 있으므로 그룹목록 초기화
                        JSONArray jsonArrayGroups = jsonObject.getJSONArray("groups");      //사용자가 속한 그룹목록을 받아옴
                        for (int i=0; i < jsonArrayGroups.length(); i++) {
                            JSONObject subJsonObject = jsonArrayGroups.getJSONObject(i);
                            String gName = subJsonObject.getString("gName");                //그룹 이름 받아옴
                            String gHostName = subJsonObject.getString("gHostName");        //그룹 호스트사용자 이름 받아옴(방장)
                            GroupInfo group = new GroupInfo(gName, gHostName);

                            JSONArray jsonArraygUsers = subJsonObject.getJSONArray("gUsers");  //그룹에 속한 사용자들의 목록을 받아옴
                            for (int j=0; j < jsonArraygUsers.length(); j++) {
                                JSONObject subJsonUser = jsonArraygUsers.getJSONObject(j);
                                String gUName = subJsonUser.getString("gUName");    //사용자 이름 받아옴
                                group.getGroupUsers().add(gUName);
                            }
                            groupList.add(group);      //사용자가 포함된 그룹목록인 grouplist에 추가함
                        }

                        Toast.makeText(getApplicationContext(), nickName+"님 환영합니다:)", Toast.LENGTH_SHORT).show();
                        Intent it_home = new Intent(LoginActivity.this, HomeActivity.class);        //홈화면으로 화면이 전환됨
                        startActivity(it_home);     //로그인이 가능하므로 Home창으로 넘어감
                        finish();
                    } else {            //로그인 가능하지 않음
                        if (existID)        //ID는 존재하나 비밀번호가 일치하지 않음
                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.\n다시 입력 바랍니다:)", Toast.LENGTH_SHORT).show();
                        else                //입력한 ID가 존재하지 않음
                            Toast.makeText(getApplicationContext(), "입력하신 ID가 존재하지 않습니다.\n확인 바랍니다:)", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };
            //서버로 Volley를 이용해서 요청을 함.(로그인 가능 여부 확인을 위한 전달)
            LoginRequest loginRequest = new LoginRequest(userID, userPW, responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(loginRequest);

            return null;
        }
    }
}
