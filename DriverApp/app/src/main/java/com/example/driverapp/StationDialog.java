package com.example.driverapp;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class StationDialog{

    private Dialog mDialog;         // 对话框对象
    private View mView;             // 视图对象
    private StationInfo mStationInfo;   // 站点信息
    private StationCallBack mCallback;  // 回调监听器

        // 构造函数，根据传参构造试图
    public StationDialog(Context context,StationInfo stationInfo,StationCallBack callBack){
        mStationInfo = stationInfo;
        mCallback = callBack;

        // 根据布局文件生成对象
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_station2,null);

        // 创建一个指定风格的对话框对象
        mDialog = new Dialog(context,R.style.CustomDialog);

        // 获取对应组件
        TextView stationid = mView.findViewById(R.id.station_id_TV);
        TextView stationname = mView.findViewById(R.id.station_name_TV);
        TextView stationaddr = mView.findViewById(R.id.station_addr_TV);
        TextView adminname = mView.findViewById(R.id.admin_name_TV);
        TextView adminTel = mView.findViewById(R.id.admin_Tel_TV);
        TextView stationstate = mView.findViewById(R.id.Station_state_TV);
        Button stationDetail = mView.findViewById(R.id.station_detail_BT);
        Button close = mView.findViewById(R.id.close_dialog);


        // 将传入的 StationInfo 传入对应组件中
        stationid.setText(mStationInfo.getStationid());
        stationname.setText(mStationInfo.getStationname());
        stationaddr.setText(mStationInfo.getStationaddr());
        adminname.setText(mStationInfo.getAdminName());
        adminTel.setText(mStationInfo.getAdminTel());
        stationstate.setText(mStationInfo.getStationstate());

        // 设置点就按钮的回调函数
        stationDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mCallback.onDetail(mStationInfo);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


    /** 关闭对话框 **/
    public void dismiss(){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }


    /** 显示对话框 **/
    public void show(){
        // 设置对话框窗口的内容视图
        mDialog.getWindow().setContentView(mView);
        Window window = mDialog.getWindow();
        if (window != null ) {
            //set animation
            window.setWindowAnimations(R.style.right_in_right_out_anim);
        }
        mDialog.show();
    }

    /** 判断对话框是否显示 **/
    public boolean isShowding(){
        if (mDialog != null){
            return mDialog.isShowing();
        }else {
            return false;
        }

    }


    // 定义一个站点动作监听器
    public interface StationCallBack{
        void onDetail(StationInfo stationInfo);     // 跳转详情面
    }

}
