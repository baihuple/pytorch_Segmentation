package com.example.driverapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class Ad_detailAdapter extends BaseAdapter {
    private List<AdDetail> list;
    private LayoutInflater layoutInflater;
    private Context context;

    public Ad_detailAdapter(List<AdDetail> list,Context context){
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public AdDetail getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;

        if(view == null){

            view = layoutInflater.inflate(R.layout.ad_detail_item,viewGroup,false);
            viewHolder = new ViewHolder();

            TextView playdate = view.findViewById(R.id.play_date);
            TextView playduration = view.findViewById(R.id.play_time);
            TextView pay = view.findViewById(R.id.ad_item_pay);

            viewHolder.playdate = playdate;
            viewHolder.playduration = playduration;
            viewHolder.pay = pay;

            view.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        AdDetail adDetail = list.get(i);

        viewHolder.playdate.setText(adDetail.getDate());
        viewHolder.playduration.setText(adDetail.getTime());
        viewHolder.pay.setText(adDetail.getPrice());

        return view;
    }

    class ViewHolder{
        TextView playdate;
        TextView playduration;
        TextView pay;
    }

}
