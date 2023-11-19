package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class Register_second extends AppCompatActivity {

    String sex;
    String drivername;
    String drivertel;
    String drivercard;
    String driverpwd;

    final String TAG = "Baihupe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_second);

        // 接收第一页数据
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            drivername = bundle.getString("drivername");
            drivertel = bundle.getString("drivertel");
            drivercard = bundle.getString("drivercard");
            driverpwd = bundle.getString("driverpwd");
        }


        // 绑定本页组件
        EditText edt_age = findViewById(R.id.input_age);
        EditText edt_city = findViewById(R.id.input_city);
        EditText edt_liscense = findViewById(R.id.input_license);
        RadioGroup rg_sex = findViewById(R.id.rb_sex);
        Button register_BT = findViewById(R.id.register_BT);

        // 检查年龄正确性
        edt_city.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if(view.getId() == R.id.input_city && hasfocus){                        // 判断密码编辑框是否获得焦点;hasfocus 为 true 代表获得焦点
                    String ageS = edt_age.getText().toString();
                    int age = Integer.parseInt(ageS);
                    if(TextUtils.isEmpty(ageS) || age > 120 || age <1){                      // 判断手机号输入是否合法,不合法让焦点回退到手机的 editview 中
                        edt_age.requestFocus();
                        Toast.makeText(Register_second.this,"请输入正确的年龄", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 检查性别
        rg_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.rb_male){
                    sex = "1";                          // sex: 1 为男；0为女
                }else if(i == R.id.rb_famale){
                    sex = "0";
                }else{
                    sex = "-1";
                }
            }
        });

        // 进行注册
        register_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取车牌、年龄、性别、城市
                String license = edt_liscense.getText().toString();
                String age = edt_age.getText().toString();
                String city = edt_city.getText().toString();

                if(TextUtils.isEmpty(license) || TextUtils.isEmpty(age) || TextUtils.isEmpty(city) || sex.equals("-1")){
                    Toast.makeText(Register_second.this,"请将信息填写完整后再注册", Toast.LENGTH_SHORT).show();
                }else{
                    // 发送网络请求
                    // 1） 构造body
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("drivername",drivername);
                        jsonObject.put("drivertel",drivertel);
                        jsonObject.put("drivercard",drivercard);
                        jsonObject.put("driverpwd",driverpwd);
                        jsonObject.put("license",license);
                        jsonObject.put("driverage",age);
                        jsonObject.put("driversex",sex);
                        jsonObject.put("city",city);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    application app = (application)getApplication();
                    String ip = app.getIp();
                    String url = "http://"+ ip + "/driver/register";

                    // 2） 构造header（空）
                    // 3）发送请求
                    HttpRequest httpRequest = new HttpRequest();
                    String response = "";
                    try {
                        response = httpRequest.OkHttpsPost(url,jsonObject,null);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    // 4) 解析数据
                    try {
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

//                            String token = record.getString("token","shit");
//                            Log.i(TAG,token);
//                            Intent intent2 = new Intent(Register_second.this,LaunchSimpleActivity.class);
//                            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent2);
                            myDialog_Successful("您已注册成功，是否进入新手引导？");
                        }else{
                            Toast.makeText(Register_second.this,message,Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
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
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent2 = new Intent(Register_second.this,LaunchSimpleActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
        }
        });
        builder.setNegativeButton("不了，直接登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent2 = new Intent(Register_second.this,MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}