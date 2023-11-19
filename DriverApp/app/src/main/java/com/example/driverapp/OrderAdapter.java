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

import org.w3c.dom.Text;

import java.util.List;

public class OrderAdapter extends BaseAdapter {

    private List<Order> list;
    private LayoutInflater layoutInflater;
    private Context context;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20)).build();;

    // 构造函数
    public OrderAdapter(List<Order> list, Context context) {
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Order getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;

        // view 为空，绑定布局
        if(view == null){

            // 绑定 view 和 order_item 子布局
            view = layoutInflater.inflate(R.layout.order_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 获取布局中的组件
            TextView stationName = view.findViewById(R.id.order_station_name);
            TextView comName = view.findViewById(R.id.order_comName);
            TextView num = view.findViewById(R.id.order_num);
            TextView sumPrice = view.findViewById(R.id.order_sumprice);
            TextView ordertime = view.findViewById(R.id.order_time);
            TextView orderid = view.findViewById(R.id.order_id);
            ImageView orderimage = view.findViewById(R.id.order_image);

            // 将组件与 viewHolder 中的成员绑定
            viewHolder.stationName = stationName;
            viewHolder.comName = comName;
            viewHolder.num = num;
            viewHolder.sumPrice = sumPrice;
            viewHolder.ordertime = ordertime;
            viewHolder.orderId = orderid;
            viewHolder.imageView = orderimage;

            // 保存标签
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 从列表获取据具体数据，填充
        Order order = list.get(i);

        viewHolder.stationName.setText(order.getStationname());
        viewHolder.comName.setText(order.getComname());
        viewHolder.num.setText(order.getNum());
        viewHolder.sumPrice.setText(order.getSumPrice());
        viewHolder.ordertime.setText(order.getTime());
        viewHolder.orderId.setText(order.getOrderid());

        /** 判断当前 holder 的 tag 避免图片重复 [ 非常重要，你肯定会回来看这个的 ] **/
        ImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String uri = getItem(i).getOrderimage();

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
        TextView stationName;
        TextView comName;
        TextView num;
        TextView sumPrice;
        TextView ordertime;
        TextView orderId;
        ImageView imageView;
    }

}
