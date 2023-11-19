package com.example.myapplication;

import static com.huawei.hms.feature.DynamicModuleInitializer.getContext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Fragment.Product_Fragment;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class ResultActivity extends AppCompatActivity {
    private final static String TAG = "ScanResultActivity";
    private TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView addProId = findViewById(R.id.TV_add_product_name);
        EditText addProNum = findViewById(R.id.TV_add_product_num);
        Button addButton = findViewById(R.id.add_button);

        // 检查扫码结果
        HmsScan hmsScan = getIntent().getParcelableExtra(ScanUtil.RESULT);
        int scanType = hmsScan.getScanType();                       /** 获取扫描码的类型 **/
        String scanResults = hmsScan.getShowResult();               /** 获取扫码结果 **/

        // 如果 scanType 不为 128，则重新扫描
        if(scanType != HmsScan.EAN13_SCAN_TYPE){
            myDialog_Warning("请重新扫描商品条码");
            // 回到主页面
        }else{
            // 填充
            addProId.setText(scanResults);
        }

        // 点击button获取数量，发送请求
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = addProNum.getText().toString();

                // 1) 封装数据
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("comid",scanResults);
                    jsonObject.put("storeNumber",num);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // 2) 获取token，构造map头部
                SharedPreferences record = getSharedPreferences("Info_Record",MODE_PRIVATE);
                String token = record.getString("token","token_null");

                Map<String,String> headers = new TreeMap<>();
                headers.put("Authorization",token);
                headers.put("identity","1");

                // 3) 构造 httpRequest 对象，发送请求
                application app = (application)getApplication();
                final String ip = app.getIp();
                final String url = "http://"+ip+"/repl/newCommodity";

                HttpRequest httpRequest = new HttpRequest();

                String response = "";
                try {
                    response = httpRequest.OkHttpsPost(url,jsonObject,headers);
                    // 4) 接收结果
                    JSONObject jsonObject_answer = new JSONObject(response);
                    String code = jsonObject_answer.getString("code");
                    String message = jsonObject_answer.getString("message");

                    // 5) 判断是否成功
                    if(code.equals("200")){
                        myDialog_Successful("添加成功");

                    }else{
                        myDialog_Warning(message);

                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });



//        parserScanResult(); // 解析扫码结果
    }

    // 解析扫码结果
    private void parserScanResult() {
        // 从意图中获取可折叠的扫码结果
        HmsScan hmsScan = getIntent().getParcelableExtra(ScanUtil.RESULT);
        try {
            String desc = String.format("扫码结果如下：\n\t\t格式为%s\n\t\t类型为%s\n\t\t内容为%s",
                    getCodeFormat(hmsScan.getScanType()),
                    getResultType(hmsScan.getScanType(), hmsScan.getScanTypeForm()),
                    hmsScan.getOriginalValue());
            Log.d(TAG, "desc="+desc);
            tv_result.setText(desc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取扫码格式
    private String getCodeFormat(int scan_type) {
        String codeFormat = "未知（Unknown）";
        if (scan_type == HmsScan.QRCODE_SCAN_TYPE) {
            codeFormat = "快速响应码（QR code）";
        } else if (scan_type == HmsScan.AZTEC_SCAN_TYPE) {
            codeFormat = "阿兹特克码（AZTEC code）";
        } else if (scan_type == HmsScan.DATAMATRIX_SCAN_TYPE) {
            codeFormat = "数据矩阵码（DATAMATRIX code）";
        } else if (scan_type == HmsScan.PDF417_SCAN_TYPE) {
            codeFormat = "便携数据文件码（PDF417 code）";
        } else if (scan_type == HmsScan.CODE93_SCAN_TYPE) {
            codeFormat = "CODE93";
        } else if (scan_type == HmsScan.CODE39_SCAN_TYPE) {
            codeFormat = "CODE39";
        } else if (scan_type == HmsScan.CODE128_SCAN_TYPE) {
            codeFormat = "CODE128";
        } else if (scan_type == HmsScan.EAN13_SCAN_TYPE) {
            codeFormat = "欧洲商品编码-标准版（EAN13 code）";
        } else if (scan_type == HmsScan.EAN8_SCAN_TYPE) {
            codeFormat = "欧洲商品编码-缩短版（EAN8 code）";
        } else if (scan_type == HmsScan.ITF14_SCAN_TYPE) {
            codeFormat = "外箱条码（ITF14 code）";
        } else if (scan_type == HmsScan.UPCCODE_A_SCAN_TYPE) {
            codeFormat = "商品统一代码-通用（UPCCODE_A）";
        } else if (scan_type == HmsScan.UPCCODE_E_SCAN_TYPE) {
            codeFormat = "商品统一代码-短码（UPCCODE_E）";
        } else if (scan_type == HmsScan.CODABAR_SCAN_TYPE) {
            codeFormat = "库德巴码（CODABAR）";
        }
        return codeFormat;
    }

    // 获取结果类型
    private String getResultType(int scan_type, int scanForm) {
        String resultType = "文本（Text）";
        if (scan_type == HmsScan.QRCODE_SCAN_TYPE) {
            if (scanForm == HmsScan.PURE_TEXT_FORM) {
                resultType = "文本（Text）";
            } else if (scanForm == HmsScan.EVENT_INFO_FORM) {
                resultType = "事件（Event）";
            } else if (scanForm == HmsScan.CONTACT_DETAIL_FORM) {
                resultType = "联系（Contact）";
            } else if (scanForm == HmsScan.DRIVER_INFO_FORM) {
                resultType = "许可（License）";
            } else if (scanForm == HmsScan.EMAIL_CONTENT_FORM) {
                resultType = "电子邮箱（Email）";
            } else if (scanForm == HmsScan.LOCATION_COORDINATE_FORM) {
                resultType = "位置（Location）";
            } else if (scanForm == HmsScan.TEL_PHONE_NUMBER_FORM) {
                resultType = "电话（Tel）";
            } else if (scanForm == HmsScan.SMS_FORM) {
                resultType = "短信（SMS）";
            } else if (scanForm == HmsScan.WIFI_CONNECT_INFO_FORM) {
                resultType = "无线网络（Wi-Fi）";
            } else if (scanForm == HmsScan.URL_FORM) {
                resultType = "网址（WebSite）";
            }
        } else if (scan_type == HmsScan.EAN13_SCAN_TYPE) {
            if (scanForm == HmsScan.ISBN_NUMBER_FORM) {
                resultType = "国际标准书号（ISBN）";
            } else if (scanForm == HmsScan.ARTICLE_NUMBER_FORM) {
                resultType = "产品（Product）";
            }
        } else if (scan_type == HmsScan.EAN8_SCAN_TYPE
                || scan_type == HmsScan.UPCCODE_A_SCAN_TYPE
                || scan_type == HmsScan.UPCCODE_E_SCAN_TYPE) {
            if (scanForm == HmsScan.ARTICLE_NUMBER_FORM) {
                resultType = "产品（Product）";
            }
        }
        return resultType;
    }

    public void myDialog_Warning(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        builder.setMessage(message);
        builder.setIcon(R.drawable.attention);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();               // 回到扫码界面
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void myDialog_Successful(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setIcon(R.drawable.successful);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 返回主界面
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },500);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}