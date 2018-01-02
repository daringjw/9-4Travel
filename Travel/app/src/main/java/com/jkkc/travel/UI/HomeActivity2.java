package com.jkkc.travel.UI;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.google.gson.Gson;
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
import com.jkkc.travel.db.NewsHelper;
import com.jkkc.travel.listento1a.ScenicSpotsOnActivity;
import com.jkkc.travel.sweepcodebindlogin.BindActivity;
import com.jkkc.travel.systemmanagerment.utils.DataCleanManager;
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

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * Created by Guan on 2017/9/10.
 */

public class HomeActivity2 extends AppCompatActivity {

    private static final String TAG = "HomeActivity2";

    @BindView(R.id.ivSun)
    ImageView ivSun;
    @BindView(R.id.tvCityWeather)
    TextView tvCityWeather;

    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.mivEmergency_contacts)
    MyImageView mivEmergencyContacts;
    @BindView(R.id.mivMessageNotification)
    MyImageView mivMessageNotification;
    @BindView(R.id.img_news)
    TextView mImgNews;
    @BindView(R.id.mivSpotsSpeak)
    MyImageView mivSpotsSpeak;
    @BindView(R.id.mivMine)
    MyImageView mivMine;
    @BindView(R.id.mivMap)
    MyImageView mivMap;
    @BindView(R.id.mivApHot)
    MyImageView mivApHot;
    @BindView(R.id.tvMsgCount)
    TextView tvMsgCount;

    private String mUserName;
    private BDLocation location1;
    private String city;

    private MyConnectionListener myConnectionListener;

    private boolean hasLogin;
    private String mac;
    private String ssid;
    private String pwd;

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {

            //已登陆，保持连接
            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);
            hasLogin = true;
            Log.e(TAG, "连接中=" + hasLogin);
//            tvUserName.setText(mUserName);


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
                        if (NetUtils.hasNetwork(HomeActivity2.this)) {
                            //连接不到聊天服务器
                            //连接断了
                            Log.e(TAG, "未连接=" + hasLogin);
                            hasLogin = false;
                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);

                            startActivity(new Intent(getApplicationContext(), BindActivity.class));
                            finish();
                            tvUserName.setText("登录");

                            Toast.makeText(getApplicationContext(), "连接不到服务器,请重新登陆",
                                    Toast.LENGTH_LONG).show();


                        } else {
                            //当前网络不可用，请检查网络设置

                        }

                    }
                }
            });
        }
    }

    private SweetAlertDialog pDialog;

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

                                mUserName = EncryptUntils.decode3DesBase64(messages.get(0)
                                        .getStringAttribute("peopleName").getBytes(), "ciatInteraction");


                                if (mUserName.equals("未知用户")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(HomeActivity2.this, "未绑定设备",
                                                    Toast.LENGTH_SHORT).show();

                                            EMClient.getInstance().logout(true);
                                            if (pDialog != null) {
                                                pDialog.dismiss();

                                            }


                                            tvUserName.setText("登录");
                                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);
                                            hasLogin = false;

                                            startActivity(new Intent(getApplicationContext(),
                                                    BindActivity.class));


                                        }
                                    });

                                } else {

                                    //更新姓名

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            PrefUtils.setString(getApplicationContext(), "peopleName", mUserName);
                                            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);
                                            hasLogin = true;
                                            Log.e(TAG, "username=" + mUserName);
                                            tvUserName.setText(mUserName);
                                            if (pDialog != null) {
                                                pDialog.dismiss();

                                            }

                                        }
                                    });

                                }


                            } catch (final HyphenateException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(HomeActivity2.this, e.getMessage(),
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

                        case "cmd:\"1032\"":

                            //解绑
                            try {

                                //服务器发送action：1032透传信息 ，2c端收到清空手机数据，跳转扫码登录界面，同时给服务器发送action 1033的透传信息
                                Log.e(": ", "解绑: " + EncryptUntils.decode3DesBase64
                                        (messages.get(0).getStringAttribute("unbind").getBytes(), "ciatInteraction"));

                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1033";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("true".getBytes(), "ciatInteraction"));

                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);


                                String filePath = getApplicationContext()
                                        .getFilesDir().getAbsolutePath();
                                DataCleanManager.cleanApplicationData(getApplicationContext(), filePath);
                                PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);

                                startActivity(new Intent(getApplicationContext(), BindActivity.class));
                                finish();


                            } catch (HyphenateException e) {

                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1033";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("false".getBytes(), "ciatInteraction"));
                                cmdMsg.setAttribute("msg", EncryptUntils.encode3DesBase64(e.toString().getBytes(), "ciatInteraction"));


                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);

                                e.printStackTrace();


                            }

                            break;

                        case "cmd:\"1036\"":

                            //同科预警


                            RxBus.getInstance().post(messages);


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


    String toUsername = "server2c";


    private void viberateUser() {


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

    private WifiManager wifiManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);
        ButterKnife.bind(this);

        //获取wifi管理服务
        wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        //读取缓存中的ap  ssid ,pwd
        ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
        pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);

        //获取mac
        mac = PrefUtils.getString(getApplicationContext(), "mac", null);

        getPeopleName();

        //注册环信连接状态监听
        //注册一个监听连接状态的listener
        if (myConnectionListener == null) {
            myConnectionListener = new MyConnectionListener();
            EMClient.getInstance().addConnectionListener(myConnectionListener);
        }

        // 接收消息监听
        initMessageListener();

        //刷新用户名
        mUserName = PrefUtils.getString(getApplicationContext(), "mUserName", null);
        if (TextUtils.isEmpty(mUserName)) {
            tvUserName.setText(mUserName);
        }

        //获取当前位置
        mLocationClient = new LocationClient(this);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
        mLocationClient.start();

        initRxBus();

        //当有推送消息时，刷新数字
        if (NewsHelper.getDBHelper().queryNotRead() != 0) {
            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);
        }


        //刷新天气,用户名,北京时间
        RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh(RefreshLayout refreshlayout) {

                refreshlayout.finishRefresh(2000);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //重新登陆，经常会掉线
                        login();
                        getPeopleName();
                        getWeatherInfo();

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
        apHandler.postDelayed(apRunnable, 1000 * 60 * 5);

    }


    private void login() {

        String pwd1 = PrefUtils.getString(getApplicationContext(), "pwd1", null);

        EMClient.getInstance().login(mac, pwd1, new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                Log.d("main", "登录聊天服务器成功！");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                Log.d("main", "登录聊天服务器失败！");

                startActivity(new Intent(getApplicationContext(), GuideActivity.class));
            }
        });

    }

    private Handler apHandler = new Handler();

    private Runnable apRunnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                apHandler.postDelayed(this, 1000 * 60 * 5);
                Log.e(TAG, "五分钟自动打开热点次数？");
                setWifiApEnabled(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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
            PrefUtils.setBoolean(getApplicationContext(), "ApOpen", true);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {
            return false;
        }
    }


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

                            case "cmd:\"1036\"":
                                handleMessage36(list);
                                break;


                        }
                    }
                });


    }

    private void handleMessage36(List<EMMessage> messages) {

        //信息推送
        try {

            viberateUser();
            //预警信息
            String warnText = EncryptUntils.decode3DesBase64
                    (messages.get(0).getStringAttribute("txt").getBytes(),
                            "ciatInteraction");
            Log.e(TAG, "同科预警=" + warnText);

            NewsBean newsBean = new NewsBean();
            newsBean.setUuid(UUID.randomUUID().toString());
            newsBean.setIsRead("0");
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            newsBean.setTime(dateString);
            newsBean.setContent(warnText);
            newsBean.setTitle("平台预警");
            newsBean.setCity(city);
            newsBean.setLevel(" 3");
            NewsHelper.getDBHelper().insert(newsBean, this);

            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);


        } catch (HyphenateException e) {
            e.printStackTrace();
            Log.e(": ", "onCmdMessageReceived: " + e.getMessage());
        }

    }

    private void handleMessage(List<EMMessage> messages) {
        //信息推送
        try {

            viberateUser();
            //预警信息
            final String msg = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("msg").getBytes(), "ciatInteraction");
            String title = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("title").getBytes(), "ciatInteraction");
            String loc = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("city").getBytes(), "ciatInteraction");
            final String level = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("level").getBytes(), "ciatInteraction");
            Log.e(TAG, "msg=" + msg);
            Log.e(TAG, "title=" + title);

            Log.e(TAG, "city=" + city);
            Log.e(TAG, "level=" + level);

            NewsBean newsBean = new NewsBean();
            newsBean.setUuid(UUID.randomUUID().toString());
            newsBean.setIsRead("0");
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            newsBean.setTime(dateString);
            newsBean.setContent(loc+"\n"+msg);
            newsBean.setTitle(title);

            newsBean.setCity(city);
            newsBean.setLevel(level);
            NewsHelper.getDBHelper().insert(newsBean, this);

            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);


        } catch (HyphenateException e) {
            e.printStackTrace();
            Log.e(": ", "onCmdMessageReceived: " + e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed");
        return;
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

        mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
        mImgNews.setVisibility(View.VISIBLE);

    }


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

    //天气
    private void getWeatherInfo() {

        String jinWei = location1.getLongitude() + "," + location1.getLatitude();
        city = location1.getCity();

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
                        //city
                        String city2 = heWeather5Entity.getBasic().getCity();
                        //天气状况描述
                        String txt = heWeather5Entity.getNow().getCond().getTxt();
                        //天气状况代码
                        String code = heWeather5Entity.getNow().getCond().getCode();
                        //温度
                        String tmp = heWeather5Entity.getNow().getTmp();
                        //体感温度
                        String fl = heWeather5Entity.getNow().getFl();


                        tvCityWeather.setText(city + city2 + "\n" + txt + "\n" + tmp + "℃");
                        ivSun.setImageResource(Constant.WEATHERIMG.get(code));


                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });

    }

    private Subscription mEmMessageSub;
    private Subscription mLogoutSub;
    private EMMessageListener msgListener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        if (mEmMessageSub != null) {
            mEmMessageSub.unsubscribe();
        }


        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        EMClient.getInstance().logout(true);
    }

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private boolean isWeather = true;


    /**
     * 向服务器发送定位信息
     *
     * @param location
     */
    private void sendLocationMessage(BDLocation location) {

        String lat = location.getLatitude() + "";
        String lng = location.getLongitude() + "";
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

    public class MyLocationListener implements BDLocationListener {


        public void onConnectHotSpotMessage(String connectWifiMac, int hotSpotState) {


        }

        @Override
        public void onReceiveLocation(BDLocation location) {

            location1 = location;

            if (isWeather) {
                getWeatherInfo();
                isWeather = false;
            }

            //给平台发送位置信息
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


    @OnClick({R.id.ivSun, R.id.tvCityWeather, R.id.tvUserName, R.id.tvDate, R.id.mivEmergency_contacts, R.id.mivMessageNotification, R.id.img_news, R.id.mivSpotsSpeak, R.id.mivMine, R.id.mivMap, R.id.mivApHot, R.id.tvMsgCount})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivSun:
                break;
            case R.id.tvCityWeather:
                break;

            case R.id.tvUserName:
                break;
            case R.id.tvDate:
                break;
            case R.id.mivEmergency_contacts:

                startActivity(new Intent(getApplicationContext(), EmergencyContacts.class));

                break;
            case R.id.mivMessageNotification:

                startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                mImgNews.setVisibility(View.GONE);
                break;

            case R.id.img_news:


                break;
            case R.id.mivSpotsSpeak:

                startActivity(new Intent(getApplicationContext(), ScenicSpotsOnActivity.class));
                break;
            case R.id.mivMine:

                startActivity(new Intent(getApplicationContext(), MineActivity.class));
                break;
            case R.id.mivMap:

                //修改为com.jinkun.map吧
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.jinkun.guide.location");
                    intent.putExtra("com.jinkun.location.latitude", Double.valueOf(location1.getLatitude()));
                    intent.putExtra("com.jinkun.location.longitude", Double.valueOf(location1.getLongitude()));

                    intent.putExtra("com.jinkun.location.coordinator.type", 2);
                    intent.putExtra("location_edition", 0);

                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity2.this, "你还没有集成这个功能", Toast.LENGTH_SHORT).show();

                }

                break;

            case R.id.mivApHot:

                startActivity(new Intent(getApplicationContext(), WifiActivity0.class));
                break;

            case R.id.tvMsgCount:

                break;

        }
    }


}
