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
import android.util.LogPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class ChangePassword extends AppCompatActivity {

    final String TAG = "Baihupe";
    public String ip;
    public String url ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_passwrod);

        // 找到ip
        application app = (application)getApplication();
        ip = app.getIp();
        url = "http://"+ip+"/driver/infoManage/changePwd";

        // 找到组件
        EditText oldPwd_ET = findViewById(R.id.oldPwd);
        EditText newPwd_ET = findViewById(R.id.newPwd);
        EditText newPwd_confirm_ET = findViewById(R.id.newPwd_confirm);
        Button changePwd_BT = findViewById(R.id.password_change_BT);

        changePwd_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPwd = oldPwd_ET.getText().toString();
                String newPwd = newPwd_ET.getText().toString();
                String cfmPwd = newPwd_confirm_ET.getText().toString();

                // 如果新老密码相同
                if(oldPwd.equals(newPwd)){
                    myDialog_Warning("新旧密码不能相同");
                }else if(!newPwd.equals(cfmPwd)){
                    myDialog_Warning("新密码输入不一致");
                }else if(TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(cfmPwd)){
                    myDialog_Warning("新旧密码均不能为空");
                }else{
                    Log.i(TAG,"新密码为："+newPwd);
                    // 发送修改密码网络请求
                    // 1) 获取头部
                    SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                    String token = record.getString("token","token_null");
                    String Identity = "0";

                    if(token.equals("token_null")){
                        Log.i(TAG,"更改密码获取 token 失败");
                    }else{
                        // 2) 构造头部
                        Map<String,String> headers = new TreeMap<>();
                        headers.put("Authorization",token);
                        headers.put("Identity",Identity);

                        // 3) 构造body
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("oldPwd",oldPwd);
                            jsonObject.put("newPwd",newPwd);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        // 4) 发送请求
                        HttpRequest httpRequest = new HttpRequest();

                        String response = "";
                        try {
                            response = httpRequest.OkHttpsPost(url,jsonObject,headers);

                            // 5) 解析数据
                            JSONObject jsonObject_answer = new JSONObject(response);
                            String code = jsonObject_answer.getString("code");
                            String message = jsonObject_answer.getString("message");

                            if(code.equals("200")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassword.this);
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
                                                Intent intent = new Intent(ChangePassword.this, Login.class);
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

}