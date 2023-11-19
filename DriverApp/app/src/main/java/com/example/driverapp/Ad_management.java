package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Headers;


public class Ad_management extends AppCompatActivity {

    final String TAG = "Baihupe";
    public String ip;
    public String url;

    float Price = 0;                        // 所有广告累计总收益
    TextView sum;
    private ListView listView;
    private List<Ad> list;
    private BaseAdapter baseAdapter;

//    @Override
//    protected void onResume() {
//        super.onResume();
//        try {
//            getAdList();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_management);

        // 获取 ip 和 url
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/adManage/adLocalList";

        // 找到组件
        listView = findViewById(R.id.ad_listview);
        sum = findViewById(R.id.sum_Price2);

        // 设置点击事件,点击列表项，进入详情页面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 获取指定项
                Ad ad = list.get(i);

                Intent intent = new Intent(Ad_management.this,Ad_detail.class);
                Bundle bundle = new Bundle();
                bundle.putString("adid",ad.getId());
                bundle.putString("adname",ad.getBrand());

                String price = ad.getPrice().substring(2,ad.getPrice().length()-3);
                bundle.putString("numberrevenue",price);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // 获取列表并渲染
        try {
            getAdList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void getAdList() throws JSONException {

        // 2) 初始化列表数据
        list = new ArrayList<>();

        // 3) 发送网络请求
        Map<String,String> headers = getHeader();
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url,headers);
//        Log.i(TAG,"这是广告列表的回复:"+response);

        // 4) 解析结果
        JSONObject jsonObject = new JSONObject(response);
        String code = jsonObject.getString("code");
        String message = jsonObject.getString("message");
        if(code.equals("200")){
            JSONArray data = jsonObject.getJSONArray("data");
            // 广告数据不用存到缓存，进行渲染
            for(int i=0;i<data.length();i++){
                JSONObject ans = data.getJSONObject(i);

                // 构造对象
                Ad ad = new Ad();
                ad.setBrand(ans.getString("adname"));
                ad.setId(ans.getString("adid"));
                ad.setPrice(ans.getString("numberrevenue"));

                // 对类型和价钱筛选
                String type = ans.getString("type");
                if(type.equals("1")){           // 1 为图片
                    ad.setType("图片广告");
                }else{                          // 2 为视频
                    ad.setType("视频广告");
                }

                ad.setPrice("￥ "+ans.getString("numberrevenue")+" /次");
                list.add(ad);

                // 对于每一个广告，请求播放信息列表
                getPlayList(ans.getString("adid"),ans.getString("numberrevenue"));

            }

            sum.setText(String.format("%.2f",Price));

            baseAdapter = new AdListAdapter(list,this);
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

    public void getPlayList(String adid,String price) throws JSONException {

        // 3) 发送网络请求
        Map<String,String> headers = getHeader();           /** 此处 getHeader 与其他页面的 getHeader 函数不一样，复用的时候请注意 **/
        HttpRequest httpRequest = new HttpRequest();
        String url_ = "http://"+ip+"/driver/adManage/playInfo/";
        String response = httpRequest.OkHttpsGet(url_+adid,headers);
        Log.i(TAG,"这是广告详细信息的回复:"+response);

        JSONObject jsonObject = new JSONObject(response);
        String code = jsonObject.getString("code");
        String message = jsonObject.getString("message");

        if(code.equals("200")){
            // 渲染数据
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject ans = jsonArray.getJSONObject(i);

                // 计算价格
                float playnum = Float.valueOf(ans.getString("playnumber"));
                float sum_price = playnum * Float.valueOf(price);

                Price += sum_price;
            }

        }else{
            myDialog_Warning(message);
        }


    }

}