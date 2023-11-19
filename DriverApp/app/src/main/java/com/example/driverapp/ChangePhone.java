package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class ChangePhone extends AppCompatActivity {

    final String TAG = "Baihupe";
    public String ip;
    public String url ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);

        // 获取 ip
        application app = (application)getApplication();
        final String ip = app.getIp();
        final String url = "http://"+ip+"/driver/infoManage/changeTel";


        EditText oldPhone_ET = findViewById(R.id.oldPhone);
        EditText newPhone_ET = findViewById(R.id.newPhone);
        EditText cfmPhone_ET = findViewById(R.id.cfmnewPhone);
        Button changePhone_BT = findViewById(R.id.phone_change_BT);

        changePhone_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取数据
                String oldPhone = oldPhone_ET.getText().toString();
                String newPhone = newPhone_ET.getText().toString();
                String cfmPhone = cfmPhone_ET.getText().toString();

                if(TextUtils.isEmpty(oldPhone) || TextUtils.isEmpty(newPhone) || TextUtils.isEmpty(cfmPhone)){
                    myDialog_Warning("新旧电话均不能为空");
                }else if(!newPhone.equals(cfmPhone)){
                    myDialog_Warning("两次新电话号码不相等");
                }else{
                    Log.i(TAG,"新电话号码为:"+newPhone);

                    // 发送网络请求
                    // 1) 获取头部
                    SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                    String token = record.getString("token","token_null");
                    String Identity = "0";

                    if(token.equals("token_null")){
                        Log.i(TAG,"更改电话获取 token 失败");
                    }else{
                        // 2) 构造头部
                        Map<String,String> headers = new TreeMap<>();
                        headers.put("Authorization",token);
                        headers.put("Identity",Identity);

                        // 3) 构造 form-data
                        FormBody formBody = new FormBody.Builder().add("newTel",newPhone).build();

                        // 4) 发送请求
                        HttpRequest httpRequest = new HttpRequest();
                        String result = "";
                        try {
                            result = httpRequest.OkHttpsPost_F(url,formBody,headers);
                            // 5) 解析结果
                            JSONObject jsonObject = new JSONObject(result);
                            String code = jsonObject.getString("code");
                            String message = jsonObject.getString("message");

                            if(code.equals("200")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePhone.this);
                                builder.setTitle("提示");
                                builder.setMessage("修改成功，请重新登录");
                                builder.setIcon(R.drawable.successful);
                                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 清除 user_status 登录状态
                                        SharedPreferences.Editor edit = record.edit();
                                        edit.clear();
                                        edit.commit();

                                        // 延时 5s 跳转主界面
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(ChangePhone.this, Login.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        },500);
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }else{
                                myDialog_Warning(message);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

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