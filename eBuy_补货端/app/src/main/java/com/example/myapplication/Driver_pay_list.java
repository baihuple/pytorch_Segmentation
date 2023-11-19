package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class Driver_pay_list extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "Baihupe";
    private ListView listView;
    private BaseAdapter baseAdapter;
    private List<Driver_bill> list;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_pay_list);

        // 找到 listview
        listView = findViewById(R.id.driver_list);
        list = new ArrayList<>();

        // 获取数据，渲染列表
        try {
            getDriverBill();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void getDriverBill() throws JSONException {
        // 从 SharedPreferences 中取得数据

        SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
        String driver_buy_list = record.getString("driver_buy_list","no_Info");

        if(driver_buy_list.equals("no_Info")){
            myDialog_Warning("暂无补货订单");
        }else{
            JSONArray jsonArray = new JSONArray(driver_buy_list);
            for(int i=jsonArray.length()-1;i>=0;i--){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject order = jsonObject.getJSONObject("order");
                // 构造 driver_bill 对象保存，并提交到列表
                Driver_bill driver_bill = new Driver_bill();
                driver_bill.setDriverID(order.getString("purchaserid"));
                driver_bill.setTime(jsonObject.getString("appointTime"));
                driver_bill.setProduct(order.getString("comname"));
                driver_bill.setNum(order.getString("purchasenumber"));
                driver_bill.setStatus(jsonObject.getString("status"));

                list.add(driver_bill);
            }

            baseAdapter = new Driver_bill_Adapter(this,list,myClickListener1,myClickListener2,myClickListener3);
            listView.setAdapter(baseAdapter);

        }

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

    /** 点击：成功 **/
    private Driver_bill_Adapter.MyClickListener1 myClickListener1 = new Driver_bill_Adapter.MyClickListener1() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {
//            Toast.makeText(Driver_pay_list.this,"点击了"+position+"号类表项成功",Toast.LENGTH_SHORT).show();
            myDialog_Successful("订单状态变更：已完成");

            // 修改源数据，重新渲染
            SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
            String driver_buy_list = record.getString("driver_buy_list","null");

            if(!driver_buy_list.equals("null")){
                JSONArray jsonArray = new JSONArray(driver_buy_list);

                // 找到在缓存中的位置
                int loc = jsonArray.length()-1-position;
                JSONObject jsonObject = jsonArray.getJSONObject(loc);

                jsonObject.put("status","已完成");
                jsonArray.remove(loc);
                jsonArray.put(loc,jsonObject);

                // 重新提交到缓存中
                SharedPreferences.Editor edit = record.edit();
                edit.putString("driver_buy_list",jsonArray.toString());
                edit.commit();

                // 对于列表项进行直接操作
                Driver_bill driver_bill = list.get(position);
                driver_bill.setStatus("已完成");
                list.remove(position);
                list.add(position,driver_bill);
                baseAdapter.notifyDataSetChanged();

            }


        }
    };

    /** 点击：失败 **/
    private Driver_bill_Adapter.MyClickListener2 myClickListener2 = new Driver_bill_Adapter.MyClickListener2() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {
//            Toast.makeText(Driver_pay_list.this,"点击了"+position+"号类表项失败",Toast.LENGTH_SHORT).show();

            myDialog_Warning("订单状态变更：失败");

            // 修改源数据，重新渲染
            SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
            String driver_buy_list = record.getString("driver_buy_list","null");

            if(!driver_buy_list.equals("null")){
                JSONArray jsonArray = new JSONArray(driver_buy_list);

                // 找到在缓存中的位置
                int loc = jsonArray.length()-1-position;
                JSONObject jsonObject = jsonArray.getJSONObject(loc);

                jsonObject.put("status","订单失败");
                jsonArray.remove(loc);
                jsonArray.put(loc,jsonObject);

                // 重新提交到缓存中
                SharedPreferences.Editor edit = record.edit();
                edit.putString("driver_buy_list",jsonArray.toString());
                edit.commit();

                // 对于列表项进行直接操作
                Driver_bill driver_bill = list.get(position);
                driver_bill.setStatus("订单失败");
                list.remove(position);
                list.add(position,driver_bill);
                baseAdapter.notifyDataSetChanged();

            }
        }
    };

    /** 点击：超时 **/
    private Driver_bill_Adapter.MyClickListener3 myClickListener3 = new Driver_bill_Adapter.MyClickListener3() {
        @Override
        public void myOnClick(int position, View v) throws JSONException {
//            Toast.makeText(Driver_pay_list.this,"点击了"+position+"号类表项超时",Toast.LENGTH_SHORT).show();
            //            Toast.makeText(Driver_pay_list.this,"点击了"+position+"号类表项失败",Toast.LENGTH_SHORT).show();

            myDialog_Warning("订单状态变更：已超时");
            // 修改源数据，重新渲染
            SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
            String driver_buy_list = record.getString("driver_buy_list","null");

            if(!driver_buy_list.equals("null")){
                JSONArray jsonArray = new JSONArray(driver_buy_list);

                // 找到在缓存中的位置
                int loc = jsonArray.length()-1-position;
                JSONObject jsonObject = jsonArray.getJSONObject(loc);

                jsonObject.put("status","订单超时");
                jsonArray.remove(loc);
                jsonArray.put(loc,jsonObject);

                // 重新提交到缓存中，进行记录保存
                SharedPreferences.Editor edit = record.edit();
                edit.putString("driver_buy_list",jsonArray.toString());
                edit.commit();

                // 对于列表项进行直接操作
                Driver_bill driver_bill = list.get(position);
                driver_bill.setStatus("订单超时");
                list.remove(position);
                list.add(position,driver_bill);
                baseAdapter.notifyDataSetChanged();

            }
        }
    };


    /** 点击列表项 **/
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(Driver_pay_list.this,"点击了"+i+"号类表项",Toast.LENGTH_SHORT).show();
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
}