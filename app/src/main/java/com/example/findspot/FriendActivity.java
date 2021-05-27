package com.example.findspot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.LoginActivity.nickName;

public class FriendActivity extends AppCompatActivity {
    ListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        final SwipeMenuListView listview = findViewById(R.id.friend_swipeMenuLv);
        adapter = new FriendListAdapter(R.layout.friendrow, friendList);
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
                        //new DelFriendTask(friendList.get(position));   //서버에게 삭제요청
                        //friendList.remove(position);   //groupList에서 삭제
                        //adapter.notify();   //리스트뷰 새로고침
                }
                return false;
            }
        });
        ImageButton addFriendBtn = (ImageButton) findViewById(R.id.friend_add);
        addFriendBtn.setOnClickListener(listner);
    }

    //친구 추가 이미지버튼 클릭 이벤트
    View.OnClickListener listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO: 다이얼로그로 닉네임 받고 1.이미친구 2.친구아님 3.그런 닉네임 없음 (일단 다이얼로그는 종료하고 새로운 다이얼로그 띄움)
            //TODO:서버에서 이미 친구이면 친구추가까지 OK해서 반환
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

    // friendlist와 연결할 어댑터
    private class FriendListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        int layout;
        ArrayList<String> src;

        public FriendListAdapter(int layout, ArrayList<String> src) {
            this.layout = layout;
            this.src = src;
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() { return this.src.size(); }

        @Override
        public String getItem(int pos) { return this.src.get(pos); }

        @Override
        public long getItemId(int pos) { return (long)pos; }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            //처음일 경우, View 생성
            if(convertView == null) convertView = inflater.inflate(layout, parent, false);

            //set friend ninckName TextView
            TextView tv_groupName = (TextView)convertView.findViewById(R.id.friendrow_title);
            tv_groupName.setText(src.get(pos));

            return convertView;
        }
    }

    //서버에게 해당 친구의 삭제를 요청함
    public class DelFriendTask extends AsyncTask<String, Void, String> {
        String delFriend;
        DelFriendTask(String delFriend) {
            this.delFriend = delFriend;
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
            DelFriendRequest delFriendRequest = new DelFriendRequest(delFriend, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueSave = Volley.newRequestQueue(getApplicationContext());
            queueSave.add(delFriendRequest);

            return null;
        }
    }

    //서버에게 전달하기 위한 정보 클래스
    private class DelFriendRequest extends StringRequest {
        final static private String URL = "http://222.111.4.158/wheremiddle/deleteFriend.php";       //php파일 연돌을 위한 서버 URL을 설정
        private Map<String, String> DelFriendInfo;        //전달할 정보(삭제할 그룹)

        //해당 친구를 삭제해주세요!
        public DelFriendRequest(String delFriend, Response.Listener<String> listener) {
            super(Method.POST, URL, listener, null);

            DelFriendInfo = new HashMap<>();
            DelFriendInfo.put("friendNickName", delFriend);
            DelFriendInfo.put("userNickName", nickName);
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError { return DelFriendInfo; }
    }
}
