package com.example.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class JoinActivity extends AppCompatActivity {
    EditText et_joinID, et_joinPW, et_joinBirthYear;
    Switch sw_joinGender;
    Button btn_id_check, btn_ok, btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_joinID = (EditText) findViewById(R.id.join_id);                  //id를 입력하는 editText
        et_joinPW = (EditText) findViewById(R.id.join_passwd);              //비밀번호를 입력하는 editText
        sw_joinGender = (Switch) findViewById(R.id.join_gender);            //성별을 입력할 수 있는 switch
        et_joinBirthYear = (EditText) findViewById(R.id.join_birthYear);    //태어난 연도를 입력하는 editText

        btn_id_check = (Button) findViewById(R.id.join_id_check);    //중복확인 버튼
        btn_ok = (Button) findViewById(R.id.join_ok);                //회원가입 확인 버튼
        btn_cancel = (Button) findViewById(R.id.join_cancel);        //회원가입 취소 버튼

        join_btn_clickListener();        //회원가입 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void join_et_sw_clickListener() {
        //아이디, 비밀번호, 성별, 태어난 연도에 해당하는 객체를 클릭해서 수정했을 때에 해당하는 onClickListener를 정의함
        //*****************************************************************************************************
    }

    void join_btn_clickListener() {
        btn_id_check.setOnClickListener(new View.OnClickListener() {        //id 중복확인 버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                //데이터베이스에 id가 이미 존재하는 지를 확인한 후, 데이터베이스에 id가 없다면 id를 사용할 수 있음을 알려줌(토스트로 보여줌)
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {              //회원가입창의 확인 버튼을 클릭했을 때(회원가입이 완료됨을 알려줌)
            @Override
            public void onClick(View v) {
                //회원가입을 위해 값을 모두 입력했는 지를 확인함
                //id중복확인을 하고 값을 모두 입력했다면, 회원가입이 완료되었다고 다이얼로그로 알려주고, 로그인 창을 확인함
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {          //회원가입창의 취소 버튼을 클릭했을 때(로그인 창으로 돌아감)
            @Override
            public void onClick(View v) {
                Intent it_login = new Intent(JoinActivity.this, LoginActivity.class);
                startActivity(it_login);
                finish();
            }
        });
    }
}