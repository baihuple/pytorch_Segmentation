package com.example.driverapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class SearchAdapter extends BaseAdapter {
    private List<StationInfo> list;
    private LayoutInflater layoutInflater;
    private Context context;

    // 构造函数
    public SearchAdapter(List<StationInfo> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public StationInfo getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if(view == null){
            // view 为空，绑定布局
            view = layoutInflater.inflate(R.layout.repl_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 获取布局中的组件
            TextView stationname = view.findViewById(R.id.search_stationname);
            TextView stationaddr = view.findViewById(R.id.search_stationaddr);
            TextView adminname = view.findViewById(R.id.search_adminname);
            TextView admintel = view.findViewById(R.id.search_admintel);
            TextView stationstatus = view.findViewById(R.id.search_status);

            // 绑定
            viewHolder.stationname = stationname;
            viewHolder.stationaddr = stationaddr;
            viewHolder.adminname = adminname;
            viewHolder.admintel = admintel;
            viewHolder.stationstatus = stationstatus;

            // 保存标签
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 从列表中获取具体数据，并且填充
        StationInfo stationInfo = list.get(i);

        viewHolder.stationname.setText(stationInfo.getStationname());
        viewHolder.stationaddr.setText(stationInfo.getStationaddr());
        viewHolder.adminname.setText(stationInfo.getAdminName());
        viewHolder.admintel.setText(stationInfo.getAdminTel());
        viewHolder.stationstatus.setText(stationInfo.getStationstate());

        return view;
    }


    class ViewHolder{
        TextView stationname;
        TextView stationaddr;
        TextView adminname;
        TextView admintel;
        TextView stationstatus;
    }

}
