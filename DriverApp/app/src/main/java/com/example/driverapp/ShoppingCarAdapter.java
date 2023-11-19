package com.example.driverapp;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.List;

public class ShoppingCarAdapter extends BaseAdapter {

    private List<Product> list;
    private LayoutInflater layoutInflater;
    private Context context;
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(20)).build();;

    /** 减法 **/
    private MyClickListener1 mListener1;
    /** 加法 **/
    private MyClickListener2 mListener2;

    // 构造函数
    public ShoppingCarAdapter(List<Product> list, Context context,MyClickListener1 listener1 ,MyClickListener2 listener2) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.mListener1 = listener1;
        this.mListener2 = listener2;
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

        // view 为空，绑定布局
        if(view == null){

            // 绑定 view 和 shoppingcar_item 子布局
            view = layoutInflater.inflate(R.layout.shoppingcar_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 获取布局中的组件
            ImageView pro_image = view.findViewById(R.id.shopping_car_proimage);
            TextView pro_title = view.findViewById(R.id.shopping_car_proname);
            TextView pro_price = view.findViewById(R.id.shopping_car_proprice);
            TextView pro_num = view.findViewById(R.id.shopping_car_pronum);
            ImageView pro_minus = view.findViewById(R.id.shopping_car_minus);
            ImageView pro_plus = view.findViewById(R.id.shopping_car_plus);

            // 将组建与viewHolder 中的成员绑定
            viewHolder.imageView = pro_image;
            viewHolder.pro_title = pro_title;
            viewHolder.pro_price = pro_price;
            viewHolder.pro_num = pro_num;
            viewHolder.pro_minus = pro_minus;
            viewHolder.pro_plus = pro_plus;

            // 保存标签
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 从列表中获取具体数据，并且填充
        Product product = list.get(i);

        viewHolder.pro_title.setText(product.getComname());
        viewHolder.pro_price.setText(product.getPrice());
        viewHolder.pro_num.setText(product.getNumber());            // 传递的数据，是从shoppingcar+stationid的缓存中的num获取的

        // 设置按钮减
        viewHolder.pro_minus.setOnClickListener(mListener1);
        viewHolder.pro_minus.setTag(i);

        // 设置按纽加
        viewHolder.pro_plus.setOnClickListener(mListener2);
        viewHolder.pro_plus.setTag(i);

        // 填充图像数据
        ImageView imageView = viewHolder.imageView;
        final String tag = (String) imageView.getTag();
        final String uri = getItem(i).getImage_uri();

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
        // 设置按钮
        ImageView pro_minus;
        ImageView pro_plus;

    }

    // 抽象类实现 -- 减法
    public static abstract class MyClickListener1 implements View.OnClickListener{
        public void onClick(View v){
            try {
                myOnClick((Integer)v.getTag(),v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public abstract void myOnClick(int position,View v) throws JSONException;
    }


    // 抽象类实现 -- 加法
    public static abstract class MyClickListener2 implements View.OnClickListener{
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
