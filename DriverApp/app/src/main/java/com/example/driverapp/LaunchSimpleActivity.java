package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

public class LaunchSimpleActivity extends AppCompatActivity {
    // 声明引导页面的图片数组
    private int[] lanuchImageArray = {
            R.drawable.welcome_1, R.drawable.welcome_2, R.drawable.welcome_3,R.drawable.welcome4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_simple);

        // 绑定翻页视图组件 viewPager
        ViewPager vp_launch = findViewById(R.id.vp_launch);

        // 构建一个引导页面翻页适配器
        LaunchSimpleAdapter adapter = new LaunchSimpleAdapter(this,lanuchImageArray);
        vp_launch.setAdapter(adapter);          // 设置翻页视图适配器
        vp_launch.setCurrentItem(0);            // 设置翻页视图显示第一页

    }
}