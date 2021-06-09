package com.example.findspot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    ImageView ivBack;
    EditText etCurrentPW, etNewPW;
    TextView tvCurrentError, tvNewError;
    Button btChangePW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

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

        btChangePW.setOnClickListener(v -> {
            //TODO: 비밀번호 형식 검증해야 함(공백 있으면 공백있다고 출력!!!!!)
        });
    }
}
