package com.co.wifi.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.co.wifi.R;
import com.co.wifi.activity.WifiSetAct;
import com.co.wifi.util.WifiUtil;

import java.util.List;

/**
 * wifi连接对话框
 * Created by zhouqiang on 2016/8/2.
 */
public class DialogueWifiCon extends AlertDialog implements View.OnClickListener {

    boolean pwdDisplay = false;
    String ssid;

    WifiManager wifiManager;
    WifiSetAct wifisetAct;


    TextView tv_tittle_wifidialogue;
    ImageView img_wifi_pwdhiden;
    EditText wifiPwd;
    View contentView;
    public DialogueWifiCon(Context context, String ssid) {
        super(context);
        wifisetAct = (WifiSetAct) context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        initUI(ssid, contentView=setCustomView(context));

    }

    private View setCustomView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_wificondialogue, null);
        return view;
    }

    private void initUI(String ssid, View view) {
        tv_tittle_wifidialogue = (TextView) view.findViewById(R.id.tv_tittle_wifidialogue);
        tv_tittle_wifidialogue.setText(this.ssid = ssid);
        wifiPwd = (EditText) view.findViewById(R.id.et_wifipwd);
        img_wifi_pwdhiden = (ImageView) view.findViewById(R.id.img_wifi_pwdhiden);
        img_wifi_pwdhiden.setOnClickListener(this);
        view.findViewById(R.id.btn_cancel_dialogue).setOnClickListener(this);
        view.findViewById(R.id.btn_con_dialogue).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel_dialogue:
                dismiss();
                break;
            case R.id.btn_con_dialogue:
//                Log.e("WifiState1", String.valueOf(wifiManager.getWifiState()));
                //默认wpa加密方式连接
                if (!WifiUtil.connectWifi(ssid, wifiPwd.getText().toString().trim(), WifiUtil.WifiCipherType.WIFICIPHER_WPA)) {
                    Toast.makeText(wifisetAct, ssid + "连接失败,请重新输入您的密码！", Toast.LENGTH_SHORT).show();
                }
                dismiss();
                break;
            case R.id.img_wifi_pwdhiden:
                pwdDisplay = !pwdDisplay;
                if (pwdDisplay) {
                    ((ImageView) v).setImageResource(R.drawable.src_pwddisplay);
                    wifiPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    wifiPwd.setSelection(wifiPwd.getText().toString().trim().length());
                } else {
                    ((ImageView) v).setImageResource(R.drawable.src_pwdhiden);
                    wifiPwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    wifiPwd.setSelection(wifiPwd.getText().toString().trim().length());
                }
                break;
        }
    }


    @Override
    public void show() {
        super.show();
        setContentView(contentView);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去除圆角外的灰色区域
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);//弹出输入法
        //自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        InputMethodManager imm=(InputMethodManager) wifisetAct.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(contentView, 0);
        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
        WindowManager.LayoutParams lp=getWindow().getAttributes();//提取Activity的窗口
        float scale = wifisetAct.getResources().getDisplayMetrics().density;//取手机的屏幕相对屏密
        lp.width= (int) (300 * scale);
        lp.height=ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);//在show之后设置

    }
}
