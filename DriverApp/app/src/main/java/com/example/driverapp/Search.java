package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity{

    final String TAG = "Baihupe";
    private ListView listView;
    private List<StationInfo> list;
    private BaseAdapter baseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 找到 listview 组件
        listView = findViewById(R.id.search_listview);

        // 搜索按钮组件，并且设置监听事件
        TextView search_TV = findViewById(R.id.search_rel);
        EditText search_ET = findViewById(R.id.search_repl);

        // 设置列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                StationInfo stationInfo = list.get(i);
                // 携带相关数据进行页面跳转
                Intent intent = new Intent(Search.this,StationDetail.class);
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
        });

        /** 设置在对输入框内容进行删除操作，列表自动清空 **/
        search_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                list = new ArrayList<>();
                baseAdapter = new SearchAdapter(list,Search.this);
                listView.setAdapter(baseAdapter);
                baseAdapter.notifyDataSetChanged();
            }
        });

        search_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1) 获取搜索框里的东西
                String stationname = search_ET.getText().toString();

                list = new ArrayList<>();

                // 2) 从缓存中拿到补货站信息
                SharedPreferences record = getSharedPreferences("Driver_Info",MODE_PRIVATE);
                String station_Info = record.getString("Rpl_basic_Info","no_station_Info");
                if(station_Info.equals("no_station_Info")){
                    Log.i(TAG,"查询捕获站时，从缓存获取捕获站信息失败");
                }else{
                    // 查找是否有该补货站信息
                    try {
                        JSONArray jsonArray = new JSONArray(station_Info);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String staname = jsonObject.getString("stationname");
                            String staadd = jsonObject.getString("stationaddr");

                            // 获取名字，如果找到则构造加入列表
                            if(staname.indexOf(stationname)!=-1 || staadd.indexOf(stationname)!=-1){
                                // 构造对象
                                StationInfo stationInfo = new StationInfo();
                                stationInfo.setStationname(jsonObject.getString("stationname"));
                                stationInfo.setStationaddr(jsonObject.getString("stationaddr"));
                                stationInfo.setAdminTel(jsonObject.getString("adminTel"));
                                stationInfo.setAdminName(jsonObject.getString("adminName"));
                                stationInfo.setStationstate(jsonObject.getString("stationstate"));
                                stationInfo.setStationid(jsonObject.getString("stationid"));
                                stationInfo.setStationlo(jsonObject.getString("stationlo"));
                                stationInfo.setStationla(jsonObject.getString("stationla"));
                                // 加入列表
                                list.add(stationInfo);
                            }
                        }

                        // 绑定列表与适配器
                        baseAdapter = new SearchAdapter(list,Search.this);
                        listView.setAdapter(baseAdapter);
                        baseAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }

}