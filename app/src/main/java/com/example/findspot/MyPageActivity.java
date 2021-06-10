package com.example.findspot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.data.GroupInfo;
import com.example.findspot.dialog.NickNameCheckDialog;
import com.example.findspot.request.ChangeNickNameRequest;
import com.example.findspot.request.SecessionRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static com.example.findspot.HomeActivity.groupList;
import static com.example.findspot.LoginActivity.mGoogleSignInClient;
import static com.example.findspot.LoginActivity.nickName;

public class MyPageActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;
    boolean isSocialLogin, isNameChanged = false;
    TextView tv_nickName, tv_editProfile, tv_changeNickName, tv_changePassword, tvInquiry, tvLogout, tvSecession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        activity = MyPageActivity.this;
        isSocialLogin = getIntent().getExtras().getBoolean("isSocialLogin");

        tv_nickName = findViewById(R.id.mypage_nickname);
        if (isSocialLogin) {
            String showName = nickName+" (소셜로그인)";
            tv_nickName.setText(showName);

            Spannable span = (Spannable) tv_nickName.getText();
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#398E3D")), nickName.length()+1, showName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(0.7f), nickName.length()+1, showName.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        else
            tv_nickName.setText(nickName);

        tv_editProfile = findViewById(R.id.mypage_edit_profile);
        tv_changeNickName = findViewById(R.id.mypage_change_nickname);
        tv_changePassword = findViewById(R.id.mypage_change_password);
        tvInquiry = findViewById(R.id.mypage_inquiry);
        tvLogout = findViewById(R.id.mypage_logout);
        tvSecession = findViewById(R.id.mypage_secession);

        if (isSocialLogin)
            tv_changePassword.setVisibility(View.GONE);

        myPage_clickListener();
    }

    public void myPage_clickListener() {
        ImageView ivBack = findViewById(R.id.mypage_back);
        ivBack.setOnClickListener(v -> finish());

        //개인정보 수정 TextView 클릭 리스너 설정
        tv_editProfile.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));      //개인정보 수정 activity로 intent 전달
        });

        //닉네임 변경 TextView 클릭 리스너 설정
        tv_changeNickName.setOnClickListener(v -> {
            NickNameCheckDialog nickNameCheckDialog = new NickNameCheckDialog(MyPageActivity.this, userName -> {    //닉네임 중복확인 완료
                try {
                    new NickNameChangeTask(userName).execute().get();   //DB에 사용자 계정 변경 요청
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            });
            nickNameCheckDialog.show();
        });

        //비밀번호 변경 TextView 클릭 리스너 설정
        tv_changePassword.setOnClickListener(v -> {
            //비밀번호 변경 activity로 intent 전달 (현재 비밀번호 입력, 새로운 비밀번호 입력)
            startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
        });

        //문의하기 TextView 클릭 리스너 설정
        tvInquiry.setOnClickListener(v -> startActivity(new Intent(MyPageActivity.this, InquiryActivity.class)));

        //로그아웃 TextView 클릭 리스너 설정
        tvLogout.setOnClickListener(v -> mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {        //로그아웃 기능
            HomeActivity homeActivity = (HomeActivity) HomeActivity.activity;
            homeActivity.finish();

            Toast.makeText(getApplicationContext(), "로그아웃 되셨습니다:)", Toast.LENGTH_SHORT).show();

            Intent itLogin = new Intent(MyPageActivity.this, LoginActivity.class);
            startActivity(itLogin);
            finish();
        }));

        //탈퇴하기 TextView 클릭 리스너 설정
        tvSecession.setOnClickListener(v -> {
            if (isSocialLogin) {
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
                    HomeActivity homeActivity = (HomeActivity) HomeActivity.activity;
                    homeActivity.finish();

                    try {
                        new SecessionTask().execute().get();
                    } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
                });
            }
            else {
                HomeActivity homeActivity = (HomeActivity) HomeActivity.activity;
                homeActivity.finish();

                try {
                    new SecessionTask().execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            }
        });
    }

    @Override
    public void finish() {
        Intent noticeNameChanged = new Intent();
        Bundle noticeInfo = new Bundle();
        noticeInfo.putBoolean("isNameChanged", isNameChanged);
        noticeNameChanged.putExtras(noticeInfo);
        setResult(RESULT_OK, noticeNameChanged);

        super.finish();
    }

    //DB에게 닉네임 변경을 위한 작업을 수행함
    @SuppressLint("StaticFieldLeak")
    public class NickNameChangeTask extends AsyncTask<String, Void, String> {
        String newNickName; //사용자가 변경을 원하는 닉네임 이름

        NickNameChangeTask(String nickName) {
            super();
            newNickName = nickName;
        }
        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean accept = jsonObject.getBoolean("accept");
                    if (accept) {   //닉네임 변경 완료시,
                        isNameChanged = true;

                        for (GroupInfo gItem : groupList) { //그룹 데이터 변경
                            //내가 방장인 방이라면 방장 이름을 변경한 닉네임으로 변경
                            if (gItem.getGHostName().equals(nickName)) gItem.setGHostName(newNickName);
                            //구성원 리스트 안에 있는 자신 닉네임을 변경한 닉네임으로 변경
                            gItem.getGroupUsers().set(gItem.getGroupUsers().indexOf(nickName), newNickName);  //대부분의 정상적인 경우 닉네임을 변경
                        }
                        nickName = newNickName; //현재 닉네임을 변경한 닉네임으로 변경
                        tv_nickName.setText(nickName);  //textView도 변경한 닉네임으로 변경
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "문제가 생겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };

            // 서버로 Volley를 이용해서 요청을 함
            ChangeNickNameRequest changeNickNameRequest = new ChangeNickNameRequest(nickName, newNickName, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MyPageActivity.this);
            queue.add(changeNickNameRequest);

            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class SecessionTask extends AsyncTask<String, Void, String> {        //탈퇴
        SecessionTask() { super(); }

        @Override
        protected String doInBackground(String... strings) {
            //DB에서 사용자 삭제 시
            Response.Listener<String> responseListener = response -> {
                //로그인 완료
                if (isSocialLogin)
                    Toast.makeText(getApplicationContext(), "로그인하신 구글 계정으로 탈퇴되셨습니다:)", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "로그인하신 계정으로 탈퇴되셨습니다:)", Toast.LENGTH_SHORT).show();

                Intent itLogin = new Intent(MyPageActivity.this, LoginActivity.class);
                startActivity(itLogin);
                finish();
            };

            //서버로 Volley를 이용해서 요청을 함
            SecessionRequest secessionRequest = new SecessionRequest(nickName, responseListener);
            RequestQueue queue = Volley.newRequestQueue(MyPageActivity.this);
            queue.add(secessionRequest);

            return null;
        }
    }
}
