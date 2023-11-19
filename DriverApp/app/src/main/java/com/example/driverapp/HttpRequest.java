package com.example.driverapp;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

    String result = "";
    final String TAG = "baihuple";

    /** GET: 调用网络请求函数 **/
    public String OkHttpsGet(String url,Map<String,String> map_header){
        /** 1) OkHttpClient类创建 okhttp 客户端对象 **/
        OkHttpClient client = new OkHttpClient();

        map_header.put("Accept-Language","zh-CN");       // GET 请求必须加上
        Headers.Builder builder_header = new Headers.Builder();
        for(String key: map_header.keySet()){
            builder_header.add(key, Objects.requireNonNull(map_header.get(key)));
        }
        Headers headers = builder_header.build();

        /** 2) 使用 Request 类创建 GET/POST 的请求结构 **/
        Request request = new Request.Builder().headers(headers).get().url(url).build();

        /** 3) 调用 okhttpclient 的 newCall 方法，传入 2）的请求结构 request，创建 Call 类型请求对象 **/
        Call call = client.newCall(request);


        /** 4) 调用 Call 对象的 enqueue方法，加入本此请求到 HTTP 请求队列 [ 同时写入回调函数, 因为是异步调用，设置接口应答的回调方法] **/
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("GET_REQUEST","Get request has errors!!!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resp = response.body().string();
                    Log.i("baihuple_GET_REQUEST",resp);
                    result = resp;
            }
        });

        while (true){
            if(!TextUtils.isEmpty(result)){
                return result;
            }
        }
    }


    /** POST: 调用网络请求函数 JSON **/
    public String OkHttpsPost(String url, JSONObject jsonObject, Map<String,String> map_header) throws JSONException {             // 第三个参数为 Map 类型的参数，用来做 header
        /** 1) 转化报文为 JSON 格式 **/
        String jsonString = jsonObject.toString();

        /** 2) 创建一个 POST 方式的请求结构: 传入 json 数据 **/
        RequestBody body = RequestBody.create(jsonString, MediaType.parse("application/json;charset=utf-8"));

        /** 3) 创建 OkHttpClient 对象 **/
        OkHttpClient client = new OkHttpClient();

        Request request;

        /** 4) 创建请求 Request 对象 **/
        if(map_header == null){
            request = new Request.Builder().post(body).url(url).build();
        }else{
            /** 3.5) 创建 header 对象 **/
            Headers.Builder builder_header = new Headers.Builder();
            for(String key: map_header.keySet()){
                builder_header.add(key, Objects.requireNonNull(map_header.get(key)));
            }
            Headers headers = builder_header.build();
            request = new Request.Builder().headers(headers).post(body).url(url).build();
        }


        /** 5) 创建 Call 对象 **/
        Call call = client.newCall(request);

        /** 6) 加入 HTTP 请求队列。[异步调用，设置回调] **/
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG,"POST 请求失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                Log.i(TAG,"POST 请求成功:"+resp);
                result = resp;
            }
        });

        while (true){
            if(!TextUtils.isEmpty(result)){
                return result;
            }
        }

    }

    /** POST: 调用网络请求函数 JSON, formBody 为表单对象 **/
    public String OkHttpsPost_F(String url, FormBody formBody, Map<String,String> map_header) throws JSONException {             // 第三个参数为 Map 类型的参数，用来做 header

        /** 3) 创建 OkHttpClient 对象 **/
        OkHttpClient client = new OkHttpClient();

        /** 3.5) 创建 header 对象 **/
        Headers.Builder builder_header = new Headers.Builder();
        for(String key: map_header.keySet()){
            builder_header.add(key, Objects.requireNonNull(map_header.get(key)));
        }
        Headers headers = builder_header.build();

        /** 4) 创建请求 Request 对象 **/
        Request request = new Request.Builder().headers(headers).post(formBody).url(url).build();

        /** 5) 创建 Call 对象 **/
        Call call = client.newCall(request);

        /** 6) 加入 HTTP 请求队列。[异步调用，设置回调] **/
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG,"POST 请求失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resp = response.body().string();
                Log.i(TAG,"POST 请求成功:"+resp);
                result = resp;
            }
        });

        while (true){
            if(!TextUtils.isEmpty(result)){
                return result;
            }
        }

    }





}
