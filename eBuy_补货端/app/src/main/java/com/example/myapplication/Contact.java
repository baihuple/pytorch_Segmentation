package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;

public class Contact extends AppCompatActivity {
    final String TAG = "Baihupe";
    private String ip;
    private String url;
    String driverid;
    private EditText inputText;
    private Button send;
    private List<Msg> msgList = new ArrayList<>();


    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // 获取传递过来的 driverid
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            driverid = bundle.getString("driverid");
        }

        // 获取 ip 地址
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://" + ip + "/repl/connectDriver";

        // 注册广播
        standardReceiver = new StandardReceiver(); // 创建一个标准广播的接收器
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter filter = new IntentFilter("Msg_detail");
        registerReceiver(standardReceiver, filter); // 注册接收器，注册之后才能正常接收广播

        // 设置顶部
        TextView top_header = findViewById(R.id.driver_name_message);
        top_header.setText("司机 "+driverid+" 号");

        // 设置其他组件
        inputText = findViewById(R.id.driver_input_text);
        send = findViewById(R.id.send);

        // 获取循环列表
        msgRecyclerView = findViewById(R.id.msg_recycler_view);

        // 获得线性布局,并设置给循环列表
        linearLayoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(linearLayoutManager);

        // 创建适配器
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        // 获取历史消息
        try {
            getHistorymsg();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // 发送消息，并且存入列表项
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString();

                // 不能为空
                if(TextUtils.isEmpty(content)){
                    return;
                }

                // 发送请求
                // 1）构造头部
                Map<String,String> headers = getHeader();

                // 2) 构造 form-body
                FormBody formBody = new FormBody.Builder().add("driverid",driverid).add("message",content).build();

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();
                String response;
                try {
                    response = httpRequest.OkHttpsPost_F(url,formBody,headers);

                    // 4) 解析请求
                    JSONObject answer = new JSONObject(response);
                    String code = answer.getString("code");
                    String message = answer.getString("message");

                    if(code.equals("200")) {
                        // 刷新消息列表
                        // 创建新的 Msg 对象，并且把他添加到 msgList 列表中
                        Msg msg = new Msg(content, Msg.TYPE_SEND);
                        msgList.add(msg);

                        // 将消息添加到列表中最后一项
                        adapter.notifyItemInserted(msgList.size() - 1);

                        // 将显示的数据定位到最后一行
                        msgRecyclerView.scrollToPosition(msgList.size() - 1);

                        // 将消息放入缓存
                        SharedPreferences record = getSharedPreferences("Info_Record", MODE_PRIVATE);
                        String history = record.getString("history_msg_" + driverid, "null_history");
                        JSONArray jsonArray = new JSONArray(history);
                        // 构造 jsonobject 对象
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("content", msg.getContent());
                        jsonObject.put("type", msg.getType());
                        jsonObject.put("time", msg.getNow_time());
                        // 放入数组中
                        jsonArray.put(jsonObject);
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("history_msg_" + driverid, jsonArray.toString());
                        edit.commit();

                        // 清空输入框
                        inputText.setText("");
                    }else{
                        myDialog_Warning(message);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        });

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

    /** 获取历史消息，渲染到循环列表中 **/
    public void getHistorymsg() throws JSONException {
        // 从缓存中取出，看看有没有该字段
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String history_msg = record.getString("history_msg_"+driverid,"no_history_message");
        SharedPreferences.Editor edit = record.edit();

        // 如果没有缓存
        if(history_msg.equals("no_history_message")){
            JSONArray jsonArray = new JSONArray();
            Log.i(TAG,"没收到消息，首次进入为空列表");
            edit.putString("history_msg_"+driverid,jsonArray.toString());
            edit.commit();
        }else{
            JSONArray jsonArray = new JSONArray(history_msg);
            Log.i(TAG,"存储里的内容"+history_msg);
            // 构造 jsonobject 取出
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // 获取其中各项字段,赋值给list
                Msg msg = new Msg();
                msg.setContent(jsonObject.getString("content"));
                msg.setType(Integer.valueOf(jsonObject.getString("type")));
                msg.setNow_time(jsonObject.getString("time"));

                // 添加到列表项中
                msgList.add(msg);
                // 将消息添加到列表中最后一项
                adapter.notifyItemInserted(msgList.size()-1);
            }
            // 添加完之后，刷新整个列表并且显示
            adapter.notifyDataSetChanged();
            // 将显示的数据定位到最后一行
            msgRecyclerView.scrollToPosition(msgList.size()-1);
        }
    }


    /** 自定义广播 **/
    private StandardReceiver standardReceiver;


    // 定义广播接收器，接收广播
    private class StandardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String driverid_received = bundle.getString("driverid");
            String message_received = bundle.getString("message");

            // 如果driverid相等则渲染，否则不用管
            if(!driverid_received.equals(driverid)){
                return;
            }

            Msg msg = new Msg();
            msg.setType(Msg.TYPE_RECEIVED);
            msg.setContent(message_received);
            // 将消息加入消息列表，并监听最后一项
            msgList.add(msg);
            adapter.notifyDataSetChanged();
            // 将显示的数据定位到最后一行
            msgRecyclerView.scrollToPosition(msgList.size()-1);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(standardReceiver); // 注销接收器，注销之后就不再接收广播
    }

    /** 构造头部 **/
    public Map<String, String> getHeader() {
        Map<String, String> headers = new TreeMap<>();

        // 获取 token
        SharedPreferences record = getSharedPreferences("Info_Record", MODE_PRIVATE);
        String token = record.getString("token", "token_null");
        if (token.equals("token_null")) {
            return null;
        } else {
            headers.put("Authorization", token);
            headers.put("identity", "1");
            return headers;
        }
    }


}