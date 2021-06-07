package com.example.findspot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.R;
import com.example.findspot.request.NickNameCheckRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class NickNameCheckDialog extends Dialog {
    private final NickNameCheckDialogListener nickNameCheckDialogListener;
    String userNickName;

    EditText et_dialogNickName;
    Button btn_dialogComplete;
    TextView tv_dialogIsCheckedNickName;

    public NickNameCheckDialog(@NonNull Context context, NickNameCheckDialogListener nickNameCheckDialogListener) {
        super(context);
        this.nickNameCheckDialogListener = nickNameCheckDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_check_nickname);

        et_dialogNickName = findViewById(R.id.nickNameDialog_nickname);
        btn_dialogComplete = findViewById(R.id.nickNameDialog_complete);
        tv_dialogIsCheckedNickName = findViewById(R.id.nickNameDialog_isCheckedNickName);
            tv_dialogIsCheckedNickName.setVisibility(View.GONE);

        //값이 변경될 때,
        et_dialogNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) //닉네임 중복 경고 지움
                    tv_dialogIsCheckedNickName.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //"완료"버튼 눌렀을 때,
        btn_dialogComplete.setOnClickListener(v -> {
            userNickName = et_dialogNickName.getText().toString();

            if (userNickName.length() == 0) {
                tv_dialogIsCheckedNickName.setVisibility(View.VISIBLE);
                tv_dialogIsCheckedNickName.setText("* 입력하신 닉네임이 없습니다.");
            }

            else {
                Response.Listener<String> responseListener = response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean existNickName = jsonObject.getBoolean("existNickName");
                        if (existNickName) {    //이미 닉네임이 존재함
                            //닉네임 중복 경고 보이게끔.
                            tv_dialogIsCheckedNickName.setVisibility(View.VISIBLE);
                            tv_dialogIsCheckedNickName.setText("* 이미 중복된 닉네임이 있습니다.");
                        } else {                //닉네임 사용가능
                            //context한테 닉네임 전달하고 거기서 DB Insert 해야함
                            nickNameCheckDialogListener.clickBtn(userNickName);     //JoinActivity에서 처리할 수 있도록.
                            dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                NickNameCheckRequest nickNameCheckRequest = new NickNameCheckRequest(userNickName, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(nickNameCheckRequest);
            }
        });
    }

    public interface NickNameCheckDialogListener{
        void clickBtn(String userName);
    }
}
