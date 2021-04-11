package com.example.findspot;

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

import java.util.ArrayList;

import static com.example.findspot.ChoiceGPSGroupActivity.list_group;

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
    @Override
    public long getItemId(int pos) { return pos; }

    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        //처음일 경우, View 생성
        if(convertView == null) convertView = inflater.inflate(layout, parent, false);

        TextView tv_indexName = (TextView) convertView.findViewById(R.id.positionrow_g_indexName);
        tv_indexName.setText(String.valueOf(pos+1)+". "+getItem(pos).getUserName());

        EditText et_inputPosition = (EditText)convertView.findViewById(R.id.positionrow_g_et_inputPosition);

        //위치를 선택해 저장했다면, 해당 내용이 리스트뷰에 보여
        for (int i = 0 ; i < list_group.size(); i++) {
            if (list_group.get(i).getUserName().equals(getItem(pos).getUserName())) {
                et_inputPosition.setText(list_group.get(i).getRoadName());
                break;
            }
        }

        //유저 정보에서 저장된 위치 불러오기
        final View finalConvertView = convertView;
        Button bt_applyPosition = (Button) convertView.findViewById(R.id.positionrow_g_applyPosition);
        bt_applyPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PositionItem 생성(저장된 정보로 설정함)
                PositionItem item = new PositionItem(getItem(pos).getUserName(), getItem(pos).getRoadName(), getItem(pos).getLatitude(), getItem(pos).getLongitude());

                //이미 한번 위치를 추가한 적이 있다면 바꾸고, 아니라면 위치 결과들을 모아놓은 list_group에 추가함
                boolean ischange_flag = false;
                for (int i = 0 ; i < list_group.size(); i++) {
                    if (list_group.get(i).getUserName().equals(getItem(pos).getUserName())) {
                        list_group.set(i, item);
                        ischange_flag = true;
                        break;
                    }
                }
                if (!ischange_flag) list_group.add(item);

                EditText et_group_position = (EditText) finalConvertView.findViewById(R.id.positionrow_g_et_inputPosition);
                et_group_position.setText(getItem(pos).getRoadName());

                notifyDataSetChanged();     //리스트 갱신
            }
        });

        //도로명 검색하기
        et_inputPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //도로명주소 API 오픈 (DaumWebViewActivity.java 실행)
                Intent it_address = new Intent(context, DaumWebViewActivity.class);         //확인해봐야 함
                it_address.putExtra("name", getItem(pos).getUserName());
                ((Activity)context).startActivityForResult(it_address, 101);
            }
        });

        return convertView;
    }
}