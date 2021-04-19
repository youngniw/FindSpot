package com.example.findspot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder>{
    private ArrayList<GroupInfo> groupList = null;

    public GroupRecyclerAdapter(ArrayList<GroupInfo> groupList) {
        this.groupList = groupList;
    }

    @Override
    public GroupRecyclerAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
        View view = inflater.inflate(R.layout.grouprow, parent, false);
        GroupRecyclerAdapter.GroupViewHolder viewHolder = new GroupRecyclerAdapter.GroupViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GroupRecyclerAdapter.GroupViewHolder holder, int position) {
        //**********************************************************확인 필요*******************************************************************************
        holder.grouprow_title.setText(groupList.get(position).getGroupName());       //해당 위치를 인덱스로 한 리스트에 저장된 그룹이름을 텍스트로 설정함
        holder.grouprow_users.setText(String.join(", ", groupList.get(position).getGroupUsers()));      //사용자 이름을 ,을 구분자로 사용하여 출력함(ex. 영은, 소은)
    }

    @Override
    public int getItemCount() {
        //Adapter가 관리하는 전체 데이터 개수 반환
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView grouprow_title;
        TextView grouprow_users;

        GroupViewHolder(View itemView) {
            super(itemView);
            grouprow_title = itemView.findViewById(R.id.grouprow_title);
            grouprow_users = itemView.findViewById(R.id.grouprow_users);
        }
    }
}
