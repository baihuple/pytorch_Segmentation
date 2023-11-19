package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class ChangePrice extends AppCompatActivity {

    String url;
    String ip;
    public final String TAG = "Baihupe";

    String shelveid;
    String price;
    String comName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_price);

        // 获取 ip、url
        application application = (application) getApplication();
        ip = application.getIp();
        url = "http://" + ip + "/driver/shelve/setComPrice?";

        // 获取传递过来的数据
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            shelveid = bundle.getString("shelveid");
            price = bundle.getString("price");
            comName = bundle.getString("comName");
        }

        // 填充到指定位置
        TextView comName_TV = findViewById(R.id.TV_car_pro_name);
        EditText price_ET = findViewById(R.id.TV_car_pro_price);
        Button edit_BT = findViewById(R.id.edit_button);

        comName_TV.setText(comName);
        price_ET.setText(price);

        // 为按钮设置点击事件
        edit_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 拿到修改后的价格
                String price_edit = price_ET.getText().toString();
                Float price_float = Float.parseFloat(price_edit);
                // 和原来相等则提示，否则发送修改请求
                if (price_edit.equals(price)) {
                    myDialog_Warning("请先修改价格");
                } else if (price_float <= 0) {
                    myDialog_Warning("请输入正确的价格");
                } else {
                    // 发送价格提交请求
                    // 1) 获取头部信息
                    Map<String, String> headers = getHeader();

                    // 2) 构造完整 url
                    String url_true = url + "shelveid=" + shelveid + "&price=" + price_edit;

                    // 3) 发送请求
                    HttpRequest httpRequest = new HttpRequest();
                    String response = httpRequest.OkHttpsGet(url_true, headers);

                    // 4) 解析请求
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String code = jsonObject.getString("code");
                        if(code.equals("200")){
                            myDialog_Successful("商品价格修改成功");
                        }else{
                            Log.i(TAG,"商品价格修改失败");
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }


    public void myDialog_Successful(String message) {
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


    public void myDialog_Warning(String message) {
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

    public Map<String, String> getHeader() {
        Map<String, String> headers = new TreeMap<>();

        // 获取 token
        SharedPreferences record = getSharedPreferences("Driver_Info", MODE_PRIVATE);
        String token = record.getString("token", "token_null");
        if (token.equals("token_null")) {
            return null;
        } else {
            headers.put("Authorization", token);
            headers.put("identity", "0");
            return headers;
        }
    }
}