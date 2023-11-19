package com.example.driverapp;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.driverapp.util.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import okhttp3.FormBody;
import okhttp3.Headers;

public class StationContact extends AppCompatActivity {

    String stationid,stationname;
    final String TAG = "Baihupe";
    private String ip;
    private String url;
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_contact);

        // 获取传递过来的 stationid
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            stationid = bundle.getString("stationid");
            stationname = bundle.getString("stationname");
        }

        standardReceiver = new StandardReceiver(); // 创建一个标准广播的接收器
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter filter = new IntentFilter("WebSocket");
        registerReceiver(standardReceiver, filter); // 注册接收器，注册之后才能正常接收广播

        // 获取 ip 地址
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://" + ip + "/driver/shelve/stationInfo/connect";

        // 找到对应组件
        inputText = findViewById(R.id.input_text);
        send = findViewById(R.id.send);
        msgRecyclerView = findViewById(R.id.msg_recycler_view);
        TextView station_name = findViewById(R.id.station_name_message);
        station_name.setText(stationname);

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


        // 发送消息点击事件
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString();
                // 不能为空
                if(TextUtils.isEmpty(content)){
                    return;
                }

                // 发送请求
                // 1) 构造头部
                Map<String ,String > headers = getHeader();

                // 2) 构造 form-body
                FormBody formBody = new FormBody.Builder().add("replStationid",stationid).add("message",content).build();

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();
                String response;
                try {
                    response = httpRequest.OkHttpsPost_F(url,formBody,headers);
                    // 4) 解析请求
                    JSONObject answer = new JSONObject(response);
                    String code = answer.getString("code");
                    String message = answer.getString("message");

                    if(code.equals("200")){
                        // 刷新消息列表
                        // 创建新的 Msg 对象，并且把他添加到 msgList 列表中
                        Msg msg = new Msg(content,Msg.TYPE_SEND);
                        msgList.add(msg);

                        // 将消息添加到列表中最后一项
                        adapter.notifyItemInserted(msgList.size()-1);

                        // 将显示的数据定位到最后一行
                        msgRecyclerView.scrollToPosition(msgList.size()-1);

                        // 将消息放入缓存
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        String history = record.getString("history_msg_"+stationid,"null_history");
                        JSONArray jsonArray = new JSONArray(history);
                        // 构造 jsonobject 对象
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("content",msg.getContent());
                        jsonObject.put("type",msg.getType());
                        jsonObject.put("time",msg.getNow_time());
                        // 放入数组中
                        jsonArray.put(jsonObject);
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("history_msg_"+stationid,jsonArray.toString());
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

    /** 获取历史消息,先渲染到循环列表中, **/
    private void getHistorymsg() throws JSONException {
        // 从缓存中获取，看有没有该字段
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String history_msg = record.getString("history_msg_"+stationid,"no_history_message");
        SharedPreferences.Editor edit = record.edit();

        // 没有，创建 Msg 类型列表
        if(history_msg.equals("no_history_message")){
            JSONArray jsonArray = new JSONArray();
            Log.i(TAG,"没收到消息，首次进入为空列表");
            edit.putString("history_msg_"+stationid,jsonArray.toString());
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

    /** 构造头部 **/
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

    /** 自定义广播 **/
    private StandardReceiver standardReceiver;

    @Override
    protected void onStart() {
        super.onStart();
    }

    // 定义广播接收器，接收广播
    private class StandardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent!=null && intent.getAction().equals("WebSocket")){;
                Bundle bundle = intent.getExtras();
                String stationid_received = bundle.getString("stationid");
                String type_received = bundle.getString("type");
                String message_received = bundle.getString("message");

                Log.i(TAG,"消息界面收到+"+stationid_received+" "+"type_received+:"+type_received+" message_receicved:"+message_received);

                // 先判断发送方和当前对话方是不是一个对象
                if(!stationid_received.equals(stationid)){  // 不是，则不显示在当前对话框中
                    return;
                }

                // 根据消息类型将数据添加到列表项中
                if(type_received.equals("0")){                       // 0 为普通消息
                    Msg msg = new Msg();
                    msg.setType(Msg.TYPE_RECEIVED);
                    msg.setContent(message_received);
                    // 将消息加入消息列表，并监听最后一项
                    msgList.add(msg);
                    adapter.notifyDataSetChanged();
                    // 将显示的数据定位到最后一行
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                }else if(type_received.equals("1")){                 // 1 为邀约消息
                    Msg msg = new Msg();
                    msg.setType(Msg.TYPE_RECEIVED);
                    // 解析并放入
                    String invitation = "欢迎购买!\n";
                    try {
                        JSONArray jsonArray_content = new JSONArray(message_received);
                        for(int i=0;i<jsonArray_content.length();i++){
                            JSONObject item = jsonArray_content.getJSONObject(i);
                            String comName = item.getString("comName");
                            String price = item.getString("price");
                            String number = item.getString("number");
                            invitation += "商品名称："+comName+", 价格："+price+", 数量："+number +"\n";
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    msg.setContent(invitation);
                    msgList.add(msg);
                    adapter.notifyDataSetChanged();
                    // 将显示的数据定位到最后一行
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
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