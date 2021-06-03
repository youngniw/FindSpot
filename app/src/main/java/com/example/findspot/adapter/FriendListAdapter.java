package com.example.findspot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.findspot.R;

import java.util.ArrayList;

public class FriendListAdapter extends BaseAdapter {        //friendlist와 연결할 어댑터
    LayoutInflater inflater;
    int layout;
    ArrayList<String> src;
    TextView tvNoFriend;

    public FriendListAdapter(Context context, int layout, ArrayList<String> src, TextView tvNoFriend) {
        this.layout = layout;
        this.src = src;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.tvNoFriend = tvNoFriend;
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

        //set friend nickName TextView
        TextView tv_groupName = (TextView)convertView.findViewById(R.id.friendrow_title);
        tv_groupName.setText(src.get(pos));

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (getCount() == 0)
            tvNoFriend.setVisibility(View.VISIBLE);
        else
            tvNoFriend.setVisibility(View.GONE);
    }
}
