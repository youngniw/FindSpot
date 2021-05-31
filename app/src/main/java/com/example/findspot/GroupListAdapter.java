package com.example.findspot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.findspot.HomeActivity.groupList;

public class GroupListAdapter extends BaseAdapter {
    LayoutInflater inflater;
    int layout;
    ArrayList<GroupInfo> src;

    public GroupListAdapter(Context context, int layout, ArrayList<GroupInfo> src) {
        this.layout = layout;
        this.src = src;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
