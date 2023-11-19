package com.example.driverapp.util;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.driverapp.R;

public class DialogUtil {
    public static boolean isclose = false;

    public static Dialog createLoadingDialog(Context context, String msg) {
        isclose = false;
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.activity_loading, null);// 得到加载view
        LinearLayout layout = v.findViewById(R.id.dialog_loading_view);// 加载布局
        TextView tipTextView = v.findViewById(R.id.dialog_loading_tipTextView);// 提示文字
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.LoadingDialog);// 创建自定义样式dialog
        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
        loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        // 设置布局
        /**
         *将显示Dialog的方法封装在这里面
         */
        Window window = loadingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        //lp.type = lp.TYPE_PHONE;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
//        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        loadingDialog.show();
        return loadingDialog;
    }

    public static void closeDialog(Dialog mDialogUtils) {

        if (mDialogUtils != null && mDialogUtils.isShowing()) {
            mDialogUtils.dismiss();
            isclose = true;
        }
    }
}

