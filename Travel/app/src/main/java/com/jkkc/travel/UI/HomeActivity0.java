package com.jkkc.travel.UI;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.jkkc.travel.SOS.SosActivity;
import com.jkkc.travel.SOS.SosActivity1;
import com.jkkc.travel.bean.HeWeather5Bean;
import com.jkkc.travel.bean.NewsBean;
import com.jkkc.travel.db.NewsHelper;
import com.jkkc.travel.listento1a.ScenicSpotsOnActivity;
import com.jkkc.travel.sweepcodebindlogin.BindActivity;
import com.jkkc.travel.systemmanagerment.utils.DataCleanManager;
import com.jkkc.travel.util.WifiAutoConnectManager;
import com.jkkc.travel.utils.Constant;
import com.jkkc.travel.utils.EncryptUntils;
import com.jkkc.travel.utils.PrefUtils;
import com.jkkc.travel.utils.RxBus;
import com.jkkc.travel.view.MyImageView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import q.rorbin.badgeview.Badge;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

//import static com.baidu.location.h.j.R;


public class HomeActivity0 extends AppCompatActivity implements View.OnClickListener {


    String mCity1;
    public static final String TAG = "HomeActivity0";
    @BindView(R.id.tvCityWeather)
    TextView mTvCityWeather;
    @BindView(R.id.ivSun)
    ImageView mIvSun;

    private MyImageView mSpots_speak;
    private MyImageView mAp;
    private Intent mIntent;
    private MyImageView mBtnMap;

    private MyImageView mContacts;
    private MyImageView mMessage;


    private String mMac;

    private int mMsgCount = 0;
    private Badge mBadge;
    private MyImageView mMivMine;
    private TextView mTvDate;
    private String mUserName;
    private TextView mTvUserName;
    private Button btnLocateOrNot;
    private String txt;
    private String code;
    private String tmp;
    private ImageView ivRefresh;
    private EMMessageListener msgListener;
    private MyConnectionListener myConnectionListener;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (NewsHelper.getDBHelper().queryNotRead() != 0) {
            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);
        } else {
            mImgNews.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {

            Log.e(TAG, "用户已经登陆");
            PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);


        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        Log.e(": ", "run: " + "显示帐号已经被移除");
                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                        Log.e(": ", "run: " + "显示帐号在其他设备登录");
                    } else {
                        if (NetUtils.hasNetwork(HomeActivity0.this)) {
                            //连接不到聊天服务器
                            Log.e(": ", "run: " + "连接不到聊天服务器");
                        } else {
                            //当前网络不可用，请检查网络设置
                            Log.e(": ", "run: " + "当前网络不可用，请检查网络设置");
                        }
                    }


                    PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);


                }
            });
        }
    }


    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        isLongPressKey = true;

        return super.onKeyLongPress(keyCode, event);

    }

    private boolean isLongPressKey;//是否长按

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // TODO Auto-generated method stub
        Log.d(TAG, "---->>onKeyDown():keyCode=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU://需要识别长按事件
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getRepeatCount() == 0) {//识别长按短按的代码
                    event.startTracking();
                    isLongPressKey = false;
                } else {
                    isLongPressKey = true;
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getRepeatCount() == 0) {//识别长按短按的代码
                    event.startTracking();
                    isLongPressKey = false;
                } else {
                    isLongPressKey = true;
                    startActivity(new Intent(getApplicationContext(),
                            SosActivity.class));

                }
                return true;


            case KeyEvent.KEYCODE_DPAD_UP:

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:

                if (event.getRepeatCount() == 0) {//识别长按短按的代码
                    event.startTracking();
                    isLongPressKey = false;
                } else {

                    isLongPressKey = true;
                    startActivity(new Intent(getApplicationContext(),
                            SosActivity1.class));

                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:

                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "---->> onKeyDown():keyCode=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (isLongPressKey) {
                    isLongPressKey = false;
                    return true;
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_DPAD_UP:

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_DPAD_DOWN:

                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private WifiManager wifiManager;
    String pwd, ssid;


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

    private WifiAutoConnectManager mWifiAutoConnectManager;

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);
        ButterKnife.bind(this);

        PrefUtils.setBoolean(getApplicationContext(), "hasLogin", true);

        getCurrentPosition();

        getHeWeather();

        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
        sendRequestUserName();

        initMap();

        ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
        pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);

        initRxBus();

        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());


        mSpots_speak = (MyImageView) findViewById(R.id.mivSpotsSpeak);
        mSpots_speak.setOnClickListener(this);

        mAp = (MyImageView) findViewById(R.id.mivApHot);
        mAp.setOnClickListener(this);

        mBtnMap = (MyImageView) findViewById(R.id.mivMap);
        mBtnMap.setOnClickListener(this);

        mContacts = (MyImageView) findViewById(R.id.mivEmergency_contacts);
        mContacts.setOnClickListener(this);

        mMessage = (MyImageView) findViewById(R.id.mivMessageNotification);
        mMessage.setOnClickListener(this);


//        mBadge = new QBadgeView(this).bindTarget(mMessage).setBadgeNumber(mMsgCount);
//        mBadge.setGravityOffset(45, 10, true);

      /*  MyImageView targetView = (MyImageView) mBadge.getTargetView();
        targetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                mMsgCount = 0;
                mBadge.setBadgeNumber(mMsgCount);

            }
        });*/


        mMivMine = (MyImageView) findViewById(R.id.mivMine);
        mMivMine.setOnClickListener(this);


        initMessageListener();


        mTvDate = (TextView) findViewById(R.id.tvDate);

        mTvDate.setText(setTime() + "," + getWeekOfDate());


        mTvUserName = (TextView) findViewById(R.id.tvUserName);


        mCity1 = PrefUtils.getString(getApplicationContext(), "city", null);
        txt = PrefUtils.getString(getApplicationContext(), "txt", null);
        code = PrefUtils.getString(getApplicationContext(), "code", null);
        tmp = PrefUtils.getString(getApplicationContext(), "tmp", null);

        if (!TextUtils.isEmpty(code)) {

            mTvCityWeather.setText(mCity1 + "\n" + txt + "\n" + tmp + "℃");
            mIvSun.setImageResource(Constant.WEATHERIMG.get(code));

        }


        if (!TextUtils.isEmpty(mCity1)) {

            if (isNetworkConnected(this)) {

                //有网络，取网络数据
                HttpUtils http = new HttpUtils();
                http.send(HttpRequest.HttpMethod.GET,
                        "https://free-api.heweather.com/v5/now?city=" + mCity1
                                + "&key=" + "b35184920b1c4b609a02b58f914216db",

                        new RequestCallBack<String>() {
                            @Override
                            public void onLoading(long total, long current, boolean isUploading) {
//                            testTextView.setText(current + "/" + total);
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
//                            textView.setText(responseInfo.result);

                                Log.i(TAG, "weather==" + responseInfo.result);
                                String jsonData = responseInfo.result;

                                Gson gson = new Gson();
                                HeWeather5Bean heWeather5Bean = gson.fromJson(jsonData, HeWeather5Bean.class);
                                HeWeather5Bean.HeWeather5Entity heWeather5Entity = heWeather5Bean.getHeWeather5().get(0);
                                //天气状况描述
                                txt = heWeather5Entity.getNow().getCond().getTxt();
                                //天气状况代码
                                code = heWeather5Entity.getNow().getCond().getCode();
                                //温度
                                tmp = heWeather5Entity.getNow().getTmp();

                                mTvCityWeather.setText(mCity1 + "\n" + txt + "\n" + tmp + "℃");
                                mIvSun.setImageResource(Constant.WEATHERIMG.get(code));

                                Log.e(TAG, "++++++++++++++++++++++++++++天气="
                                        + mCity1 + txt + tmp);

                                PrefUtils.setString(getApplicationContext(), "txt", txt);
                                PrefUtils.setString(getApplicationContext(), "code", code);
                                PrefUtils.setString(getApplicationContext(), "tmp", tmp);


                            }

                            @Override
                            public void onStart() {


                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {


                            }
                        });

            } else {

                //没有网络，取缓存数据
                txt = PrefUtils.getString(getApplicationContext(), "txt", null);
                code = PrefUtils.getString(getApplicationContext(), "code", null);
                tmp = PrefUtils.getString(getApplicationContext(), "tmp", null);

                mTvCityWeather.setText(mCity1 + "\n" + txt + "\n" + tmp + "℃");

                if (!TextUtils.isEmpty(code)) {
                    mIvSun.setImageResource(Constant.WEATHERIMG.get(code));
                }


            }


        }

        mTvCityWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "开始刷新界面", Toast.LENGTH_SHORT).show();
                HomeActivity0.this.recreate();
            }
        });

        mIvSun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        "开始刷新界面", Toast.LENGTH_SHORT).show();
                HomeActivity0.this.recreate();

            }
        });


        String lat = PrefUtils.getString(getApplicationContext(), "lat", null);
        String lng = PrefUtils.getString(getApplicationContext(), "lng", null);

        Log.e(TAG, mMac + ";" + lat + ";" + lng);


        if (!TextUtils.isEmpty(lat)) {
            sendLoc(lat, lng);
        }

        mUserName = PrefUtils.getString(getApplicationContext(), "mUserName", null);
        if (!TextUtils.isEmpty(mUserName)) {
            mTvUserName.setText(this.mUserName);
        }

        Intent intent = getIntent();
        mUserName = intent.getStringExtra("mUserName");
        if (!TextUtils.isEmpty(mUserName)) {
            mTvUserName.setText(this.mUserName);
        }


        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //关闭wifi,开启热点
        setWifiApEnabled(true);


        //当有推送消息时，刷新数字
        if (NewsHelper.getDBHelper().queryNotRead() != 0) {
            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);
        }

        //5分钟内无人连接将关闭热点，五分钟后将自动开启热点
        apHandler.postDelayed(apRunnable, 1000 * 60 * 5);

    }


    @BindView(R.id.img_news)
    TextView mImgNews;

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

    private void getHeWeather() {

        if (!TextUtils.isEmpty(mCity1)) {

            if (isNetworkConnected(this)) {

                //有网络，取网络数据
                HttpUtils http = new HttpUtils();
                http.send(HttpRequest.HttpMethod.GET,
                        "https://free-api.heweather.com/v5/now?city=" + mCity1
                                + "&key=" + "b35184920b1c4b609a02b58f914216db",

                        new RequestCallBack<String>() {
                            @Override
                            public void onLoading(long total, long current, boolean isUploading) {
//                            testTextView.setText(current + "/" + total);
                            }

                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
//                            textView.setText(responseInfo.result);

                                Log.i(TAG, "weather==" + responseInfo.result);
                                String jsonData = responseInfo.result;

                                Gson gson = new Gson();
                                HeWeather5Bean heWeather5Bean = gson.fromJson(jsonData, HeWeather5Bean.class);
                                HeWeather5Bean.HeWeather5Entity heWeather5Entity = heWeather5Bean.getHeWeather5().get(0);
                                //天气状况描述
                                String txt = heWeather5Entity.getNow().getCond().getTxt();
                                //天气状况代码
                                String code = heWeather5Entity.getNow().getCond().getCode();
                                //温度
                                String tmp = heWeather5Entity.getNow().getTmp();


                                PrefUtils.setString(getApplicationContext(), "txt", txt);
                                PrefUtils.setString(getApplicationContext(), "code", code);
                                PrefUtils.setString(getApplicationContext(), "tmp", tmp);

                                mTvCityWeather.setText(mCity1 + "\n" + txt + "\n" + tmp + "℃");

                                mIvSun.setImageResource(Constant.WEATHERIMG.get(code));


                            }

                            @Override
                            public void onStart() {


                            }

                            @Override
                            public void onFailure(HttpException error, String msg) {


                            }
                        });

            } else {

                //没有网络，取缓存数据
                String txt = PrefUtils.getString(getApplicationContext(), "txt", null);
                String code = PrefUtils.getString(getApplicationContext(), "code", null);
                String tmp = PrefUtils.getString(getApplicationContext(), "tmp", null);


            }


        }

    }

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
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

    public class MyLocationListener implements BDLocationListener {


        public void onConnectHotSpotMessage(String connectWifiMac, int hotSpotState) {


        }

        @Override
        public void onReceiveLocation(BDLocation location) {


            //获取定位结果
            StringBuffer sb = new StringBuffer(256);

            sb.append("time : ");
            sb.append(location.getTime());    //获取定位时间

            sb.append("\nerror code : ");
            sb.append(location.getLocType());    //获取类型类型

            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息

//            Log.e("Latitude", "Latitude==" + location.getLatitude()
//                    + "--Longitude==" + location.getLongitude());


            PrefUtils.setString(getApplicationContext(), "lat", location.getLatitude() + "");
            PrefUtils.setString(getApplicationContext(), "lng", location.getLongitude() + "");

            mLat = location.getLatitude();
            mLng = location.getLongitude();


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

                String addrStr = location.getAddrStr();
                int shi = addrStr.indexOf("市");
                mCity1 = addrStr.substring(shi - 2, shi);

                PrefUtils.setString(getApplicationContext(), "city", mCity1);

                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                String addrStr = location.getAddrStr();
                int shi = addrStr.indexOf("市");
                mCity1 = addrStr.substring(shi - 2, shi);

                PrefUtils.setString(getApplicationContext(), "city", mCity1);


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

            //Log.i("BaiduLocationApiDem", sb.toString());
        }
    }


    private void getCurrentPosition() {


        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
        mLocationClient.start();


    }

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

    String mMac1;


    private void sendRequestUserName() {


        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1004";//action可以自定义
        String regex = "(.{2})";
        mMac1 = mMac.replaceAll(regex, "$1 ");
        mMac1 = mMac1.toUpperCase();
        mMac1.trim();
        Log.e("account==", "account==" + mMac1);

        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac1.getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo("server2c");
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);

        cmdMsg.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {

                Log.e(TAG, "onSuccess: " + "获取联系人成功");

            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {


            }
        });
    }

    private void sendLoc(String lat, String lng) {

        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1014";//action可以自定义
        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac.getBytes(), "ciatInteraction"));
        Log.e("login", "onClick: " + EncryptUntils.decode3DesBase64(mMac.getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("latitude", EncryptUntils.encode3DesBase64(lat.getBytes(), "ciatInteraction"));
        Log.e("login", "lat=" + lat);
        cmdMsg.setAttribute("longitude", EncryptUntils.encode3DesBase64(lng.getBytes(), "ciatInteraction"));
        Log.e("login", "lng=" + lng);
        cmdMsg.setAttribute("coordinates", EncryptUntils.encode3DesBase64("WGS84".getBytes(), "ciatInteraction"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss     ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        Log.e("sos", str);

        cmdMsg.setAttribute("sendTime", EncryptUntils.encode3DesBase64(str.getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);

    }


    //当前时间的显示
    public String setTime() {

        long longtime = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(longtime);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int Month = mMonth + 1;
        return new String(mYear + "-" + Month + "-" + mDay);


    }

    //当前时间是星期几
    public static String getWeekOfDate() {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        Date curDate = new Date(System.currentTimeMillis());
        cal.setTime(curDate);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    private void initRxBus() {
        RxBus.getInstance().toObserverable(List.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List>() {
                    @Override
                    public void call(List messageList) {
                        switch (((EMMessage) messageList.get(0)).getBody().toString()) {
                            case "cmd:\"1018\"":
                                handleMessage(messageList);
                                break;

                            case "cmd:\"1013\"":

                                handleMessage1013(messageList);

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
        newsBean.setCity(mCity1);
        newsBean.setLevel(" 4");
        NewsHelper.getDBHelper().insert(newsBean, this);


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
            String city = EncryptUntils.decode3DesBase64(messages.get(0)
                    .getStringAttribute("city").getBytes(), "ciatInteraction");
            final String level = EncryptUntils.decode3DesBase64(messages.get(0)
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
            NewsHelper.getDBHelper().insert(newsBean, this);

            mImgNews.setText(String.valueOf(NewsHelper.getDBHelper().queryNotRead()));
            mImgNews.setVisibility(View.VISIBLE);

            if (level.equals("5")) {
                showPopupWindow(msg, city);
            }
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

    private void showPopupWindow(String msg, String city) {

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.popuwindow_layout, null);
        TextView tvMsg = (TextView) view.findViewById(R.id.tv_popu_msg);
        tvMsg.setText(msg);
        TextView tvCity = (TextView) view.findViewById(R.id.tv_popu_city);
        tvCity.setText(city);
        Button button = (Button) view.findViewById(R.id.btn_cancel);

        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(false);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(": ", "onClick: ");
                popupWindow.dismiss();
            }
        });
    }


    String toUsername = "server2c";

    String A_WIFI_SSID;
    String PASSWORD1;

    /**
     * 初始化环信监听事件
     */
    private void initMessageListener() {

        if (msgListener == null) {

            msgListener = new EMMessageListener() {

                @Override
                public void onMessageReceived(List<EMMessage> messages) {

                    //收到消息
                    Log.e(TAG, "文本消息=" + messages.get(0).getBody().toString());


                }

                @Override
                public void onCmdMessageReceived(List<EMMessage> messages) {
                    //收到透传消息.
                    String cmd = messages.get(0).getBody().toString();

                    Log.e(TAG, "cmd=" + cmd);

                    switch (cmd) {

                        case "cmd:\"1005\"":

                            try {

                                mUserName = EncryptUntils.decode3DesBase64
                                        (messages.get(0).getStringAttribute("peopleName").getBytes(),
                                                "ciatInteraction");
                                Log.e(TAG, "用户名=" + mUserName);

                                PrefUtils.setString(getApplicationContext(), "mUserName", mUserName);

                            } catch (HyphenateException e) {
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


                                anwserTheVoice();

                                A_WIFI_SSID = PrefUtils.getString(getApplicationContext(), "1A_WIFI_SSID", null);
                                PASSWORD1 = PrefUtils.getString(getApplicationContext(), "PASSWORD", null);


                                startActivity(new Intent(getApplicationContext(), ScenicSpotsOnActivity.class));


                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }


                            break;
                        case "cmd:\"1026\"":
                            //服务器配置上传周期
                            try {
                                Log.e(": ", "服务器配置上传周期: " + EncryptUntils.decode3DesBase64
                                        (messages.get(0).getStringAttribute("round").getBytes(), "ciatInteraction"));

                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1027";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("true".getBytes(), "ciatInteraction"));
                                cmdMsg.setAttribute("msg", EncryptUntils.encode3DesBase64("".getBytes(), "ciatInteraction"));
                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);
                                Log.e(": ", "onClick: " + "上报频率回复");


                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "cmd:\"1028\"":
                            //网络配置应急电话
                            try {
                                Log.e(": ", "网络配置应急电话: " + EncryptUntils.decode3DesBase64(messages.get(0).
                                        getStringAttribute("phoneList").getBytes(), "ciatInteraction"));

                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1029";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("true".getBytes(), "ciatInteraction"));
                                cmdMsg.setAttribute("msg", EncryptUntils.encode3DesBase64("".getBytes(), "ciatInteraction"));

                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);
                                Log.e(": ", "onClick: " + "网络应急电话回复");


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
                            try {

                            /*String s = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("warnText").getBytes(),
                                            "ciatInteraction");*/

                                String warnText = messages.get(0).getStringAttribute("warnText");
                                Log.e(TAG, "同科预警=" + warnText);


                                //国安预警ack
                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1037";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("true".getBytes(), "ciatInteraction"));

                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);


                            } catch (HyphenateException e) {

                                e.printStackTrace();

                                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                                String action = "1037";//action可以自定义
                                cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("false".getBytes(), "ciatInteraction"));
                                cmdMsg.setAttribute("msg", EncryptUntils.encode3DesBase64(e.toString().getBytes(), "ciatInteraction"));


                                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                                cmdMsg.addBody(cmdBody);
                                cmdMsg.setTo(toUsername);
                                EMClient.getInstance().chatManager().sendMessage(cmdMsg);


                            }


                            break;


                        case "cmd:\"1018\"":
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


        if (myConnectionListener == null) {
            myConnectionListener = new MyConnectionListener();
            //注册一个监听连接状态的listener
            EMClient.getInstance().addConnectionListener(myConnectionListener);
        }


    }

    //回复语音讲解
    private void anwserTheVoice() {

        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1025";//action可以自定义

        cmdMsg.setAttribute("result", EncryptUntils.encode3DesBase64("true".getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("msg", EncryptUntils.encode3DesBase64("".getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);
        Log.e(TAG, "1025" + "语音讲解回复");


    }

    private void viberateUser() {

        HomeActivity0.this.recreate();


        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat
                        .Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.sos_marker)
                        .setContentTitle("国安预警")
                        .setContentText("预警消息")
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

    }

    private double mLat;
    private double mLng;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.mivApHot:

                mIntent = new Intent(getApplicationContext(), WifiActivity0.class);
                startActivity(mIntent);


                break;

            case R.id.mivSpotsSpeak:

                startActivity(new Intent(getApplicationContext(),
                        ScenicSpotsOnActivity.class));
                break;

            case R.id.mivMap:


                String lat1 = PrefUtils.getString(getApplicationContext(), "lat", "");
                String lng1 = PrefUtils.getString(getApplicationContext(), "lng", "");

                //修改为com.jinkun.map吧
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.jinkun.guide.location");
                    intent.putExtra("com.jinkun.location.latitude", Double.valueOf(lat1));
                    intent.putExtra("com.jinkun.location.longitude", Double.valueOf(lng1));

                    intent.putExtra("com.jinkun.location.coordinator.type", 2);
                    intent.putExtra("location_edition", 0);

                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity0.this, "你还没有集成这个功能", Toast.LENGTH_SHORT).show();

                }


                break;


            case R.id.mivMessageNotification:

//                startActivity(new Intent(getApplicationContext(), JieKouActivity.class));
                startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                mImgNews.setVisibility(View.GONE);


                break;


            case R.id.mivEmergency_contacts:

                startActivity(new Intent(getApplicationContext(), EmergencyContacts.class));


                break;

            case R.id.mivMine:

                startActivity(new Intent(getApplicationContext(), MineActivity.class));

                break;


        }


    }


}




