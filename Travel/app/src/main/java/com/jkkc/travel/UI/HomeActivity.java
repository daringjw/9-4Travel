package com.jkkc.travel.UI;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.NetUtils;
import com.jkkc.travel.R;
import com.jkkc.travel.bean.HeWeather5Bean;
import com.jkkc.travel.bean.NewsBean;
import com.jkkc.travel.bean.UpdateInfo;
import com.jkkc.travel.config.Config;
import com.jkkc.travel.db.NewsHelper;
import com.jkkc.travel.http.UpdateAppHttpUtil;
import com.jkkc.travel.listento1a.ScenicSpotsOnActivity;
import com.jkkc.travel.utils.Constant;
import com.jkkc.travel.utils.EncryptUntils;
import com.jkkc.travel.utils.PrefUtils;
import com.jkkc.travel.utils.RxBus;
import com.jkkc.travel.view.MyImageView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.vector.update_app.UpdateAppManager;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Guan on 2017/9/8.
 */

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    @BindView(R.id.ivWeatherIcon)
    ImageView ivWeatherIcon;
    @BindView(R.id.tvCity)
    TextView tvCity;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvWeatherState)
    TextView tvWeatherState;
    @BindView(R.id.tvTemperature)
    TextView tvTemperature;
    @BindView(R.id.mivNavigationPosition)
    MyImageView mivNavigationPosition;
    @BindView(R.id.mivEmergencyTool)
    MyImageView mivEmergencyTool;
    @BindView(R.id.mivNews)
    MyImageView mivNews;
    @BindView(R.id.mivInterpretationSpot)
    MyImageView mivInterpretationSpot;
    @BindView(R.id.mivAddressBook)
    MyImageView mivAddressBook;
    @BindView(R.id.mivForeignAssistant)
    MyImageView mivForeignAssistant;
    @BindView(R.id.mivWifiHotspot)
    MyImageView mivWifiHotspot;
    @BindView(R.id.tvUserName)
    Button tvUserName;
    @BindView(R.id.tvMsgCount)
    TextView tvMsgCount;
    @BindView(R.id.tvBeiJing)
    TextView tvBeiJing;
    @BindView(R.id.tvBeiJingTime)
    TextView tvBeiJingTime;
    @BindView(R.id.tvBeiJingDate)
    TextView tvBeiJingDate;


    private String mLocalVersionName;

    /**
     * 获取当前程序的版本号
     */
    private String getLocalVersionName() throws Exception {
        //获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        return packInfo.versionName;

    }

    private void initMap() {

        Constant.WEATHERIMG.put("100", R.mipmap.weather100);
        Constant.WEATHERIMG.put("101", R.mipmap.weather101);
        Constant.WEATHERIMG.put("102", R.mipmap.weather102);
        Constant.WEATHERIMG.put("103", R.mipmap.weather103);
        Constant.WEATHERIMG.put("104", R.mipmap.weather104);
        Constant.WEATHERIMG.put("200", R.mipmap.weather200);
        Constant.WEATHERIMG.put("201", R.mipmap.weather201);
        Constant.WEATHERIMG.put("202", R.mipmap.weather202);
        Constant.WEATHERIMG.put("203", R.mipmap.weather203);
        Constant.WEATHERIMG.put("204", R.mipmap.weather204);
        Constant.WEATHERIMG.put("205", R.mipmap.weather205);
        Constant.WEATHERIMG.put("206", R.mipmap.weather206);
        Constant.WEATHERIMG.put("207", R.mipmap.weather207);
        Constant.WEATHERIMG.put("208", R.mipmap.weather208);
        Constant.WEATHERIMG.put("209", R.mipmap.weather209);
        Constant.WEATHERIMG.put("210", R.mipmap.weather210);
        Constant.WEATHERIMG.put("211", R.mipmap.weather211);
        Constant.WEATHERIMG.put("212", R.mipmap.weather212);
        Constant.WEATHERIMG.put("213", R.mipmap.weather213);
        Constant.WEATHERIMG.put("300", R.mipmap.weather300);
        Constant.WEATHERIMG.put("301", R.mipmap.weather301);
        Constant.WEATHERIMG.put("302", R.mipmap.weather302);
        Constant.WEATHERIMG.put("303", R.mipmap.weather303);
        Constant.WEATHERIMG.put("304", R.mipmap.weather304);
        Constant.WEATHERIMG.put("305", R.mipmap.weather305);
        Constant.WEATHERIMG.put("306", R.mipmap.weather306);
        Constant.WEATHERIMG.put("307", R.mipmap.weather307);
        Constant.WEATHERIMG.put("308", R.mipmap.weather308);
        Constant.WEATHERIMG.put("309", R.mipmap.weather309);
        Constant.WEATHERIMG.put("310", R.mipmap.weather310);
        Constant.WEATHERIMG.put("311", R.mipmap.weather311);
        Constant.WEATHERIMG.put("312", R.mipmap.weather312);
        Constant.WEATHERIMG.put("313", R.mipmap.weather313);
        Constant.WEATHERIMG.put("400", R.mipmap.weather400);
        Constant.WEATHERIMG.put("401", R.mipmap.weather401);
        Constant.WEATHERIMG.put("402", R.mipmap.weather402);
        Constant.WEATHERIMG.put("403", R.mipmap.weather403);
        Constant.WEATHERIMG.put("404", R.mipmap.weather404);
        Constant.WEATHERIMG.put("405", R.mipmap.weather405);
        Constant.WEATHERIMG.put("406", R.mipmap.weather406);
        Constant.WEATHERIMG.put("407", R.mipmap.weather407);
        Constant.WEATHERIMG.put("500", R.mipmap.weather500);
        Constant.WEATHERIMG.put("501", R.mipmap.weather501);
        Constant.WEATHERIMG.put("502", R.mipmap.weather502);
        Constant.WEATHERIMG.put("503", R.mipmap.weather503);
        Constant.WEATHERIMG.put("504", R.mipmap.weather504);
        Constant.WEATHERIMG.put("507", R.mipmap.weather507);
        Constant.WEATHERIMG.put("900", R.mipmap.weather900);
        Constant.WEATHERIMG.put("901", R.mipmap.weather901);
        Constant.WEATHERIMG.put("999", R.mipmap.weather999);

    }


    private Handler apHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //初始化天气图标
        initMap();


        //显示当地当前日期，星期几，时间
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd EEEE");
        String currentDate = format.format(date);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvDate.setText(currentDate);

        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
        String currentTime = format1.format(date);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvTime.setText(currentTime);

        //获取地理位置
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
        mLocationClient.start();


        //首页，检查更新软件
        try {
            mLocalVersionName = getLocalVersionName();

        } catch (Exception e) {

            e.printStackTrace();

        }

        OkGo.<String>get(Config.UPDATE_URL)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        Log.e(TAG, "result=" + response.body().toString());
                        Gson gson = new Gson();
                        UpdateInfo updateInfo = gson.fromJson(response.body().toString()
                                , UpdateInfo.class);
                        String new_version = updateInfo.getNew_version();

                        if (new_version.equals(mLocalVersionName)) {

                            Toast.makeText(getApplicationContext(),
                                    "已经是最新版本了，无需更新", Toast.LENGTH_SHORT).show();

                        } else {

                            new UpdateAppManager
                                    .Builder()
                                    //当前Activity
                                    .setActivity(HomeActivity.this)
                                    //更新地址
                                    .setUpdateUrl(Config.UPDATE_URL)
                                    //实现httpManager接口的对象
                                    .setHttpManager(new UpdateAppHttpUtil())
                                    .build()
                                    .update();

                        }


                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });


        //获取mac,设置ssid, pwd 存储sharedpreference
        mac = PrefUtils.getString(getApplicationContext(), "mac", null);
        if (TextUtils.isEmpty(mac)) {
            //mac为空
            mac = getLocalMacAddress();
        }
        //mac不为空，直接读缓存

        //判断是否有sim卡
        if (isCanUseSim()) {
            //有手机卡
            if (isNetworkConnected(this)) {
                //有手机卡，有流量，可以上网


            } else {
                //有手机卡，无流量，无法上网
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("有手机卡，无流量，无法上网")
                        .setContentText("亲，请换张手机卡试一试！")
                        .setConfirmText("确定")
                        .show();

            }

        } else {

            //无手机卡
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("检测到没有手机卡")
                    .setContentText("亲，请插入手机卡！")
                    .setConfirmText("确定")
                    .show();


        }


        //二维码信息：mac+ssid+pwd
        //createQRImage(mac + "," + ssid + "," + pwd);
        // mac 后四位
        Log.e(TAG, "mac=" + mac);
        ssid = "ATB_" + mac.substring(mac.length() - 4, mac.length());
        pwd = ssid;
        //存储 ap的 ssid 和 pwd
        PrefUtils.setString(getApplicationContext(), "ssid", ssid);
        PrefUtils.setString(getApplicationContext(), "pwd", pwd);
        Log.e(TAG, "ssid=" + ssid);

        //关闭wifi,开启热点
        //获取wifi管理服务
        wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
//        setWifiApEnabled(true);


        //用户名显示
        peopleName = PrefUtils.getString(getApplicationContext(), "peopleName", null);
        if (!TextUtils.isEmpty(peopleName)) {
            tvUserName.setText(peopleName);
        } else {
            tvUserName.setText("登录");
        }

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //判断是否有sim卡
                if (isCanUseSim()) {
                    //有手机卡
                    if (isNetworkConnected(getApplicationContext())) {
                        //有手机卡，有流量，可以上网
                        //判断是否已经登陆
//                        hasLogin = PrefUtils.getBoolean(getApplicationContext(), "hasLogin", false);

                        if (hasLogin) {
                            //已经登陆，进入我的页面
                            startActivity(new Intent(getApplicationContext(), MineActivity.class));


                        } else {

                            //没有登陆，进入登录对话框
                            enterLoginDialog();
                        }


                    } else {
                        //有手机卡，无流量，无法上网
                        new SweetAlertDialog(HomeActivity.this)
                                .setTitleText("有手机卡，无流量，无法上网")
                                .setContentText("亲，请换张手机卡试一试！")
                                .show();

                    }

                } else {

                    //无手机卡
                    new SweetAlertDialog(HomeActivity.this)
                            .setTitleText("检测到没有手机卡")
                            .setContentText("亲，请插入手机卡！")
                            .show();


                }

            }
        });

        // 接收消息监听
        initMessageListener();

        //注册环信连接状态监听
        //注册一个监听连接状态的listener
        if (myConnectionListener == null) {
            myConnectionListener = new MyConnectionListener();
            EMClient.getInstance().addConnectionListener(myConnectionListener);
        }


        initRxBus();

        //刷新天气,用户名,北京时间
        RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh(RefreshLayout refreshlayout) {

                refreshlayout.finishRefresh(2000);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        recreate();

                    }
                }, 2000);

            }

        });

        refreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {


                refreshlayout.finishLoadmore(2000);
            }

        });

        //设置 Header 为 Material风格
        refreshLayout.setRefreshHeader(new MaterialHeader(this).setShowBezierWave(true));
        //设置 Footer 为 球脉冲
        refreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));


        //5分钟内无人连接将关闭热点，五分钟后将自动开启热点
//        apHandler.postDelayed(apRunnable, 1000 * 60 * 5);

        //获取天气
//        getWeatherInfo();


    }

    private Runnable apRunnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                apHandler.postDelayed(this, 1000 * 60 * 5);
                Log.e(TAG, "五分钟自动打开热点次数？" + i++);
                setWifiApEnabled(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Subscription mEmMessageSub;

    private void initRxBus() {

        mEmMessageSub = RxBus.getInstance().toObserverable(List.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List>() {
                    @Override
                    public void call(List list) {
                        String cmd = ((EMMessage) (list.get(0))).getBody().toString();
                        switch (cmd) {
                            case "cmd:\"1018\"":
                                handleMessage(list);
                                break;

                            case "cmd:\"1013\"":

                                handleMessage1013(list);

                                break;
                        }
                    }
                });


    }

    private void handleMessage1013(List<EMMessage> messages) {


        //信息推送

        viberateUser();
        //报警成功回复信息

        NewsBean newsBean = new NewsBean();
        newsBean.setUuid(UUID.randomUUID().toString());
        newsBean.setIsRead("0");
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        newsBean.setTime(dateString);
        newsBean.setContent("报警成功");
        newsBean.setTitle("平台回复");
        newsBean.setCity(city);
        newsBean.setLevel(" 4");
        NewsHelper.getDBHelper().insert(newsBean, this);


    }

    private void viberateUser() {

        HomeActivity.this.recreate();


        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat
                        .Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.sos_marker)
                        .setContentTitle("新消息")
                        .setContentText("您有新消息")
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(System.currentTimeMillis());

        Intent resultIntent = new Intent(getApplicationContext(), MessageActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        int mNotificationId = 001;

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        mNotifyMgr.notify(mNotificationId, notification);

//        playAlarm();

    }

    /**
     * 震动+提示音
     */
    private void playAlarm() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        r.play();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {500, 100, 500, 100};
        vibrator.vibrate(pattern, -1);
        vibrator.vibrate(2000);
    }

    int i = 0;

    private void handleMessage(List<EMMessage> messages) {
        //信息推送
        try {

            viberateUser();
            //预警信息
            String msg = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("msg").getBytes(), "ciatInteraction");
            String title = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("title").getBytes(), "ciatInteraction");
            String city = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("city").getBytes(), "ciatInteraction");
            String level = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("level").getBytes(), "ciatInteraction");
            Log.e(TAG, "msg=" + msg);
            Log.e(TAG, "title=" + title);
            int i = city.indexOf("*");
            city = city.substring(0, i);
            Log.e(TAG, "city=" + city);
            Log.e(TAG, "level=" + level);

            NewsBean newsBean = new NewsBean();
            newsBean.setUuid(UUID.randomUUID().toString());
            newsBean.setIsRead("0");
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            newsBean.setTime(dateString);
            newsBean.setContent(msg);
            newsBean.setTitle(title);
            newsBean.setCity(city);
            newsBean.setLevel(level);

            Log.e(TAG, "执行几次？" + i++);


            if (level.equals("5")) {


            }

        } catch (HyphenateException e) {
            e.printStackTrace();
            Log.e(": ", "onCmdMessageReceived: " + e.getMessage());
        }
    }

    private MyConnectionListener myConnectionListener;

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {

            //已登陆，保持连接
            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);
            hasLogin = true;
            Log.e(TAG, "连接中=" + hasLogin);
            tvUserName.setText(peopleName);


        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除

                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录

                    } else {
                        if (NetUtils.hasNetwork(HomeActivity.this)) {
                            //连接不到聊天服务器
                            //连接断了
                            hasLogin = false;
                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);
                            tvUserName.setText("登录");

//                            Toast.makeText(getApplicationContext(), "连接不到服务器,请重新登陆",
//                                    Toast.LENGTH_LONG).show();

                            Log.e(TAG, "未连接=" + hasLogin);


                        } else {
                            //当前网络不可用，请检查网络设置

                        }

                    }
                }
            });
        }
    }

    private EMMessageListener msgListener;
    private boolean hasLogin;

    private void initMessageListener() {


        if (msgListener == null) {
            msgListener = new EMMessageListener() {

                @Override
                public void onMessageReceived(final List<EMMessage> messages) {

                }

                @Override
                public void onCmdMessageReceived(List<EMMessage> messages) {
                    //收到透传消息.
                    String cmd = messages.get(0).getBody().toString();
                    Log.e(TAG, "cmd=" + cmd);

                    switch (cmd) {
                        case "cmd:\"1005\"":
                            try {

                                peopleName = EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("peopleName").getBytes(), "ciatInteraction");

                                Log.e(TAG, "userName=" + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("peopleName").getBytes(), "ciatInteraction"));

                                if (peopleName.equals("未知用户")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(HomeActivity.this, "未绑定设备",
                                                    Toast.LENGTH_SHORT).show();

                                            EMClient.getInstance().logout(true);
                                            if (pDialog != null) {
                                                pDialog.dismiss();

                                            }

                                            new SweetAlertDialog(HomeActivity.this,
                                                    SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("未绑定设备")
                                                    .setContentText("请先扫码绑定!")
                                                    .show();

                                            tvUserName.setText("登录");
                                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);
                                            hasLogin = false;


                                        }
                                    });

                                } else {

                                    //更新姓名

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            PrefUtils.setString(getApplicationContext(), "peopleName", peopleName);
                                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);
                                            hasLogin = true;
                                            tvUserName.setText(peopleName);
                                            if (pDialog != null) {
                                                pDialog.dismiss();

                                            }

                                        }
                                    });

                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (pDialog != null) {
                                            pDialog.dismiss();

                                        }

                                    }
                                });

                            } catch (final HyphenateException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(HomeActivity.this, e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                e.printStackTrace();
                            }
                            break;


                        case "cmd:\"1013\"":
                            //报警返回
                            try {
                                Log.e(": ", "报警返回msg: " + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("msg").getBytes(), "ciatInteraction"));
                                Log.e(": ", "报警返回result: " + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("result").getBytes(), "ciatInteraction"));
                                RxBus.getInstance().post(messages);


                                viberateUser();

                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }
                            break;

                        case "cmd:\"1018\"":
                            RxBus.getInstance().post(messages);
                            break;


                        case "cmd:\"1024\"":
                            //语音讲解回复
                            try {
                                Log.e(": ", "语音讲解回复: " + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("1A_MAC").getBytes(), "ciatInteraction"));
                                Log.e(": ", "语音讲解回复: " + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("1A_WIFI_SSID").getBytes(), "ciatInteraction"));
                                Log.e(": ", "语音讲解回复: " + EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("PASSWORD").getBytes(), "ciatInteraction"));

                                PrefUtils.setString(getApplicationContext(), "1A_MAC",
                                        EncryptUntils.decode3DesBase64(messages.get(0)
                                                .getStringAttribute("1A_MAC").getBytes(), "ciatInteraction"));

                                PrefUtils.setString(getApplicationContext(), "1A_WIFI_SSID",
                                        EncryptUntils.decode3DesBase64(messages.get(0)
                                                .getStringAttribute("1A_WIFI_SSID").getBytes(), "ciatInteraction"));

                                PrefUtils.setString(getApplicationContext(), "PASSWORD",
                                        EncryptUntils.decode3DesBase64(messages.get(0)
                                                .getStringAttribute("PASSWORD").getBytes(), "ciatInteraction"));


                                String A_WIFI_SSID = PrefUtils.getString(getApplicationContext(), "1A_WIFI_SSID", null);
                                String PASSWORD1 = PrefUtils.getString(getApplicationContext(), "PASSWORD", null);


                                startActivity(new Intent(getApplicationContext(), ScenicSpotsOnActivity.class));


                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }


                            break;


                    }
                }

                @Override
                public void onMessageRead(List<EMMessage> messages) {
                    //收到已读回执
                }

                @Override
                public void onMessageDelivered(List<EMMessage> message) {
                    //收到已送达回执
                }


                @Override
                public void onMessageChanged(EMMessage message, Object change) {
                    //消息状态变动
                }
            };

            EMClient.getInstance().chatManager().addMessageListener(msgListener);
        }


    }

    private ImageView ivQR;
    private Button button;
    private SweetAlertDialog pDialog;

    private void getPeopleName() {


        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1004";//action可以自定义
        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mac.getBytes(),
                "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo("server2c");
        cmdMsg.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {

                Log.e(TAG, "onSuccess: 发送获取用户名信息成功");

            }

            @Override
            public void onError(int code, String error) {


            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });

        EMClient.getInstance().chatManager().sendMessage(cmdMsg);


    }

    private void enterLoginDialog() {
        View loginDialog = View.inflate(HomeActivity.this, R.layout.dialog_login, null);
        ivQR = (ImageView) loginDialog.findViewById(R.id.ivQR);
        createQRImage(mac + "," + ssid + "," + pwd);
        button = (Button) loginDialog.findViewById(R.id.button);
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(loginDialog);
        dialog.show();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                pDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("正在登陆...");
                pDialog.setCancelable(true);
                pDialog.show();

                String userName = mac;
                String passWord = EncryptUntils.encode3DesBase64(userName.getBytes(), "ciatInteraction");
                passWord = passWord.trim();
                Log.e(TAG, "userName=" + userName + ",password=" + passWord);


                EMClient.getInstance().login(userName, passWord, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        getPeopleName();

                    }

                    @Override
                    public void onError(int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();

                            }
                        });

                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });


            }
        });
    }

    private String peopleName;

    private WifiManager wifiManager;

    // wifi热点开关
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

    private int QR_WIDTH = 300;
    private int QR_HEIGHT = 300;

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
            ivQR.setImageBitmap(bitmap);


        } catch (WriterException e) {
            e.printStackTrace();
        }


    }


    private String ssid;
    private String pwd;


    //检测网络，是否可以上网
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //sim卡是否可读
    public boolean isCanUseSim() {
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            return TelephonyManager.SIM_STATE_READY == mgr
                    .getSimState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //在wifi开启状态下，获取设备mac地址
    public String getLocalMacAddress() {
        try {
            NetworkInterface wlan0 = NetworkInterface.getByName("wlan0");
            byte[] hardwareAddress = wlan0.getHardwareAddress();
            String s = "";
            for (int i = 0; i < hardwareAddress.length; i++) {
                if (i == 0) {
                    s = s + String.format("%02X", hardwareAddress[i] ^ 0x02);
                } else {
                    s = s + String.format("%02X", hardwareAddress[i]);
                }
            }
            s = s.trim().toLowerCase();
            PrefUtils.setString(getApplicationContext(), "mac", s);

            return s;

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String mac;

    private String city;

    private void getWeatherInfo() {

        String jinWei = mLocation.getLongitude() + "," + mLocation.getLatitude();
        city = mLocation.getCity();

        OkGo.<String>get("https://free-api.heweather.com/v5/now?" +
                "city=" + jinWei +
                "&key=b35184920b1c4b609a02b58f914216db")
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        String result = response.body().toString();
                        Log.e(TAG, "weather=" + result);
                        Gson gson = new Gson();
                        HeWeather5Bean heWeather5Bean = gson.fromJson(result, HeWeather5Bean.class);
                        HeWeather5Bean.HeWeather5Entity heWeather5Entity = heWeather5Bean.getHeWeather5().get(0);
                        //天气状况描述
                        String txt = heWeather5Entity.getNow().getCond().getTxt();
                        //天气状况代码
                        String code = heWeather5Entity.getNow().getCond().getCode();
                        //温度
                        String tmp = heWeather5Entity.getNow().getTmp();
                        //体感温度
                        String fl = heWeather5Entity.getNow().getFl();

                        TextView tvCity = (TextView) findViewById(R.id.tvCity);
                        tvCity.setText(city);
                        TextView tvWeatherState = (TextView) findViewById(R.id.tvWeatherState);
                        tvWeatherState.setText(txt);
                        TextView tvTemperature = (TextView) findViewById(R.id.tvTemperature);
                        tvTemperature.setText(tmp + "℃");

                        ImageView ivWeatherIcon = (ImageView) findViewById(R.id.ivWeatherIcon);
                        ivWeatherIcon.setImageResource(Constant.WEATHERIMG.get(code));


                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });

    }

    String toUsername = "server2c";


    /**
     * 向服务器发送定位信息
     *
     * @param location
     */
    private void sendLocationMessage(BDLocation location) {

        String lat = mLocation.getLatitude() + "";
        String lng = mLocation.getLongitude() + "";

        PrefUtils.setString(getApplicationContext(), "lat", lat);
        PrefUtils.setString(getApplicationContext(), "lng", lng);


        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1014";//action可以自定义
        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mac.getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("latitude", EncryptUntils.encode3DesBase64(lat.getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("longitude", EncryptUntils.encode3DesBase64(lng.getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("coordinates", EncryptUntils.encode3DesBase64("WGS84".getBytes(), "ciatInteraction"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss     ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        cmdMsg.setAttribute("sendTime", EncryptUntils.encode3DesBase64(str.getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);

    }

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    public BDLocation mLocation;
    private boolean isGetWeather = true;

    public class MyLocationListener implements BDLocationListener {


        public void onConnectHotSpotMessage(String connectWifiMac, int hotSpotState) {


        }

        @Override
        public void onReceiveLocation(BDLocation location) {

            mLocation = location;

            if (isGetWeather) {
                getWeatherInfo();
                isGetWeather = false;
            }

            sendLocationMessage(location);

            //获取定位结果
            StringBuffer sb = new StringBuffer(256);

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息


            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息


            sb.append("\nradius : ");
            sb.append(location.getRadius());    //获取定位精准度

            if (location.getLocType() == BDLocation.TypeGpsLocation) {

                // GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());    // 单位：公里每小时

                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());    //获取卫星数

                sb.append("\nheight : ");
                sb.append(location.getAltitude());    //获取海拔高度信息，单位米

                sb.append("\ndirection : ");
                sb.append(location.getDirection());    //获取方向信息，单位度

                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息


                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                sb.append("\noperationers : ");
                sb.append(location.getOperators());    //获取运营商信息

                sb.append("\ndescribe : ");
                sb.append("网络定位成功");

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");

            } else if (location.getLocType() == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

            }

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());    //位置语义化信息

            List<Poi> list = location.getPoiList();    // POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

        }
    }

    @Override
    public void onBackPressed() {

        recreate();

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("WGS84");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);
    }


    @OnClick({R.id.ivWeatherIcon, R.id.tvCity, R.id.tvDate, R.id.tvTime, R.id.tvWeatherState, R.id.tvTemperature, R.id.mivNavigationPosition, R.id.mivEmergencyTool, R.id.mivNews, R.id.mivInterpretationSpot, R.id.mivAddressBook, R.id.mivForeignAssistant, R.id.mivWifiHotspot, R.id.tvUserName, R.id.tvMsgCount})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivWeatherIcon:
                break;
            case R.id.tvCity:
                break;
            case R.id.tvDate:
                break;
            case R.id.tvTime:
                break;
            case R.id.tvWeatherState:
                break;
            case R.id.tvTemperature:
                break;
            case R.id.mivNavigationPosition:

                Intent intent = getPackageManager()
                        .getLaunchIntentForPackage("com.baidu.BaiduMap");

                if (intent != null) {
                    startActivity(intent);
                } else {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("抱歉")
                            .setContentText("请先安装百度地图！")
                            .setConfirmText("确定")
                            .show();

                }
                intent = getPackageManager()
                        .getLaunchIntentForPackage("com.baidu.BaiduMap");

                if (intent != null) {
                    startActivity(intent);
                } else {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("抱歉")
                            .setContentText("请先安装百度地图！")
                            .setConfirmText("确定")
                            .show();

                }

//                String lat1 = PrefUtils.getString(getApplicationContext(), "lat", "");
//                String lng1 = PrefUtils.getString(getApplicationContext(), "lng", "");
//
//                //修改为com.jinkun.map吧
//                try {
//                    Intent intent = new Intent();
//                    intent.setAction("com.jinkun.guide.location");
//                    intent.putExtra("com.jinkun.location.latitude", Double.valueOf(lat1));
//                    intent.putExtra("com.jinkun.location.longitude", Double.valueOf(lng1));
//
//                    intent.putExtra("com.jinkun.location.coordinator.type", 2);
//                    intent.putExtra("location_edition", 0);
//
//                    startActivity(intent);
//                } catch (Exception e) {
//
//                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
//                            .setTitleText("抱歉")
//                            .setContentText("该功能正在建设中！")
//                            .setConfirmText("确定")
//                            .show();
//
//
//                }

                break;
            case R.id.mivEmergencyTool:

//                startActivity(new Intent(getApplicationContext(), SosActivity.class));
                Uri uri = Uri.parse("tel:008612308");
                Intent it = new Intent("android.intent.action.CALL", uri);
                startActivity(it);

                break;


            case R.id.mivNews:

//                rexsee.smb

                intent = getPackageManager()
                        .getLaunchIntentForPackage("rexsee.smb");

                if (intent != null) {
                    startActivity(intent);
                } else {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("抱歉")
                            .setContentText("请先安装rexsee.smb！")
                            .setConfirmText("确定")
                            .show();

                }

//                startActivity(new Intent(getApplicationContext(), MessageActivity.class));

                break;


            case R.id.mivInterpretationSpot:

                startActivity(new Intent(getApplicationContext(), ScenicSpotsOnActivity.class));
                break;

            case R.id.mivAddressBook:

                startActivity(new Intent(getApplicationContext(), EmergencyContacts.class));
                break;
            case R.id.mivForeignAssistant:

                // com.google.android.apps.translate

                intent = getPackageManager()
                        .getLaunchIntentForPackage("com.google.android.apps.translate");

                if (intent != null) {
                    startActivity(intent);
                } else {

                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("抱歉")
                            .setContentText("请先安装com.google.android.apps.translate！")
                            .setConfirmText("确定")
                            .show();

                }



                break;
            case R.id.mivWifiHotspot:

                startActivity(new Intent(getApplicationContext(),
                        WifiActivity0.class));
                break;

            case R.id.tvUserName:
                break;
            case R.id.tvMsgCount:
                break;
            /*case R.id.tvBeiJing:
                break;
            case R.id.tvBeiJingTime:
                break;
            case R.id.tvBeiJingDate:
                break;*/
        }
    }


}
