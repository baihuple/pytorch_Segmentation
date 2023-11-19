package com.example.driverapp.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driverapp.HistoryOrder;
import com.example.driverapp.HttpRequest;
import com.example.driverapp.R;
import com.example.driverapp.Search;
import com.example.driverapp.StationDetail;
import com.example.driverapp.StationDialog;
import com.example.driverapp.StationInfo;
import com.example.driverapp.application;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Replenishment_Fragment extends Fragment implements TencentLocationListener,StationDialog.StationCallBack{

    View root;

    JSONArray Repl_Info_Array;
    String city;

    /** 相关参数 **/
    private final static String TAG = "Baihupe";

    /** 声明一个腾讯定位管理器对象 **/
    private TencentLocationManager mLocationManager;

    /** 声明一个地图试图对象 **/
    private MapView mMapView;

    /** 声明地图组件对象 **/
    private TencentMap mTencentMap;

    /** 是否首次定位 **/
    private boolean isFirstLoc = true;

    /** 缩放级别 **/
    private float mZoom = 12;

    /** 当前经纬度 **/
    private LatLng mMyPos;

    /** 当前详细地址 **/
    String mAddress;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Replenishment_Fragment() {
    }

    /** 每次进入页面要求获取定位权限 **/
    public void onResume() {
        super.onResume();
        /** 先检查定位功能开没有 **/
        checkLocationIsOpen(getContext(),"请先打开定位权限");
        isFirstLoc = true;

        /** init 之前要将所有标记清空 **/
        mTencentMap.clearAllOverlays();

        /** 重新初始化 **/
        initLocation();

        mMapView.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /** 页面消除时，清除监听器 **/
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this); // 移除定位监听
        mMapView.onDestroy();
    }



    public static Replenishment_Fragment newInstance(String param1, String param2) {
        Replenishment_Fragment fragment = new Replenishment_Fragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_replenishment, container, false);

        /** 初始化定位服务，同时获取补货站信息 **/
        initLocation();

        // 显示历史订单信息
        ImageView history_IV = root.findViewById(R.id.history_order);
        history_IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HistoryOrder.class);
                startActivity(intent);
            }
        });

        // 进入搜索页面
        TextView search_TV = root.findViewById(R.id.search_text);
        search_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Search.class);
                startActivity(intent);
            }
        });

        // 设置地图样式及交通情况
        initView();

        return root;
    }


    private void initView() {
        RadioGroup rg_type = root.findViewById(R.id.rg_type);
        rg_type.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_common) {
                mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL); // 设置普通地图
            } else if (checkedId == R.id.rb_satellite) {
                mTencentMap.setMapType(TencentMap.MAP_TYPE_SATELLITE); // 设置卫星地图
            }
        });
        CheckBox ck_traffic = root.findViewById(R.id.ck_traffic);
        ck_traffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mTencentMap.setTrafficEnabled(isChecked); // 是否显示交通拥堵状况
        });
    }

    /** 初始化定位服务：回调为异步函数 **/
    private void initLocation(){
        mMapView = root.findViewById(R.id.tencent_MapView);     //找到地图组件
        mTencentMap = mMapView.getMap();                        // 获取腾讯地图对象

        /** 设置地图标记点的点击监听器，点击弹出基本信息 **/
        mTencentMap.setOnMarkerClickListener(new TencentMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                /** 自定义弹出对话框 **/
                StationInfo stationInfo = (StationInfo) marker.getTag();
                if (stationInfo.getStationid() == null){
                    Toast.makeText(getContext(),"这是您的位置",Toast.LENGTH_SHORT).show();
                }else{
                    StationDialog dialog = new StationDialog(getContext(),stationInfo,Replenishment_Fragment.this);
                    dialog.show();
                }
                return true;
            }
        });

        mLocationManager = TencentLocationManager.getInstance(getContext());    // 实例化对象
        // 创建腾讯定位请求对象
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(30000).setAllowGPS(true);                       // 设置定位间隔时间为 3s,允许使用 GPS
        request.setIndoorLocationMode(true).setAllowDirection(true);        // 设置允许室内定位、允许获取传感器方向
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);  // 设置定位精度模式
        mLocationManager.requestLocationUpdates(request,this);
    }

    /** 异步函数: 获取本机所处位置 **/
    @Override
    public void onLocationChanged(TencentLocation location, int resultCode, String resultDesc) {
        if(resultCode == TencentLocation.ERROR_OK){             // 定位成功
            if(location != null && isFirstLoc){                 // 且为首次定位
                isFirstLoc = false;

                // 创建位置对象< 包含经纬度 >
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                city = location.getCity();

                /** 获取本城市的补货站信息 **/
                try {
                    getReplenishmentBasicInfo();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    moveLocation(latLng,location.getAddress());              // 将地图移动到当前位置
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else{                                              // 定位失败
                Log.d(TAG, "定位失败，错误代码为"+resultCode+"，错误描述为"+resultDesc);
            }
        }
    }

    /** 两个异步函数 **/
    @Override
    public void onStatusUpdate(String s, int i, String s1) {}

    /** 切换地图视角为当前定位 **/
    public void moveLocation(LatLng latLng,String address) throws JSONException {
        mMyPos = latLng;
        mAddress = address;
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,mZoom);  // 设置视角参数
        mTencentMap.moveCamera(update);                                         // 调整视角角度

        // 从指定试图中获取位图描述
        BitmapDescriptor bitmapDesc = BitmapDescriptorFactory.fromView(getMarkerView_Mine("您的位置"));
        MarkerOptions marker = new MarkerOptions(latLng).draggable(false).visible(true).icon(bitmapDesc).tag(new StationInfo());
        mTencentMap.addMarker(marker);          // 添加标记

        // 将我的当前位置保存到缓存中
        SharedPreferences record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
        SharedPreferences.Editor edit = record.edit();
        edit.putString("position_lo",String.valueOf(latLng.getLongitude()));
        edit.putString("position_la",String.valueOf(latLng.getLatitude()));
        edit.commit();

        // Log.i(TAG,Repl_Info_Array.get(0).toString());

         // 获取补货站数据
        for(int i=0;i<Repl_Info_Array.length();i++){
            JSONObject jsonObject = Repl_Info_Array.getJSONObject(i);
            // 获取补货站相关信息
            String stationid = jsonObject.getString("stationid");
            String stationname = jsonObject.getString("stationname");
            String stationaddr = jsonObject.getString("stationaddr");
            String stationla = jsonObject.getString("stationla");
            String stationlo = jsonObject.getString("stationlo");
            String adminName = jsonObject.getString("adminName");
            String adminTel = jsonObject.getString("adminTel");
            String stationstate = jsonObject.getString("stationstate");

            /** 创建 stationInfo 对象,为再 MarkerOptions 中设置 tag 做准备 **/
            StationInfo stationInfo = new StationInfo(stationid,stationname,stationaddr,adminName,adminTel,stationstate,stationla,stationlo);

            LatLng latLng_station = new LatLng(Float.parseFloat(stationla),Float.parseFloat(stationlo));
            BitmapDescriptor bitmapdesc = BitmapDescriptorFactory.fromView(getMarkerView_Station(stationname));

            /** 设置标记，添加标记 tag 标签 **/
            MarkerOptions marker_station = new MarkerOptions(latLng_station).draggable(false).visible(true).icon(bitmapdesc).tag(stationInfo);
            mTencentMap.addMarker(marker_station);          // 添加标记
        }

    }

    /** 标记我的试图 **/
    private View getMarkerView_Mine(String address) {
        View view = getLayoutInflater().inflate(R.layout.marker_me,null);
        TextView address_Mine = view.findViewById(R.id.tv_address);
        address_Mine.setText(address);
        return view;
    }

    /** 标记站点试图 **/
    private View getMarkerView_Station(String address) {
        View view = getLayoutInflater().inflate(R.layout.marker_station,null);
        TextView address_Mine = view.findViewById(R.id.tv_address_station);
        address_Mine.setText(address);
        return view;
    }


    /** 获取定位功能的开关状态 **/
    public static boolean getLocationStatus(Context ctx) {
        // 从系统服务中获取定位管理器
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /** 检查定位功能是否打开，若未打开则跳到系统的定位功能设置页面 **/
    public static void checkLocationIsOpen(Context ctx, String hint) {
        if (!getLocationStatus(ctx)) {
            Toast.makeText(ctx, hint, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ctx.startActivity(intent);
        }else{
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ctx,"请允许该应用获取定位状态", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** 获取本城市的补货站的基本信息 **/
    public void getReplenishmentBasicInfo() throws JSONException {

        // 如果缓存有补货站基本信息，则不用再获取
        SharedPreferences record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String Rpl_Info = record.getString("Rpl_basic_Info","null_Info");

        // 空则获取
        if(Rpl_Info.equals("null_Info")){
            // 1) 创建请求
            HttpRequest httpRequest = new HttpRequest();

            // 获取 city、token
            record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
            String token = record.getString("token","no_token");
            String Identity = "0";

            // 2) 构造 url
            application app = (application)getActivity().getApplication();
            final String ip = app.getIp();
            final String url = "http://"+ ip + "/driver/shelve/nearbyRepl?city="+city;

            // 3) 构造头部
            Map<String,String> headers = new TreeMap<>();
            headers.put("Authorization",token);
            headers.put("Identity",Identity);

            // 4) 发送请求
            String response = httpRequest.OkHttpsGet(url,headers);

            // 5) 解析请求
            JSONObject jsonObject_ans = new JSONObject(response);
            String code = jsonObject_ans.getString("code");

            if(code.equals("200")){
                // 获取返回补货站data，为 jsonArray 类型的数组
                JSONArray jsonArray = jsonObject_ans.getJSONArray("data");

                // 保存到本地缓存
                SharedPreferences.Editor edit = record.edit();
                edit.putString("Rpl_basic_Info",jsonArray.toString());
                edit.commit();

                Repl_Info_Array = jsonArray;
            }else{
                Log.i(TAG,"请求附近捕获站信息失败");
            }
        }else{
            // 否则将该信息保存到 Rep_Info 中
            Repl_Info_Array = new JSONArray(Rpl_Info);
        }
    }


    /** 如果弹出框点击详情的 button 回调函数 **/
    public void onDetail(StationInfo stationInfo){
        Intent intent = new Intent(getActivity(), StationDetail.class);
        // 传递站点信息到详情页面,并且根据所需传递数据
        Bundle bundle = new Bundle();
        bundle.putString("stationid",stationInfo.getStationid());
        bundle.putString("stationname",stationInfo.getStationname());
        bundle.putString("stationadd",stationInfo.getStationaddr());
        bundle.putString("stationla",stationInfo.getStationla());
        bundle.putString("stationlo",stationInfo.getStationlo());
        bundle.putString("stationtel",stationInfo.getAdminTel());

        intent.putExtras(bundle);
        startActivity(intent);
    }


}