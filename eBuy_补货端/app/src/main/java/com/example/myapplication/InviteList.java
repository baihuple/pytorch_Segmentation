package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InviteList extends AppCompatActivity {

    private ListView listView;

    private List<Invited> list;
    private BaseAdapter baseAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            getInvitedList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_list);

        listView = findViewById(R.id.invite_list);
        list = new ArrayList<>();
    }

    /** 获取已邀约列表信息 **/
    public void getInvitedList() throws JSONException {
        // 获取数据
        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String invited_list = record.getString("invited_list","no_invited_list");

        if(invited_list.equals("no_invited_list")){     // 缓存为空，没有信息
           return;
        }else{
            JSONArray jsonArray = new JSONArray(invited_list);
            for(int i=jsonArray.length()-1;i>=0;i--){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // 构造 invited 对象
                Invited invited = new Invited();
                invited.setTime(jsonObject.getString("time"));

                // content 内容为 jsonArray to string 类型，需要重新渲染
                JSONArray content_array = new JSONArray(jsonObject.getString("content"));
                String result = "";
                for(int j=0;j<content_array.length();j++){
                    JSONObject content_object = content_array.getJSONObject(j);
                    String comName = content_object.getString("comName");
                    String price = content_object.getString("price");
                    String number = content_object.getString("number");

                    result += "名称：" + comName +"，价格：" + price + "，数量：" + number + "\n";
                }

                invited.setContent(result);
                list.add(invited);
            }

            // 渲染列表
            baseAdapter = new InvitedAdapter(list,InviteList.this);
            listView.setAdapter(baseAdapter);
            baseAdapter.notifyDataSetChanged();
        }

    }

}