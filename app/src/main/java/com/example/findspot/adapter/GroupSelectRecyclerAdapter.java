package com.example.findspot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findspot.R;
import com.example.findspot.data.GroupInfo;

import java.util.ArrayList;

import static com.example.findspot.SelectWhomActivity.btn_selwhom_next;
import static com.example.findspot.SelectWhomActivity.selectedGroup;

public class GroupSelectRecyclerAdapter extends RecyclerView.Adapter<GroupSelectRecyclerAdapter.GroupViewHolder>{
    private final ArrayList<GroupInfo> groupList;
    private int selected_pos = -1;

    public GroupSelectRecyclerAdapter(ArrayList<GroupInfo> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupSelectRecyclerAdapter.GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
        View view = inflater.inflate(R.layout.grouprow, parent, false);
        return new GroupSelectRecyclerAdapter.GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupSelectRecyclerAdapter.GroupViewHolder holder, int position) {
        holder.grouprow_title.setText(groupList.get(position).getGroupName());       //해당 위치를 인덱스로 한 리스트에 저장된 그룹이름을 텍스트로 설정함
        holder.grouprow_users.setText(String.join(", ", groupList.get(position).getGroupUsers()));      //사용자 이름을 ,을 구분자로 사용하여 출력함(ex. 영은, 소은)
        if (position == selected_pos) holder.itemView.setBackgroundColor(Color.parseColor("#D4D4D4"));
        else holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
    }

    @Override
    public int getItemCount() {
        //Adapter가 관리하는 전체 데이터 개수 반환
        return groupList.size();
    }

    public void setSelected_pos(int selected_pos) {
        this.selected_pos = selected_pos;   //선택된 position을 외부에서 변경
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView grouprow_title;
        TextView grouprow_users;

        GroupViewHolder(final View itemView) {
            super(itemView);
            grouprow_title = itemView.findViewById(R.id.grouprow_title);
            grouprow_users = itemView.findViewById(R.id.grouprow_users);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    selected_pos = pos;
                    notifyDataSetChanged();
                    selectedGroup.setGroupName(groupList.get(pos).getGroupName());      //그룹이름 수정
                    selectedGroup.setGHostName(groupList.get(pos).getGHostName());      //그룹방장이름 수정
                    selectedGroup.setGroupUsers(groupList.get(pos).getGroupUsers());    //그룹에 속한 사용자 목록 수정

                    btn_selwhom_next.setEnabled(true);      //그룹 목록에서의 항목을 선택시 활성화되어야 함
                }
            });
        }
    }
}
