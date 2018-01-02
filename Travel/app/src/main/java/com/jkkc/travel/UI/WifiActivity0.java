package com.jkkc.travel.UI;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jkkc.travel.R;
import com.jkkc.travel.config.Config;
import com.jkkc.travel.http.UpdateAppHttpUtil;
import com.jkkc.travel.utils.PrefUtils;
import com.jkkc.travel.utils.WifiAutoConnectManager;
import com.umeng.analytics.MobclickAgent;
import com.vector.update_app.UpdateAppManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Hashtable;


public class WifiActivity0 extends AppCompatActivity {

    private ImageView sweepIV;
    private ImageView btnBack;
    private int QR_WIDTH = 300;
    private int QR_HEIGHT = 300;
    private WifiManager wifiManager;
    private Switch mBtnToggle;
    private TextView mTvSSID;
    private TextView mTvPwd;
    private String ssid;
    private String pwd;
    private String mLocalMacAddress;
    private WifiAutoConnectManager mWifiAutoConnectManager;
    private int REQUEST_CODE;
    private boolean wifi;
    private String mMac;
    private TextView mTvSim;
    private boolean mFlowhelper = false;
    private boolean mApOpen;
    private TextView tvConnectPeopleCount;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.System.canWrite(this)) {
                //检查返回结果
                Toast.makeText(WifiActivity0.this,
                        "允许修改系统设置已经开启，恭喜，wifi热点可以使用",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(WifiActivity0.this,
                        "允许修改系统设置没有开启，抱歉，wifi热点不能使用，请重新打开应用，并且开启权限",
                        Toast.LENGTH_LONG).show();


            }
        }
    }


    /**
     * @param bytes
     * @return
     * @Description long转文件大小M单位方法
     * @author temdy
     */
    public String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
        return returnValue + "";
    }

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrimage);

        long l = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();

        mTvSim = (TextView) findViewById(R.id.tvSim);
        mTvSim.setText("手机卡流量已经使用 " + bytes2kb(l) + "M");
        mTvSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFlowhelper = PrefUtils.getBoolean(getApplicationContext(), "flowhelper", false);

                if (!mFlowhelper) {
                    new UpdateAppManager
                            .Builder()
                            //当前Activity
                            .setActivity(WifiActivity0.this)
                            //更新地址
                            .setUpdateUrl(Config.FLOWHELPER_URL)
                            //实现httpManager接口的对象
                            .setHttpManager(new UpdateAppHttpUtil())

                            .build()
                            .update();
                }

                Intent intent = getPackageManager()
                        .getLaunchIntentForPackage("com.aidian.flowhelper");

                if (intent != null) {

                    PrefUtils.setBoolean(getApplicationContext(), "flowhelper", true);
                    startActivity(intent);

                } else {

                    Toast.makeText(getApplicationContext(), "你还没有集成这个功能",
                            Toast.LENGTH_SHORT).show();

                }


            }
        });


        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
        ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
        pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);

        sweepIV = (ImageView) findViewById(R.id.sweepIV);
        mTvSSID = (TextView) findViewById(R.id.tvSSID);
        mTvPwd = (TextView) findViewById(R.id.tvPwd);

        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiAutoConnectManager = new WifiAutoConnectManager(WifiActivity0.this);

        btnBack = (ImageView) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


        mBtnToggle = (Switch) findViewById(R.id.btnToggle);

//        wifi = PrefUtils.getBoolean(getApplicationContext(), "wifi", false);

        mApOpen = PrefUtils.getBoolean(getApplicationContext(), "ApOpen", false);
        if (!isWifiApOpen(getApplicationContext())) {

            mBtnToggle.setBackgroundResource(R.mipmap.toggle_bg);
            mBtnToggle.setTextColor(Color.BLACK);
            mBtnToggle.setText("       开启热点");
            mBtnToggle.setChecked(false);
            createQRImage(mMac + "," + ssid + "," + pwd);
            mTvSSID.setText("");
            mTvPwd.setText("");
            sweepIV.setImageResource(R.mipmap.no_ap);

        } else {

            mBtnToggle.setBackgroundResource(R.mipmap.toogle_en);
            mBtnToggle.setTextColor(Color.RED);
            mBtnToggle.setText("       关闭热点");
            mBtnToggle.setChecked(true);
            createQRImage(mMac + "," + ssid + "," + pwd);
            mTvSSID.setText("账号：" + ssid);
            mTvPwd.setText("密码：" + pwd);

        }

        mBtnToggle.setChecked(isWifiApOpen(getApplicationContext()));


        mBtnToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                mBtnToggle.setChecked(isChecked);
                if (isChecked) {

                    mBtnToggle.setBackgroundResource(R.mipmap.toogle_en);
                    mBtnToggle.setTextColor(Color.RED);
                    mBtnToggle.setText("       关闭热点");
                    //关闭wifi,开启热点
                    setWifiApEnabled(true);
                    PrefUtils.setBoolean(getApplicationContext(), "ApOpen", true);
                    PrefUtils.setBoolean(getApplicationContext(), "wifi", false);
                    //  WIFI:T:WPA;P:"zjxy12345678";S:zjxy;
                    //支持小米手机自动连接wifi
//                    createQRImage("WIFI:T:WPA;P:\"" + pwd + "\";S:" + ssid + );
                    createQRImage(mMac + "," + ssid + "," + pwd);
                    mTvSSID.setText("账号：" + ssid);
                    mTvPwd.setText("密码：" + pwd);


                } else {

                    mBtnToggle.setBackgroundResource(R.mipmap.toggle_bg);
                    mBtnToggle.setTextColor(Color.BLACK);
                    mBtnToggle.setText("       开启热点");
                    sweepIV.setImageResource(R.mipmap.no_ap);
                    //关闭热点
                    setWifiApEnabled(false);
                    //打开wifi
                    wifiManager.setWifiEnabled(true);
                    PrefUtils.setBoolean(getApplicationContext(), "ApOpen", false);
                    PrefUtils.setBoolean(getApplicationContext(), "wifi", true);
                    mTvSSID.setText("");
                    mTvPwd.setText("");


                }


            }
        });

        tvConnectPeopleCount = (TextView) findViewById(R.id.tvConnectPeopleCount);
        tvConnectPeopleCount.setText(isWifiApOpen(getApplicationContext()) ?
                String.valueOf(getLinkedNum()) : "0");

        handler.postDelayed(runnable, 5000);


    }

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

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                handler.postDelayed(this, 5000);
                tvConnectPeopleCount.setText(String.valueOf(getLinkedNum()));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 获取热点连接个数
     *
     * @return num
     */
    private int getLinkedNum() {
        int linkedNum = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = reader.readLine();
            //读取第一行信息，就是IP address HW type Flags HW address Mask Device
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("[ ]+");
                if (tokens.length < 6) {
                    continue;
                }
                //0 ip  3  mac  2  连接状态
                String flag = tokens[2];//表示连接状态
                if (flag.equals("0x2")) {
                    linkedNum++;
                }
            }
            return linkedNum;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linkedNum;
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }


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


    //是否连接WIFI
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }


    public void createQRImage(String url) {
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            sweepIV.setImageBitmap(bitmap);


        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


}
