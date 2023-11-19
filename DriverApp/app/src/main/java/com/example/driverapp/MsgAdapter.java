package com.example.driverapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.glassfish.grizzly.utils.Holder;
import org.w3c.dom.Text;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{

    private List<Msg> mMsgList;                     // 消息列表

    // 构造函数
    public MsgAdapter(List<Msg> msgList){
        this.mMsgList = msgList;
    }

    // 设置继承的 viewholder
    static class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout leftLayout;
        RelativeLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView date1,date2;

        public ViewHolder(@NonNull View view) {
            super(view);
            leftLayout = (RelativeLayout) view.findViewById(R.id.left_layout);
            rightLayout = (RelativeLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            date1 = (TextView) view.findViewById(R.id.msg_time1);
            date2 = (TextView) view.findViewById(R.id.msg_time2);
        }
    }


    // 该类用于创建 viewholder 实例
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);
        return new ViewHolder(view);
    }

    // 绑定列表项的视图持有者
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Msg msg = mMsgList.get(position);
        if(msg.getType() == Msg.TYPE_RECEIVED){         // 如果消息类型：接收 --- 显示左边布局
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.date2.setText(msg.getNow_time());
            holder.leftMsg.setText(msg.getContent());
        }else{
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.date1.setText(msg.getNow_time());
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }




}
