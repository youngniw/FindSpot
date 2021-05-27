package com.example.findspot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.findspot.request.SaveHistoryRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.findspot.ChoiceGPSGroupActivity.list_group;
import static com.example.findspot.HomeActivity.groupList;
import static com.example.findspot.SelectWhomActivity.selectedGroup;

//TODO: 그룹추가 리스너 만들어야 되고 그룹삭제, 그룹추가 php 만들어야됨
public class GroupActivity extends AppCompatActivity {
    ListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        final SwipeMenuListView listview = findViewById(R.id.group_swipeMenuLv);    //스와이프 메뉴 가능한 리스트뷰 생성
        adapter = new GroupListAdapter(R.layout.grouprow, groupList);
        listview.setAdapter(adapter);       //어댑터 연결
        listview.setMenuCreator(creator);   //스와이프시 나올 메뉴 연결
        listview.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            //swipe start
            public void onSwipeStart(int position) { listview.smoothOpenMenu(position); }
            @Override
            //swipe end: 이때 smoothOpenMenu() 호출해야 메뉴바가 고정됨
            public void onSwipeEnd(int position) { listview.smoothOpenMenu(position); }
        });
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0: //delete
                        Log.i("디버그","------delete");
                        //TODO: 서버에게 삭제 요청과 grouplist에서 삭제해야 함
                        //new DelGroupTask(groupList.get(position));   //서버에게 삭제요청
                        //groupList.remove(position);   //groupList에서 삭제
                        //adapter.notify();   //리스트뷰 새로고침
                }
                return false;
            }
        });
        ImageButton addGroupBtn = (ImageButton) findViewById(R.id.group_add);
        addGroupBtn.setOnClickListener(listner);
    }

    //그룹 추가 이미지버튼 클릭 이벤트
    View.OnClickListener listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO: 다이얼로그로 친구 선택 리스트 보여주고 친구 선택하고 완료 버튼 누르면 그룹명 입력받고 서버에게 요청
        }
    };

    //스와이프 했을 때 나올 삭제 메뉴
    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            //create "delete" item
            SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
            openItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25))); //set background
            openItem.setWidth(150);     //set width
            openItem.setTitle("삭제");    //set title
            openItem.setTitleSize(18);    //set title font size
            openItem.setTitleColor(Color.WHITE);    //set title font color
            menu.addMenuItem(openItem); //add to menu
        }
    };

    private class GroupListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        int layout;
        ArrayList<GroupInfo> src;

        public GroupListAdapter(int layout, ArrayList<GroupInfo> src) {
            this.layout = layout;
            this.src = src;
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() { return this.src.size(); }

        @Override
        public GroupInfo getItem(int pos) { return this.src.get(pos); }

        @Override
        public long getItemId(int pos) { return (long)pos; }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            //처음일 경우, View 생성
            if(convertView == null) convertView = inflater.inflate(layout, parent, false);

            //set groupID TextView
            TextView tv_groupName = (TextView)convertView.findViewById(R.id.grouprow_title);
            tv_groupName.setText(src.get(pos).getGroupName());

            //set groupUsers TextView
            TextView tv_groupUsers = (TextView)convertView.findViewById(R.id.grouprow_users);
            tv_groupUsers.setText(String.join(", ", groupList.get(pos).getGroupUsers()));

            return convertView;
        }
    }


    //서버에게 해당 그룹의 삭제를 요청함
    public class DelGroupTask extends AsyncTask<String, Void, String> {
        GroupInfo delGroup;
        DelGroupTask(GroupInfo delGroup) {
            this.delGroup = delGroup;
        }
        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);   //사실상 결과 받는건 없음
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };

            // 서버로 Volley를 이용해서 요청을 함
            DelGroupRequest delGroupRequest = new DelGroupRequest(delGroup, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueSave = Volley.newRequestQueue(getApplicationContext());
            queueSave.add(delGroupRequest);

            return null;
        }
    }
    //서버에게 전달하기 위한 정보 클래스
    private class DelGroupRequest extends StringRequest {
        final static private String URL = "http://222.111.4.158/wheremiddle/deleteGroup.php";       //php파일 연돌을 위한 서버 URL을 설정
        private Map<String, String> DelGroupInfo;        //전달할 정보(삭제할 그룹)

        //해당 그룹을 삭제해주세요!
        public DelGroupRequest(GroupInfo delGroup, Response.Listener<String> listener) {
            super(Method.POST, URL, listener, null);

            DelGroupInfo = new HashMap<>();
            DelGroupInfo.put("groupID", delGroup.getGroupName());
            DelGroupInfo.put("groupHostNickName", delGroup.getGHostName());
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError { return DelGroupInfo; }
    }
}
