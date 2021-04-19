package com.example.findspot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.findspot.HomeActivity.friendList;

public class FriendActivity extends AppCompatActivity {
    FriendRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        RecyclerView rv_friendlist = (RecyclerView) findViewById(R.id.friend_rv);
        LinearLayoutManager manager = new LinearLayoutManager(FriendActivity.this, LinearLayoutManager.VERTICAL,false);
        rv_friendlist.setLayoutManager(manager);        //LayoutManager 등록
        adapter = new FriendRecyclerAdapter(friendList);
        rv_friendlist.setAdapter(adapter);      //어댑터 등록
        rv_friendlist.addItemDecoration(new DividerItemDecoration(FriendActivity.this, 1)); //리스트 사이의 구분선

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv_friendlist);
    }

    //목록의 항목을 스와이프했을 때에 대한 액션 설정(스와이프하면 삭제되게 됨********************************수정요구)
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            friendList.remove(position);        //수정해야함************************************************************************************
            adapter.notifyItemRemoved(position);
        }
    };
}
