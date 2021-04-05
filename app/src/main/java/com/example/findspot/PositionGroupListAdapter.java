package com.example.findspot;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

//그룹에서 각각의 사용자 위치 리스트를 UI로 나타내기 위한 어댑터
public class PositionGroupListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int layout;
    ArrayList<PositionItem> src_group;

    public PositionGroupListAdapter(Context context, int layout, ArrayList<PositionItem> src_group) {
        this.context = context;
        this.layout = layout;
        this.src_group = src_group;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //항목 개수 반환
    @Override
    public int getCount() { return this.src_group.size(); }

    //위치에 대응하는 아이템 반환
    @Override
    public PositionItem getItem(int pos) { return this.src_group.get(pos); }

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

        //도로명 검색하기*********************************************(여기에 나중에 넘겨받은 값 setText로 보여줘야함)
        EditText et_inputPosition = (EditText)convertView.findViewById(R.id.positionrow_g_et_inputPosition);
        et_inputPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
                Intent it_address = new Intent(context, DaumWebViewActivity.class);         //확인해봐야 함
                startActivityForResult(it_address, 100);
            }
        });

        //유저 정보에서 저장된 위치 불러오기
        Button bt_applyPosition = (Button) convertView.findViewById(R.id.positionrow_g_applyPosition);
        bt_applyPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //**********************************************
                //*********************저장된 위치 불러오기(도로명)

                notifyDataSetChanged();     //리스트 갱신
            }
        });
        return convertView;
    }
}
