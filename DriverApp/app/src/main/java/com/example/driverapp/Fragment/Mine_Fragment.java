package com.example.driverapp.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driverapp.Ad_management;
import com.example.driverapp.HttpRequest;
import com.example.driverapp.Login;
import com.example.driverapp.Msg;
import com.example.driverapp.R;
import com.example.driverapp.ScanActivity;
import com.example.driverapp.Settings;
import com.example.driverapp.StationContact;
import com.example.driverapp.application;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Mine_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Mine_Fragment extends Fragment {

    final String TAG = "Baihupe";
    public String ip;
    public String url;

    TextView name_TV ;
    TextView phone_TV ;
    TextView city_TV;
    TextView license_TV ;
    Button logout_BT;
    ImageView scan_IV ;
    ImageView setting_IV;

    TextView equipmentID_TV;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Mine_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Mine.
     */
    // TODO: Rename and change types and number of parameters
    public static Mine_Fragment newInstance(String param1, String param2) {
        Mine_Fragment fragment = new Mine_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 获取绑定设备信息，如果为空则不做操作；不为空则填入
        SharedPreferences record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
        String deviceID = record.getString("deviceID","null");
        if(!deviceID.equals("null") && !deviceID.equals("0")){
            equipmentID_TV.setText(deviceID);
        }else{
            equipmentID_TV.setText("未绑定设备");
        }

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
        View root = inflater.inflate(R.layout.fragment_mine, container, false);

        // 获取ip
        application app = (application)getActivity().getApplication();
        ip = app.getIp();
        url = "http://"+ ip + "/driver/infoManage/driverInfo";

        // 绑定所有组件
        TextView name_TV = root.findViewById(R.id.mine_name_TV);
        TextView phone_TV = root.findViewById(R.id.mine_telephone);
        TextView city_TV = root.findViewById(R.id.location_TV);
        equipmentID_TV = root.findViewById(R.id.mine_equipment_TV);
        TextView license_TV = root.findViewById(R.id.mine_license_TV);
        Button logout_BT = root.findViewById(R.id.logout_button);
        ImageView scan_IV = root.findViewById(R.id.scan_device);
        ImageView setting_IV = root.findViewById(R.id.setting);
        TextView driverid_TV = root.findViewById(R.id.mine_driver_id);
        TextView sex = root.findViewById(R.id.sex_tv);
        ImageView adManagement = root.findViewById(R.id.ad_cal);


        // 点击跳转到广告页面
        adManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Ad_management.class);
                startActivity(intent);
            }
        });

        // 点击设置跳转设置页面 18066734948
        setting_IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
            }
        });

        // 点击设备码进行扫描
        scan_IV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });

        // 点击设置进行对应修改

        logout_BT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清除缓存
                SharedPreferences record = getActivity().getSharedPreferences("Driver_Info",MODE_PRIVATE);
                SharedPreferences.Editor edit = record.edit();
                edit.clear();
                edit.commit();

                // 跳转到登录页面
                Intent intent = new Intent(getActivity(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // 向服务器发送请求,获取司机信息
        // 1) 获取 token
        SharedPreferences record = getActivity().getSharedPreferences("Driver_Info", MODE_PRIVATE);
        String token = record.getString("token","token_null");

        String Info = record.getString("Info","Info_null");
        if(!Info.equals("Info_null")){
            JSONObject driverInfo = null;
            try {
                driverInfo = new JSONObject(Info);
                // 获取司机信息，填入对应的框中
                String name_ = driverInfo.getString("name");
                String phone_ = driverInfo.getString("tel");
                String license_ = driverInfo.getString("license");
                String deviceID_ = driverInfo.getString("deviceID");
                String location_ = driverInfo.getString("city");
                String driverid_ = driverInfo.getString("driverid");

                Log.i(TAG,"deviceID:"+deviceID_);

                // 填入指定字段
                name_TV.setText(name_);
                phone_TV.setText(phone_);
                license_TV.setText(license_);
                city_TV.setText(location_);
                driverid_TV.setText(driverid_);

                if(deviceID_.equals("0")){
                    equipmentID_TV.setText("尚未绑定设备");
                }else{
                    equipmentID_TV.setText(deviceID_);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }else{
            if(token.equals("token_null")){
                Log.i(TAG,"我的界面获取 token 失败");
            }else{
                // 2) 构造头部
                Map<String ,String > headers = new TreeMap<>();
                headers.put("Authorization",token);
                headers.put("Identity","0");

                // 3) 发送请求
                HttpRequest httpRequest = new HttpRequest();
                String response = httpRequest.OkHttpsGet(url,headers);

                // 4) 解析结果
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("message");
                    if (code.equals("200")){
                        String data = jsonObject.getString("data");
                        JSONObject driverInfo = new JSONObject(data);

                        // 获取司机信息，填入对应的框中
                        String name_ = driverInfo.getString("name");
                        String phone_ = driverInfo.getString("tel");
                        String license_ = driverInfo.getString("license");
                        String deviceID_ = driverInfo.getString("deviceID");
                        String location_ = driverInfo.getString("city");
                        String sex_ = driverInfo.getString("sex");

                        Log.i(TAG,"deviceID:"+deviceID_);

                        // 填入指定字段
                        name_TV.setText(name_);
                        phone_TV.setText(phone_);
                        license_TV.setText(license_);
                        city_TV.setText(location_);
                        sex.setText(sex_);

                        if(deviceID_.equals("0")){
                            equipmentID_TV.setText("尚未绑定设备");
                        }else{
                            equipmentID_TV.setText(deviceID_);
                        }

                        // 将其保存到 sharedPreference 中
                        SharedPreferences.Editor edit = record.edit();
                        if(TextUtils.isEmpty(deviceID_)){
                            edit.putString("Info",driverInfo.toString());
                            edit.putString("deviceID","noID");
                            edit.commit();
                        }else{
                            edit.putString("Info",driverInfo.toString());
                            edit.putString("deviceID",deviceID_);
                            edit.commit();
                        }

                    }else{
                        Log.i(TAG,"获取司机信息失败");
//                    Toast.makeText(getContext(),"获取司机信息失败",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return root;
    }



}