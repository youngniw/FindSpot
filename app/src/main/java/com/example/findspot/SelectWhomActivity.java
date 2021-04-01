package com.example.findspot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectWhomActivity extends AppCompatActivity {
    int peopleNumToSelect = 0;
    RadioGroup rg_selectwhom_radiogroup;
    LinearLayout ll_selectwhom_insertNum;
    Button btn_selectwhom_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectwhom);

        rg_selectwhom_radiogroup = (RadioGroup) findViewById(R.id.selectwhom_radiogroup);
        ll_selectwhom_insertNum = (LinearLayout) findViewById(R.id.selectwhom_insertNum);
        btn_selectwhom_next = (Button) findViewById(R.id.selectwhom_next);
        btn_selectwhom_next.setEnabled(false);//초기에는 사용자에게 입력받은 내용이 없으므로 버튼을 비활성화함

        selectwhom_radiogroup_checkedChangeListener(); //SelectWhom 화면에서 라디오그룹에 대한 setOnCheckedChangeListener를 정의한 함수를 호출함
        selectwhom_btn_clickListener();         //SelectWhom 화면에서 "위치 선택하기"버튼에 대한 onClickListener를 정의한 함수를 호출함
    }

    void selectwhom_radiogroup_checkedChangeListener() {
        rg_selectwhom_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.selectwhom_random) {
                    btn_selectwhom_next.setEnabled(false);      //그룹에서 개인으로 넘어가는 경우에도 버튼 비활성화를 해줘야함
                    TextView tv_selectwhom_peopleNum = new TextView(getApplicationContext());
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv_selectwhom_peopleNum.setLayoutParams(p);
                    tv_selectwhom_peopleNum.setTextColor(Color.BLACK);
                    tv_selectwhom_peopleNum.setText("인원 수: ");
                    ll_selectwhom_insertNum.addView(tv_selectwhom_peopleNum);
                    EditText et_selectwhom_peopleNum = new EditText(getApplicationContext());
                    LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    et_selectwhom_peopleNum.setLayoutParams(p2);
                    et_selectwhom_peopleNum.setTextColor(Color.BLACK);
                    et_selectwhom_peopleNum.setInputType(InputType.TYPE_CLASS_NUMBER);
                    et_selectwhom_peopleNum.setHint("2");
                    ll_selectwhom_insertNum.addView(et_selectwhom_peopleNum);       //사용자에게 인원 수를 입력받음
                    et_selectwhom_peopleNum.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) { }
                        @Override
                        public void afterTextChanged(Editable s) {
                            if (! s.toString().equals("") && Integer.parseInt(s.toString()) != 0 ) {       //양수일때 가능 (값이 없거나 0이면 안됨)
                                btn_selectwhom_next.setEnabled(true);       //인원수를 입력받았으므로 버튼을 클릭해 위치를 선정할 수 있음
                                peopleNumToSelect = Integer.parseInt(s.toString());
                            }
                            else
                                btn_selectwhom_next.setEnabled(false);      //0명 이하일 때는 버튼을 눌러 다음 화면으로 넘어가지 못함
                        }
                    });
                }
                else {
                    peopleNumToSelect = 0;  //랜덤 인원 수 값을 0으로 설정함
                    btn_selectwhom_next.setEnabled(false);
                    ll_selectwhom_insertNum.removeAllViews();       //"임의로 위치 선택" 후에 "그룹 선택"한것이므로 리니어레이아웃 초기화함


                    //********************************************************************************************************************
                    //************************(그룹 선택 시에 그룹 선택 후 수정해야함)********************************************************
                    //그룹을 실제로 선택 시에 버튼 setEnabled를 true로 해야함


                }

            }
        });
    }

    void selectwhom_btn_clickListener() {
        btn_selectwhom_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_choice_gps로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_choiceGps = new Intent(SelectWhomActivity.this, ChoiceGPSActivity.class);

                // 넘겨줄 데이터 Bundle 형태로 만들기
                Bundle extras = new Bundle();
                if (peopleNumToSelect == 0) {
                    extras.putString("standard_tag", "group");      //그룹선택
                } else {
                    extras.putString("standard_tag", "random");     //개인이 임의로 위치 선택
                }
                it_choiceGps.putExtras(extras);
                startActivity(it_choiceGps);
            }
        });
    }
}
