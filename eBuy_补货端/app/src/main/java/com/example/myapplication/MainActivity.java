package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class MainActivity extends AppCompatActivity {

    private ViewPager VP_nav;
    private RadioGroup RG_nav;
    private final String TAG = "Baihupe";
    private String ip;
    private AppClientEndpoint mAppTask; // 声明一个WebSocket客户端任务对象
    private String ReplURL;
    private String ReplID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取 ip 地址
        application app = (application) getApplication();
        ip = app.getIp();

        // 获取补货站 id
        try {
            JSONObject jsonObject = getReplInfo();
            ReplID = jsonObject.getString("stationid");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // 建立连接
        ReplURL = "ws://" + ip + "/websocket/repl_" + ReplID;

        new Thread(new Runnable() {
            @Override
            public void run() {
                initWebSocket();
            }
        }).start();


        /** 1) 获取翻页试图，构建翻页适配器; **/
        VP_nav = findViewById(R.id.VP_nav);
        NavPagerAdapter navPagerAdapter = new NavPagerAdapter(getSupportFragmentManager());

        /** 2) 绑定翻页适配器与适配器对象 **/
        VP_nav.setAdapter(navPagerAdapter);

        /** 3) 给翻页试图添加 + 页面变更监听器：翻页事件和 RadioGroup 的子id绑定 **/
        /** 左右滑动页面，当停留在何处，设置 RadioButton 的指定项为 check 选中 **/
        VP_nav.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position){
                RG_nav.check(RG_nav.getChildAt(position).getId());
            }
        });

        /** 4) 获取 RadioGroup 对象，设置单选组的选中监听器 **/
        /** 遍历 RadioBox 中所有子 RadioButton，将每一个子按钮的 id 与当前页面已经选中的 id 进行匹配，匹配到那个，根据 NavPagerAdapter中的规则，进行对应 Fragment 跳转 **/
        RG_nav = findViewById(R.id.RG_nav);
        RG_nav.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                for(int pos=0;pos<RG_nav.getChildCount();pos++){
                    // 获取指定位置的单选按钮
                    RadioButton tab = (RadioButton) RG_nav.getChildAt(pos);
                    if(tab.getId()==checkedId){
                        VP_nav.setCurrentItem(pos);
                    }
                }
            }
        });
    }

    /** 在 mainactivity 中建立 websocket 连接 **/
    private void initWebSocket(){
        // 创建文本运输任务，并且指定消息应答监听器 ---- resp 为接收到的返回消息
        mAppTask = new AppClientEndpoint(this, resp -> {
            Log.i(TAG,"MainActivity收到内容:"+resp);

            // 1.检查缓存 message_list 字段，有无保存该消息的记录
            SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
            SharedPreferences.Editor edit = record.edit();
            String message_list = record.getString("message_list","null_message_list");

            // 2.解析数据，获取 id 和 内容
            JSONObject jsonObject = new JSONObject(resp);
            JSONObject sender = jsonObject.getJSONObject("sender");

            String driverid = sender.getString("driverid");
            String content = jsonObject.getString("message");

            // 3.将数据保存到列表缓存中
            // 如果没有记录列表，创建 jsonArray 数组
            if(message_list.equals("null_message_list")){
                JSONArray jsonArray = new JSONArray();

                Msg msg = new Msg();
                msg.setContent(content);
                msg.setDriver_id(driverid);
                msg.setType(Msg.TYPE_RECEIVED);

                // 创建 JSonobject
                JSONObject message_list_item = new JSONObject();
                message_list_item.put("driverid",msg.getDriver_id());
                message_list_item.put("content",msg.getContent());
                message_list_item.put("type",msg.getType());
                message_list_item.put("time",msg.getNow_time());

                // 存入缓存
                jsonArray.put(message_list_item);
                edit.putString("message_list",jsonArray.toString());
                edit.commit();

                Log.i(TAG,"存入1"+record.getString("message_list","no1"));

            }else{     // 如果有记录，则获取数组并且添加内容
                JSONArray jsonArray = new JSONArray(message_list);

                // 检查是否存过对应id，存过则替换
                // flag: 标志位；为 0 ：之前存过;  为 1 ：之前没存过
                int flag = 1;
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject item_exist = jsonArray.getJSONObject(i);
                    String driverid_exists = item_exist.getString("driverid");

                    // 相等替换
                    if(driverid_exists.equals(driverid)){
                        Msg msg = new Msg();
                        // 修改该项内容,重新放内容和时间
                        jsonArray.getJSONObject(i).put("content",content);
                        jsonArray.getJSONObject(i).put("time",msg.getNow_time());
                        flag = 0;
                        break;
                    }
                }

                // 没有则重新创建内容并且存入
                if(flag == 0){
                    // 重新存入缓存
                    edit.putString("message_list",jsonArray.toString());
                    edit.commit();
                }else{
                    Msg msg = new Msg();
                    msg.setContent(content);
                    msg.setDriver_id(driverid);
                    msg.setType(Msg.TYPE_RECEIVED);

                    // 创建 JSonobject
                    JSONObject message_list_item = new JSONObject();
                    message_list_item.put("driverid",msg.getDriver_id());
                    message_list_item.put("content",msg.getContent());
                    message_list_item.put("type",msg.getType());
                    message_list_item.put("time",msg.getNow_time());

                    // 存入缓存
                    jsonArray.put(message_list_item);
                    edit.putString("message_list",jsonArray.toString());
                    edit.commit();
                }

                Log.i(TAG,"存入2"+record.getString("message_list","no2"));
            }

            // 发送消息列表广播
            Intent intent = new Intent("Msg_list_changed");
            sendBroadcast(intent);

            // 发送消息内容等具体聊天广播
            Intent intent2 = new Intent("Msg_detail");
            Bundle bundle = new Bundle();
            bundle.putString("driverid",driverid);
            bundle.putString("message",content);
            intent2.putExtras(bundle);
            sendBroadcast(intent2);

            // 将该条消息加入到对应为某个司机分配的缓存中
            String history = record.getString("history_msg_"+driverid,"null_record");

            // 如果没存，创建 jsonarray 并且存入
            if(history.equals("null_record")){
                JSONArray jsonArray = new JSONArray();

                JSONObject item = new JSONObject();
                Msg msg = new Msg();
                item.put("type",Msg.TYPE_RECEIVED);
                item.put("time",msg.getNow_time());
                item.put("content",content);

                jsonArray.put(item);
                edit.putString("history_msg_"+driverid,jsonArray.toString());
                edit.commit();
            }else{                   // 如果存了，则直接取出并且存入新的 jsonobject
                JSONArray jsonArray = new JSONArray(history);

                JSONObject item = new JSONObject();
                Msg msg = new Msg();
                item.put("type",Msg.TYPE_RECEIVED);
                item.put("time",msg.getNow_time());
                item.put("content",content);

                jsonArray.put(item);
                edit.putString("history_msg_"+driverid,jsonArray.toString());
                edit.commit();
            }
        });

        // 获取WebSocket容器
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            Log.i(TAG,"websocket连接的："+ ReplURL);
            URI uri = new URI(ReplURL); // 创建一个URI对象
            // 连接WebSocket服务器，并关联文本传输任务获得连接会话
            Session session = container.connectToServer(mAppTask, uri);
            // 设置文本消息的最大缓存大小
            session.setMaxTextMessageBufferSize(1024 * 1024 * 10);
            // 设置二进制消息的最大缓存大小
            //session.setMaxBinaryMessageBufferSize(1024 * 1024 * 10);
            // 建立连接
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /** 获取补货站 id **/
    public JSONObject getReplInfo() throws JSONException{
        // 向服务器发送请求，获取补货站信息
        // 1) 获取 token
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String token = record.getString("token","token_null");
        String Info = record.getString("Info","Info_null");

        if(!Info.equals("Info_null")){
            return new JSONObject(Info);
        }else{
            if(token.equals("token_null")){
                Log.i(TAG,"MainActivity获取 token 失败");
            }else{
                // 2) 构造头部
                Map<String,String > headers = getHeader();

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();

                String url2 = "http://"+ ip + "/repl/localInfo";

                String response = httpRequest.OkHttpsGet(url2,headers);

                // 4) 解析结果
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("message");
                    if (code.equals("200")){
                        String data = jsonObject.getString("data");
                        JSONObject replInfo = new JSONObject(data);
                        return  replInfo;
                    }else{
                        Log.i(TAG,"获取站点信息失败");
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

}