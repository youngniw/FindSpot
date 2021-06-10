package com.example.findspot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.dialog.NickNameCheckDialog;
import com.example.findspot.request.IDCheckRequest;
import com.example.findspot.request.JoinRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class JoinActivity extends AppCompatActivity {
    String nickName;
    String checkedID = "";
    TextView tv_joinIsCheckedID, tvPWError, tvBirthError;
    EditText et_joinID, et_joinPW, et_joinBirthYear;
    Button btn_id_check, btn_ok, btn_cancel;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_joinGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        tv_joinIsCheckedID = findViewById(R.id.join_isCheckedID);    //id 중복확인한 후 id 사용가능시 보이게 되는 textView
            tv_joinIsCheckedID.setVisibility(View.GONE);
        tvPWError = findViewById(R.id.join_pwError);
            tvPWError.setVisibility(View.GONE);
        tvBirthError = findViewById(R.id.join_birthError);
            tvBirthError.setVisibility(View.GONE);

        et_joinID = findViewById(R.id.join_id);                  //id를 입력하는 editText
        et_joinPW = findViewById(R.id.join_passwd);              //비밀번호를 입력하는 editText
        sw_joinGender = findViewById(R.id.join_gender);          //성별을 입력할 수 있는 switch
        et_joinBirthYear = findViewById(R.id.join_birthYear);    //태어난 연도를 입력하는 editText

        btn_id_check = findViewById(R.id.join_id_check);    //중복확인 버튼
        btn_ok = findViewById(R.id.join_ok);                //회원가입 확인 버튼
        btn_cancel = findViewById(R.id.join_cancel);        //회원가입 취소 버튼

        join_et_textChangedListener();      //edittext의 텍스트를 수정할 시에 이벤트가 발생하게 함
        join_clickListener();           //회원가입 화면에 button과 edittext의 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void join_et_textChangedListener() {
        //아이디를 입력받는 edittext의 텍스트를 수정했을 때에 해당하는 textChangedListener를 정의함
        et_joinID.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //텍스트
                if (tv_joinIsCheckedID.getVisibility() == View.VISIBLE)
                    tv_joinIsCheckedID.setVisibility(View.GONE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        et_joinPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //텍스트
                if (tvPWError.getVisibility() == View.VISIBLE)
                    tvPWError.setVisibility(View.GONE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @SuppressLint("SetTextI18n")
    void join_clickListener() {
        //태어난 연도를 입력하기 위한 edittext를 클릭했을 때
        et_joinBirthYear.setOnClickListener(v -> {
            final NumberPicker yearPick = new NumberPicker(getApplicationContext());    //연도를 선택할 수 있는 다이얼로그 생성
            yearPick.setMinValue(1900);     //1900년도를 최소값으로 설정함
            yearPick.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));    //올해를 Max값으로 설정함
            if (et_joinBirthYear.getText().toString().equals(""))
                yearPick.setValue(Calendar.getInstance().get(Calendar.YEAR));   //선택한 연도가 없으므로 올해를 설정함
            else
                yearPick.setValue(Integer.parseInt(et_joinBirthYear.getText().toString()));     //선택한 연도가 이전에 있었으므로 그 값으로 설정함

            AlertDialog.Builder dialog = new AlertDialog.Builder(JoinActivity.this);    //다이얼로그 설정
            dialog.setTitle("출생년도 입력");     //다이얼로그 제목 설정
            dialog.setView(yearPick);           //내용으로 숫자 선택할 수 있게 보여줌
            //버튼 클릭시 동작
            dialog.setPositiveButton("확인", (dialog1, which) -> {
                tvBirthError.setVisibility(View.GONE);
                et_joinBirthYear.setText(String.valueOf(yearPick.getValue()));
            });
            dialog.setNegativeButton("취소", (dialog12, which) -> dialog12.dismiss());
            dialog.show();
        });

        //id 중복확인 버튼을 클릭했을 때
        btn_id_check.setOnClickListener(v -> {
            InputMethodManager inputMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);      //키보드 안보이게 하기 위한 InputMethodManager객체 생성
            inputMM.hideSoftInputFromWindow(et_joinID.getWindowToken(), 0);
            inputMM.hideSoftInputFromWindow(et_joinPW.getWindowToken(), 0);

            String id = et_joinID.getText().toString();
            if (id.length() == 0) {
                tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                tv_joinIsCheckedID.setTextColor(Color.RED);
                tv_joinIsCheckedID.setText("ID를 입력해 주세요.");
            }
            else if (id.contains(" ")) {
                tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                tv_joinIsCheckedID.setTextColor(Color.RED);
                tv_joinIsCheckedID.setText("ID에는 공백이 포함될 수 없습니다. 다시 입력해 주세요.");
            }
            else {
                try {
                    new IDCheckTask().execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            }
        });

        //회원가입창의 확인 버튼을 클릭했을 때(회원가입이 완료됨을 알려줌)
        btn_ok.setOnClickListener(v -> {
            InputMethodManager inputMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);      //키보드 안보이게 하기 위한 InputMethodManager객체 생성
            inputMM.hideSoftInputFromWindow(et_joinID.getWindowToken(), 0);
            inputMM.hideSoftInputFromWindow(et_joinPW.getWindowToken(), 0);

            //회원가입을 위해 값을 모두 입력했는지를 확인함
            if (!checkedID.equals(et_joinID.getText().toString())) {    //ID가 중복확인을 했는지를 점검(중복확인하지 않았다면 회원가입되지 않음)
                tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                tv_joinIsCheckedID.setTextColor(Color.RED);
                tv_joinIsCheckedID.setText("ID 중복 확인해 주세요.");
                return;
            }

            //비밀번호 확인
            String password = et_joinPW.getText().toString();
            if (password.length()<8 || password.length()>16) {  //비밀번호가 8자리 이상 16자리 이하인지 확인
                tvPWError.setVisibility(View.VISIBLE);
                tvPWError.setText("비밀번호는 8자 이상 16자 이내여야 합니다.");
                return;
            }
            else if (password.contains(" ")) {
                tvPWError.setVisibility(View.VISIBLE);
                tvPWError.setText("비밀번호에 공백이 포함되어 있습니다. 공백을 제거해 주세요:)");
                return;
            }
            else if (!password.matches(".*[0-9].*")){
                tvPWError.setVisibility(View.VISIBLE);
                tvPWError.setText("비밀번호에 숫자가 1개 이상 포함되어야 합니다.");
                return;
            }
            else if (!password.matches(".*[a-zA-Z].*")) {
                tvPWError.setVisibility(View.VISIBLE);
                tvPWError.setText("새 비밀번호에 영문자가 1개 이상 포함되어야 합니다.");
                return;
            }
            else if (password.matches(".*[^0-9a-zA-Z~!?@_[-][*]].*")) {
                tvPWError.setVisibility(View.VISIBLE);
                tvPWError.setText("새 비밀번호에 사용 불가능한 특수문자가 1개 이상 포함되어 있습니다.");
                return;
            }

            //연도 확인
            if (et_joinBirthYear.getText().toString().equals("")) {
                tvBirthError.setVisibility(View.VISIBLE);
                return;
            }

            //마지막으로 닉네임 입력받음
            NickNameCheckDialog nickNameCheckDialog = new NickNameCheckDialog(JoinActivity.this, userName -> {    //닉네임 중복확인 완료
                //DB에 사용자 계정 추가 요청
                nickName = userName;
                try {
                    new JoinSubmitTask().execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            });
            nickNameCheckDialog.show();
        });

        //회원가입창의 취소 버튼을 클릭했을 때(로그인 창으로 돌아감)
        btn_cancel.setOnClickListener(v -> {
            finish();       //기존의 로그인 창으로 다시 돌아감(폰의 뒤로가기 버튼 클릭해도 로그인창으로 돌아감)
        });
    }

    //ID 중복 확인을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class IDCheckTask extends AsyncTask<String, Void, String> {
        IDCheckTask() { super(); }
        @Override
        protected String doInBackground(String... strings) {
            String userID = et_joinID.getText().toString();     //EditText에 현재 입력된 id 값을 가져옴

            //데이터베이스에 id가 이미 존재하는 지를 확인한 후, 데이터베이스에 id가 없다면 id를 사용할 수 있음을 textView로 보여줌
            @SuppressLint("SetTextI18n") Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean formatError = jsonObject.getBoolean("formatError");
                    boolean existID = jsonObject.getBoolean("existID");
                    if (formatError) {      //이메일 형식과 맞지 않음
                        tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                        tv_joinIsCheckedID.setTextColor(Color.RED);
                        tv_joinIsCheckedID.setText("이메일 형식으로 ID를 입력해주세요.");
                    } else if (existID) {   //이미 ID가 존재함(바꿔야함)
                        tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                        tv_joinIsCheckedID.setTextColor(Color.RED);
                        tv_joinIsCheckedID.setText("이미 존재하는 ID입니다.");
                    } else {       //ID가 존재하지 않아, 사용 가능함(회원가입 가능)
                        tv_joinIsCheckedID.setVisibility(View.VISIBLE);
                        tv_joinIsCheckedID.setTextColor(Color.parseColor("#398E3D"));
                        tv_joinIsCheckedID.setText("사용가능한 ID입니다.");
                        checkedID = jsonObject.getString("currentID");      //서버에서 중복확인한 ID를 저장함
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };
            // 서버로 Volley를 이용해서 요청을 함.
            IDCheckRequest idCheckRequest = new IDCheckRequest(userID, responseListener);
            RequestQueue queue = Volley.newRequestQueue(JoinActivity.this);
            queue.add(idCheckRequest);

            return null;
        }
    }

    //입력한 값들에 대한 회원가입을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class JoinSubmitTask extends AsyncTask<String, Void, String> {
        JoinSubmitTask() { super(); }
        @Override
        protected String doInBackground(String... strings) {
            String userID = et_joinID.getText().toString();     //EditText에 현재 입력된 id값을 가져옴
            String userPW = et_joinPW.getText().toString();     //EditText에 현재 입력된 비밀번호값을 가져옴
            String gender = "M";
            if (sw_joinGender.isChecked())      //현재 스위치가 on 상태라면 여자인 것이므로 F를 저장함
                gender = "F";
            String birthYear = et_joinBirthYear.getText().toString();

            String userNickName = nickName;

            //데이터베이스에 사용자의 ID와 PW와 성별, 그리고 태어난 연도를 저장함으로써 회원가입이 완료되었는 지를 확인
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean accept = jsonObject.getBoolean("accept");
                    if (accept) {   //회원가입 완료
                        Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다:) 로그인 부탁드려요!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {        //회원가입이 되지 않음
                        Toast.makeText(getApplicationContext(), "회원가입이 되지 않았네요. 다시 작성 부탁드려요!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };
            // 서버로 Volley를 이용해서 요청을 함
            JoinRequest joinRequest = new JoinRequest(userID, userPW, userNickName, gender, birthYear, "0", responseListener);
            RequestQueue queue = Volley.newRequestQueue(JoinActivity.this);
            queue.add(joinRequest);

            return null;
        }
    }
}
