package com.example.findspot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.request.GroupInfoRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.findspot.HomeActivity.groupList;

public class SelectWhomActivity extends AppCompatActivity {
    static Button btn_selwhom_next;
    static GroupInfo selectedGroup;     //SelectWhomActivity에서 선택한 그룹에 속한 사용자 닉네임 정보
    static ArrayList<String> ghistory;     //이전에 해당 그룹이 중간지점을 찾았다면 그에 대한 정보(인덱스 순서대로 중간지점을 시간(T)기준인지 거리(D)기준인지, 위도, 경도, 도로명)
    static ArrayList<PositionItem> list_g_users;       //(그룹에 속한 사용자 닉네임, 데이터베이스에 저장된 위치의 위도 및 경도)로 구성된 리스트

    Bundle extras;
    boolean isRandom = true;
    RadioGroup rg_selwhom_radiogroup;
    RecyclerView rv_selwhom_grouplist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectwhom);

        ghistory = new ArrayList<String>();
        list_g_users = new ArrayList<PositionItem>();

        rg_selwhom_radiogroup = (RadioGroup) findViewById(R.id.selectwhom_radiogroup);
        rv_selwhom_grouplist = (RecyclerView) findViewById(R.id.selectwhom_grouplist_rv);        //사용자가 포함된 그룹리스트를 보여줌
            rv_selwhom_grouplist.setVisibility(View.INVISIBLE);     //그룹리스트 내용 안보이게
            LinearLayoutManager manager = new LinearLayoutManager(SelectWhomActivity.this, LinearLayoutManager.VERTICAL,false);
            rv_selwhom_grouplist.setLayoutManager(manager);        //LayoutManager 등록
            rv_selwhom_grouplist.setAdapter(new GroupRecyclerAdapter(groupList, true));      //어댑터 등록
            rv_selwhom_grouplist.addItemDecoration(new DividerItemDecoration(SelectWhomActivity.this, 1)); //리스트 사이의 구분선 설정
            //그룹리스트에 그룹 항목에 대한 배열을 연결해놔야함
        btn_selwhom_next = (Button) findViewById(R.id.selectwhom_next);
            btn_selwhom_next.setEnabled(false);//초기에는 사용자에게 입력받은 내용이 없으므로 버튼을 비활성화함

        selectedGroup = new GroupInfo("tmp", "tmp");    //임시로 설정(선택된 그룹 정보 저장)

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

                    rv_selwhom_grouplist.setVisibility(View.VISIBLE);     //어댑터 내용이 보이게 함
                    //그룹을 실제로 선택 시에 버튼 setEnabled를 true로 함(adapter에서 수행함)
                    //항목 위치에 따라 그 항목의 그룹이름과 호스트이름으로 그룹에 속한 사람들의 x와 y좌표 값을 구할 수 있음(또한 최근에 선택한 위치값도 불러올 수 있음)
                }
            }
        });
    }

    void selwhom_btn_clickListener() {
        btn_selwhom_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 넘겨줄 데이터 Bundle 형태로 만들기
                extras = new Bundle();

                if (isRandom) {
                    Intent it_choiceGPSR = new Intent(SelectWhomActivity.this, ChoiceGPSRandomActivity.class);
                    it_choiceGPSR.putExtras(extras);
                    startActivity(it_choiceGPSR);        //랜덤하게 위치 선택하는 화면으로 전환
                }

                else {
                    try {
                        new SelectWhomActivity.GetGroupInfoTask().execute().get();      //DB로부터 그룹의 속한 사용자들의 닉네임, 도로명, 지정한 x, y값을 받아옴, 또한 최근에 찾은 위치를 불러옴
                    } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
                }
            }
        });
    }

    //그룹에 속한 사용자 정보를 받고 이 그룹에서 이전에 중간지점을 찾은 기록을 얻기 위한 작업 수행함
    public class GetGroupInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스에 그룹에 속한 사용자의 정보(닉네임, 도로명, 경도, 위도)와 최근에 찾은 위치를 얻음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        list_g_users.clear();
                        JSONObject jsonObject = new JSONObject(response);

                        //TODO: 확인 요망
                        if (jsonObject.getJSONObject("group").getString("historyTF").equals("T")) {     //이전에 이 그룹에서 중간 지점 찾기를 했었음(기록이 있음)
                            ghistory.add(jsonObject.getJSONObject("group").getString("standard"));  //시간(T) or 거리(D) 기준
                            if (jsonObject.getJSONObject("group").getString("standard").equals("T"))    //최근 기록이 시간기준으로 중간지점을 찾은 기록일 때
                                extras.putString("history", "최근 기록 확인 [시간 기준]  >>");      //기록 전달
                            else        //최근 기록이 거리기준으로 중간지점을 찾은 기록일 때
                                extras.putString("history", "최근 기록 확인 [거리 기준]  >>");      //기록 전달
                            ghistory.add(String.valueOf(jsonObject.getJSONObject("group").getDouble("y")));     //최근 중간지점의 위도
                            ghistory.add(String.valueOf(jsonObject.getJSONObject("group").getDouble("x")));     //최근 중간지점의 경도
                            ghistory.add(jsonObject.getJSONObject("group").getString("roadName"));      //최근 중간지점의 도로명주로
                        }
                        else
                            extras.putString("history", "noHistory");      //기록 전달(보여주지 마라!)

                        JSONArray members = jsonObject.getJSONArray("members");
                        for (int i=0; i<members.length(); i++) {    //사용자 정보를 list_g_users에 저장함
                            PositionItem memberInfo = new PositionItem(members.getJSONObject(i).getString("nickName"), members.getJSONObject(i).getString("roadName"),
                                    members.getJSONObject(i).getDouble("y"), members.getJSONObject(i).getDouble("x"));
                            list_g_users.add(memberInfo);       //TODO: null일때는 추가가 안됨(x와 y값이 없을 시)
                        }

                        Intent it_choiceGPSG = new Intent(SelectWhomActivity.this, ChoiceGPSGroupActivity.class);
                        it_choiceGPSG.putExtras(extras);
                        startActivity(it_choiceGPSG);        //그룹 속 사용자들의 위치 선택하는 화면으로 전환

                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };

            //서버로 Volley를 이용해서 요청을 함.(그룹에 속한 사용자들의 정보를 받기 위한 전달)
            GroupInfoRequest gUserInfoRequest = new GroupInfoRequest(selectedGroup, responseListener);
            RequestQueue queue = Volley.newRequestQueue(SelectWhomActivity.this);
            queue.add(gUserInfoRequest);

            return null;
        }
    }
}
