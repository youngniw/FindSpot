package com.example.findspot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.findspot.adapter.GroupListAdapter;
import com.example.findspot.data.GroupInfo;
import com.example.findspot.dialog.AddGroupDialog;
import com.example.findspot.request.DelGroupRequest;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.HomeActivity.groupList;
import static com.example.findspot.LoginActivity.nickName;

public class GroupActivity extends AppCompatActivity {
    GroupListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        TextView tvNoGroup = findViewById(R.id.group_tvNoGroup);
            if (groupList.size() == 0)
                tvNoGroup.setVisibility(View.VISIBLE);
            else
                tvNoGroup.setVisibility(View.GONE);
        final SwipeMenuListView listview = findViewById(R.id.group_swipeMenuLv);    //스와이프 메뉴 가능한 리스트뷰 생성
        adapter = new GroupListAdapter(GroupActivity.this, R.layout.grouprow, groupList, tvNoGroup);
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
        listview.setOnMenuItemClickListener((position, menu, index) -> {
            if (index == 0) { //delete
                new DelGroupTask(groupList.get(position)).execute();   //서버에게 삭제요청
                groupList.remove(position);         //groupList에서 삭제
                adapter.notifyDataSetChanged();
            }
            return false;
        });
        ImageButton addGroupBtn = (ImageButton) findViewById(R.id.group_add);
        addGroupBtn.setOnClickListener(listener);
    }

    //그룹 추가 이미지버튼 클릭 이벤트
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddGroupDialog addGroupDialog = new AddGroupDialog(GroupActivity.this, group -> {
                groupList.add(group);
                adapter.notifyDataSetChanged();
            }, nickName, friendList, groupList);
            addGroupDialog.show();
        }
    };

    //스와이프 했을 때 나올 삭제 메뉴
    SwipeMenuCreator creator = menu -> {
        //create "delete" item
        SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
        openItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25))); //set background
        openItem.setWidth(150);     //set width
        openItem.setTitle("삭제");    //set title
        openItem.setTitleSize(18);    //set title font size
        openItem.setTitleColor(Color.WHITE);    //set title font color
        menu.addMenuItem(openItem); //add to menu
    };

    //서버에게 해당 그룹의 삭제를 요청함
    @SuppressLint("StaticFieldLeak")
    public class DelGroupTask extends AsyncTask<String, Void, String> {
        GroupInfo delGroup;

        DelGroupTask(GroupInfo delGroup) {
            super();

            this.delGroup = delGroup;
        }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                groupList.remove(delGroup);
                adapter.notifyDataSetChanged();
                Toast.makeText(GroupActivity.this, "그룹 삭제가 완료됐습니다.", Toast.LENGTH_SHORT).show();
            };

            DelGroupRequest delGroupRequest = new DelGroupRequest(delGroup, responseListener);
            RequestQueue queueSave = Volley.newRequestQueue(getApplicationContext());
            queueSave.add(delGroupRequest);

            return null;
        }
    }
}
