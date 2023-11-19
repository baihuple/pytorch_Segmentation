package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Ad_detail extends AppCompatActivity {

    final String TAG = "Baihupe";
    public String ip;
    public String url;
    public String adprice;
    String adid;
    String adname;

    private ListView listView;
    private List<AdDetail> list;
    private BaseAdapter baseAdapter;
    TextView allPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_detail);

        // 获取传递的数据
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            adid = bundle.getString("adid");
            adname = bundle.getString("adname");
            adprice = bundle.getString("numberrevenue");
        }

        // 获取 ip 和 url
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/adManage/playInfo/";

        // 找到组件
        TextView adname_TV = findViewById(R.id.ad_name);
        adname_TV.setText(adname);

        listView = findViewById(R.id.ad_detail_listview);
        allPrice = findViewById(R.id.sum_Price);

        // 获取该广告播放详情
        try {
            getPlayList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void getPlayList() throws JSONException {

        // 2) 初始化列表数据
        list = new ArrayList<>();

        // 3) 发送网络请求
        Map<String,String> headers = getHeader();           /** 此处 getHeader 与其他页面的 getHeader 函数不一样，复用的时候请注意 **/
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url+adid,headers);
        Log.i(TAG,"这是广告详细信息的回复:"+response);

        JSONObject jsonObject = new JSONObject(response);
        String code = jsonObject.getString("code");
        String message = jsonObject.getString("message");

        float Price = 0;

        if(code.equals("200")){
            // 渲染数据
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject ans = jsonArray.getJSONObject(i);

                // 创建对象保存
                AdDetail adDetail = new AdDetail();
                adDetail.setDate(ans.getString("playdate"));
                adDetail.setTime(ans.getString("playnumber"));

                // 计算价格
                float playnum = Float.valueOf(ans.getString("playnumber"));
                float sum_price = playnum * Float.valueOf(adprice);

                Price += sum_price;

                adDetail.setPrice(String.format("%.2f",sum_price));
                list.add(adDetail);
            }

            allPrice.setText(String.format("%.2f",Price));
            baseAdapter = new Ad_detailAdapter(list,this);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

        }else{
            myDialog_Warning(message);
        }


    }

    // 获取并封装token和identity
    public Map<String,String> getHeader(){
        Map<String,String> headers = new TreeMap<>();

        // 获取 token
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String token = record.getString("token","token_null");
        if(token.equals("token_null")){
            return null;
        }else{
            headers.put("Authorization",token);
            headers.put("identity","0");
            headers.put("Content-Type","application/x-www-form-urlencoded");
            return headers;
        }
    }

    public void myDialog_Warning(String message){
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

}