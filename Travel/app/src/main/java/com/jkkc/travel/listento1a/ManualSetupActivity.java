package com.jkkc.travel.listento1a;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jkkc.travel.R;
import com.jkkc.travel.connectAp.WifiAdmin;
import com.jkkc.travel.sweepcodebindlogin.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2017/7/14.
 */

public class ManualSetupActivity extends Activity {

    @BindView(R.id.etEnterAccount)
    EditText mEtEnterAccount;
    @BindView(R.id.etEnterPwd)
    EditText mEtEnterPwd;
    @BindView(R.id.btnConnect)
    Button mBtnConnect;
    private String mSsid;
    private String mPwd;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manual_setup);
        ButterKnife.bind(this);


    }

    public static final String TAG = "ManualSetupActivity";
    private WifiAdmin mWifiAdmin;
    WifiManager mWifiManager;

    private void wifiConnect() {


        // 连接到外网
        WifiConfiguration mWifiConfiguration;


        //检测指定SSID的WifiConfiguration 是否存在
        WifiConfiguration tempConfig = mWifiAdmin.IsExsits(mSsid);

        if (tempConfig == null) {
            //创建一个新的WifiConfiguration ，CreateWifiInfo()需要自己实现
            mWifiConfiguration = mWifiAdmin.createWifiInfo
                    (mSsid, mPwd, WifiAdmin.TYPE_WPA);
            int wcgID = mWifiAdmin.addNetwork(mWifiConfiguration);

            boolean b = mWifiManager.enableNetwork(wcgID, true);


        } else {
            //发现指定WiFi，并且这个WiFi以前连接成功过
            mWifiConfiguration = tempConfig;
            boolean b = mWifiManager.enableNetwork(mWifiConfiguration.networkId, true);

        }

    }


    @SuppressLint("WifiManagerLeak")
    @OnClick(R.id.btnConnect)
    public void onViewClicked() {

        String A_WIFI_SSID = PrefUtils.getString(getApplicationContext(), "1A_WIFI_SSID", "");
        String PASSWORD = PrefUtils.getString(getApplicationContext(), "PASSWORD", "");

        mEtEnterAccount.setText(A_WIFI_SSID);
        mEtEnterPwd.setText(PASSWORD);

        mSsid = mEtEnterAccount.getText().toString().trim();
        mPwd = mEtEnterPwd.getText().toString().trim();


        //获取wifi管理服务
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        mWifiAdmin = new WifiAdmin(this) {

            @Override
            public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {

                ManualSetupActivity.this.registerReceiver(receiver, filter);
                return null;

            }

            @Override
            public void myUnregisterReceiver(BroadcastReceiver receiver) {

                ManualSetupActivity.this.unregisterReceiver(receiver);
            }

            @Override
            public void onNotifyWifiConnected() {

                Log.v(TAG, "have connected success!");
                Log.v(TAG, "###############################");
            }

            @Override
            public void onNotifyWifiConnectFailed() {

                Log.v(TAG, "have connected failed!");
                Log.v(TAG, "###############################");
            }

        };


        mWifiAdmin.openWifi();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                wifiConnect();

            }

        }, 8000);

        startActivity(new Intent(getApplicationContext(), GuideSpeak.class));



    }
}
