package com.example.findspot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findspot.data.GroupInfo;

import java.util.ArrayList;

import static com.example.findspot.LoginActivity.nickName;

public class HomeActivity extends AppCompatActivity {
    public static final int MYPAGE_REQUEST = 7000;  //requestCode로 사용될 상수(마이페이지)
    public static ArrayList<GroupInfo> groupList;
    public static ArrayList<String> friendList;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    boolean isSocialLogin;

    LinearLayout ll_home_user, linear_home_findspot;
    TextView tvUserName, tv_home_goFriend, tv_home_goGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        activity = HomeActivity.this;
        isSocialLogin = getIntent().getExtras().getBoolean("isSocialLogin");

        ll_home_user = findViewById(R.id.home_user);
        tvUserName = findViewById(R.id.home_userName);
            tvUserName.setText(nickName);
        linear_home_findspot = findViewById(R.id.home_findspot);
        tv_home_goFriend = findViewById(R.id.home_goFriend);
        tv_home_goGroup = findViewById(R.id.home_goGroup);

        home_clickListener();         //홈(Home) 화면에서 4개 객체의 onClickListener를 정의한 함수를 호출함
    }

    void home_clickListener() {
        //"중간지점 찾기" 버튼을 클릭했을 때
        linear_home_findspot.setOnClickListener(v -> {
            Intent it_selectwhom = new Intent(HomeActivity.this, SelectWhomActivity.class); //중간지점찾기를 위한 방법 선택(그룹 혹은 임의) 창으로 화면이 전환됨
            startActivity(it_selectwhom);
        });

        //"마이페이지" 버튼을 클릭했을 때
        ll_home_user.setOnClickListener(v -> {
            Intent it_mypage = new Intent(HomeActivity.this, MyPageActivity.class);         //마이페이지 창으로 화면이 전환됨
            Bundle bundle = new Bundle();
            bundle.putBoolean("isSocialLogin", isSocialLogin);
            it_mypage.putExtras(bundle);
            startActivityForResult(it_mypage, MYPAGE_REQUEST);
        });
        //"친구(추가/삭제)" 버튼을 클릭했을 때
        tv_home_goFriend.setOnClickListener(v -> {
            Intent it_friend = new Intent(HomeActivity.this, FriendActivity.class);         //친구(추가, 삭제) 창으로 화면이 전환됨
            startActivity(it_friend);
        });
        //"그룹(추가/삭제)" 버튼을 클릭했을 때
        tv_home_goGroup.setOnClickListener(v -> {
            Intent it_group = new Intent(HomeActivity.this, GroupActivity.class);           //그룹(추가, 삭제) 창으로 화면이 전환됨
            startActivity(it_group);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == MYPAGE_REQUEST){        //MyPage에서 User 정보 변경 시 적용 위함
                if (data.getExtras().getBoolean("isNameChanged"))
                    tvUserName.setText(nickName);
            }
        }
    }
}
