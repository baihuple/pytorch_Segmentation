
package com.example.myapplication.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.example.myapplication.Driver_pay_list;
import com.example.myapplication.EditProduct;
import com.example.myapplication.HttpRequest;
import com.example.myapplication.Product;
import com.example.myapplication.ProductAdapter;
import com.example.myapplication.R;
import com.example.myapplication.ScanActivity;
import com.example.myapplication.application;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Product_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Product_Fragment extends Fragment implements AdapterView.OnItemClickListener {

    public String ip;
    public String url;
    final String TAG = "Baihupe";

    // 1) 获取列表组件
    private GridView gridView;

    private List<Product> list;
    private BaseAdapter baseAdapter;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Product_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Product_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Product_Fragment newInstance(String param1, String param2) {
        Product_Fragment fragment = new Product_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // 每次进入页面重新加载
    /** 服务器端必须开启时再放出来 **/
    @Override
    public void onResume() {
        super.onResume();
        getProductList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_product, container, false);

        // 获取ip和全局变量
        application app = (application)getActivity().getApplication();
        ip = app.getIp();
        url = "http://"+ip+"/repl/currentComs";

        // 点击跳转新增商品界面
        Button product_plus_BT =root.findViewById(R.id.pro_plus);
        product_plus_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });

        gridView = root.findViewById(R.id.product_GridView);
        // gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(),true,true));         // 不允许滑动的时候加载
        getProductList();

        return root;
    }

    /** 点击列表项响应事件 --- 跳转到修改信息基本界面 **/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // 获取当前列表项信息
        Product product = list.get(i);
        String number = product.getNumber();
        String price = product.getPrice();
        String comName = product.getComname();
        String storeid = product.getStoreid();

        //Toast.makeText(getContext(),"你点击了"+i,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), EditProduct.class);

        // 传递当前列表项信息
        Bundle bundle = new Bundle();
        bundle.putString("comName",comName);
        bundle.putString("price",price);
        bundle.putString("number",number);
        bundle.putString("storeid",storeid);

        intent.putExtras(bundle);

        // 跳转页面
        startActivity(intent);
    }

    // 实现类
    private ProductAdapter.MyClickListener mListener = new ProductAdapter.MyClickListener() {       /** 点击按钮，将商品信息保存到本地 **/
    @Override
    public void myOnClick(int position, View v) throws JSONException {
        Product product = list.get(position);                                                       /** 获取商品列表信息 **/
        String number = product.getNumber();
        if(number.equals("缺货中")){
            myDialog_Warning("商品缺货中，请重新选择");
            // Toast.makeText(getContext(),"商品缺货中",Toast.LENGTH_SHORT).show();
        }else if(number.equals("剩余 0")){
            myDialog_Warning("商品已售完");
        } else{
            // 1) 从缓存获取 invitation, 如果已经有了，那么处理，没有则直接创建字段加入
            SharedPreferences record = getActivity().getSharedPreferences("Info_Record",MODE_PRIVATE);
            String notice = record.getString("invitation","no_invitation");

            Log.i(TAG,"商品信息:"+notice);

            /** SharedPreferences.Editor edit = record.edit();    //用来开发人员手动清空invitation缓存
             edit.remove("invitation");
             edit.commit(); **/

            // 1.2) 没有此字段，创建 JSONArray 类型，添加 JSONObject 数据，最后转化成字符串保存
            if(notice.equals("no_invitation")){
                JSONArray jsonArray = new JSONArray();
                JSONObject productJson = new JSONObject();

                String comName = product.getComname();
                String price = product.getPrice();
                String storeid = product.getStoreid();               // 新创建的邀约 storeid 可以直接加入
                productJson.put("comName",comName);
                productJson.put("price",price);
                productJson.put("storeid",storeid);

                number = number.substring(3);
                productJson.put("number",number);

                jsonArray.put(productJson);                     // 保存 jsonObject 到 JSONArray 中
                SharedPreferences.Editor edit = record.edit();
                edit.putString("invitation", jsonArray.toString());   // 存入缓存
                edit.commit();

                // 提示补货成功
                myDialog_Successful("已添加商品到补货列表");

            }else{                          // 有此字段，获取 JsonArray，构造 Object，加入，提交
                JSONArray jsonArray = new JSONArray(notice);
                // 如果 storeid 存在则不添加，不存在则添加
                String storeid = product.getStoreid();
                // 标志位: 0 --- 未保存过，1 --- 保存过
                int flag = 0;
                // 索引所有
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject isPut = jsonArray.getJSONObject(i);
                    if(isPut.get("storeid").equals(storeid)){
                        flag = 1;
                        break;
                    }
                }

                // 未保存过，进行保存
                if(flag == 0){
                    JSONObject productJson = new JSONObject();
                    String comName = product.getComname();
                    String price = product.getPrice();
                    productJson.put("comName",comName);
                    productJson.put("price",price);
                    number = number.substring(3);
                    productJson.put("number",number);
                    productJson.put("storeid",storeid);

                    jsonArray.put(productJson);                     // 保存 jsonObject 到 JSONArray 中
                    SharedPreferences.Editor edit = record.edit();
                    edit.putString("invitation", jsonArray.toString());   // 存入缓存
                    edit.commit();

                    /**String t = record.getString("invitation","取出数据失败");
                     Log.i(TAG,"结果："+t);**/
                    myDialog_Successful("已添加商品到补货列表");

                }else{
                    myDialog_Warning("商品已添加到邀请中，请勿重复添加");
                }
            }

            //Toast.makeText(getContext(),"添加成功",Toast.LENGTH_SHORT).show();
        }
    }
    };

    public void myDialog_Warning(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    public void getProductList(){

        //创建默认的imageloader配置函数[ 进行图片加载      ]
        ImageLoaderConfiguration configuration=new ImageLoaderConfiguration.Builder(getContext())
                .memoryCacheExtraOptions(500,500)//缓存文件最大宽高
                .threadPoolSize(15) //线程池的加载数量
                .threadPriority(Thread.NORM_PRIORITY-2)//优先级定义
                .memoryCacheSize(2*1024*1024)
                .diskCacheSize(50*1024*1024) //50mb sd卡(本地)缓存最大值
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .imageDownloader(new BaseImageDownloader(getContext(),5*100,30*1000))
                .denyCacheImageMultipleSizesInMemory()
                .writeDebugLogs()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .build();

        //初始化imageloader
        ImageLoader.getInstance().init(configuration);

        // 2) 初始化列表数据
        list = new ArrayList<>();

        // 1) 请求商品信息数据
        SharedPreferences record = getActivity().getSharedPreferences("Info_Record", MODE_PRIVATE);
        String token = record.getString("token","获取值为空");

        Log.i("baihupe","check:"+token);

        Map<String,String> headers = new TreeMap<>();
        headers.put("Authorization",token);
        headers.put("identity","1");

        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url,headers);

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
//        product1.setImgResId(R.drawable.mine_admin);
//        list.add(product1);
//
//        Product product2 = new Product();
//        product2.setComname("shit");
//        product2.setPrice("17.8");
//        product2.setStoreid("2");
//        product2.setNumber("缺货");
//        list.add(product2);
//
//        Product product3 = new Product();
//        product3.setComname("shit");
//        product3.setPrice("17.8");
//        product3.setNumber("剩余 3");
//        product3.setStoreid("3");
//        list.add(product3);

        baseAdapter = new ProductAdapter(getContext(),list,mListener);
        gridView.setAdapter(baseAdapter);
        gridView.setOnItemClickListener(this);                                                      /** 设置列表项、列表内部元素的点击事件 **/
        baseAdapter.notifyDataSetChanged();
    }

}