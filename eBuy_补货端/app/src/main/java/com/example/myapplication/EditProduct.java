package com.example.myapplication;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.Fragment.Product_Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class EditProduct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // 接收商品页面传来的数据
        Bundle bundle = getIntent().getExtras();

        String comName = bundle.getString("comName");               // 商品名称
        String price = bundle.getString("price");                   // 商品价格（课改）
        String num = bundle.getString("number");                    // 商品数量
        String storeid = bundle.getString("storeid");
        final String TAG = "Baihupe";


        String number;
        if(num.equals("缺货")){
            number = "0";
        }else{
            number = num.substring(3);
        }

        // 将数据自行填充到 TextView 中
        TextView productname = findViewById(R.id.TV_pro_name);
        EditText ED_number = findViewById(R.id.TV_pro_num);
        Button BT_edit = findViewById(R.id.edit_button);

        ED_number.setText(number);
        productname.setText(comName);

        // 获取 token
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String token = record.getString("token","token_null");
        Log.i(TAG,"修改信息获取的token:"+token);

        // 设置点击修改事件
        BT_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1) 获取文本框中用户自己输入的数量
                String editnum = ED_number.getText().toString();

                // 2) 判断和原来的相等不相等，返回提示框
                if(editnum.equals(number) ){
                    myDialog_Warning("请修改后再提交");
                }else if(!editnum.equals(number) ){
                    // 发送数量修改请求
                    // a、构造 header
                    Map<String,String > headers = new TreeMap<>();
                    headers.put("Authorization",token);
                    headers.put("identity","1");

                    // b、构造 formbody
                    FormBody formBody = new FormBody.Builder().add("storeid",storeid).add("storeNumber",editnum).build();

                    // c、构造 url
                    application app = (application)getApplication();

                    final String ip = app.getIp();
                    final String url = "http://"+ip+"/repl/setComNumber";

                    // d、构造 client
                    HttpRequest httpRequest = new HttpRequest();
                    String result;
                    JSONObject ans;
                    String code;
                    String message;
                    try {
                       result = httpRequest.OkHttpsPost_F(url,formBody,headers);
                       ans = new JSONObject(result);
                       code = ans.getString("code");
                       message = ans.getString("message");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    // 判断 code 状态
                    if(code.equals("10011")){
                        myDialog_Warning("数量修改失败");
                    }else if(code.equals("200")){
                        myDialog_Successful("数量修改成功");
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
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }





}