package com.example.driverapp;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ShoppingCar extends AppCompatActivity implements AdapterView.OnItemClickListener {


    final String TAG = "Baihupe";
    public String ip;
    public String url;
    public String ap_time;

    public TextView appointment_time;           // 预约时间的显示框

    public String stationid;                // 补货站 id
    public String station_name;                   // 补货站名称

    private ListView listView;
    private List<Product> list;
    private BaseAdapter baseAdapter;

    private TextView sumPrice;

    @Override
    public void onResume() {
        super.onResume();
        try {
            getShoppingCarList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_car);

        // 获取 ip 和 url
        application application = (application)getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/order/buy";

        // 获取 stationid 和 stationname
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            stationid = bundle.getString("stationid");
            station_name = bundle.getString("station_name");
        }


        Button pay = findViewById(R.id.pay_BT);
        sumPrice = findViewById(R.id.pay_money);
        // 找到 listview 组件
        listView = findViewById(R.id.shoppingCar_listview);
        ImageView appointment = findViewById(R.id.appoingment);
        appointment_time = findViewById(R.id.appoingment_time);

        // 点击弹出时间下拉框
        appointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 获取当前日期
                SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                //获取当前时间
                String str = formatter.format(curDate);
                String year = str.substring(0,4);
                String month = str.substring(5,7);
                String date = str.substring(8,10);
                Log.i(TAG,"month"+month);

                Log.i(TAG,"当前时间"+year+"-"+month+"-"+date);

                new DatePickerDialog(ShoppingCar.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {      // 点击选中之后的处理事件
                        Log.i(TAG,"选择了"+i+"-"+(i1+1)+"-"+i2);
                        String YMD = i+"-"+(i1+1)+"-"+i2+" ";

                        // 如果选择日期比当前日期早，则进行提示
                        if( i > Integer.valueOf(year) || (i == Integer.valueOf(year) && i1+1>Integer.valueOf(month)) ||( (i == Integer.valueOf(year) && (i1+1) == Integer.valueOf(month)) && i2 >= Integer.valueOf(date))){
                            popTime(YMD,Integer.valueOf(year),i,Integer.valueOf(month),i1+1,Integer.valueOf(date),i2);
                        }else{
                            myDialog_Warning("请您选择正确的日期");
                            return;
                        }
                    }
                },Integer.valueOf(year),Integer.valueOf(month)-1,Integer.valueOf(date)).show();
            }
        });
//

        // 获取缓存中的购物车信息
        try {
            getShoppingCarList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // 点击结算按钮结算
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ap_time == null){
                    myDialog_Warning("请您先预约时间再支付");
                    return;
                }

                // 发送订单消息
                // 1) 构造 header
                Map<String,String> headers = getHeader();

                // 2) 构造 body 共有部分
                JSONObject jsonObject = new JSONObject();
                // 先获取司机信息
                try {
                    JSONObject driver_Info = getDriverInfo();

                    jsonObject.put("sellerid",stationid);
                    jsonObject.put("seller",station_name);
                    jsonObject.put("purchaserid",driver_Info.getString("deviceID"));        //??????
                    jsonObject.put("purchaser",driver_Info.getString("name"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                };

                // 3） 构造 body 私有部分,获取购物车列表,计算每一类商品价格
                SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                String shopping_car = record.getString("shopping_car"+stationid,"no_list");

                if(shopping_car.equals("no_list")){
                    myDialog_Warning("没有需要结算的商品");
                    return;
                }else{
                    try {
                        JSONArray jsonArray = new JSONArray(shopping_car);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            // 获取商品数量、单价、计算总价格
                            String num = jsonObject1.getString("num");
                            String price = jsonObject1.getString("price");
                            String name = jsonObject1.getString("comName");
                            String comid = jsonObject1.getString("comid");
                            float sumPrice = Float.parseFloat(num)*Float.parseFloat(price);
                            String comBrand = jsonObject1.getString("comBrand");

                            // 将剩余部分填充到订单信息里面
                            jsonObject.put("comid",comid);
                            jsonObject.put("comname",name);
                            jsonObject.put("purchasenumber",num);
                            jsonObject.put("sumprice",String.valueOf(sumPrice));
                            jsonObject.put("brand",comBrand);
                            jsonObject.put("time",ap_time);

                            // 发送请求
                            HttpRequest httpRequest = new HttpRequest();
                            String response = httpRequest.OkHttpsPost(url,jsonObject,headers);

                            // 解析请求
                            JSONObject json_answer = new JSONObject(response);
                            if(json_answer.getString("code").equals("200")){
                                /** 购买成功 **/
                                Log.i(TAG,"成功发送订单消息:"+name);
                            }else{
                                Log.i(TAG,"发送订单消息失败:"+name);
                            }

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                // 跳转到付款码页面，贴出收款码
                Intent intent1 = new Intent(ShoppingCar.this,pay_code.class);
                Bundle bundle = new Bundle();
                bundle.putString("sumPrice",sumPrice.getText().toString());
                bundle.putString("stationid",stationid);
                intent1.putExtras(bundle);

                // 清空时间
                appointment_time.setText("尚未预约时间");
                startActivity(intent1);
            }
        });

    }



    public void getShoppingCarList() throws JSONException {

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

        // 3) 获取缓存信息
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String shoppingcar_List = record.getString("shopping_car"+stationid,"no_list");

        if(shoppingcar_List.equals("no_list")){         // 购物车里没有东西
            // Log.i(TAG,"购物车为空");
            baseAdapter = new ShoppingCarAdapter(list,this,myClickListenerminus,myClickListenerplus);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

            sumPrice.setText("0");
            myDialog_Warning("购物车为空");
            return;
        }else{
            // 购物车里有东西
            // 4) 获取购物车数据,填充列表
            JSONArray jsonArray = new JSONArray(shoppingcar_List);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // 构造 Product 对象，填充到列表中
                Product product = new Product();
                product.setComname(jsonObject.getString("comName"));
                product.setPrice(jsonObject.getString("price"));
                product.setComid(jsonObject.getString("comid"));
                product.setNumber(jsonObject.getString("num"));
                product.setImage_uri(jsonObject.getString("image_uri"));
                product.setReal_num(jsonObject.getString("real_num"));
                product.setComBrand(jsonObject.getString("comBrand"));

                // 加入列表
                list.add(product);
            }

            baseAdapter = new ShoppingCarAdapter(list,this,myClickListenerminus,myClickListenerplus);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();

            // 5）操作页面数据

            // 重新计算总价格
            float sum = getSum();
            // 修改页面总价栏目


            sumPrice.setText(String.format("%.2f",sum));
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    sumPrice.setText(String.valueOf(sum));
//                }
//            });

        }

    }

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


    /** 点击列表项 **/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(ShoppingCar.this,"点击列表项"+i,Toast.LENGTH_SHORT).show();
    }


    /** 抽象实现1：列表减法 **/
    private ShoppingCarAdapter.MyClickListener1 myClickListenerminus = new ShoppingCarAdapter.MyClickListener1() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {

            Product product = list.get(position);
            String num = product.getNumber();               // 购物车的数量

            // 判断数量是否为: 1  --- 为 1 弹出确定删除吗？
            if(num.equals("1")){

                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCar.this);
                builder.setTitle("注意");
                builder.setMessage("确定将该商品移出购物车？");
                builder.setIcon(R.drawable.attention);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // 清除该项缓存，重新刷新列表
                        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                        String shopping_car = record.getString("shopping_car"+stationid,"null");
                        if(shopping_car.equals("null")){
                            Log.i(TAG,"删除商品出现问题");
                        }else{
                            try {
                                JSONArray shopping_Array = new JSONArray(shopping_car);
                                // 查找该项，并清除
                                for(int j=0;j<shopping_Array.length();j++){
                                    JSONObject shopping_item = shopping_Array.getJSONObject(j);
                                    if(shopping_item.getString("comid").equals(product.getComid())){
                                        shopping_Array.remove(j);
                                        break;
                                    }
                                }

                                // 提交到缓存
                                SharedPreferences.Editor edit = record.edit();
                                edit.putString("shopping_car"+stationid,shopping_Array.toString());
                                edit.commit();

                                // 刷新列表
                                getShoppingCarList();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }else{
                // 改变数量
                int num_int = Integer.parseInt(num);
                num_int --;

                // 修改缓存中记录的数量
                SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                String shopping_car = record.getString("shopping_car"+stationid,"null");
                if(shopping_car.equals("null")){
                    Log.i(TAG,"减少商品数量出现问题");
                }else{
                    try {
                        JSONArray shopping_Array = new JSONArray(shopping_car);
                        // 查找该项，并将数量 -1
                        for(int j=0;j<shopping_Array.length();j++){
                            JSONObject shopping_item = shopping_Array.getJSONObject(j);
                            if(shopping_item.getString("comid").equals(product.getComid())){
                                shopping_item.put("num",String.valueOf(num_int));
                                break;
                            }
                        }

                        // 提交到缓存
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("shopping_car"+stationid,shopping_Array.toString());
                        edit.commit();

                        // 刷新列表
                        getShoppingCarList();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

        }
    };


    /** 抽象实现2：列表加法 **/
    private ShoppingCarAdapter.MyClickListener2 myClickListenerplus = new ShoppingCarAdapter.MyClickListener2() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {
            Product product = list.get(position);
            int num = Integer.parseInt(product.getNumber());
            int real_num = Integer.parseInt(product.getReal_num());

            // 如果购物车数量比补货站数量少,那么可以添加，否则不行
            if(num < real_num){
                // 改变数量
                num++;

                // 修改缓存中记录的数量
                SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                String shopping_car = record.getString("shopping_car"+stationid,"null");
                if(shopping_car.equals("null")){
                    Log.i(TAG,"加法商品数量出现问题");
                }else{
                    try {
                        JSONArray shopping_Array = new JSONArray(shopping_car);
                        // 查找该项，并将数量 +11
                        for(int j=0;j<shopping_Array.length();j++){
                            JSONObject shopping_item = shopping_Array.getJSONObject(j);
                            if(shopping_item.getString("comid").equals(product.getComid())){
                                shopping_item.put("num",String.valueOf(num));
                                break;
                            }
                        }

                        // 提交到缓存
                        SharedPreferences.Editor edit = record.edit();
                        edit.putString("shopping_car"+stationid,shopping_Array.toString());
                        edit.commit();

                        // 刷新列表
                        getShoppingCarList();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else{          // 相等，则不能添加
                myDialog_Warning("已达到最大数量");
            }

        }
    };


    /** 计算总价格 **/
    private float getSum() throws JSONException {

        // 1) 获取缓存指定字段
        SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String shopping_car = record.getString("shopping_car"+stationid,"no_product");

        // 2) 如果没有商品，返回为 0
        if(shopping_car.equals("no_product")){
            return 0;
        }else{
            float sum = 0;
            // 3) 获取每一项，计算价格
            JSONArray jsonArray = new JSONArray(shopping_car);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // 获取数量
                float num = Float.valueOf(jsonObject.getString("num"));
                // 获取单价
                float price = Float.valueOf(jsonObject.getString("price"));
                // 计算二者乘积，并且加和
                sum += num*price;
            }

            // 返回总价格
            return sum;
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

    // 获取司机信息
    public JSONObject getDriverInfo() throws JSONException {
        // 向服务器发送请求,获取司机信息
        // 1) 获取 token
        SharedPreferences record = getSharedPreferences("Driver_Info", MODE_PRIVATE);
        String token = record.getString("token","token_null");
        String Info = record.getString("Info","Info_null");

        if(!Info.equals("Info_null")){
            return new JSONObject(Info);
        }else{
            if(token.equals("token_null")){
                Log.i(TAG,"购物车界面获取 token 失败");
            }else{
                // 2) 构造头部
                Map<String,String > headers = getHeader();

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();

                String url2 = "http://"+ ip + "/driver/infoManage/driverInfo";

                String response = httpRequest.OkHttpsGet(url2,headers);

                // 4) 解析结果
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("message");
                    if (code.equals("200")){
                        String data = jsonObject.getString("data");
                        JSONObject driverInfo = new JSONObject(data);
                        return  driverInfo;
                    }else{
                        Log.i(TAG,"获取司机信息失败");
//                    Toast.makeText(getContext(),"获取司机信息失败",Toast.LENGTH_SHORT).show();
                        return  null;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    // 选择时间
    private void popTime(String YMD,int now_year,int year,int now_month,int month,int now_date,int date){
        // 获取当前时间

        new TimePickerDialog(ShoppingCar.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {

                // 获取当前日期
                SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                //获取当前时间
                String str = formatter.format(curDate);
                String hour = str.substring(11,13);
                String minute = str.substring(14,16);

                if((year > now_year) || (year == now_year && month > now_month) || (year == now_year && month == now_month && date > now_date ) || (year == now_year && month == now_month && date == now_date && i > Integer.valueOf(hour) ) || (year == now_year && month == now_month && date == now_date && i ==  Integer.valueOf(hour) && i1 > Integer.valueOf(minute))){
                    Log.i(TAG,YMD+i+":"+i1);

                    String hour_change = "",minute_change = "";

                    if(i<10){
                        hour_change = "0" + i;
                    }else{
                        hour_change = String.valueOf(i);
                    }
                    if(i1<10){
                        minute_change = "0" + i1;
                    }else{
                        minute_change = String.valueOf(i1);
                    }

                    String time = YMD + hour_change + ":" + minute_change;

                    appointment_time.setText(time);
                    ap_time = time;
                }else{
                    Log.i(TAG,YMD+i+":"+i1);
                    myDialog_Warning("请您选择正确的时间");
                    return;
                }
            }
        },0,0,true).show();
    }

}