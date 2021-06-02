package com.example.findspot.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.findspot.DaumWebViewActivity;
import com.example.findspot.data.PositionItemInfo;
import com.example.findspot.R;

import java.util.ArrayList;

import static com.example.findspot.ChoiceGPSGroupActivity.list_group;
import static com.example.findspot.SelectWhomActivity.list_g_users;

//그룹에서 각각의 사용자 위치 리스트를 UI로 나타내기 위한 어댑터
public class PositionGroupListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int layout;
    ArrayList<PositionItemInfo> src_group;      //그룹에 속한 사용자들의 최종적 위치가 저장됨(사용자 정보)

    public PositionGroupListAdapter(Context context, int layout, ArrayList<PositionItemInfo> src_group) {
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
    public PositionItemInfo getItem(int pos) { return this.src_group.get(pos); }

    //위치에 대응하는 아이템 아이디 반환
    @Override
    public long getItemId(int pos) { return pos; }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        //처음일 경우, View 생성
        if(convertView == null) convertView = inflater.inflate(layout, parent, false);

        TextView tv_indexName = (TextView) convertView.findViewById(R.id.positionrow_g_indexName);
        tv_indexName.setText((pos + 1) + ". " + getItem(pos).getUserName());

        EditText et_inputPosition = (EditText)convertView.findViewById(R.id.positionrow_g_et_inputPosition);

        //위치를 선택해 저장했다면, 해당 내용이 리스트뷰에 보여
        for (PositionItemInfo pi : src_group) {
            if (pi.getUserName().equals(getItem(pos).getUserName())) {
                et_inputPosition.setText(pi.getRoadName());
                break;
            }
        }

        //유저 정보에서 저장된 위치 불러오기
        final View finalConvertView = convertView;
        Button bt_applyPosition = (Button) convertView.findViewById(R.id.positionrow_g_applyPosition);
        bt_applyPosition.setOnClickListener(v -> {
            //PositionItem 생성(저장된 정보로 설정함)
            PositionItemInfo item = new PositionItemInfo(list_g_users.get(pos).getUserName(), list_g_users.get(pos).getRoadName(), list_g_users.get(pos).getLatitude(), list_g_users.get(pos).getLongitude());

            if (list_group.get(pos).getUserName().equals(list_g_users.get(pos).getUserName()))
                list_group.set(pos, item);

            EditText et_group_position = (EditText) finalConvertView.findViewById(R.id.positionrow_g_et_inputPosition);
            et_group_position.setText(getItem(pos).getRoadName());

            notifyDataSetChanged();     //리스트 갱신
        });

        //도로명 검색하기
        et_inputPosition.setOnClickListener(v -> {
            //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
            Intent it_address = new Intent(context, DaumWebViewActivity.class);         //확인해봐야 함
            it_address.putExtra("name", getItem(pos).getUserName());
            ((Activity)context).startActivityForResult(it_address, 101);
        });

        return convertView;
    }
}
