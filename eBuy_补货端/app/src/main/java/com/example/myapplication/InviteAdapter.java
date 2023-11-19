package com.example.myapplication;

import android.content.Context;
import android.media.Image;
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

public class InviteAdapter extends BaseAdapter {

    private List<InviteProduct> list;
    private LayoutInflater layoutInflater;
    private Context context;
    private MyClickListener mListener;


    public InviteAdapter(Context context,List<InviteProduct> list,MyClickListener listener){
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
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if(view==null){

            // 绑定 view 和 Item 布局
            view = layoutInflater.inflate(R.layout.invite_item,viewGroup,false);

            // 从绑定好的 item 获取对应的组件
            TextView invite_name = view.findViewById(R.id.invite_name);
            TextView invite_num = view.findViewById(R.id.invite_num);
            TextView invite_price = view.findViewById(R.id.invite_price);
            Button invite_remove = view.findViewById(R.id.invite_delete_BT);

            // 传递给 viewHolder 中
//            viewHolder.imageView = imageView;
            viewHolder.invite_name = invite_name;
            viewHolder.invite_num = invite_num;
            viewHolder.invite_price = invite_price;
            viewHolder.invite_remove = invite_remove;

            // 保存到标签，为复用提供数据
            view.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) view.getTag();
        }

        // 填充 ViewHolder 中的数据
        // 1) 实例化对象
        InviteProduct product = list.get(i);

        viewHolder.invite_name.setText(product.getInvite_name());
        viewHolder.invite_price.setText("￥"+product.getInvite_price());
        viewHolder.invite_num.setText(product.getInvite_num());

        viewHolder.invite_remove.setOnClickListener(mListener);
        viewHolder.invite_remove.setTag(i);
        // 执行图片加载动作

        return view;                                                                        // 传递值一定不能为空
    }

    // 内部类
    class ViewHolder{
        TextView invite_name;
        TextView invite_price;
        TextView invite_num;
        Button invite_remove;
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
