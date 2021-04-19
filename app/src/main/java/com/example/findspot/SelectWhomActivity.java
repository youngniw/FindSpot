package com.example.findspot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.HomeActivity.groupList;

public class SelectWhomActivity extends AppCompatActivity {
    boolean isRandom = true;
    RadioGroup rg_selwhom_radiogroup;
    RecyclerView rv_selwhom_grouplist;
    Button btn_selwhom_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectwhom);

        rg_selwhom_radiogroup = (RadioGroup) findViewById(R.id.selectwhom_radiogroup);
        rv_selwhom_grouplist = (RecyclerView) findViewById(R.id.selectwhom_grouplist_rv);        //사용자가 포함된 그룹리스트를 보여줌
            rv_selwhom_grouplist.addItemDecoration(new DividerItemDecoration(SelectWhomActivity.this, 1)); //리스트 사이의 구분선선
            //그룹리스트에 그룹 항목에 대한 배열을 연결해놔야함
        btn_selwhom_next = (Button) findViewById(R.id.selectwhom_next);
            btn_selwhom_next.setEnabled(false);//초기에는 사용자에게 입력받은 내용이 없으므로 버튼을 비활성화함

        selwhom_radiogroup_checkedChangeListener(); //SelectWhom 화면에서 라디오그룹에 대한 setOnCheckedChangeListener를 정의한 함수를 호출함
        selwhom_btn_clickListener();         //SelectWhom 화면에서 "위치 선택하기"버튼에 대한 onClickListener를 정의한 함수를 호출함
    }

    void selwhom_radiogroup_checkedChangeListener() {
        rg_selwhom_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.selectwhom_random) {
                    isRandom = true;    //랜덤으로 중간지점 찾기를 할 것이라고 저장
                    btn_selwhom_next.setEnabled(true);      //랜덤으로 할 시 버튼이 활성화되어야 함
                }
                else {
                    isRandom = false;   //그룹으로 중간지점 찾기를 할 것이라고 저장
                    btn_selwhom_next.setEnabled(false);      //그룹 목록에서의 항목을 선택시 활성화되어야 함

                    //********************************************************************************************************************
                    //************************(그룹 선택 시에 그룹 선택 후 수정해야함)********************************************************
                    RecyclerView rv_grouplist = (RecyclerView) findViewById(R.id.selectwhom_grouplist_rv);
                    LinearLayoutManager manager = new LinearLayoutManager(SelectWhomActivity.this, LinearLayoutManager.VERTICAL,false);
                    rv_grouplist.setLayoutManager(manager);        //LayoutManager 등록
                    rv_grouplist.setAdapter(new GroupRecyclerAdapter(groupList));      //어댑터 등록
                    rv_grouplist.addItemDecoration(new DividerItemDecoration(SelectWhomActivity.this, 1)); //리스트 사이의 구분선 설정

                    //그룹을 실제로 선택 시에 버튼 setEnabled를 true로 해야함*********************************************************************

                }

            }
        });
    }

    void selwhom_btn_clickListener() {
        btn_selwhom_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_choice_gps로 화면 이동하고 시간 기준임을 intent로 전달
                Intent it_choiceGPS = new Intent(SelectWhomActivity.this, ChoiceGPSRandomActivity.class);

                // 넘겨줄 데이터 Bundle 형태로 만들기
                Bundle extras = new Bundle();
                if (isRandom)
                    extras.putString("standard_tag", "random");     //개인이 임의로 위치 선택
                else
                    extras.putString("standard_tag", "group");      //그룹선택
                it_choiceGPS.putExtras(extras);
                startActivity(it_choiceGPS);
            }
        });
    }
}
