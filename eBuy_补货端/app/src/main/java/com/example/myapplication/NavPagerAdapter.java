package com.example.myapplication;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapplication.Fragment.Invite_Fragment;
import com.example.myapplication.Fragment.Mine_Fragment;
import com.example.myapplication.Fragment.Product_Fragment;

public class NavPagerAdapter extends FragmentPagerAdapter {

    /** 翻页适配器的构造方法 --- 传入碎片管理器 **/
    public NavPagerAdapter( FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    /** 获取指定位置的碎片 Fragment **/
    @Override
    public Fragment getItem(int position) {
        if(position==0){
            return new Product_Fragment();
        }else if(position==1){
            return new Invite_Fragment();
        }else if(position==2){
            return new Mine_Fragment();
        }else{
            return null;
        }
    }

    /** 获取碎片 Fragment 个数 **/
    @Override
    public int getCount() {
        return 3;
    }
}
