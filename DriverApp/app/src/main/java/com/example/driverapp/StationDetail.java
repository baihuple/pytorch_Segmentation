package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driverapp.util.MapUtil;
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

public class StationDetail extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private String TAG = "Baihupe";
    private StationInfo stationInfo;
    public String ip;
    private String url;

    // 1) 获取列表组件
    private GridView gridView;
    private List<Product> list;
    private BaseAdapter baseAdapter;

    @Override
    public void onResume() {
        super.onResume();
        getProductList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station_detail);

        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/shelve/stationInfo?stationid=";

        /** 接收传递数据 **/
        Intent intent = getIntent();
        if(intent!=null){
            stationInfo = new StationInfo();
            Bundle bundle = intent.getExtras();
            stationInfo.setStationid(bundle.getString("stationid"));
            stationInfo.setStationname(bundle.getString("stationname"));
            stationInfo.setStationaddr(bundle.getString("stationadd"));
            stationInfo.setStationla(bundle.getString("stationla"));
            stationInfo.setStationlo(bundle.getString("stationlo"));
            stationInfo.setAdminTel(bundle.getString("stationtel"));
        }

        // 绑定组件、设置内容
        TextView stationName= findViewById(R.id.stname);
        TextView stationAdd = findViewById(R.id.staddr);
        TextView stationPhone = findViewById(R.id.staphone);
        gridView = findViewById(R.id.Station_GridView);
        ImageView shopping_car = findViewById(R.id.shopping_car_BT);
        ImageView navigate = findViewById(R.id.navigate_IV);
        ImageView message = findViewById(R.id.message_IV);


        stationName.setText(stationInfo.getStationname());
        stationAdd.setText(stationInfo.getStationaddr());
        stationPhone.setText(stationInfo.getAdminTel());

        // 设置跳转腾讯地图
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTencentMap();
            }
        });


        // 请求网络信息并渲染图表
        getProductList();

        // 消息点击事件，点击跳转到消息页面
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 传递补货站的 stationid,司机的 driverid
                Intent intent1 = new Intent(StationDetail.this,StationContact.class);
                Bundle bundle = new Bundle();
                bundle.putString("stationid",stationInfo.getStationid());
                bundle.putString("stationname",stationInfo.getStationname());

                intent1.putExtras(bundle);
                startActivity(intent1);
            }
        });

        // 购物车点击事件,跳转购物车页面
        shopping_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(StationDetail.this,ShoppingCar.class);
                // 传递参数 stationid，其余信息可以从缓存中获取
                Bundle bundle = new Bundle();
                bundle.putString("stationid",stationInfo.getStationid());
                bundle.putString("station_name",stationInfo.getStationname());

                intent1.putExtras(bundle);
                startActivity(intent1);
            }
        });

    }

    public void getProductList(){

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

        // 1) 请求商品信息数据
        SharedPreferences record = getSharedPreferences("Driver_Info", MODE_PRIVATE);
        String token = record.getString("token","获取值为空");

        Log.i("baihupe","check:"+token);

        Map<String,String> headers = new TreeMap<>();
        headers.put("Authorization",token);
        headers.put("identity","0");

        String url_true = url + stationInfo.getStationid();
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url_true,headers);

        /** 解析商品数据 **/
        try {
            JSONObject jsonObject_answer = new JSONObject(response);
            String code = jsonObject_answer.get("code").toString();
            if(code.equals("200")){
                // 获取返回商品数据的 json 数组形式
                JSONArray jsonArray = (JSONArray) jsonObject_answer.getJSONArray("data");

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject producJSON = (JSONObject) jsonArray.get(i);
                    // 构造 product 类型对象，保存商品信息
                    Product product = new Product();
                    product.setComname(producJSON.get("comName").toString());
                    product.setPrice(producJSON.get("price").toString());
                    product.setStoreid(producJSON.get("storeid").toString());
                    product.setComid(producJSON.get("comid").toString());
                    product.setComBrand(producJSON.get("comBrand").toString());          // 添加商品种类

                    String sellState = producJSON.get("sellState").toString();      // 判断是否缺货
                    if(sellState.equals("缺货")){
                        product.setNumber("缺货中");
                    }else{
                        product.setNumber("剩余 "+producJSON.get("number").toString());
                    }

                    // 设置图片 URL
                    product.setImage_uri(producJSON.get("imageURL").toString());
                    list.add(product);
                }

            }else{
                Log.i(TAG,"补货站商品请求失败");
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

//        Product product1 = new Product();
//        product1.setComname("shit");
//        product1.setPrice("17.8");
//        product1.setNumber("剩余 15");
//        product1.setStoreid("1");
//        product1.setComid("9123489");
//        list.add(product1);
//
//        Product product2 = new Product();
//        product2.setComname("shit");
//        product2.setPrice("17.8");
//        product2.setStoreid("2");
//        product2.setComid("234908520");
//        product2.setNumber("缺货");
//        list.add(product2);
//
//        Product product3 = new Product();
//        product3.setComname("shit");
//        product3.setPrice("17.8");
//        product3.setNumber("剩余 3");
//        product3.setStoreid("3");
//        product3.setComid("8923485");
//        list.add(product3);

        baseAdapter = new ProductAdapter(this,list,mListener);
        gridView.setAdapter(baseAdapter);
        gridView.setOnItemClickListener(this);                                                      /** 设置列表项、列表内部元素的点击事件 **/
        baseAdapter.notifyDataSetChanged();
    }


    /** 点击列表项响应时间--无响应 **/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(StationDetail.this,"点击了"+i+"项",Toast.LENGTH_SHORT).show();
    }


    /** 点击加号，加入购物车 **/
    private ProductAdapter.MyClickListener mListener = new ProductAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {
            // 1) 获取该项商品信息
            Product product = list.get(position);
            String number = product.getNumber();

            if(number.equals("缺货中")){                            // 商品缺货，不能加入购物车
                myDialog_Warning("商品缺货中，请重新选择");
            }else if(number.equals("剩余 0")){
                myDialog_Warning("商品卖光了，下次再来吧");
            }else {                                               // 正常加入购物车

                // 2) 缓存获取 shopping_car，有了则添加，没有则创建字段加入
                SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                String notice = record.getString("shopping_car"+stationInfo.getStationid(),"no_shopping_car");

                // 2.1) 没有字段，则创建 JSONArray 类型，添加 JSONObject 数据
                if(notice.equals("no_shopping_car")){
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject = new JSONObject();

                    // 列表存放商品相关信息
                    String comName = product.getComname();
                    String price = product.getPrice();
                    String comid = product.getComid();
                    String real_num = product.getNumber();
                    String image_uri = product.getImage_uri();
                    String comBrand = product.getComBrand();
                    int num = 1;                                // num 为购物车商品数量

                    jsonObject.put("comName",comName);
                    jsonObject.put("price",price);
                    jsonObject.put("comid",comid);
                    jsonObject.put("num",num);
                    jsonObject.put("image_uri",image_uri);
                    jsonObject.put("real_num",real_num.substring(3));
                    jsonObject.put("comBrand",comBrand);

                    // 放入数组中
                    jsonArray.put(jsonObject);

                    // 保存到 shopping_car 缓存中
                    SharedPreferences.Editor edit = record.edit();
                    edit.putString("shopping_car"+stationInfo.getStationid(),jsonArray.toString());
                    edit.commit();

                    // 提示添加成功
                    myDialog_Successful("添加成功");
                }else{

                    // 3) 取出 JSONarray 数组，检查是否放过，未放过再放入
                    JSONArray jsonArray = new JSONArray(notice);
                    // 如果 comid 不存在则添加
                    String comid = product.getComid();
                    // 标志位：0 --- 未保存过；1---保存过
                    int flag = 0;
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject isPut = jsonArray.getJSONObject(i);
                        if(isPut.get("comid").equals(comid)){
                            flag = 1;
                            break;
                        }
                    }

                    // 未保存过
                    if(flag == 0){
                        JSONObject jsonObject = new JSONObject();
                        // 列表存放商品相关信息
                        String comName = product.getComname();
                        String price = product.getPrice();
                        String real_num = product.getNumber();
                        String image_uri = product.getImage_uri();
                        String comBrand = product.getComBrand();
                        int num = 1;                                // num 为购物车商品数量

                        jsonObject.put("comName",comName);
                        jsonObject.put("price",price);
                        jsonObject.put("comid",comid);
                        jsonObject.put("num",num);
                        jsonObject.put("image_uri",image_uri);
                        jsonObject.put("real_num",real_num.substring(3));
                        jsonObject.put("comBrand",comBrand);

                        jsonArray.put(jsonObject);
                        //存入缓存中
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("shopping_car"+stationInfo.getStationid(),jsonArray.toString());
                        edit.commit();

                        String c = record.getString("shopping_car"+stationInfo.getStationid(),"no");
                        Log.i(TAG,c);

                        myDialog_Successful("添加成功");
                    }else if(flag == 1){        // 保存过
                        myDialog_Warning("请勿重复添加商品");
                    }
                }
            }

        }
    };

    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setIcon(R.drawable.successful);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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


    /** 打开腾讯地图 **/
    private void openTencentMap(){
            SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
            String now_la = record.getString("position_la","null");
            String now_lo = record.getString("position_lo","null");

            if(now_lo.equals("null") || now_la.equals("null")){
                myDialog_Warning("获取当前位置失败");
            }else{
                MapUtil.openTencentMap(this,Float.parseFloat(now_la),Float.parseFloat(now_lo),"你的位置",Float.parseFloat(stationInfo.getStationla()),Float.parseFloat(stationInfo.getStationlo()),stationInfo.getStationname());
            }
    }


}