package com.example.findspot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class PositionPagerAdapter extends PagerAdapter {
    Context context;
    ArrayList<PositionItem> userList;
    boolean isGroup;
    ArrayList<CandidateTimePosition> tPositions;

    //생성자
    public PositionPagerAdapter(Context context, ArrayList<PositionItem> userList, ArrayList<CandidateTimePosition> resultTPositions) {
        this.context = context;
        this.userList = userList;
        isGroup = !userList.get(0).equals("");
        tPositions = resultTPositions;
    }

    @Override
    public int getCount() {
        int size = tPositions.size() < 5 ? tPositions.size() : 5;
        return size;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.viewpager_childview, container, false);

        //결과 장소 TextView
        TextView resultStationTV = view.findViewById(R.id.tv_stationName);
        resultStationTV.setText("\uD83D\uDCCD "+tPositions.get(position).getStationName()+"역");

        //소요시간 차 TextView
        TextView timeGapTV = view.findViewById(R.id.tv_timeGap);
        timeGapTV.setText("오차시간: "+tPositions.get(position).getTimeGap()+"분");

        LinearLayout ll_container = (LinearLayout)view.findViewById(R.id.ll_container);
        //각 사용자별로 소요시간 출력
        for (int i=0; i<tPositions.get(position).getSize(); i++) {
            LinearLayout userLayout = new LinearLayout(context);
            userLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ll_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll_params.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
            ll_params.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
            userLayout.setLayoutParams(ll_params);

            //사용자 닉네임과 소요시간을 담은 LinearLayout
            LinearLayout userResultLayout = new LinearLayout(context);
            userResultLayout.setOrientation(LinearLayout.HORIZONTAL);
            userResultLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            //TextView를 위한 Params
            LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //사용자 닉네임 TextView
            TextView userNameTV = new TextView(context);
            userNameTV.setTextColor(Color.BLACK);
            if (isGroup) userNameTV.setText(userList.get(i).getUserName()+" : ");
            else userNameTV.setText("사람"+(i+1)+ " : ");
            userNameTV.setLayoutParams(tv_params);

            //사용자 소요시간 TextView
            TextView userTimeTV = new TextView(context);
            userTimeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            userTimeTV.setTextColor(Color.BLACK);
            userTimeTV.setTypeface(userTimeTV.getTypeface(), Typeface.BOLD);
            userTimeTV.setText(tPositions.get(position).getRouteInfo().get(i).getTotalTime()+" MIN");
            userTimeTV.setLayoutParams(tv_params);

            //사용자 길찾기 경로
            TextView userPathTV = new TextView(context);
            userPathTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            userPathTV.setTextColor(Color.BLACK);
            String path="";
            ArrayList<RouteInfo.SubPath> tmpPath = tPositions.get(position).getRouteInfo().get(i).getPaths();
            for(int j=0; j<tPositions.get(position).getRouteInfo().get(i).getPaths().size(); j++) {
                if (tmpPath.get(j).getTrafficType() == 1) path += "\uD83D\uDE8A" + tmpPath.get(j).getBoardName() + "(" + tmpPath.get(j).getSubwayName() + ")-";
                else if (tmpPath.get(j).getTrafficType() == 2) path += "\uD83D\uDE8C" + tmpPath.get(j).getBoardName() + "(" + tmpPath.get(j).getBusName() + ")-";
            }
            path += tPositions.get(position).getStationName() + " 도착";
            userPathTV.setText(path);
            userPathTV.setLayoutParams(tv_params);

            //결과로 나와야 될것
            userResultLayout.addView(userNameTV);
            userResultLayout.addView(userTimeTV);
            userLayout.addView(userResultLayout);
            userLayout.addView(userPathTV);
            ll_container.addView(userLayout);
        }
        container.addView(view);
        return view;
    }
}
