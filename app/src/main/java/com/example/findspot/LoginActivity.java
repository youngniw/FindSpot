package com.example.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText et_loginID, et_loginPW;
    Button btn_login, btn_join;
    ImageButton imgbtn_login_kakao, imgbtn_login_naver, imgbtn_login_google, imgbtn_login_facebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_loginID = (EditText) findViewById(R.id.login_ID_input);      //id를 입력하는 editText
        et_loginPW = (EditText) findViewById(R.id.login_PW_input);      //비밀번호를 입력하는 editText

        btn_login = (Button) findViewById(R.id.login_login_btn);        //로그인 버튼
        btn_join = (Button) findViewById(R.id.login_join_btn);          //회원가입 버튼

        imgbtn_login_kakao = (ImageButton) findViewById(R.id.login_kakao);          //카카오로 로그인하는 이미지버튼
        imgbtn_login_naver = (ImageButton) findViewById(R.id.login_naver);          //네이버로 로그인하는 이미지버튼
        imgbtn_login_google = (ImageButton) findViewById(R.id.login_google);        //구글로 로그인하는 이미지버튼
        imgbtn_login_facebook = (ImageButton) findViewById(R.id.login_facebook);    //페이스북으로 로그인하는 이미지버튼

        login_et_clickListener();         //로그인 화면에서 id와 비밀번호를 입력할 수 있는 edittext객체의 onClickListener를 정의한 함수를 호출함
        login_btn_clickListener();        //로그인 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
        login_socialbtn_clickListener();  //로그인 화면의 소셜로그인 버튼(4개)에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void login_et_clickListener() {
        //아이디, 비밀번호에 해당하는 객체를 클릭해서 값을 입력했을 때에 해당하는 onClickListener를 정의함
        //*****************************************************************************************************
    }

    void login_btn_clickListener() {
        btn_login.setOnClickListener(new View.OnClickListener() {       //로그인버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                //로그인을 위해 입력한 id와 비밀번호가 유효한지를 체크함
                //유효하다면 home창으로 넘어감(Intent사용)
                //*****************************************************************************************************
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {        //회원가입버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                Intent it_join = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(it_join);
                finish();
            }
        });
    }

    void login_socialbtn_clickListener() {
        //소셜로그인 가능한 4개의 버튼을 클릭했을 때에 해당하는 onClickListener를 정의함
        //*****************************************************************************************************
    }
}
