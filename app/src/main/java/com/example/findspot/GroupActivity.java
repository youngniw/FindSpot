package com.example.findspot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.HomeActivity.groupList;

public class GroupActivity extends AppCompatActivity {
    GroupRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        RecyclerView rv_grouplist = (RecyclerView) findViewById(R.id.group_rv);
        LinearLayoutManager manager = new LinearLayoutManager(GroupActivity.this, LinearLayoutManager.VERTICAL,false);
        rv_grouplist.setLayoutManager(manager);        //LayoutManager 등록
        adapter = new GroupRecyclerAdapter(groupList);
        rv_grouplist.setAdapter(adapter);      //어댑터 등록
        rv_grouplist.addItemDecoration(new DividerItemDecoration(GroupActivity.this, 1)); //리스트 사이의 구분선 설정

    }
    //목록의 항목을 스와이프했을 때에 대한 액션 설정(스와이프하면 삭제되게 됨********************************수정요구)
}
