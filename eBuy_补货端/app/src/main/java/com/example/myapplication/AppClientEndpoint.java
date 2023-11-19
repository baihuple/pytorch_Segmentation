package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.example.myapplication.util.NotifyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

@ClientEndpoint
public class AppClientEndpoint {
    private final static String TAG = "Baihupe";
    private Activity mAct;                          // 声明活动实例
    private OnRespListener mListener;                 // 消息应答监听器

    private Session mSession;                       // 连接会话

    private NotificationManager manager;


    // 构造方法
    public AppClientEndpoint(Activity act, OnRespListener listener) {
        mAct = act;
        mListener = listener;
    }

    // 向服务器发送请求报文
    public void sendRequest(String req) throws IOException {
        Log.i(TAG,"发送请求报文"+req);
        if(mSession != null){
            RemoteEndpoint.Basic remote = mSession.getBasicRemote();
            remote.sendText(req);            // 发送文本数据
        }
    }


    // 连接成功后调用
    @OnOpen
    public void onOpen(final Session session) {
        mSession = session;
        Log.i(TAG, "成功创建连接");
    }

    // 收到服务端消息时调用,回调函数
    @OnMessage
    public void processMessage(Session session, String message) {
        Log.i(TAG,"收到ssss消息:"+message);

        // 如果当前为退出登录状态，那么就不接受消息
        SharedPreferences record = mAct.getSharedPreferences("Info_Record",MODE_PRIVATE);
        String status = record.getString("user_status","null");
        if(!status.equals("login")){
            return;
        }

        if (mListener != null) {
            mAct.runOnUiThread(() -> {
                try {
                    if(!message.equals("Hi!") && !message.equals("null") && !message.equals("1")){

                        JSONObject jsonObject = new JSONObject(message);
                        if(jsonObject.getString("type").equals("3")){           // 如果 type = 3，为司机的购买字段

                            jsonObject.put("status","待处理");
                            // 获取订单 jsonobject,放入当前时间
                            JSONObject order = jsonObject.getJSONObject("order");
                            // 存入缓存,jsonArray to string 的数组类型
                            String driver_buy_list = record.getString("driver_buy_list","null");
                            if(driver_buy_list.equals("null")){     // 为空，创建 jsonArray 并且放入
                                JSONArray jsonArray = new JSONArray();
                                jsonArray.put(jsonObject);
                                SharedPreferences.Editor edit = record.edit();
                                edit.putString("driver_buy_list",jsonArray.toString());
                                edit.commit();
                            }else{              // 不为空，获取并加入
                                JSONArray jsonArray = new JSONArray(driver_buy_list);
                                jsonArray.put(jsonObject);
                                SharedPreferences.Editor edit = record.edit();
                                edit.putString("driver_buy_list",jsonArray.toString());
                                edit.commit();
                            }

                            // 发出提示消息
                            String driverid = order.getString("purchaserid");
                            sendSimpleNotify2("消息：来自"+driverid+"号司机","您有新的补货订单");

                        }else{
                            // 解析数据并且返回  0
                            String driverid;
                            String content;
                            Bundle bundle;
                            try {
                                    JSONObject sender = jsonObject.getJSONObject("sender");

                                    driverid = sender.getString("driverid");
                                    content = jsonObject.getString("message");
                                    bundle = new Bundle();

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            sendSimpleNotify("消息：来自"+driverid+"号司机",content,bundle);
                            mListener.receiveResponse(message);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    // 收到服务端错误时调用
    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }


    /** 定义一个 websocket 应答的监听器接口 **/
    public interface OnRespListener{
        void receiveResponse(String resp) throws JSONException;
    }

    private void sendSimpleNotify(String title, String message,Bundle bundle) {
         //创建该intent，设置点击通知后的操作
        Intent clickIntent = new Intent(mAct.getApplicationContext(),Message_list.class);
        clickIntent.putExtras(bundle);

         //创建一个用于页面跳转的延迟意图
        PendingIntent contentIntent = PendingIntent.getActivity(mAct.getApplicationContext(),
                R.string.app_name, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         //创建一个通知消息的建造器
        Notification.Builder builder = new Notification.Builder(mAct.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0开始必须给每个通知分配对应的渠道
            builder = new Notification.Builder(mAct.getApplicationContext(), "0");
        }
        builder.setContentIntent(contentIntent) // 设置内容的点击意图
                .setAutoCancel(true) // 点击通知栏后是否自动清除该通知
                .setSmallIcon(R.drawable.notice_iamge) // 设置应用名称左边的小图标
                .setContentTitle(title) // 设置通知栏里面的标题文本
                .setContentText(message); // 设置通知栏里面的内容文本
        Notification notify = builder.build(); // 根据通知建造器构建一个通知对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotifyUtil.createNotifyChannel(mAct.getApplicationContext(),"0","mychannelname", NotificationManager.IMPORTANCE_HIGH);
        }
        // 从系统服务中获取通知管理器
        NotificationManager notifyMgr = (NotificationManager) mAct.getSystemService(Context.NOTIFICATION_SERVICE);
        // 使用通知管理器推送通知，然后在手机的通知栏就会看到该消息，多条通知需要指定不同的通知编号
        notifyMgr.notify(Integer.parseInt("0"), notify);
        if ( NotificationManager.IMPORTANCE_HIGH!= NotificationManager.IMPORTANCE_NONE) {
//            Toast.makeText(mAct.getApplicationContext(), "已发送渠道消息", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSimpleNotify2(String title, String message) {
        //创建该intent，设置点击通知后的操作
        Intent clickIntent = new Intent(mAct.getApplicationContext(),Driver_pay_list.class);

        //创建一个用于页面跳转的延迟意图
        PendingIntent contentIntent = PendingIntent.getActivity(mAct.getApplicationContext(),
                R.string.app_name, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建一个通知消息的建造器
        Notification.Builder builder = new Notification.Builder(mAct.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0开始必须给每个通知分配对应的渠道
            builder = new Notification.Builder(mAct.getApplicationContext(), "0");
        }
        builder.setContentIntent(contentIntent) // 设置内容的点击意图
                .setAutoCancel(true) // 点击通知栏后是否自动清除该通知
                .setSmallIcon(R.drawable.notice_iamge) // 设置应用名称左边的小图标
                .setContentTitle(title) // 设置通知栏里面的标题文本
                .setContentText(message); // 设置通知栏里面的内容文本
        Notification notify = builder.build(); // 根据通知建造器构建一个通知对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotifyUtil.createNotifyChannel(mAct.getApplicationContext(),"0","mychannelname", NotificationManager.IMPORTANCE_HIGH);
        }
        // 从系统服务中获取通知管理器
        NotificationManager notifyMgr = (NotificationManager) mAct.getSystemService(Context.NOTIFICATION_SERVICE);
        // 使用通知管理器推送通知，然后在手机的通知栏就会看到该消息，多条通知需要指定不同的通知编号
        notifyMgr.notify(Integer.parseInt("0"), notify);
        if ( NotificationManager.IMPORTANCE_HIGH!= NotificationManager.IMPORTANCE_NONE) {
//            Toast.makeText(mAct.getApplicationContext(), "已发送渠道消息", Toast.LENGTH_SHORT).show();
        }
    }


}
