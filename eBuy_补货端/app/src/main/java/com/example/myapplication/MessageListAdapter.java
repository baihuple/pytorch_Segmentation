package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {

    private List<Msg> list;
    private LayoutInflater layoutInflater;
    private Context context;

    // 构造函数


    public MessageListAdapter(List<Msg> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Msg getItem(int i) {
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

            // 绑定 view 和 Item 布局
            view = layoutInflater.inflate(R.layout.message_list_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 从绑定好的 item 获取对应的组件
            TextView driver_id = view.findViewById(R.id.message_driverid);
            TextView time = view.findViewById(R.id.message_time);
            TextView message = view.findViewById(R.id.message_content);

            // 传递给 viewHolder 中
            viewHolder.driver_id = driver_id;
            viewHolder.time = time;
            viewHolder.message = message;

            // 保存到标签，为复用提供数据
            view.setTag((viewHolder));
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 填充 ViewHolder 中的数据
        Msg msg = list.get(i);

        viewHolder.driver_id.setText(msg.getDriver_id());
        viewHolder.time.setText(msg.getNow_time());
        viewHolder.message.setText(msg.getContent());

        return view;
    }

    class ViewHolder{
        TextView driver_id;
        TextView time;
        TextView message;
    }

}
