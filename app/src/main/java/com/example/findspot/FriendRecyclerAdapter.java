package com.example.findspot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendRecyclerAdapter extends RecyclerView.Adapter<FriendRecyclerAdapter.FriendViewHolder> {
    private ArrayList<String> friendList = null;

    public FriendRecyclerAdapter(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //전개자(Inflater)를 통해 얻은 참조 객체를 통해 뷰홀더 객체 생성
        View view = inflater.inflate(R.layout.friendrow, parent, false);
        FriendViewHolder viewHolder = new FriendViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.friendrow_title.setText(friendList.get(position));       //리스트에 저장된 값들을 각각의 위치에 항목으로 보이게 함
    }

    @Override
    public int getItemCount() {
        //Adapter가 관리하는 전체 데이터 개수 반환
        return friendList.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendrow_title;

        FriendViewHolder(View itemView) {
            super(itemView);
            friendrow_title = itemView.findViewById(R.id.friendrow_title);
        }
    }
}