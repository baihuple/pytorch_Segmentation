package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.Fragment.Product_Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;


public class Login extends AppCompatActivity {

    public String ip;
    public String url;
    EditText edt_phone,edt_password;

    final String TAG = "baihuple";
    String phone,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);;

        // 获取ip和全局变量
        application app = (application)getApplication();
        ip = app.getIp();
        url = "http://"+ip+"/login/repl";

        Button but_Login = findViewById(R.id.login);
        edt_phone = (EditText) findViewById(R.id.input_phone);
        edt_password = (EditText) findViewById(R.id.input_password);

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


        /** 检擦缓存是否有 user_status 的 login **/
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String user_status = record.getString("user_status","logout");

        /** 检测到已经登陆,填充文本，自动进行跳转 **/
        if(user_status.equals("login")){
            /** 延迟登陆进入主界面 **/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    // 重新获取 token 和登录状态
                    SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
                    String response = record.getString("login_Info","noInfo");

                    if(!response.equals("noInfo")){
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            // 发出登录请求，重新保存 token
                            Map<String,String> headers = new TreeMap<>();
                            headers.put("identity","1");
                            HttpRequest httpRequest = new HttpRequest();
                            String back = "";
                            back = httpRequest.OkHttpsPost(url,jsonObject,headers);

                            // 保存token
                            JSONObject answer = new JSONObject(back);
                            if(answer.getString("code").equals("200")){
                                SharedPreferences.Editor edit = record.edit();
                                edit.putString("token",answer.getString("data"));
                                edit.commit();
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }else{
                        Log.i(TAG,"自动获取登录信息失败");
                    }

                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    Login.this.finish();
                }
            },0);
        }

        /** 设置登录背景 **/
//        //找VideoView控件
//        CustomVideoView customVideoView = (CustomVideoView) findViewById(R.id.videoview);
//        //加载视频文件
//        customVideoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.login_bagro));
//        //播放
//        customVideoView.start();
//        //循环播放
//        customVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                customVideoView.start();
//            }
//        });

        // 检查登陆状态
        //      ----- String token = record.getString("token"，"获取值为空"); 渠道数据

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

        /** 点击按钮向后端发送请求 **/
        but_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = edt_phone.getText().toString();
                password = edt_password.getText().toString();

                // 封装 JSONObject
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("account",phone);
                    jsonObject.put("password",password);
                    jsonObject.put("stationid",1);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // 构造 map 类型的header
                Map<String,String> headers = new TreeMap<>();
                headers.put("identity","1");

                HttpRequest httpRequest = new HttpRequest();

                // 调用对象方法，进行 post 请求,获得返回参数 response
                String response = "";
                try {
                   response = httpRequest.OkHttpsPost(url,jsonObject,headers);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                 // 解析数据
                try {
                    JSONObject jsonObject_answer = new JSONObject(response);
                    String code = jsonObject_answer.get("code").toString();
                    String data = jsonObject_answer.get("data").toString();
                    String message = jsonObject_answer.get("message").toString();

                    Log.i(TAG,"登录页面获取数据成功");
                    if(code.equals("200")){                                 // 登录状态正常，保存数据到本地 SharedPreferences         ----- 使用 SharedPreferences record = getSharedPreferences("Info_record",MODE_PRIVATE);
                        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);              //      ----- String token = record.getString("token"，"获取值为空"); 渠道数据
                        SharedPreferences.Editor edit = record.edit();

                        // 将用户名和密码保存到本机缓存中
                        String account = edt_phone.getText().toString();
                        String password = edt_password.getText().toString();
                        JSONObject loginInfo = new JSONObject();
                        loginInfo.put("account",account);
                        loginInfo.put("password",password);
                        loginInfo.put("stationid","1");

                        // 存入用户登陆状态 user_status
                        edit.putString("user_status","login");
                        edit.putString("token",data);
                        edit.putString("login_Info",loginInfo.toString());
                        edit.commit();


                        // 跳转进入主界面，且登录之后不再进入登陆界面
                        Intent intent = new Intent(Login.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);


                        Log.i(TAG,data);
                    }else{
                        Toast.makeText(Login.this,"登录失败:"+message,Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

}