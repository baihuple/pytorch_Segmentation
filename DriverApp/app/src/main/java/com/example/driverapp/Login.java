package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class Login extends AppCompatActivity {

    EditText edt_phone,edt_password;
    public String ip;
    public String url;

    final String TAG = "Baihupe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 获取ip
        application app = (application)getApplication();
        ip = app.getIp();
        url = "http://"+ ip + "/login";

        // 获取三个组件
        edt_phone = findViewById(R.id.input_phone);
        edt_password = findViewById(R.id.input_password);
        Button button = findViewById(R.id.login);
        TextView textView = findViewById(R.id.Jump_to_register);
        ImageView logo_image = findViewById(R.id.Logo);

        // 为图片添加动画
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(logo_image,"translationX",1000f,0f);
        translateAnim.setDuration(1000);
        translateAnim.start();

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(logo_image,"alpha",0.0f,1.0f);
        alphaAnim.setDuration(1000);
        alphaAnim.start();

        ObjectAnimator scaleAnimY = ObjectAnimator.ofFloat(logo_image,"scaleY",0f,1f);
        ObjectAnimator scaleAnimX = ObjectAnimator.ofFloat(logo_image,"scaleX",0f,1f);
        scaleAnimX.setDuration(1000);
        scaleAnimX.start();
        scaleAnimY.setDuration(1000);
        scaleAnimY.start();

        // 点击跳转注册界面
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register_first.class);
                startActivity(intent);
            }
        });

        /** 检查缓存是否有 user_status 为 login **/
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String user_status = record.getString("user_status","logout");

        if(user_status.equals("login")){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    Login.this.finish();
                }
            },0);
        }

        /** 输入完电话后，在密码框设置 ”焦点“ 变化监听器，而不是 ”点击事件“ 变化监听器 **/
        /** 注册焦点变化监听器给编辑框 **/
        edt_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if(view.getId() == R.id.input_password && hasfocus){                        // 判断密码编辑框是否获得焦点;hasfocus 为 true 代表获得焦点
                    String phone2 = edt_phone.getText().toString();
                    if(TextUtils.isEmpty(phone2) || phone2.length()<11){                      // 判断手机号输入是否合法,不合法让焦点回退到手机的 editview 中
                        edt_phone.requestFocus();
                        Toast.makeText(Login.this,"请输入11位手机号码", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        // 为按钮设置点击事件
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取电话和密码
                String account = edt_phone.getText().toString();
                String password = edt_password.getText().toString();

                // 发送请求给后端
                // 1) 构造 body
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("account",account);
                    jsonObject.put("password",password);
                    jsonObject.put("identity","0");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // 2）构造 header
                Map<String,String> headers = new TreeMap<>();
                headers.put("identity","0");

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();
                String response = "";
                try {
                    response = httpRequest.OkHttpsPost(url,jsonObject,headers);
                    // 4) 解析数据
                    JSONObject jsonObject_answer = new JSONObject(response);
                    String code = jsonObject_answer.getString("code");
                    String message = jsonObject_answer.getString("message");
                    String data = jsonObject_answer.getString("data");      // token

                    if(code.equals("200")){
                        // 将 token 存入内存
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        SharedPreferences.Editor edit = record.edit();

                        // 存入登陆状态和 token
                        edit.putString("user_status","login");
                        edit.putString("token",data);
                        edit.commit();

                        // 用于验证存入
                        // String token = record.getString("token","null_token");
                        // Log.i(TAG,token);

                        // 跳转主页面，且登陆之后不可再进入登陆界面
                        Intent intent = new Intent(Login.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }else{
                        Toast.makeText(Login.this,message,Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }




            }
        });



    }
}