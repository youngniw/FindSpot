package com.example.findspot.adapter;

import android.annotation.SuppressLint;
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

import com.example.findspot.CandidateTimePosition;
import com.example.findspot.data.PositionItemInfo;
import com.example.findspot.R;
import com.example.findspot.ResultDistancePosition;
import com.example.findspot.data.RouteInfo;

import java.util.ArrayList;

public class PositionPagerAdapter extends PagerAdapter {
    Context context;
    boolean isHistory, isGroup, isStandardTime;
    ArrayList<PositionItemInfo> userList;
    ArrayList<CandidateTimePosition> tPositions = null;
    ArrayList<ResultDistancePosition> dPositions = null;

    //시간 기준 생성자
    public PositionPagerAdapter(Context context, boolean isHistory, ArrayList<PositionItemInfo> userList, ArrayList<CandidateTimePosition> resultTPositions) {
        isStandardTime = true;

        this.context = context;
        this.isHistory = isHistory;
        this.userList = userList;
        isGroup = !userList.get(0).getUserName().equals("");
        tPositions = resultTPositions;
    }

    //거리 기준 생성자
    public PositionPagerAdapter(Context context, ArrayList<PositionItemInfo> userList, ArrayList<ResultDistancePosition> resultDPositions){
        isStandardTime = false;

        this.context = context;
        this.userList = userList;
        isGroup = !userList.get(0).getUserName().equals("");
        dPositions = resultDPositions;
    }

    @Override
    public int getCount() {
        int size;
        if (isStandardTime)
            size = Math.min(tPositions.size(), 5);
        else
            size = dPositions.size();
        return size;
    }

    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.viewpager_childview, container, false);

        TextView resultStationTV = view.findViewById(R.id.tv_stationName);      //결과 장소 TextView
        TextView gapTV = view.findViewById(R.id.tv_Gap);        //소요시간/거리 차 TextView
        LinearLayout ll_container = (LinearLayout)view.findViewById(R.id.ll_container);

        if (isStandardTime) {
            resultStationTV.setText("\uD83D\uDCCD "+tPositions.get(position).getStationName()+"역");
            gapTV.setText("오차시간: "+tPositions.get(position).getTimeGap()+"분");

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
                if (isGroup)
                    userNameTV.setText(userList.get(i).getUserName()+" : ");
                else
                    userNameTV.setText("사람"+(i+1)+ " : ");
                userNameTV.setLayoutParams(tv_params);

                //사용자 소요시간 TextView
                TextView userTimeTV = new TextView(context);
                userTimeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                userTimeTV.setTextColor(Color.BLACK);
                userTimeTV.setTypeface(userTimeTV.getTypeface(), Typeface.BOLD);
                userTimeTV.setText(tPositions.get(position).getRouteInfo().get(i).getTotalTime()+" MIN");
                userTimeTV.setLayoutParams(tv_params);

                TextView userPathTV = new TextView(context);
                if (!isHistory) {
                    //사용자 길찾기 경로
                    userPathTV = new TextView(context);
                    userPathTV.setLineSpacing(0f, 1.3f);
                    userPathTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    userPathTV.setTextColor(Color.BLACK);
                    StringBuilder path= new StringBuilder();
                    ArrayList<RouteInfo.SubPath> tmpPath = tPositions.get(position).getRouteInfo().get(i).getPaths();
                    for(int j=0; j<tmpPath.size(); j++) {
                        if (tmpPath.get(j).getTrafficType() == 1) {     //지하철일 경우
                            if (j>1 && tmpPath.get(j-2).getTrafficType()==1 && tmpPath.get(j-1).getTrafficType()==3)     //지하철 -> 지하철(현재)
                                path.append("\uD83D\uDE8A").append(tmpPath.get(j).getBoardName()).append("(").append(tmpPath.get(j-2).getSubwayName()).append(" \u21A6 ").append(tmpPath.get(j).getSubwayName()).append(") \u27AD ");

                            else
                                path.append("\uD83D\uDE8A").append(tmpPath.get(j).getBoardName()).append("(").append(tmpPath.get(j).getSubwayName()).append(") \u27AD ");
                        }
                        else if (tmpPath.get(j).getTrafficType() == 2) {    //버스일 경우
                            if (j>1 && tmpPath.get(j-2).getTrafficType()==1 && tmpPath.get(j-1).getTrafficType()==3)  //지하철 -> 버스(현재)
                                path.append("\uD83D\uDE8A").append(tmpPath.get(j-2).getArriveName()).append("(").append(tmpPath.get(j-2).getSubwayName()).append(") \u27AD ");

                            path.append("\uD83D\uDE8C").append(tmpPath.get(j).getBoardName()).append("(").append(tmpPath.get(j).getBusName()).append(") \u21A6 ");
                            path.append(tmpPath.get(j).getArriveName()).append("(하차) \u27AD ");
                        }
                    }
                    path.append(tPositions.get(position).getStationName()).append(" 도착");
                    userPathTV.setText(path.toString().replace(" ", "\u00A0"));
                    userPathTV.setLayoutParams(tv_params);
                }

                //결과로 나와야 될것
                userResultLayout.addView(userNameTV);
                userResultLayout.addView(userTimeTV);
                userLayout.addView(userResultLayout);
                if (!isHistory)
                    userLayout.addView(userPathTV);
                ll_container.addView(userLayout);
            }
        }
        else {
            resultStationTV.setText("\uD83D\uDCCD "+dPositions.get(position).getStationName()+"역");
            gapTV.setText("오차거리: "+dPositions.get(position).getDistanceGap()+"km");

            //각 사용자별로 이동거리 출력
            for (int i=0; i<dPositions.get(position).getSize(); i++) {
                LinearLayout userLayout = new LinearLayout(context);
                userLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams ll_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll_params.topMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
                ll_params.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
                userLayout.setLayoutParams(ll_params);

                //사용자 닉네임과 이동거리를 담은 LinearLayout
                LinearLayout userResultLayout = new LinearLayout(context);
                userResultLayout.setOrientation(LinearLayout.HORIZONTAL);
                userResultLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                //TextView를 위한 Params
                LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                //사용자 닉네임 TextView
                TextView userNameTV = new TextView(context);
                userNameTV.setTextColor(Color.BLACK);
                if (isGroup)
                    userNameTV.setText(userList.get(i).getUserName()+" : ");
                else
                    userNameTV.setText("사람"+(i+1)+ " : ");
                userNameTV.setLayoutParams(tv_params);

                //사용자 이동거리 TextView
                TextView userDistanceTV = new TextView(context);
                userDistanceTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                userDistanceTV.setTextColor(Color.BLACK);
                userDistanceTV.setTypeface(userDistanceTV.getTypeface(), Typeface.BOLD);
                userDistanceTV.setText(dPositions.get(position).getDistanceList().get(i)+" KM");
                userDistanceTV.setLayoutParams(tv_params);

                //결과로 나와야 될것
                userResultLayout.addView(userNameTV);
                userResultLayout.addView(userDistanceTV);
                userLayout.addView(userResultLayout);
                ll_container.addView(userLayout);
            }
        }
        container.addView(view);
        return view;
    }
}
