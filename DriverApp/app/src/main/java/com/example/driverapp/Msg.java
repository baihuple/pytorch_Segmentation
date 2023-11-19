package com.example.driverapp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Msg {

    public static final int TYPE_RECEIVED = 0;      // 收到的消息
    public static final int TYPE_SEND = 1;          // 发出的消息

    private String content;                         // 消息内容
    private int type;                               // 消息类型
    private String now_time;                          // 当前时间

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setNow_time(String now_time) {
        this.now_time = now_time;
    }

    public Msg() {
        SimpleDateFormat sim = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Date time = new Date(System.currentTimeMillis());
        this.now_time = sim.format(time);
    };

    public String getNow_time() {
        return now_time;
    }

    public Msg(String content, int type){
        this.content = content;
        this.type = type;
        SimpleDateFormat sim = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Date time = new Date(System.currentTimeMillis());
        this.now_time = sim.format(time);
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

}
