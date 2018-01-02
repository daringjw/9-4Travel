package com.jkkc.travel.listento1a;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jkkc.travel.R;
import com.jkkc.travel.UI.HomeActivity0;
import com.jkkc.travel.service.UdpPcmPlayerService;
import com.jkkc.travel.sweepcodebindlogin.PrefUtils;
import com.jkkc.travel.util.WifiAutoConnectManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Guan on 2017/7/6.
 */

public class GuideSpeak extends Activity {

    @BindView(R.id.btnGuidePlay)
    ImageView btnGuidePlay;
    @BindView(R.id.btnGuideStop)
    ImageView btnGuideStop;

    private ImageView mBtnBack;

    private long mEnd;
    private long mStart;

    public static final String TAG = "GuideSpeak";

    private WifiManager wifiManager;
    private WifiAutoConnectManager mWifiAutoConnectManager;

    private String ssid;
    private String pwd;
    private Thread audioPlayThread;
    private Thread listenThread;
    private Handler audioHandler;
    private boolean isPlaying;

    // wifi热点开关
    @TargetApi(Build.VERSION_CODES.M)
    public boolean setWifiApEnabled(boolean enabled) {

        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();

            apConfig.SSID = ssid;
            //配置热点的密码

            apConfig.preSharedKey = pwd;
            //标记已经设置密码

            Log.e(TAG, "ssid=" + ssid + "pwd=" + pwd);

            //apConfig.enterpriseConfig
            apConfig.allowedKeyManagement.set(4);
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {

            return false;
        }
    }

    String A_WIFI_SSID;
    String PASSWORD1;

    //是否连接WIFI
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private ServiceConnection conn;
    private UdpPcmPlayerService mUdpPcmPlayerService;


    @Override
    protected void onResume() {

        super.onResume();

        MobclickAgent.onResume(this);


    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);


    }

    private ContentResolver mContentResolver;

    //方法三
    public void unLock() {

        mContentResolver = getContentResolver();
        //不建议使用
        //setLockPatternEnabled(Android.provider.Settings.System.LOCK_PATTERN_ENABLED,false);

        //推荐使用
        setLockPatternEnabled(android.provider.Settings.Secure.LOCK_PATTERN_ENABLED, false);
    }

    private void setLockPatternEnabled(String systemSettingKey, boolean enabled) {
        //不建议使用
        //android.provider.Settings.System.putInt(mContentResolver,systemSettingKey, enabled ? 1 : 0);

        //推荐使用
        android.provider.Settings.Secure.putInt(mContentResolver, systemSettingKey, enabled ? 1 : 0);
    }
    //但注意要加权限AndroidManifest.xml文件中加入
    //<uses-permission android:name="android.permission.WRITE_SETTINGS" />
    //还要特别注意的是要加入 android:sharedUserId="android.uid.system"，但有一个问题，
    //如果加入了sharedUserId后就不能使用eclipse编译了，一定要手动通过 mm -B进行编译，然后把apk install到模拟器或设备中


    WifiManager.WifiLock wifiLock;

    /**
     * @param lockName 锁的名字
     * @return wifiLock
     */
    public WifiManager.WifiLock createWifiLock(String lockName) {
        wifiLock = wifiManager.createWifiLock(lockName);
        return wifiLock;

    }


    /**
     * @param lockName 锁的名称
     * @param lockType WIFI_MODE_FULL == 1 <br/>
     *                 扫描，自动的尝试去连接一个曾经配置过的点<br />
     *                 WIFI_MODE_SCAN_ONLY == 2 <br/>
     *                 只剩下扫描<br />
     *                 WIFI_MODE_FULL_HIGH_PERF = 3 <br/>
     *                 在第一种模式的基础上，保持最佳性能<br />
     * @return wifiLock
     */
    public WifiManager.WifiLock createWifiLock(String lockName, int lockType) {
        wifiLock = wifiManager.createWifiLock(lockType, lockName);
        return wifiLock;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide_speak);
        ButterKnife.bind(this);

        mHandler = new Handler(); // 创建Handler

        ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
        pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);


        A_WIFI_SSID = PrefUtils.getString(getApplicationContext(), "1A_WIFI_SSID", null);
        PASSWORD1 = PrefUtils.getString(getApplicationContext(), "PASSWORD", null);

        //获取wifi管理服务
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);

        if (!isWifiConnected(getApplicationContext())) {

            wifiManager.setWifiEnabled(true);
            PrefUtils.setBoolean(getApplicationContext(), "ApOpen", false);

            try {
                mWifiAutoConnectManager.connect(A_WIFI_SSID, PASSWORD1,
                        WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);


            } catch (Exception e) {
                Log.e(TAG, "" + e);
            }

            // 暂时这个线程没啥用，只是起到一个提示
            Thread thread = new Thread() {
                public boolean isWorking = true;

                @Override
                public void run() {
                    int times = 0;
                    while (!isWifiConnected(getApplicationContext()) && isWorking) {
                        try {
                            if (times > 90) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "wifi连接失败，请返回页面重试",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                isWorking = false;
                            }
                            Thread.sleep(340);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        times++;
                    }
                }
            };
            thread.start();
        }


        start1();

        Intent intent = new Intent(getApplicationContext(), UdpPcmPlayerService.class);
        startService(intent);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("xxxxx 绑定成功调用：onServiceConnected");
                // 获取Binder
                UdpPcmPlayerService.UdpPcmPlayerBinder binder = (UdpPcmPlayerService.UdpPcmPlayerBinder) service;
                mUdpPcmPlayerService = binder.getService();
                mUdpPcmPlayerService.start();
                mUdpPcmPlayerService.play();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mUdpPcmPlayerService = null;
            }

        };

        bindService(intent, conn, Service.BIND_AUTO_CREATE);


        mBtnBack = (ImageView) findViewById(R.id.btnBack);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stop1();

                finish();
            }
        });


    }


    /**
     * 判断wifi的锁是否持有
     *
     * @return
     */
    public boolean isHeld() {
        return wifiLock.isHeld();
    }

    /**
     * 加上锁
     */
    public void lockWifi() {
        wifiLock.acquire();
    }

    private Handler mHandler; // 用于子线程发送更新UI的线程消息到主线程

    Runnable mRunnablePlayerStop = new Runnable() {
        @Override
        public void run() {
            System.out.println("xxxxx 导游讲解服务已经停止");
            Toast.makeText(getApplicationContext(), "导游讲解服务已经停止", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), HomeActivity0.class));
            finish();

        }
    };


    TestIsPlayingThread mTestIsPlayingThread;

    class TestIsPlayingThread extends Thread {
        public boolean isWorking = false;


        PowerManager mPowerManager;
        PowerManager.WakeLock mWakeLock;

        public TestIsPlayingThread(String name) {

            super(name);
        }

        public void run() {

            while (isWorking) {
                try {
                    Thread.sleep(1000);
                    if ((mUdpPcmPlayerService != null)) {
                        if (mUdpPcmPlayerService.getPlayState() == UdpPcmPlayerService.PLAY_STATE.STOP) {
                            mHandler.post(mRunnablePlayerStop);
                            isWorking = false;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("xxxxx TestIsPlayingThread e = " + e.getMessage());
                }


                try {
                    sleep(1000 * 280);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                // 定时唤醒屏幕，此段内容在可放入一线程定时执行
                try {
                    if (mPowerManager == null)
                        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (mWakeLock == null) {
                        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "xxxxTag"); // 定时唤醒屏幕
                        //mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "xxxxxWakeLock");
                    } else {
                        if (!mWakeLock.isHeld()) {
                            mWakeLock.acquire();
                        } else {
                            mWakeLock.release();
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void start1() {

        try {

            btnGuidePlay.setImageResource(R.mipmap.guide_pause);

            if (mUdpPcmPlayerService != null) {
                mUdpPcmPlayerService.play();
            }

            mTestIsPlayingThread = new TestIsPlayingThread("TestIsPlayingThread");
            mTestIsPlayingThread.isWorking = true;
            mTestIsPlayingThread.start();

            mStart = System.currentTimeMillis();
            Log.e(TAG, "start=" + mStart);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void stop1() {

        if (mUdpPcmPlayerService != null) {
            mUdpPcmPlayerService.pause();
        }

        mTestIsPlayingThread.isWorking = false;

        btnGuidePlay.setImageResource(R.mipmap.guide_play);

        mEnd = System.currentTimeMillis();
        Log.e(TAG, "end=" + mEnd);

        long disTime = mEnd - mStart;
        String usedTime = formatDuring(disTime);
        PrefUtils.setString(getApplicationContext(), "usedTime", usedTime);

        Log.e(TAG, "用时=" + usedTime);

        Intent intent = getIntent();
        intent.putExtra("usedTime", usedTime);
        this.setResult(1, intent);

    }


    /**
     * 用来判断服务是否运行.
     *
     * @param
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
//        return days + "日" + hours + "小时" + minutes + "分"
//                + seconds + "秒";
        return minutes + "分" + seconds + "秒";
    }

    @Override
    protected void onDestroy() {
        if (mUdpPcmPlayerService != null) {
            mUdpPcmPlayerService.stop();
            unbindService(conn);
        }

        mEnd = System.currentTimeMillis();
        long disTime = mEnd - mStart;
        String usedTime = formatDuring(disTime);

        PrefUtils.setString(getApplicationContext(), "usedTime", usedTime);
        super.onDestroy();
    }

    private boolean isWork = true;

    @OnClick({R.id.btnGuidePlay, R.id.btnGuideStop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnGuidePlay:
                //开始
                if (isWork) {

                    if (mUdpPcmPlayerService != null) {
                        mUdpPcmPlayerService.pause();
                    }

                    btnGuidePlay.setImageResource(R.mipmap.guide_play);

                    mEnd = System.currentTimeMillis();
                    Log.e(TAG, "end=" + mEnd);

                    long disTime = mEnd - mStart;
                    String usedTime = formatDuring(disTime);
                    PrefUtils.setString(getApplicationContext(), "usedTime", usedTime);

                    Log.e(TAG, "用时=" + usedTime);

                    Intent intent = getIntent();
                    intent.putExtra("usedTime", usedTime);
                    this.setResult(1, intent);
                    isWork = false;


                } else {

                    start1();
                    isWork = true;

                }


                break;
            case R.id.btnGuideStop:

                //结束
                finish();

                break;
        }
    }


}
