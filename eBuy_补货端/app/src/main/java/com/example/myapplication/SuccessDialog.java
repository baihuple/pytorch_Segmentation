package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SuccessDialog extends Dialog {

    public SuccessDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{

        private View mLayout;

        private TextView mMessage;                  /** 提示内容 **/
        private Button mButton;                     /** 按钮 **/

        private View.OnClickListener mButtonClickListener;

        private SuccessDialog mDialog;

        public Builder(Context context){
            mDialog = new SuccessDialog(context, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            /** 加载自定义布局文件 **/
            mLayout = inflater.inflate(R.layout.alertdialog_success,null,false);
            /** 添加布局文件到 Dialog **/
            mDialog.addContentView(mLayout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            mMessage = mLayout.findViewById(R.id.AD_success_text);
            mButton = mLayout.findViewById(R.id.BT_success);
        }

        /** 设置内容 **/
        public Builder setMessage(@NonNull String message){
            mMessage.setText(message);
            return this;
        }

        /** 设置按钮文字和监听 **/
        public Builder setButton(@NonNull String text,View.OnClickListener listener){
            mButton.setText(text);
            mButtonClickListener = listener;
            return this;
        }

        public SuccessDialog create(){
            mButton.setOnClickListener(view ->{
                mDialog.dismiss();
                mButtonClickListener.onClick(view);
            });
            mDialog.setContentView(mLayout);
            mDialog.setCancelable(true);                    /** 用户可以点击后退键关闭 Dialog **/
            mDialog.setCanceledOnTouchOutside(false);       /** 用户不可以点击外部关闭 Dialog **/
            return mDialog;
        }

    }

}
