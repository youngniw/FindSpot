package com.example.findspot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static com.example.findspot.SelectWhomActivity.btn_selwhom_next;
import static com.example.findspot.SelectWhomActivity.selectedGroup;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder>{
    private ArrayList<GroupInfo> groupList = null;
    private boolean isSelectWhomActivity = true;

    public GroupRecyclerAdapter(ArrayList<GroupInfo> groupList, boolean isSelectWhomActivity) {
        this.groupList = groupList;
        this.isSelectWhomActivity = isSelectWhomActivity;
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
        holder.grouprow_title.setText(groupList.get(position).getGroupName());       //해당 위치를 인덱스로 한 리스트에 저장된 그룹이름을 텍스트로 설정함
        holder.grouprow_users.setText(String.join(", ", groupList.get(position).getGroupUsers()));      //사용자 이름을 ,을 구분자로 사용하여 출력함(ex. 영은, 소은)
    }

    @Override
    public int getItemCount() {
        //Adapter가 관리하는 전체 데이터 개수 반환
        return groupList.size();        //TODO: 문제 발생(NullPointer참조 문제)
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView grouprow_title;
        TextView grouprow_users;

        GroupViewHolder(View itemView) {
            super(itemView);
            grouprow_title = itemView.findViewById(R.id.grouprow_title);
            grouprow_users = itemView.findViewById(R.id.grouprow_users);

            itemView.setOnClickListener(new View.OnClickListener() {        //항목을 클릭했을 때
                @Override
                public void onClick(View v) {
                    if (isSelectWhomActivity) {     //SelectWhomActivity에서 그룹의 항목을 클릭할 시
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            selectedGroup.setGroupName(groupList.get(pos).getGroupName());      //그룹이름 수정
                            selectedGroup.setGHostName(groupList.get(pos).getGHostName());      //그룹방장이름 수정
                            selectedGroup.setGroupUsers(groupList.get(pos).getGroupUsers());    //그룹에 속한 사용자 목록 수정

                            btn_selwhom_next.setEnabled(true);      //그룹 목록에서의 항목을 선택시 활성화되어야 함
                            //TODO: 항목선택 시 배경색을 바꾸게 해야함
                        }
                    }
                }
            });
        }
    }
}
