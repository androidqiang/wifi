package com.co.wifi.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.co.wifi.R;
import com.co.wifi.util.WifiUtil;
import com.co.wifi.view.DialogueWifiCon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;


public class WifiSetAct extends Activity {
    private HashSet<String> compatability_nopass;{
        compatability_nopass = new HashSet<>();//无密码加密协议
        compatability_nopass.add("[WPS][ESS]");
        compatability_nopass.add("[ESS]");
    }
    private ArrayList<ScanResult> list;                   //存放周围wifi热点对象的列表
    private WifiUtil wifiUtil;
    private HashSet<String> ssid_nopwd = new HashSet<>();
    private ListView listView;
    private TextView myWifiName;
    private ImageView selected, mywifiwithpwd, mywifilevel;
    private BroadcastReceiver networkchangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid_new = wifiInfo.getSSID();
                        ssid_new = ssid_new.substring(1, ssid_new.length() - 1);
                        setWifiTittle(ssid_new);
                        refreshContent(ssid_new);
                    } else {
                        resetWifiTittle();
                        refreshContent("");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_wifiset);
        getData();
        initUI();
        //注册网络状态改变（wifi连接成功）广播接收器
        registerReceiver(networkchangedReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    private void getData() {
        wifiUtil = new WifiUtil();
        list = (ArrayList<ScanResult>) wifiUtil.getScanResults();
        for (ScanResult reslut:list) {
            String capabilities = reslut.capabilities.trim();
            if (compatability_nopass.contains(capabilities)){
                ssid_nopwd.add(reslut.SSID);
            }
        }
        Iterator iterator = list.iterator();
        ScanResult scanResult;
        while (iterator.hasNext()) {
            scanResult = (ScanResult) iterator.next();
            if (scanResult.SSID.length() < 1) {
                iterator.remove();//移除没有名字的wifi设备
            }
        }
        Collections.sort(list, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return rhs.level - lhs.level;//按照信号由强到弱排序
            }
        });
    }

    private void initUI() {
        listView = (ListView) findViewById(R.id.otherWifi);
        listView.setAdapter(new BaseAdapter() {
                                String ssid;

                                @Override
                                public int getCount() {
                                    return list.size();
                                }

                                @Override
                                public Object getItem(int position) {
                                    return list.get(position);
                                }

                                @Override
                                public long getItemId(int position) {
                                    return position;
                                }

                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    ViewHolder viewHolder;
                                    convertView = getLayoutInflater().inflate(R.layout.item_wifiset, null);
                                    viewHolder = new ViewHolder();
                                    viewHolder.wifiName = (TextView) convertView.findViewById(R.id.WifiName);
                                    viewHolder.img_wifiwithpwd = (ImageView) convertView.findViewById(R.id.img_wifiwithpwd);
                                    ssid = list.get(position).SSID;
                                    if (ssid.length() > 16) {
                                        ssid = ssid.substring(0, 16) + "...";
                                    }
                                    viewHolder.wifiName.setText(ssid);
                                    String capabilities = list.get(position).capabilities.trim();
                                    if (compatability_nopass.contains(capabilities)) {

                                        viewHolder.img_wifiwithpwd.setVisibility(View.INVISIBLE);
                                    }
                                    return convertView;
                                }
                                class ViewHolder {
                                    TextView wifiName;
                                    ImageView img_wifiwithpwd;
                                }
                            }
        );
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            DialogueWifiCon dialogue;
            int prevPos = -1;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //解决listview连续点击出现多个对话框
                if (prevPos == position && dialogue != null && dialogue.isShowing()) {
                    return;
                }
                prevPos = position;
                //需要密码弹出对话框，不需要直接连接
                String clickSSID = list.get(position).SSID;
                if (!ssid_nopwd.contains(clickSSID)) {
                    dialogue = new DialogueWifiCon(WifiSetAct.this, clickSSID);
                    dialogue.show();
                } else if (!wifiUtil.connectWifi(clickSSID, null, WifiUtil.WifiCipherType.WIFICIPHER_NOPASS)) {
                    Toast.makeText(WifiSetAct.this, clickSSID + "连接失败，请重试", Toast.LENGTH_SHORT).show();
                }
            }
        });
        myWifiName = (TextView) findViewById(R.id.mywifiname);
        selected = (ImageView) findViewById(R.id.selected);
        mywifiwithpwd = (ImageView) findViewById(R.id.img_mywifiwithpwd);
        mywifilevel = (ImageView) findViewById(R.id.mywifilevel);

    }

    public void setWifiTittle(String name) {
        myWifiName.setText(name);
        selected.setVisibility(View.VISIBLE);
        mywifilevel.setVisibility(View.VISIBLE);
        if (ssid_nopwd.contains(name)) {
            mywifiwithpwd.setVisibility(View.INVISIBLE);
        }else {
            mywifiwithpwd.setVisibility(View.VISIBLE);
        }
    }

    public void resetWifiTittle() {
        myWifiName.setText("WiFi无连接");
        selected.setVisibility(View.INVISIBLE);
        mywifiwithpwd.setVisibility(View.INVISIBLE);
        mywifilevel.setVisibility(View.INVISIBLE);
    }

    /*移除设备集合中已连接的设备
    *@author zhouqiang
    * created at 2016/8/9  14:55
    */
    public void refreshContent(String ssid) {
        list.clear();
        list.addAll((ArrayList<ScanResult>) wifiUtil.getScanResults());
        Iterator<ScanResult> resluts = list.iterator();
        for (; resluts.hasNext(); ) {
            if (ssid.equals(resluts.next().SSID)) {
                resluts.remove();
                break;
            }
        }
        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    public void closeWifiAct(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkchangedReceiver);
        super.onDestroy();
    }
}
