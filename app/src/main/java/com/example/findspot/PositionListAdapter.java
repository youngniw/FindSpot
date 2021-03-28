package com.example.findspot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

//사용자 위치 리스트를 UI로 나타내기 위한 어댑터
public class PositionListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int layout;
    ArrayList<PositionItem> src;

    public PositionListAdapter(Context context, int layout, ArrayList<PositionItem> src) {
        this.context = context;
        this.layout = layout;
        this.src = src;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //항목 개수 반환
    @Override
    public int getCount() { return this.src.size(); }

    //위치에 대응하는 아이템 반환
    @Override
    public PositionItem getItem(int pos) { return this.src.get(pos); }

    //위치에 대응하는 아이템 아이디 반환
    //*******************************
    @Override
    public long getItemId(int pos) { return pos; }

    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        //처음일 경우, View 생성
        if(convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }

        //도로명주소 TextView 값 지정
        TextView tv_name = (TextView)convertView.findViewById(R.id.positionrow_text);
        tv_name.setText(src.get(pos).getName());

        //선택된 항목 삭제하기 (이벤트)
        TextView tv_delete = (TextView)convertView.findViewById(R.id.positionrow_delete);
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != ListView.INVALID_POSITION) {
                    src.remove(pos);        //해당 항목 삭제
                    notifyDataSetChanged(); //리스트 갱신
                }
            }
        });
        return convertView;
    }

}