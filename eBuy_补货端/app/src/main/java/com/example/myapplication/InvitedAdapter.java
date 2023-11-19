package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class InvitedAdapter extends BaseAdapter {
    private List<Invited> list;
    private LayoutInflater layoutInflater;
    private Context context;

    // 构造函数


    public InvitedAdapter(List<Invited> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Invited getItem(int i) {
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
            view = layoutInflater.inflate(R.layout.invited_item,viewGroup,false);
            viewHolder = new ViewHolder();

            // 从绑定好的 item 获取对应的组件
            TextView time = view.findViewById(R.id.invite_list_time);
            TextView content = view.findViewById(R.id.invited_content);

            // 传递给 viewHolder 中
            viewHolder.time = time;
            viewHolder.content = content;

            // 保存到标签，为复用提供数据
            view.setTag((viewHolder));
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        // 填充 ViewHolder 中的数据
        Invited invited = list.get(i);

        viewHolder.time.setText(invited.getTime());
        viewHolder.content.setText(invited.getContent());

        return view;
    }

    class ViewHolder{
        TextView time;
        TextView content;
    }

}
