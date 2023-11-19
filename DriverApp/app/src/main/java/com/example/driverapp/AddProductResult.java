package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class AddProductResult extends AppCompatActivity {

    public final String TAG = "Baihupe";
    String ip;
    String url;
    String comid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_result);

        application application = (application) getApplication();
        ip = application.getIp();

        TextView addProId = findViewById(R.id.TV_add_product_name);
        EditText addProNum = findViewById(R.id.TV_add_product_num);
        EditText addProPrice = findViewById(R.id.TV_add_product_price);
        EditText addProShelveid = findViewById(R.id.TV_add_product_shelveid);

        Button addButton = findViewById(R.id.add_button);

        // 检查扫码结果
        HmsScan hmsScan = getIntent().getParcelableExtra(ScanUtil.RESULT);
        int scanType = hmsScan.getScanType();                       /** 获取扫描码的类型 **/
        String scanResults = hmsScan.getShowResult();               /** 获取扫码结果 **/

        // 如果 scanType 不为 128，则重新扫描
        if(scanType != HmsScan.EAN13_SCAN_TYPE){
            myDialog_Warning("请重新扫描商品条码");
            // 回到主页面
        }else{
            // 填充
            addProId.setText(scanResults);
            comid = scanResults;
        }

        // 当用户点击按钮的时候开始补货流程
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1) 获取两个 edittext 中的数据
                String price = addProPrice.getText().toString();
                String num = addProNum.getText().toString();
                String shelveid = addProShelveid.getText().toString();
                // 2) 提交申请
                if(TextUtils.isEmpty(price) || TextUtils.isEmpty(num) || TextUtils.isEmpty(shelveid)){
                    Toast.makeText(AddProductResult.this,"请输入正确的数量、价格、货架号",Toast.LENGTH_SHORT).show();
                }else if(Float.parseFloat(price)<=0 || Float.parseFloat(num) <=0 || Float.parseFloat(shelveid)<=0){
                    Toast.makeText(AddProductResult.this,"请输入正确的数量、价格、货架号",Toast.LENGTH_SHORT).show();
                } else{
                    // 1) 获取 deviceid
                    try {
                            // 请求添加商品接口
                            String url_add = "http://" + ip + "/driver/shelve/addCommdoty";

                            // 构造 header
                            Map<String,String> headers = getHeader();

                            // 构造 body
                            JSONObject jsonObject1 = new JSONObject();
                            jsonObject1.put("shelveid",shelveid);
                            jsonObject1.put("comid",comid);
                            jsonObject1.put("price",price);
                            jsonObject1.put("number",num);

                            // 发送请求
                            HttpRequest httpRequest1 = new HttpRequest();
                            String response = httpRequest1.OkHttpsPost(url_add,jsonObject1,headers);

                            // 解析请求
                            JSONObject jsonObject_answer = new JSONObject(response);
                            String code = jsonObject_answer.getString("code");
                            String message = jsonObject_answer.getString("message");

                            if(code.equals("200")){
                                myDialog_Successful("添加成功");
                            }else{
                                myDialog_Warning(message);
                            }

                        } catch (JSONException e) {
                        throw new RuntimeException(e);
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
                finish();               // 回到扫码界面
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },500);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public Map<String,String> getHeader(){
        Map<String,String> headers = new TreeMap<>();

        // 获取 token
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String token = record.getString("token","token_null");
        if(token.equals("token_null")){
            return null;
        }else{
            headers.put("Authorization",token);
            headers.put("identity","0");
            return headers;
        }
    }


}