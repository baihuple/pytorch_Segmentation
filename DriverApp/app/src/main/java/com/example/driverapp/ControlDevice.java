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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class ControlDevice extends AppCompatActivity {

    public final String TAG = "Baihupe";
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);

        // 获取ip
        application application = (application) getApplication();
        ip = application.getIp();

        // 获取三个组件
        ImageView unlock = findViewById(R.id.open_button);
        ImageView lock = findViewById(R.id.close_button);
        Button add = findViewById(R.id.jump_to_add);
        TextView status = findViewById(R.id.device_status);

        // 点击开锁，发送开锁命令
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject jsonObject = getDriverInfo();
                    String deviceid = jsonObject.getString("deviceID");

                    // 2) 向设备发送command
                    String url_command = "http://" + ip + "/driver/infoManage/connectDevice";

                    // 3）构造header
                    Map<String,String> headers = getHeader();

                    // 4) 发送请求
                    HttpRequest httpRequest = new HttpRequest();
                    FormBody formBody = new FormBody.Builder().add("deviceId",deviceid).add("commond","补货").build();

                    String response = httpRequest.OkHttpsPost_F(url_command,formBody,headers);

                    // 5) 解析请求
                    JSONObject jsonObject_command = new JSONObject(response);
                    String code = jsonObject_command.getString("code");

                    if(code.equals("200")) {
                        Toast.makeText(ControlDevice.this,"设备已经打开，可以添加商品",Toast.LENGTH_SHORT).show();
                        status.setText("解锁");
                    }else{
                        Toast.makeText(ControlDevice.this,"开锁失败",Toast.LENGTH_SHORT).show();
                        Log.i(TAG,"向设备发送补货命令失败");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        // 点击关锁，发送关锁指令
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 构造头部
                Map<String ,String> headers = getHeader();

                // 发送指令
                HttpRequest httpRequest = new HttpRequest();
                String url_close = "http://" + ip + "/driver/shelve/overAdd";

                // 解析指令
                String response = httpRequest.OkHttpsGet(url_close,headers);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    if (code.equals("200")){
                        Toast.makeText(ControlDevice.this,"关锁成功",Toast.LENGTH_SHORT).show();
                        status.setText("锁定");
                    }else{
                        Toast.makeText(ControlDevice.this,"关锁失败",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        });


        // 点击添加商品，进行跳转
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String read_status = status.getText().toString();
                if(read_status.equals("锁定")){           // 锁定状态不能添加商品
                    myDialog_Warning("设备锁定，请解锁后添加商品");
                }else{
                    Intent intent = new Intent(ControlDevice.this,AddProductScan.class);
                    startActivity(intent);
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


    public JSONObject getDriverInfo() throws JSONException {
        // 向服务器发送请求,获取司机信息
        // 1) 获取 token
        SharedPreferences record = getSharedPreferences("Driver_Info", MODE_PRIVATE);
        String token = record.getString("token","token_null");
        String Info = record.getString("Info","Info_null");

        if(!Info.equals("Info_null")){
            return new JSONObject(Info);
        }else{
            if(token.equals("token_null")){
                Log.i(TAG,"购物车界面获取 token 失败");
            }else{
                // 2) 构造头部
                Map<String,String > headers = getHeader();

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();

                String url2 = "http://"+ ip + "/driver/infoManage/driverInfo";

                String response = httpRequest.OkHttpsGet(url2,headers);

                // 4) 解析结果
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("message");
                    if (code.equals("200")){
                        String data = jsonObject.getString("data");
                        JSONObject driverInfo = new JSONObject(data);
                        return  driverInfo;
                    }else{
                        Log.i(TAG,"获取司机信息失败");
//                    Toast.makeText(getContext(),"获取司机信息失败",Toast.LENGTH_SHORT).show();
                        return  null;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    // 获取并封装token和identity
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