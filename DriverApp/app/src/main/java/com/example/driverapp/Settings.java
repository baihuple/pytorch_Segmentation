package com.example.driverapp;

import static com.huawei.hms.feature.DynamicModuleInitializer.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class Settings extends AppCompatActivity {

    final String TAG = "Baihupe";
    String ip;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        application application = (application) getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/infoManage/resetDevice";

        // 绑定三个组件
        ImageView password_BT = findViewById(R.id.change_password);
        ImageView phone_BT = findViewById(R.id.change_phone);
        ImageView device_BT = findViewById(R.id.change_device);

        // 设置点击事件
        // 跳转修改密码界面
        password_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this,ChangePassword.class);
                startActivity(intent);
            }
        });


        // 跳转修改手机号界面
        phone_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(Settings.this,ChangePhone.class);
                startActivity(intent2);
            }
        });

        // 重置设备
        device_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 否则清空信息
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle("提示");
                builder.setMessage("您确定要取消绑定的设备吗");
                builder.setIcon(R.drawable.notice);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HttpRequest httpRequest = new HttpRequest();

                        // 1) 获取头部所需数据
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        String deviceId = record.getString("deviceID","deviceID_null");
                        String token = record.getString("token","token_null");
                        String Identity = "0";

                        if(token.equals("token_null")){
                            Log.i(TAG,"重置设备获取 token 失败");
                        }else if(deviceId.equals("0")){
                            myDialog_Warning("请先绑定设备再操作");
                        }else{
                            // 发送请求
                            // 2） 构造头部
                            Map<String,String> headers = new TreeMap<>();
                            headers.put("Authorization",token);
                            headers.put("Identity",Identity);

                            // 3) 构造 form-body
                            FormBody formBody = new FormBody.Builder().add("deviceid",deviceId).build();

                            // 5) 发送请求
                            String result = "";
                            try {
                                result = httpRequest.OkHttpsPost_F(url,formBody,headers);
                                JSONObject jsonObject_answer = new JSONObject(result);
                                String code = jsonObject_answer.getString("code");
                                String data = jsonObject_answer.getString("data");

                                if(code.equals("200")){
                                    // 同时设置 sharedPreference 对应字段为0，即未绑定状态
                                    SharedPreferences.Editor edit = record.edit();
                                    edit.putString("deviceID","0");
                                    edit.commit();

                                    myDialog_Successful(data);
                                }else{
                                    myDialog_Warning(data);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


    }

    public void myDialog_Warning(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        builder.setMessage(message);
        builder.setIcon(R.drawable.attention);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setIcon(R.drawable.successful);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 返回主界面
                }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


}