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

public class GroupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        final SwipeMenuListView listview = findViewById(R.id.group_swipeMenuLv);    //스와이프 메뉴 가능한 리스트뷰 생성
        ListAdapter adapter = new GroupListAdapter(R.layout.grouprow, groupList);
        listview.setAdapter(adapter);   //어댑터 연결
        listview.setMenuCreator(creator);   //스와이프시 나올 메뉴 연결
        listview.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                //swipe start
                listview.smoothOpenMenu(position);
            }
            @Override
            public void onSwipeEnd(int position) {
                //swipe end
                listview.smoothOpenMenu(position);  //onSwipeEnd 이벤트 때 smoothOpenMenu() 호출해야 메뉴바가 고정됨
            }
        });
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //delete
                        Log.i("디버그","------delete");
                        //TODO: 서버에게 삭제 요청과 grouplist에서 삭제해야 함
                        //new DeleteGroupTask(groupList.get(position));   //서버에게 삭제요청
                        //groupList.remove(position);   //groupList에서 삭제
                        //adapter.notifyDataSetChanged();   //리스트뷰 새로고침
                }
                return false;
            }
        });
    }
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
    public class DeleteGroupTask extends AsyncTask<String, Void, String> {
        GroupInfo delGroup;
        DeleteGroupTask(GroupInfo delGroup) {
            this.delGroup = delGroup;
        }
        @Override
        protected String doInBackground(String... strings) {
            //데이터베이스로부터 주어진 x와 y값을 위치를 중심으로 가장 가까운 역을 받고, 또한 그 가까운 역을 중심으로 반경 2km이내의 역에 대한 정보를 반환받음
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);   //전달한 위치 근처의 5개 지하철 역 정보(이름, 경도, 위도)를 받음
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            };

            // 서버로 Volley를 이용해서 요청을 함
            DeleteGroupRequest deleteGroupRequest = new DeleteGroupRequest(delGroup, responseListener);    //반경을 radius로(km) 하며 위치를 줌
            RequestQueue queueSave = Volley.newRequestQueue(getApplicationContext());
            queueSave.add(deleteGroupRequest);

            return null;
        }
    }

    private class DeleteGroupRequest extends StringRequest {
        final static private String URL = "http://222.111.4.158/wheremiddle/deleteGroup.php";       //php파일 연돌을 위한 서버 URL을 설정
        private Map<String, String> DeleteGroupInfo;        //전달할 정보(삭제할 그룹)

        //해당 그룹을 삭제해주세요!
        public DeleteGroupRequest(GroupInfo deleteGroup, Response.Listener<String> listener) {
            super(Method.POST, URL, listener, null);

            DeleteGroupInfo = new HashMap<>();
            DeleteGroupInfo.put("groupID", deleteGroup.getGroupName());
            DeleteGroupInfo.put("groupHostNickName", deleteGroup.getGHostName());
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return DeleteGroupInfo;
        }
    }
}
