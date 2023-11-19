package com.example.myapplication;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;

public class Driver_bill_Adapter extends BaseAdapter {

    private List<Driver_bill> list;
    private LayoutInflater layoutInflater;
    private Context context;

    /** 成功 **/
    private MyClickListener1 mSuccess;

    /** 失败 **/
    private MyClickListener2 mFail;

    /** 超时 **/
    private MyClickListener3 mTimeout;


    // 构造函数
    public Driver_bill_Adapter(Context context,List<Driver_bill> list,MyClickListener1 mSuccess,MyClickListener2 mFail,MyClickListener3 mTimeout){
        this.context = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
        this.mSuccess = mSuccess;
        this.mFail = mFail;
        this.mTimeout = mTimeout;


    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Driver_bill getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if(view == null){
            view = layoutInflater.inflate(R.layout.driver_account_item,viewGroup,false);

            TextView driverID = view.findViewById(R.id.dri_purID);
            TextView time = view.findViewById(R.id.dri_purTime);
            TextView product = view.findViewById(R.id.dri_purPro);
            TextView num = view.findViewById(R.id.dri_purNum);
            TextView status = view.findViewById(R.id.status);
            ImageView mSuccess2 = view.findViewById(R.id.success_button);
            ImageView mFail2 = view.findViewById(R.id.fail_button);
            ImageView mTimeout2 = view.findViewById(R.id.timeout_button);


            viewHolder.driverID = driverID;
            viewHolder.time = time;
            viewHolder.product = product;
            viewHolder.num = num;
            viewHolder.status = status;
            viewHolder.mSuccess = mSuccess2;
            viewHolder.mFail = mFail2;
            viewHolder.mTimeout = mTimeout2;

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        Driver_bill driver_bill = list.get(i);

        viewHolder.driverID.setText(driver_bill.getDriverID());
        viewHolder.time.setText(driver_bill.getTime());
        viewHolder.product.setText(driver_bill.getProduct());
        viewHolder.num.setText(driver_bill.getNum());
        viewHolder.status.setText(driver_bill.getStatus());

        viewHolder.mSuccess.setOnClickListener(mSuccess);
        viewHolder.mSuccess.setTag(i);
        viewHolder.mFail.setOnClickListener(mFail);
        viewHolder.mFail.setTag(i);
        viewHolder.mTimeout.setOnClickListener(mTimeout);
        viewHolder.mTimeout.setTag(i);



        return view;
    }

    class ViewHolder{
        TextView driverID;
        TextView time;
        TextView product;
        TextView num;
        TextView status;
        ImageView mSuccess;
        ImageView mFail;
        ImageView mTimeout;

    }

    // 成功
    public static abstract class MyClickListener1 implements View.OnClickListener{
        public void onClick(View v){
            try {
                myOnClick((Integer)v.getTag(),v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public abstract void myOnClick(int position,View v) throws JSONException;
    }

    // 失败
    public static abstract class MyClickListener2 implements View.OnClickListener{
        public void onClick(View v){
            try {
                myOnClick((Integer)v.getTag(),v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public abstract void myOnClick(int position,View v) throws JSONException;
    }

    // 超时
    public static abstract class MyClickListener3 implements View.OnClickListener{
        public void onClick(View v){
            try {
                myOnClick((Integer)v.getTag(),v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public abstract void myOnClick(int position,View v) throws JSONException;
    }


}
