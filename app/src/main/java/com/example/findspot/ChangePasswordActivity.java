package com.example.findspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.request.PWChangeRequest;
import com.example.findspot.request.PWCheckRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static com.example.findspot.LoginActivity.nickName;

public class ChangePasswordActivity extends AppCompatActivity {
    ImageView ivBack;
    EditText etCurrentPW, etNewPW;
    TextView tvCurrentError, tvNewError;
    Button btChangePW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ivBack = findViewById(R.id.changePW_back);
        etCurrentPW = findViewById(R.id.changePW_cPW);
        tvCurrentError = findViewById(R.id.changePW_cPWError);
            tvCurrentError.setVisibility(View.GONE);
        etNewPW = findViewById(R.id.changePW_newPW);
        tvNewError = findViewById(R.id.changePW_newPWError);
            tvNewError.setVisibility(View.GONE);
        btChangePW = findViewById(R.id.changePW_change);

        changePW_clickListener();
    }

    void changePW_clickListener() {
        ivBack.setOnClickListener(v -> finish());

        //현재 비밀번호 입력값이 변경될 때,
        etCurrentPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1)
                    tvCurrentError.setVisibility(View.GONE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //새 비밀번호 입력값이 변경될 때,
        etNewPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1)
                    tvNewError.setVisibility(View.GONE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //비밀번호 변경 버튼 클릭 시,
        btChangePW.setOnClickListener(v -> {
            InputMethodManager inputMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMM.hideSoftInputFromWindow(etCurrentPW.getWindowToken(), 0);
            inputMM.hideSoftInputFromWindow(etNewPW.getWindowToken(), 0);
            getCurrentFocus().clearFocus();

            etCurrentPW.setEnabled(false);      //그 사이에 변경 안되게 함
            etNewPW.setEnabled(false);

            //현재 사용중인 비밀번호가 올바른 지 확인
            try {
                new PWCheckTask().execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }

        });
    }

    @SuppressLint("StaticFieldLeak")
    public class PWCheckTask extends AsyncTask<String, Void, String> {      //비밀번호 변경 가능한지 확인(현재 비밀번호 입력한 것이 옳은 지 확인)
        PWCheckTask() { super(); }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    boolean isRight = jsonObject.getBoolean("isRight");
                    if (isRight) {
                        String newPW = etNewPW.getText().toString();
                        if (etCurrentPW.getText().toString().equals(newPW)) {
                            ShowNewPWError("현재 사용 중인 비밀번호와 일치합니다. 새로운 비밀번호를 입력해 주세요.");
                        }
                        else if (newPW.length()<8 || newPW.length()>16) {
                            ShowNewPWError("새 비밀번호는 8자 이상 16자 이내여야 합니다.");
                        }
                        else if (newPW.contains(" ")) {       //새 비밀번호에 공백 포함시
                            ShowNewPWError("입력하신 새 비밀번호에 공백이 포함되어 있습니다. 공백을 제거해 주세요:)");
                        }
                        else if (!newPW.matches(".*[0-9].*")) {
                            ShowNewPWError("새 비밀번호에 숫자가 1개 이상 포함되어야 합니다.");
                        }
                        else if (!newPW.matches(".*[a-zA-Z].*")) {
                            ShowNewPWError("새 비밀번호에 영문자가 1개 이상 포함되어야 합니다.");
                        }
                        else if (newPW.matches(".*[^0-9a-zA-Z~!?@_[-][*]].*")) {
                            ShowNewPWError("새 비밀번호에 사용 불가능한 특수문자가 1개 이상 포함되어 있습니다.");
                        }
                        else {      //비밀번호 변경 가능
                            try {
                                new PWChangeTask().execute().get();
                            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
                        }
                    }
                    else {
                        tvCurrentError.setVisibility(View.VISIBLE);
                        etCurrentPW.setEnabled(true);
                        etNewPW.setEnabled(true);
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };

            PWCheckRequest passwordCheckRequest = new PWCheckRequest(nickName, etCurrentPW.getText().toString(), responseListener);
            RequestQueue queue = Volley.newRequestQueue(ChangePasswordActivity.this);
            queue.add(passwordCheckRequest);

            return null;
        }
    }
    private void ShowNewPWError(String errorText) {
        tvNewError.setVisibility(View.VISIBLE);
        tvNewError.setText(errorText);
        etCurrentPW.setEnabled(true);
        etNewPW.setEnabled(true);
    }

    @SuppressLint("StaticFieldLeak")
    public class PWChangeTask extends AsyncTask<String, Void, String> {      //비밀번호 변경
        PWChangeTask() { super(); }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                etCurrentPW.setEnabled(true);
                etNewPW.setEnabled(true);

                MyPageActivity myPageActivity = (MyPageActivity) MyPageActivity.activity;
                myPageActivity.finish();
                HomeActivity homeActivity = (HomeActivity) HomeActivity.activity;
                homeActivity.finish();

                Toast.makeText(getApplicationContext(), "비밀번호 변경이 완료되었습니다. 다시 로그인해주세요:)", Toast.LENGTH_SHORT).show();

                Intent itLogin = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                startActivity(itLogin);
                finish();
            };

            PWChangeRequest passwordChangeRequest = new PWChangeRequest(nickName, etNewPW.getText().toString(), responseListener);
            RequestQueue queue = Volley.newRequestQueue(ChangePasswordActivity.this);
            queue.add(passwordChangeRequest);

            return null;
        }
    }
}
