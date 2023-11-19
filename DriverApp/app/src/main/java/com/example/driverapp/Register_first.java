package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Register_first extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 绑定三个组件
        EditText edt_name = findViewById(R.id.input_name);
        EditText edt_phone = findViewById(R.id.input_RE_phone);
        EditText edt_card = findViewById(R.id.input_card);
        EditText edt_password = findViewById(R.id.input_RE_password);
        TextView next_step = findViewById(R.id.next_step_TV);

        edt_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if(view.getId() == R.id.input_RE_phone && hasfocus){                        // 判断密码编辑框是否获得焦点;hasfocus 为 true 代表获得焦点
                    String nameed = edt_name.getText().toString();
                    if(TextUtils.isEmpty(nameed)){                      // 判断手机号输入是否合法,不合法让焦点回退到手机的 editview 中
                        edt_name.requestFocus();
                        Toast.makeText(Register_first.this,"姓名不能为空", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 对于电话进行焦点验证
        /** 输入完电话后，在密码框设置 ”焦点“ 变化监听器，而不是 ”点击事件“ 变化监听器 **/
        /** 注册焦点变化监听器给编辑框 **/
        edt_card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if(view.getId() == R.id.input_card && hasfocus){                        // 判断密码编辑框是否获得焦点;hasfocus 为 true 代表获得焦点
                    String phone2 = edt_phone.getText().toString();
                    if(TextUtils.isEmpty(phone2) || phone2.length()<11){                      // 判断手机号输入是否合法,不合法让焦点回退到手机的 editview 中
                        edt_phone.requestFocus();
                        Toast.makeText(Register_first.this,"请输入11位手机号码", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 对身份正好进行验证
        edt_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if(view.getId() == R.id.input_RE_password && hasfocus){
                    String card2 = edt_card.getText().toString();
                    if(TextUtils.isEmpty(card2) || card2.length()<18){
                        edt_card.requestFocus();
                        Toast.makeText(Register_first.this,"请输入正确的身份证号码", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 跳转下一步
        next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edt_name.getText().toString();
                String phone = edt_phone.getText().toString();
                String card = edt_card.getText().toString();
                String password = edt_password.getText().toString();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(card) || TextUtils.isEmpty(password)){
                    Toast.makeText(Register_first.this,"请填写完整信息",Toast.LENGTH_SHORT).show();
                }else{
                    // 传递数据并且跳转
                    Intent intent = new Intent(Register_first.this,Register_second.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("drivername",name);
                    bundle.putString("drivertel",phone);
                    bundle.putString("drivercard",card);
                    bundle.putString("driverpwd",password);

                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });




    }
}