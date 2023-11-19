package com.example.myapplication;

import android.app.Application;

public class application extends Application {

    final String ip = "47.115.221.250:80";

//    final String ip = "192.168.1.113:80";
    public String getIp() {
        return ip;
    }

}
