package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HistoryBill extends AppCompatActivity {
    final String TAG = "Baihupe";
    public String ip;
    public String url;

    private ListView listView;
    private TextView sumPrice;
    private List<SellProdudct> list;
    private BaseAdapter baseAdapter;

//    public void onResume() {
//        super.onResume();
//        try {
//            getPurchaseList();
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_bill);

        // 获取 ip 和 url
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/shelve/sellInfo";

        sumPrice = findViewById(R.id.sum_get);
        listView = findViewById(R.id.seller_listview);
        // 获取历史乘客消费订单
        try {
            getPurchaseList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    /** 获取乘客历史消费订单 **/
    public void getPurchaseList() throws JSONException {
        //创建默认的imageloader配置函数[ 进行图片加载  ]
        ImageLoaderConfiguration configuration=new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(500,500)//缓存文件最大宽高
                .threadPoolSize(15) //线程池的加载数量
                .threadPriority(Thread.NORM_PRIORITY-2)//优先级定义
                .memoryCacheSize(2*1024*1024)
                .diskCacheSize(50*1024*1024) //50mb sd卡(本地)缓存最大值
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .imageDownloader(new BaseImageDownloader(this,5*100,30*1000))
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .build();

        //初始化imageloader
        ImageLoader.getInstance().init(configuration);


        // 2) 初始化列表数据
        list = new ArrayList<>();

        // 3) 发送请求获取消费列表
        // 获取头部
        Map<String,String> headers = getHeader();
        // 发送请求
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url,headers);

        // 4) 解析数据
        JSONObject jsonObject = new JSONObject(response);
        String code = jsonObject.getString("code");
        if(code.equals("200")){
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            float sum = 0;
            for(int i=jsonArray.length()-1;i>=0;i--){
                JSONObject item = jsonArray.getJSONObject(i);
                // 构造 sellProduct 对象，填充数据
                SellProdudct sellProdudct = new SellProdudct();
                sellProdudct.setPro_title(item.getString("comname"));
                sellProdudct.setPro_num(item.getString("purchasenumber"));
                sellProdudct.setSumprice(item.getString("sumprice"));
                sellProdudct.setDate(item.getString("purchasetime"));
                sellProdudct.setOrderid(item.getString("orderid"));
                sellProdudct.setImageURI(item.getString("image"));

                sum += Float.parseFloat(item.getString("sumprice"));
                list.add(sellProdudct);
            }

            sumPrice.setText(String.format("%.2f",sum));
            baseAdapter = new SellProductAdapter(list,this);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

        }else{
            Log.i(TAG,"获取消费历史信息失败");
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


}