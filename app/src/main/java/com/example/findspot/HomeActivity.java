package com.example.findspot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    LinearLayout linear_home_findspot;
    ImageButton imgbtn_home_user;
    TextView tv_home_goFriend, tv_home_goGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        linear_home_findspot = (LinearLayout) findViewById(R.id.home_findspot);
        imgbtn_home_user = (ImageButton) findViewById(R.id.home_user);
        tv_home_goFriend = (TextView) findViewById(R.id.home_goFriend);
        tv_home_goGroup = (TextView) findViewById(R.id.home_goGroup);

        home_clickListener();         //홈(Home) 화면에서 4개 객체의 onClickListener를 정의한 함수를 호출함
    }

    void home_clickListener() {
        linear_home_findspot.setOnClickListener(new View.OnClickListener() {    //"중간지점 찾기" 버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                Intent it_selectwhom = new Intent(HomeActivity.this, SelectWhomActivity.class);
                startActivity(it_selectwhom);
                finish();
            }
        });
        imgbtn_home_user.setOnClickListener(new View.OnClickListener() {        //"마이페이지" 버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                Intent it_mypage = new Intent(HomeActivity.this, MyPageActivity.class);
                startActivity(it_mypage);
                finish();
            }
        });
        tv_home_goFriend.setOnClickListener(new View.OnClickListener() {       //"친구(추가/삭제)" 버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                Intent it_friend = new Intent(HomeActivity.this, FriendActivity.class);
                startActivity(it_friend);
                finish();
            }
        });
        tv_home_goGroup.setOnClickListener(new View.OnClickListener() {       //"그룹(추가/삭제)" 버튼을 클릭했을 때
            @Override
            public void onClick(View v) {
                Intent it_group = new Intent(HomeActivity.this, GroupActivity.class);
                startActivity(it_group);
                finish();
            }
        });
    }
}
