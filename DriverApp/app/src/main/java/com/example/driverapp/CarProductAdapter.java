package com.example.driverapp;

import android.content.Context;
import android.media.Image;
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

public class CarProductAdapter extends BaseAdapter {

    private List<CarProduct> list;
    private LayoutInflater layoutInflater;
    private Context context;
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20)).build();

    // 构造函数
    public CarProductAdapter(List<CarProduct> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CarProduct getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;

        // 如果view为空，绑定布局
        if(view == null){

            // 绑定布局
            view = layoutInflater.inflate(R.layout.carproduct_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 获取布局中的文件
            ImageView imageView = view.findViewById(R.id.car_prodcut_image);
            TextView pro_title = view.findViewById(R.id.car_product_title);
            TextView pro_price = view.findViewById(R.id.car_product_price);
            TextView pro_num = view.findViewById(R.id.car_product_num);
            TextView pro_status = view.findViewById(R.id.car_product_status);
            TextView pro_shelve = view.findViewById(R.id.car_shelveid);

            // 将组件与 viewHolder 中的成员绑定
            viewHolder.imageView = imageView;
            viewHolder.pro_title = pro_title;
            viewHolder.pro_price = pro_price;
            viewHolder.pro_num = pro_num;
            viewHolder.pro_status = pro_status;
            viewHolder.pro_shelve = pro_shelve;

            // 保存标签
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 从列表中获取具体数据，并且填充
        CarProduct carProduct = list.get(i);

        viewHolder.pro_title.setText(carProduct.getComName());
        viewHolder.pro_price.setText(carProduct.getPrice());
        viewHolder.pro_num.setText(carProduct.getNumber());
        viewHolder.pro_status.setText(carProduct.getSellstate());
        viewHolder.pro_shelve.setText(carProduct.getShelveid());

        // 填充图像数据
        ImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String uri = getItem(i).getImageURL();

        if(!uri.equals(tag) && tag != null){
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
        TextView pro_price;
        TextView pro_num;
        TextView pro_status;
        TextView pro_shelve;
    }

}
