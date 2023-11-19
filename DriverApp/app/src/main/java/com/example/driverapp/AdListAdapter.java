package com.example.driverapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class AdListAdapter extends BaseAdapter {

    private List<Ad> list;
    private LayoutInflater layoutInflater;
    private Context context;

    // 构造函数
    public AdListAdapter(List<Ad> list,Context context){
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Ad getItem(int i) {
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

            // 绑定布局
            view = layoutInflater.inflate(R.layout.ad_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 获取布局中的文件
            TextView brand = view.findViewById(R.id.adBrand);
            TextView type = view.findViewById(R.id.ad_type);
            TextView id = view.findViewById(R.id.ad_id);
            TextView price = view.findViewById(R.id.ad_price);

            viewHolder.brand = brand;
            viewHolder.type = type;
            viewHolder.id = id;
            viewHolder.price = price;

            // 保存标签
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 从列表中获取具体数据并填充
        Ad ad = list.get(i);

        viewHolder.brand.setText(ad.getBrand());
        viewHolder.type.setText(ad.getType());
        viewHolder.id.setText(ad.getId());
        viewHolder.price.setText(ad.getPrice());

        return view;
    }

    class ViewHolder{
        TextView brand;
        TextView type;
        TextView id;
        TextView price;
    }

}
