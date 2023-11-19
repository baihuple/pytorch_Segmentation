package com.example.driverapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.List;

public class SellProductAdapter extends BaseAdapter {

    private List<SellProdudct> list;
    private LayoutInflater layoutInflater;
    private Context context;
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20)).build();;

    // 构造函数
    public SellProductAdapter(List<SellProdudct> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public SellProdudct getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if(view==null){

            // 绑定 view 和 Item 布局
            view = layoutInflater.inflate(R.layout.seller_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 从绑定好的 item 获取对应的组件
            ImageView imageView = view.findViewById(R.id.seller_image);
            TextView pro_title = view.findViewById(R.id.seller_pro_title);
            TextView pro_number = view.findViewById(R.id.seller_num);
            TextView date = view.findViewById(R.id.seller_time);
            TextView order_id = view.findViewById(R.id.seller_id);
            TextView sumprice = view.findViewById(R.id.seller_sumprice);

            // 传递给 viewholder 中
            viewHolder.imageView = imageView;
            viewHolder.pro_title = pro_title;
            viewHolder.pro_number = pro_number;
            viewHolder.date = date;
            viewHolder.order_id = order_id;
            viewHolder.sumprice = sumprice;

            // 保存到标签，为复用提供数据
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 填充 viewholder
        SellProdudct sellProdudct = list.get(i);

        viewHolder.pro_title.setText(sellProdudct.getPro_title());
        viewHolder.pro_number.setText(sellProdudct.getPro_num());
        viewHolder.date.setText(sellProdudct.getDate());
        viewHolder.order_id.setText(sellProdudct.getOrderid());
        viewHolder.sumprice.setText(sellProdudct.getSumprice());

        /** 判断当前 holder 的 tag 避免图片重复 [ 非常重要，你肯定会回来看这个的 ] **/
        ImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String uri = getItem(i).getImageURI();

        if(!uri.equals(tag) && tag!=null){
            // imageView.setImageResource(R.drawable.loading);
            ImageLoader.getInstance().displayImage(uri,viewHolder.imageView,options);       // 执行图片加载动作
            imageView.setTag(null);
        }else{
            imageView.setTag(uri);
            // imageView.setImageResource(R.drawable.loading);
            /** 设置标志位 **/
            ImageLoader.getInstance().displayImage(uri,viewHolder.imageView,options);       // 执行图片加载动作
        }


        return view;
    }

    class ViewHolder{
        ImageView imageView;
        TextView pro_title;
        TextView pro_number;
        TextView date;
        TextView order_id;
        TextView sumprice;
    }

}
