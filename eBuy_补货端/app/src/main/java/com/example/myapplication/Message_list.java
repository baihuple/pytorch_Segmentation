package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Message_list extends AppCompatActivity {
    public final String TAG = "Baihupe";

    private ListView listView;

    private List<Msg> list;
    private BaseAdapter baseAdapter;

    @Override
    public void onResume() {
        super.onResume();
        try {
            getMessageList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        listView = findViewById(R.id.message_list);

        standardReceiver = new StandardReceiver(); // 创建一个标准广播的接收器
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter filter = new IntentFilter("Msg_list_changed");
        registerReceiver(standardReceiver, filter); // 注册接收器，注册之后才能正常接收广播

        // 为 listView 的每一项设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 获取指定列表处的消息
                Msg msg = list.get(i);

                // 跳转到指定页面并且传递数据
                Intent intent = new Intent(Message_list.this,Contact.class);
                Bundle bundle = new Bundle();
                bundle.putString("driverid",msg.getDriver_id());
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

    }

    public void getMessageList() throws JSONException {

        list = new ArrayList<>();

        // 1. 获取缓存中信息
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String message_list = record.getString("message_list","no_list");

        // 如果暂时没有数据
        if(message_list.equals("no_list")){
            Log.i(TAG,"当前消息列表缓存中暂无数据");
            return;
        }else{
            // 获取数据
            JSONArray jsonArray = new JSONArray(message_list);
            Log.i(TAG,message_list);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // 构造消息 Msg 类型对象
                Msg message = new Msg();
                message.setType(Msg.TYPE_RECEIVED);
                message.setDriver_id(jsonObject.getString("driverid"));
                message.setNow_time(jsonObject.getString("time"));
                message.setContent(jsonObject.getString("content"));

                // 添加到列表中
                list.add(message);
            }

            baseAdapter = new MessageListAdapter(list,this);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

        }
    }

    // 接收广播
    /** 自定义广播 **/
    private StandardReceiver standardReceiver;

    // 定义广播接收器，接收广播
    private class StandardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null && intent.getAction().equals("Msg_list_changed")){
                try {
                    getMessageList();
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