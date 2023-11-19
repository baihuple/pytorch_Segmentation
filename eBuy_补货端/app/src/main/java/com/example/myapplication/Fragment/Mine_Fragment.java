package com.example.myapplication.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.HttpRequest;
import com.example.myapplication.Login;
import com.example.myapplication.R;
import com.example.myapplication.application;

import org.json.JSONArray;
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
     * @return A new instance of fragment Mine_Fragment.
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

        // 1) 网络请求获取站点信息
        application app = (application)getActivity().getApplication();
        String ip = app.getIp();
        String url = "http://"+ip+"/repl/localInfo";
        final String TAG = "Baihupe";

        Button logout = root.findViewById(R.id.logout_button);
        TextView admin_TV = root.findViewById(R.id.TV_head_name);
        TextView phone_TV = root.findViewById(R.id.TV_head_phone);
        TextView adminid_TV = root.findViewById(R.id.admin_ID);
        TextView stationid_TV = root.findViewById(R.id.station_id);
        TextView location_TV = root.findViewById(R.id.sta_location);
        TextView stationname_TV =root.findViewById(R.id.head_message);


        // 先获取 stationinfo，如果为空再请求
        SharedPreferences record = getActivity().getSharedPreferences("Info_Record", MODE_PRIVATE);
        String station_stored = record.getString("stationInfo","stationInfo_null");

        // 请求站点信息并保存
        if(station_stored.equals("stationInfo_null")){
            String token = record.getString("token","获取 token 为空");

            // 构造头部
            Map<String,String> headers = new TreeMap<>();
            headers.put("Authorization",token);
            headers.put("identity","1");

            /** 发送请求 **/
            HttpRequest httpRequest = new HttpRequest();
            String response = httpRequest.OkHttpsGet(url,headers);

            /** 解析结果,下方注释绝对不能删 **/
            try {
                JSONObject jsonObject_answer = new JSONObject(response);
                String code = jsonObject_answer.get("code").toString();
                String message = jsonObject_answer.get("message").toString();

                if(code.equals("200")){
                    // 结果转化为 JSON 数组
                    JSONObject data = (JSONObject) jsonObject_answer.getJSONObject("data");

                    // 读取指定字段
                    String station_address = data.get("stationaddr").toString();
                    String adminName = data.get("adminName").toString();
                    String adminTel = data.get("adminTel").toString();
                    String adminid = data.get("adminid").toString();
                    String stationid = data.get("stationid").toString();
                    String stationname = data.get("stationname").toString();

                    // 渲染到前端指定位置
                    admin_TV.setText(adminName);
                    phone_TV.setText(adminTel);
                    adminid_TV.setText(adminid);
                    stationid_TV.setText(stationid);
                    location_TV.setText(station_address);
                    stationname_TV.setText(stationname);


                    // 保存站点信息到 sharedpreferences,以字符串的形式保存
                    SharedPreferences.Editor edit = record.edit();
                    edit.putString("stationInfo",data.toString());
                    edit.commit();

                    String string = record.getString("stationInfo","查不到站点信息");
                    Log.i(TAG,"站点信息:"+string);


                }else{
                    Log.i(TAG,"获取站点信息失败:"+message);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }else{
            // 否则直接从缓存中获取站点信息
            try {
                JSONObject data = new JSONObject(station_stored);

                // 读取指定字段
                String station_address = data.get("stationaddr").toString();
                String adminName = data.get("adminName").toString();
                String adminTel = data.get("adminTel").toString();
                String adminid = data.get("adminid").toString();
                String stationid = data.get("stationid").toString();
                String stationname = data.get("stationname").toString();

                // 渲染到前端指定位置
                admin_TV.setText(adminName);
                phone_TV.setText(adminTel);
                adminid_TV.setText(adminid);
                stationid_TV.setText(stationid);
                location_TV.setText(station_address);
                stationname_TV.setText(stationname);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }


         // 退出登录

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 先清除缓存再跳转页面
                SharedPreferences.Editor edit = record.edit();
                edit.clear();
                edit.commit();

                Intent intent = new Intent(getActivity(), Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        return root;
    }
}