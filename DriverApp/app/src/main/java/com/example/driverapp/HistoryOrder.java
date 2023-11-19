package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

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

public class HistoryOrder extends AppCompatActivity {

    final String TAG = "Baihupe";
    public String ip;
    public String url;

    private ListView listView;
    private List<Order> list;
    private BaseAdapter baseAdapter;


    @Override
    public void onResume() {
        super.onResume();
        try {
            getOrderList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);

        // 获取 ip 和 url
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/shelve/purchaseInfo";

        listView = findViewById(R.id.order_listview);

        // 获取 order 信息并且渲染
        try {
            getOrderList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }


    /** 获取历史订单信息 **/
    public void getOrderList() throws JSONException {

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

        list = new ArrayList<>();
        // 1) 发送请求获取信息
        Map<String,String> headers = getHeader();

        // 2) 发送请求
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url,headers);

        // 3) 解析结果
        JSONObject ans = new JSONObject(response);
        String code = ans.getString("code");
        if(code.equals("200")){
            // 获取 jsonarray 订单数组
            JSONArray orderArray = ans.getJSONArray("data");
            // 解析其中的每一项
            for(int i=orderArray.length()-1;i>=0;i--){
                JSONObject order_item = orderArray.getJSONObject(i);

                // 构造 order 对象存储对应内容
                Order order = new Order();
                order.setStationname(order_item.getString("seller"));
                order.setComname(order_item.getString("comname"));
                order.setNum(order_item.getString("purchasenumber"));
                order.setOrderid(order_item.getString("orderid"));
                order.setSumPrice(order_item.getString("sumprice"));
                order.setTime(order_item.getString("purchasetime"));
                order.setOrderimage(order_item.getString("image"));

                // 加入列表
                list.add(order);
            }

            baseAdapter = new OrderAdapter(list,this);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

        }else{
            Log.i(TAG,"请求历史订单失败");
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