package com.example.driverapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.driverapp.util.NotifyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
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
    public AppClientEndpoint(Activity act,OnRespListener listener) {
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
        Log.i(TAG, "WebSocket服务端返回：" + message);
        SharedPreferences record = mAct.getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String isLogin = record.getString("user_status","null");
        if(isLogin.equals("null")){         // 如果已经退出状态，那么则不做任何操作,不显示通知和消息
            return;
        }

        if (mListener != null) {
            mAct.runOnUiThread(() -> {
                try {
                    // 如果收到的消息为 2, 那么就发送广播 并且直接return
                    if(message.equals("2")){
                        Intent intent = new Intent("device_bind_success");
                        mAct.sendBroadcast(intent);
                    }else if(!message.equals("Hi!") && !message.equals("null") && !message.equals("1")){

                        Log.i(TAG,"收到消息: "+message);

                        // 解析数据,并且返回
                        JSONObject jsonObject = new JSONObject(message);

                        // 如果是乘客端传来的商品购买消息
                        if(jsonObject.getString("type").equals("2")){
                            // 在这里检查货架商品数量
                            sendSimpleNotify2("消息：乘客购买商品","请点击查看详情");

                            if(jsonObject.getString("nomore").equals("0")){
                            // 延迟 5s 进行
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // 查看是否为 0
                                    JSONObject order = null;
                                    try {
                                        order = new JSONObject(jsonObject.getString("order"));
                                        sendSimpleNotify2("缺货提示",order.getString("shelveid")+"号货架："+order.getString("comname")+"已缺货，请及时处理");
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            },2000);
                            }

                        }else{
                            JSONObject sender = jsonObject.getJSONObject("sender");
                            String stationname = sender.getString("stationname");
                            String content = jsonObject.getString("message");
                            String type = jsonObject.getString("type");

                            // 构造 bundle，设置点击横幅进行跳转的参数项
                            Bundle bundle = new Bundle();
                            bundle.putString("stationid",sender.getString("stationid"));
                            bundle.putString("stationname",sender.getString("stationname"));
                            bundle.putString("stationadd",sender.getString("stationaddr"));
                            bundle.putString("stationla",sender.getString("stationla"));
                            bundle.putString("stationlo",sender.getString("stationlo"));
                            bundle.putString("stationtel",sender.getString("adminTel"));

                            // 等尹给邀约信息内部的sender加上adminTel字段
                            // 分为两种：1）如果为 1 邀约信息；2）如果为 0 普通信息

                            if(type.equals("0")){
                                sendSimpleNotify("消息：来自"+stationname,content,bundle);         // 顶部横幅提示
                            }else{
                                sendSimpleNotify("消息：来自"+stationname,"商品上新啦，快来看看吧",bundle);
                            }

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
        // 创建该intent，设置点击通知后的操作
        Intent clickIntent = new Intent(mAct.getApplicationContext(),StationDetail.class);
        clickIntent.putExtras(bundle);

        // 创建一个用于页面跳转的延迟意图
        PendingIntent contentIntent = PendingIntent.getActivity(mAct.getApplicationContext(),
                R.string.app_name, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 创建一个通知消息的建造器
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
            NotifyUtil.createNotifyChannel(mAct.getApplicationContext(),"0","mychannelname", NotificationManager.IMPORTANCE_DEFAULT);
        }
        // 从系统服务中获取通知管理器
        NotificationManager notifyMgr = (NotificationManager) mAct.getSystemService(Context.NOTIFICATION_SERVICE);
        // 使用通知管理器推送通知，然后在手机的通知栏就会看到该消息，多条通知需要指定不同的通知编号
        notifyMgr.notify(Integer.parseInt("0"), notify);
        if ( NotificationManager.IMPORTANCE_DEFAULT!= NotificationManager.IMPORTANCE_NONE) {
//            Toast.makeText(mAct.getApplicationContext(), "已发送渠道消息", Toast.LENGTH_SHORT).show();
        }
    }

    // 弹出乘客的购买信息，跳转到历史购买记录
    private void sendSimpleNotify2(String title, String message) {
        // 创建该intent，设置点击通知后的操作
        Intent clickIntent = new Intent(mAct.getApplicationContext(),HistoryBill.class);

        // 创建一个用于页面跳转的延迟意图
        PendingIntent contentIntent = PendingIntent.getActivity(mAct.getApplicationContext(),
                R.string.app_name, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 创建一个通知消息的建造器
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
