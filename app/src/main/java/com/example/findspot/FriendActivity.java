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
import com.example.findspot.adapter.FriendListAdapter;
import com.example.findspot.dialog.AddFriendDialog;
import com.example.findspot.request.DelFriendRequest;

import java.util.concurrent.ExecutionException;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.LoginActivity.nickName;

public class FriendActivity extends AppCompatActivity {
    FriendListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        TextView tvNoFriend = findViewById(R.id.friend_tvNoFriend);
            if (friendList.size() == 0)
                tvNoFriend.setVisibility(View.VISIBLE);
            else
                tvNoFriend.setVisibility(View.GONE);
        final SwipeMenuListView listview = findViewById(R.id.friend_swipeMenuLv);
        adapter = new FriendListAdapter(FriendActivity.this, R.layout.friendrow, friendList, tvNoFriend);
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
                try {
                    new DelFriendTask(friendList.get(position)).execute().get();   //서버에게 삭제요청
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
        ImageButton addFriendBtn = (ImageButton) findViewById(R.id.friend_add);
        addFriendBtn.setOnClickListener(listener);
    }

    //친구 추가 이미지버튼 클릭 이벤트
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddFriendDialog addFriendDialog = new AddFriendDialog(FriendActivity.this, friendNick -> {
                friendList.add(friendNick);     //친구리스트에 추가
                adapter.notifyDataSetChanged();
            }, nickName, friendList);
            addFriendDialog.show();
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

    //서버에게 해당 친구의 삭제를 요청함
    @SuppressLint("StaticFieldLeak")
    public class DelFriendTask extends AsyncTask<String, Void, String> {
        String delFriend;

        DelFriendTask(String delFriend) {
            super();

            this.delFriend = delFriend;
        }

        @Override
        protected String doInBackground(String... strings) {
            Response.Listener<String> responseListener = response -> {
                friendList.remove(delFriend);
                adapter.notifyDataSetChanged();   //리스트뷰 새로고침
                Toast.makeText(FriendActivity.this, "친구 삭제가 완료됐습니다.", Toast.LENGTH_SHORT).show();
            };
            // 서버로 Volley를 이용해서 요청을 함
            DelFriendRequest delFriendRequest = new DelFriendRequest(nickName, delFriend, responseListener);
            RequestQueue queueSave = Volley.newRequestQueue(getApplicationContext());
            queueSave.add(delFriendRequest);

            return null;
        }
    }
}
