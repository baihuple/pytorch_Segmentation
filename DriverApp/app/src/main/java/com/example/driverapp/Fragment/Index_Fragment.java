package com.example.driverapp.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.driverapp.AddProductScan;
import com.example.driverapp.CarProduct;
import com.example.driverapp.CarProductAdapter;
import com.example.driverapp.ChangePrice;
import com.example.driverapp.ControlDevice;
import com.example.driverapp.HistoryBill;
import com.example.driverapp.HttpRequest;
import com.example.driverapp.R;
import com.example.driverapp.application;
import com.huawei.hms.scankit.C;
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
 * Use the {@link Index_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Index_Fragment extends Fragment {

    final String TAG = "Baihupe";
    public String ip;
    public String url;

    private GridView gridView;
    private List<CarProduct> list;
    private BaseAdapter baseAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Index_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Index.
     */
    // TODO: Rename and change types and number of parameters
    public static Index_Fragment newInstance(String param1, String param2) {
        Index_Fragment fragment = new Index_Fragment();
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

    @Override
    public void onResume() {
        super.onResume();
        try {
            getCarProductList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_index, container, false);

        // 获取 ip 和 url
        application application = (application) getActivity().getApplication();
        ip = application.getIp();
        url = "http://"+ip+"/driver/shelve/shelveInfo";

        gridView = root.findViewById(R.id.carProduct_GridView);
        ImageView add_product_BT = root.findViewById(R.id.add_product);
        ImageView history_bill_BT = root.findViewById(R.id.history_sell);
        ImageView refresh_list = root.findViewById(R.id.refresh_list);

        refresh_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getCarProductList();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // 跳转到历史账单页面
        history_bill_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HistoryBill.class);
                startActivity(intent);
            }
        });

        // 跳转控制设备页面
        add_product_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ControlDevice.class);
                startActivity(intent);
            }
        });

        // 拿到商品货架
        try {
            getCarProductList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // 为每一个列表项设置点击事件
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 获取列表中元素
                CarProduct carProduct = list.get(i);
                // 跳转修改商品价格页面
                Intent intent = new Intent(getActivity(), ChangePrice.class);
                Bundle bundle = new Bundle();
                bundle.putString("shelveid",carProduct.getShelveid());
                bundle.putString("price",carProduct.getPrice());
                bundle.putString("comName", carProduct.getComName());

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        return root;
    }


    /** 渲染本车货架上的商品列表 **/
    public void getCarProductList() throws JSONException {

        //创建默认的imageloader配置函数[ 进行图片加载  ]
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

        // 3) 获取头部信息
        Map<String,String> headers =  getHeader();

        // 4) 发送 get 请求
        HttpRequest httpRequest = new HttpRequest();
        String response = httpRequest.OkHttpsGet(url,headers);

        // 5) 解析商品数据 jsonArray 类型的
        JSONObject jsonObject = new JSONObject(response);
        String code = jsonObject.getString("code");
        if(code.equals("200")){         // 成功收到

            JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("data");

            // fake：如果收到的商品数据的第一项为空，则直接请求
            if(jsonArray.length() == 0){
                getRecommendProduct();
                baseAdapter = new CarProductAdapter(list,getContext());
                gridView.setAdapter(baseAdapter);
                baseAdapter.notifyDataSetChanged();
                return;
            }

            if(jsonArray.getJSONObject(0).getString("comBrand").equals("null")){
                getRecommendProduct();
                baseAdapter = new CarProductAdapter(list,getContext());
                gridView.setAdapter(baseAdapter);
                baseAdapter.notifyDataSetChanged();
                return;
            }

            for(int i=0;i<jsonArray.length();i++){
                // 构造实体，渲染到列表中
                JSONObject item = jsonArray.getJSONObject(i);

                CarProduct carProduct = new CarProduct();
                carProduct.setComName(item.getString("comName"));
                carProduct.setNumber(item.getString("number"));
                carProduct.setPrice(item.getString("price"));
                carProduct.setShelveid(item.getString("shelveid"));
                carProduct.setImageURL(item.getString("imageURL"));
                carProduct.setSellstate(item.getString("sellState"));

                list.add(carProduct);
            }

        }else{
            Log.i(TAG,"获取本车商品失败");
        }

        baseAdapter = new CarProductAdapter(list,getContext());
        gridView.setAdapter(baseAdapter);
        baseAdapter.notifyDataSetChanged();

    }

    /** 获取头部信息 **/
    public Map<String,String> getHeader(){
        Map<String,String> headers = new TreeMap<>();

        // 获取 token
        SharedPreferences record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String token = record.getString("token","token_null");
        if(token.equals("token_null")){
            return null;
        }else{
            headers.put("Authorization",token);
            headers.put("identity","0");
            return headers;
        }
    }


    /** 获取推荐数据 **/
    public void getRecommendProduct(){

        CarProduct carProduct = new CarProduct();
        carProduct.setComName("双汇火腿肠");
        carProduct.setNumber("");
        carProduct.setPrice("10.92");
        carProduct.setShelveid("1");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/uploada/gra/1803203849/06902890239307/06902890239307.1.jpg");
        carProduct.setSellstate("");
        list.add(carProduct);

        carProduct = new CarProduct();
        carProduct.setComName("心相印纸面巾 183");
        carProduct.setNumber("");
        carProduct.setPrice("2.28");
        carProduct.setShelveid("2");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/uploada/gra/1703285357/06903244671033/06903244671033.1.jpg");
        carProduct.setSellstate("");
        list.add(carProduct);

        carProduct = new CarProduct();
        carProduct.setComName("农夫山泉东方树叶红茶");
        carProduct.setNumber("");
        carProduct.setPrice("3.70");
        carProduct.setShelveid("3");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/uploada/gra/1704174799/06921168558025/06921168558025.1.jpg");
        carProduct.setSellstate("");
        list.add(carProduct);

        carProduct = new CarProduct();
        carProduct.setComName("蒙牛纯牛奶");
        carProduct.setNumber("");
        carProduct.setPrice("11.95");
        carProduct.setShelveid("4");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/uploada/gra/sj210401103859218927/06923644210151/06923644210151.1.jpg");
        carProduct.setSellstate("");
        list.add(carProduct);

        carProduct = new CarProduct();
        carProduct.setComName("绿箭无糖薄荷糖柠檬薄");
        carProduct.setNumber("");
        carProduct.setPrice("3.80");
        carProduct.setShelveid("5");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/2021128/761550621.png");
        carProduct.setSellstate("");
        list.add(carProduct);

        carProduct = new CarProduct();
        carProduct.setComName("百事可乐");
        carProduct.setNumber("");
        carProduct.setPrice("9.90");
        carProduct.setShelveid("6");
        carProduct.setImageURL("https://oss.gds.org.cn/userfile/uploada/gra/1703274916/06924882496116/06924882496116.1.jpg");
        carProduct.setSellstate("");

        list.add(carProduct);

    }

}