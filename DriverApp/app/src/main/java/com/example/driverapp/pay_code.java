package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class pay_code extends AppCompatActivity {

    public String price;
    public String stationid;
    TextView pay_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_code);

        // 接收总金额数据
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            price = bundle.getString("sumPrice");
            stationid = bundle.getString("stationid");
        }

        // 填充到指定位置
        pay_price = findViewById(R.id.pay_price);
        pay_price.setText("￥"+price);

        // 2s 弹出付款成功
        // 5s 钟自动返回主页面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myDialog_Successful("支付成功");
            }
        },3000);

    }


    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setIcon(R.drawable.successful);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // 5s 钟自动返回主页面
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 清空购物车
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        SharedPreferences.Editor edit = record.edit();
                        edit.remove("shopping_car"+stationid);
                        edit.commit();

                        finish();
                    }
                },1000);

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}