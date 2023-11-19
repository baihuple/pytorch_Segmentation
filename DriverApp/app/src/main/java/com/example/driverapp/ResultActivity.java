package com.example.driverapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.driverapp.util.DialogUtil;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class ResultActivity extends AppCompatActivity {

    private final static String TAG = "Baihupe";
    private String ip;
    String scanResults;
    String deviceId;

    private Dialog mDialog;

    private String token;
    private String identity = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        application app = (application)getApplication();
        ip = app.getIp();

        // 获取组件框
        EditText equipNum = findViewById(R.id.equipment_Num);
        Button bind = findViewById(R.id.scan_eqip_button);

        // 点击绑定
        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设备码为空报错
                if(TextUtils.isEmpty(equipNum.getText().toString())){
                    myDialog_Warning("设备码不能为空");
                }else{
                    // (一) 向设备发送命令：连接设备

                    // 获取 token
                    SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                    token = record.getString("token","token_null");

                    //  1. 构造 url
                    String url = "http://"+ip+"/driver/infoManage/connectDevice";

                    // 2. 获取发送的数据
                    deviceId = scanResults;
                    String commond = "连接设备";

                    // 3、构造头部信息
                    Map<String,String> headers = new TreeMap<>();
                    headers.put("Authorization",token);
                    headers.put("identity",identity);

                    // 4、构造 form-body
                    FormBody formBody = new FormBody.Builder().add("deviceId",deviceId).add("commond",commond).build();

                    // 5、发送请求
                    String result = "";
                    HttpRequest httpRequest = new HttpRequest();
                    try {
                        result = httpRequest.OkHttpsPost_F(url,formBody,headers);
                        JSONObject answer = new JSONObject(result);

                        // 6、解析数据
                        String code = answer.getString("code");
                        String message = answer.getString("message");
                        if(code.equals("200")){
                            Log.i(TAG,"connect succeed:"+message);

                            // 7、成功连接后，发送绑定请求
                            mDialog = DialogUtil.createLoadingDialog(ResultActivity.this,"正在绑定中");

                            // 创建广播接收器，开始接收
                            standardReceiver = new StandardReceiver(); // 创建一个标准广播的接收器
                            // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
                            IntentFilter filter = new IntentFilter("device_bind_success");
                            registerReceiver(standardReceiver, filter); // 注册接收器，注册之后才能正常接收广播


                        }else{
                            Log.i(TAG,"connect failed:"+message);
                            myDialog_Warning("连接设备失败，请确认设备处于开启状态");
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    // (二) 绑定设备


                }
            }
        });

        // 检查扫码结果
        HmsScan hmsScan = getIntent().getParcelableExtra(ScanUtil.RESULT);
        int scanType = hmsScan.getScanType();                       /** 获取扫描码的类型 **/
        scanResults = hmsScan.getShowResult();               /** 获取扫码结果 **/

        // 扫码样式不对,返回之前界面
        if(scanType!=1){
            myDialog_Warning("请扫描正确的设备码");
        }else{
            equipNum.setText(scanResults);
        }
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

    // 设置广播监听 websocket
    /** 自定义广播 **/
    private StandardReceiver standardReceiver;

    // 定义广播接收器，接收广播
    private class StandardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null && intent.getAction().equals("device_bind_success")){

                // 发送绑定设备
                // 8、构造 header
                Map<String ,String> headers2 = new TreeMap<>();
                headers2.put("Authorization",token);
                headers2.put("Identity",identity);

                // 构造 url
                String url2 = "http://"+ip+"/driver/infoManage/bindDevice";

                // 9、构造 formbody
                FormBody formBody2 = new FormBody.Builder().add("deviceId",deviceId).build();

                // 10、发送请求
                HttpRequest httpRequest1 = new HttpRequest();
                String result1;
                try {
                    result1 = httpRequest1.OkHttpsPost_F(url2,formBody2,headers2);

                    // 11、解析请求
                    JSONObject result_js = new JSONObject(result1);
                    String code1 = result_js.getString("code");
                    String message1 = result_js.getString("message");

                    if(code1.equals("200")){
                        DialogUtil.closeDialog(mDialog);

                        Log.i(TAG,"绑定设备成功:"+message1);

                        // 放入缓存
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("deviceID",deviceId);
                        edit.commit();

                        //fake 弹窗提示进行补货，点击确定之后 --- 弹出补货建议
                        myDialog_Successful("绑定设备成功，请您开始补货");

                    }else{
                        Log.i(TAG,"绑定设备失败:"+message1);
                        myDialog_Warning("绑定设备失败："+message1);
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(standardReceiver); // 注销接收器，注销之后就不再接收广播
    }


}