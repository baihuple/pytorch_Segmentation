package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.List;

public class ProductAdapter extends BaseAdapter {

    // 初始化 options
    private List<Product> list;
    private LayoutInflater layoutInflater;
    private Context context;
    private MyClickListener mListener;
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20)).build();;

    public ProductAdapter(Context context,List<Product> list,MyClickListener listener){
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Product getItem(int i) {
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
            view = layoutInflater.inflate(R.layout.product_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 从绑定好的 item 获取对应的组件
            TextView pro_title = view.findViewById(R.id.product_title);
            TextView pro_price = view.findViewById(R.id.product_price);
            TextView pro_number = view.findViewById(R.id.product_number);
            ImageView pro_image = view.findViewById(R.id.prodcut_image);
            Button pro_button = view.findViewById(R.id.product_button);

            // 传递给 viewHolder 中
            viewHolder.pro_title = pro_title;
            viewHolder.pro_price = pro_price;
            viewHolder.pro_number = pro_number;
            viewHolder.imageView = pro_image;
            viewHolder.pro_button = pro_button;

            // 保存到标签，为复用提供数据
            view.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) view.getTag();
        }

        // 填充 ViewHolder 中的数据
        // 1) 实例化对象
        Product product = list.get(i);

        viewHolder.pro_title.setText(product.getComname());
        viewHolder.pro_price.setText("￥"+product.getPrice());
        viewHolder.pro_number.setText(product.getNumber());

        viewHolder.pro_button.setOnClickListener(mListener);
        viewHolder.pro_button.setTag(i);

        /** 判断当前 holder 的 tag 避免图片重复 [ 非常重要，你肯定会回来看这个的 ] **/
        ImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String uri = getItem(i).getImage_uri();

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

        return view;                                                                        // 传递值一定不能为空
    }

    // 内部类
    class ViewHolder{
        ImageView imageView;
        TextView pro_title;
        TextView pro_price;
        TextView pro_number;
        Button pro_button;

    }



    // 抽象类实现
    public static abstract class MyClickListener implements View.OnClickListener{
        public void onClick(View v){
            try {
                myOnClick((Integer)v.getTag(),v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public abstract void myOnClick(int position,View v) throws JSONException;
    }


}