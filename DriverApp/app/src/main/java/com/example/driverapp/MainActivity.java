package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.driverapp.Fragment.Mine_Fragment;
import com.example.driverapp.util.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.sql.Driver;
import java.util.Map;
import java.util.TreeMap;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class MainActivity extends AppCompatActivity {
    String Driver_URL;      // 司机的 websocket 地址
    String ip;
    String driverid;
    private AppClientEndpoint mAppTask; // 声明一个WebSocket客户端任务对象
    private NavViewPager VP_nav;
    private RadioGroup RG_nav;
    private String TAG = "Baihupe";
    private Thread myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取 ip、thread
        application aapplication = (application)getApplication();
        ip = aapplication.getIp();


        // 获取 driverid
        try {
            JSONObject jsonObject = getDriverInfo();
            driverid = jsonObject.getString("driverid");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // 设置 driverURL,为其 websocket 地址
        Driver_URL = "ws://" + ip + "/websocket/driver_" + driverid;

        // 设置 websocket 部分负责监听返回来的消息
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initWebSocket();
            }
        };

        myThread = new Thread(runnable);
        myThread.start();


        /** 1) 获取翻页试图，构建翻页适配器; **/
        VP_nav = findViewById(R.id.VP_nav);
        VP_nav.setScanScroll(false);
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
            Log.i(TAG,"主页面收到的回复消息:"+resp);

            // 对于回调，解析其内容 -- 传递 message、stationid、type；如果stationid相等，添加到消息，否则不添加
            JSONObject jsonObject = new JSONObject(resp);
            JSONObject sender = jsonObject.getJSONObject("sender");
            String stationid = sender.getString("stationid");
            String type = jsonObject.getString("type");
            String message = jsonObject.getString("message");

            /** 发送广播：如果用户停留在聊天页面--接收广播的同时，修改列表；如果没有，该页面广播是关闭的，不用管 **/
            Intent intent = new Intent("WebSocket");
            Bundle bundle = new Bundle();
            bundle.putString("stationid",stationid);
            bundle.putString("type",type);
            bundle.putString("message",message);
            intent.putExtras(bundle);
            sendBroadcast(intent);

            /** 添加到缓存中,缓存中消息类型为 jsonobject **/
            SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
            String history = record.getString("history_msg_"+stationid,"null_record");
            SharedPreferences.Editor edit = record.edit();

            Log.i(TAG,"收到消息"+message);

            // 如果缓存中为空,并且保存第一条消息,创建 JSONArray 放入进去
            if(history.equals("null_record")){
                JSONArray jsonArray = new JSONArray();
                JSONObject receive_item = new JSONObject();
                Msg msg = new Msg();
                receive_item.put("type",Msg.TYPE_RECEIVED);
                receive_item.put("time",msg.getNow_time());

                // 判断消息类型:0 -- 普通消息 1-- 为邀约消息
                if(type.equals("1")){
                    // 对于邀约消息，content 为 jsonarray to string 类型
                    JSONArray jsonArray_content = new JSONArray(message);
                    String invitation = "";

                    for(int i=0;i<jsonArray_content.length();i++){
                        JSONObject item = jsonArray_content.getJSONObject(i);
                        String comName = item.getString("comName");
                        String price = item.getString("price");
                        String number = item.getString("number");
                        invitation += "商品名称："+comName+", 价格："+price+", 数量："+number +"\n";
                    }

                    receive_item.put("content",invitation);
                }else{
                    receive_item.put("content",message);        // 普通消息，直接放入即可
                }

                jsonArray.put(receive_item);
                edit.putString("history_msg_"+stationid,jsonArray.toString());
                edit.commit();

            }else{
                // 不为空，获取 JSONArray，放入jsonobject
                JSONObject receive_item = new JSONObject();
                Msg msg = new Msg();
                receive_item.put("type",Msg.TYPE_RECEIVED);
                receive_item.put("time",msg.getNow_time());

                // 判断消息类型:0 -- 普通消息 1-- 为邀约消息
                if(type.equals("1")){
                    // 对于邀约消息，content 为 jsonarray to string 类型
                    JSONArray jsonArray_content = new JSONArray(message);
                    String invitation = "欢迎购买！\n";

                    for(int i=0;i<jsonArray_content.length();i++){
                        JSONObject item = jsonArray_content.getJSONObject(i);
                        String comName = item.getString("comName");
                        String price = item.getString("price");
                        String number = item.getString("number");
                        invitation += "商品："+comName+", 特售价："+price+", 数量："+number +"\n";
                    }

                    receive_item.put("content",invitation);
                }else{
                    receive_item.put("content",message);        // 普通消息，直接放入即可
                }

                JSONArray jsonArray = new JSONArray(history);
                jsonArray.put(receive_item);
                edit.putString("history_msg_"+stationid,jsonArray.toString());
                edit.commit();

            }
        });

        // 获取WebSocket容器
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            Log.i(TAG,"websocket连接的："+ Driver_URL);
            URI uri = new URI(Driver_URL); // 创建一个URI对象
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


    // 获取司机 Info
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

    /** 获取当前司机的 driverid **/
    // 获取司机信息
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



}