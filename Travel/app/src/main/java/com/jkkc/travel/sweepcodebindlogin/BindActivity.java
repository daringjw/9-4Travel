package com.jkkc.travel.sweepcodebindlogin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.jkkc.travel.R;
import com.jkkc.travel.UI.GuideActivity;
import com.jkkc.travel.UI.HomeActivity;
import com.jkkc.travel.UI.HomeActivity2;
import com.jkkc.travel.bean.HeWeather5Bean;
import com.jkkc.travel.bean.UpdateInfo;
import com.jkkc.travel.config.Config;
import com.jkkc.travel.http.UpdateAppHttpUtil;
import com.jkkc.travel.systemmanagerment.view.LoadingDialog;
import com.jkkc.travel.utils.Constant;
import com.jkkc.travel.utils.EncryptUntils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.umeng.analytics.MobclickAgent;
import com.vector.update_app.UpdateAppManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BindActivity extends AppCompatActivity {

    @BindView(R.id.tvSSID)
    TextView mTvSSID;
    @BindView(R.id.tvPWD)
    TextView mTvPWD;
    private ImageView mIvQR;

    private WifiManager wifiManager;
    private String mLocalMacAddress;
    private Button mBtnLogin;
    private String mMac;
    private String mPwd1;
    private String mPhoneNum;
    private LoadingDialog mLoadingDialog;


    private String mUserName;
    private String mLocalVersionName;
    private EMMessageListener msgListener;


    /**
     * * 保存文件
     * * @param toSaveString
     * * @param filePath
     */

    public static void saveFile(String toSaveString, String filePath) {
        try {
            File saveFile = new File(filePath);
            if (!saveFile.exists()) {
                File dir = new File(saveFile.getParent());
                dir.mkdirs();
                saveFile.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(saveFile);
            outStream.write(toSaveString.getBytes());
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * 读取文件内容
     *
     * @param filePath
     * @return 文件内容
     */
    public static String readFile(String filePath) {

        String str = "";
        try {
            File readFile = new File(filePath);
            if (!readFile.exists()) {
                return null;
            }
            FileInputStream inStream = new FileInputStream(readFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            str = stream.toString();
            stream.close();
            inStream.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public final static String TAG = "Bind";
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private double mLat;
    private double mLng;
    private String mCity;


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


            PrefUtils.setString(BindActivity.this, "lat", location.getLatitude() + "");
            PrefUtils.setString(BindActivity.this, "lng", location.getLongitude() + "");

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
                mCity = addrStr.substring(shi - 2, shi);

                PrefUtils.setString(getApplicationContext(), "city", mCity);


                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());    //获取地址信息

                String addrStr = location.getAddrStr();
                int shi = addrStr.indexOf("市");
                mCity = addrStr.substring(shi - 2, shi);

                PrefUtils.setString(getApplicationContext(), "city", mCity);


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

    private void getCurrentPosition() {


        mLocationClient = new LocationClient(this);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        initLocation();
        mLocationClient.start();


    }

    private String localVersionName;
    private String netVersionName;
    private String des;
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private long mApkSize;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sweep_code);
        //友盟统计
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        ButterKnife.bind(this);
        //初始化天气图标
        initMap();

        if (isNetworkConnected(this)) {
            updateApk();
        }

        getCurrentPosition();

        //通过注册消息监听来接收消息。
        if (msgListener == null) {
            msgListener = new EMMessageListener() {

                @Override
                public void onMessageReceived(List<EMMessage> messages) {
                    //收到消息
                    Log.e(TAG, "onMessageReceived:=" + messages.get(0).getFrom());
                    Log.e(TAG, "onMessageReceived:=" + messages.get(0).getBody().toString());
                }

                @Override
                public void onCmdMessageReceived(List<EMMessage> messages) {

                    String s = messages.get(0).getBody().toString();
                    Log.e(TAG, "s=" + s);

                    switch (s) {

                        case "cmd:\"1005\"":

                            mLoadingDialog.dismiss();

                            try {
                                mUserName = EncryptUntils.decode3DesBase64
                                        (messages.get(0).getStringAttribute("peopleName").getBytes(),
                                                "ciatInteraction");

                                if (mUserName.equals("未知用户")) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),
                                                    "请先绑定再点击登陆", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {

                                    Log.e(TAG, "用户名=" + mUserName);
                                    PrefUtils.setString(getApplicationContext(), "mUserName", mUserName);
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity2.class);
                                    intent.putExtra("mUserName", mUserName);
                                    startActivity(intent);

                                }


                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }

                            break;

                    }

                }

                @Override
                public void onMessageRead(List<EMMessage> messages) {
                    Log.e(": ", "onMessageRead: " + "收到已读回执");
                    //收到已读回执
                }

                @Override
                public void onMessageDelivered(List<EMMessage> message) {
                    //收到已送达回执
                    Log.e(": ", "onMessageDelivered: " + "收到已送达回执");
                }

                @Override
                public void onMessageChanged(EMMessage message, Object change) {
                    Log.e(": ", "onMessageChanged: " + "消息状态变动");
                    //消息状态变动
                }
            };

            EMClient.getInstance().chatManager().addMessageListener(msgListener);
        }

        mIvQR = (ImageView) findViewById(R.id.ivQR);


        mPhoneNum = getPhoneNum();
        Log.e("guide", "phone==" + mPhoneNum);
        PrefUtils.setString(getApplicationContext(), "phoneNum", mPhoneNum);

        if (isNetworkConnected(this)) {
            getHeWeather();
        }

        invoke();


        //获取wifi管理服务
        wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);


        if (!TextUtils.isEmpty(mMac)) {

            ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
            pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);
            if (TextUtils.isEmpty(ssid)) {
                //二维码信息：mac+ssid+pwd
                //createQRImage(mac + "," + ssid + "," + pwd);
                // mac 后四位
                Log.e(TAG, "mac=" + mMac);
                ssid = "ATB_" + mMac.substring(mMac.length() - 4, mMac.length());

                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < 8; i++) {
                    double num = Math.random() * 10;
                    System.out.print((int) num);
                    sb.append((int) num + "");
                }
                pwd = sb.toString();
                PrefUtils.setString(getApplicationContext(), "ssid", ssid);
                PrefUtils.setString(getApplicationContext(), "pwd", pwd);

            }

            setWifiApEnabled(true);
            mTvSSID.setText("wifi账号=" + ssid);
            mTvPWD.setText("wifi密码=" + pwd);
            PrefUtils.setBoolean(getApplicationContext(), "ApOpen", true);

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

    private void getHeWeather() {

        if (!TextUtils.isEmpty(mCity)) {

            if (isNetworkConnected(this)) {

                //有网络，取网络数据
                HttpUtils http = new HttpUtils();
                http.send(HttpRequest.HttpMethod.GET,
                        "https://free-api.heweather.com/v5/now?city=" + mCity
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

    private void updateApk() {
        try {
            mLocalVersionName = getLocalVersionName();

        } catch (Exception e) {

            e.printStackTrace();

        }


        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                Config.UPDATE_URL,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {

                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        String result = responseInfo.result;

                        Log.d(TAG, "result=" + result);

                        Gson gson = new Gson();
                        UpdateInfo updateInfo = gson.fromJson(result, UpdateInfo.class);
                        String new_version = updateInfo.getNew_version();

                        if (new_version.equals(mLocalVersionName)) {

                            Toast.makeText(getApplicationContext(),
                                    "已经是最新版本了，无需更新", Toast.LENGTH_SHORT).show();

                        } else {

                            new UpdateAppManager
                                    .Builder()
                                    //当前Activity
                                    .setActivity(BindActivity.this)
                                    //更新地址
                                    .setUpdateUrl(Config.UPDATE_URL)
                                    //实现httpManager接口的对象
                                    .setHttpManager(new UpdateAppHttpUtil())
                                    .build()
                                    .update();

                        }


                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
    }


    @Override
    public void onBackPressed() {

        startActivity(new Intent(getApplicationContext(), GuideActivity.class));
        finish();


    }

    private void invoke() {

        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
        mPwd1 = PrefUtils.getString(getApplicationContext(), "pwd1", null);

        if (TextUtils.isEmpty(mMac)) {

            try {

                mLocalMacAddress = getLocalMacAddress();
                mMac = mLocalMacAddress.toLowerCase();
                mMac = mMac.trim();
                PrefUtils.setString(getApplicationContext(), "mac", mMac);

                mPwd1 = EncryptUntils.encode3DesBase64(mMac.getBytes(),
                        "ciatInteraction");
                mPwd1 = mPwd1.trim();
                PrefUtils.setString(getApplicationContext(), "pwd1", mPwd1);

                Log.e(TAG, mMac + ",pwd1==" + mPwd1);

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(),
                        "客官你别急啊，重新打开页面，等待5秒", Toast.LENGTH_SHORT)
                        .show();


            }


        }


        createQRImage(mMac + "," + ssid + "," + pwd);
        Log.e(TAG, "qr=" + mMac + "," + ssid + "," + pwd + "," + mPhoneNum);

        mBtnLogin = (Button) findViewById(R.id.btnLogin);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Login();

            }


        });


    }


    private String getPhoneNum() {
        TelephonyManager mTm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String devicePhone = mTm.getLine1Number();

        if (devicePhone != null) {


        } else {
            devicePhone = "0";
        }

        return devicePhone;
    }


    private void Login() {


        mLoadingDialog = new LoadingDialog(this);
        mLoadingDialog.setMessage("正在登陆，请稍后...");
        mLoadingDialog.show();

        //// TODO: 2017/9/21
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));


        //检查网络
        if (isNetworkConnected(getApplicationContext())) {

//            huanXin();


        } else {

            mLoadingDialog.dismiss();
            Toast.makeText(getApplicationContext(), "没有网络，请更换有流量的手机卡",
                    Toast.LENGTH_SHORT).show();

        }


    }

    private void huanXin() {
        Log.e("login", "account=" + mMac + ",password=" + mPwd1);

        EMClient.getInstance().login(mMac, mPwd1, new EMCallBack() {//回调

            @Override
            public void onSuccess() {


                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                Log.e("login", "登录聊天服务器成功！");

                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                String action = "1004";//action可以自定义
                cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac.getBytes(),
                        "ciatInteraction"));
                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                cmdMsg.addBody(cmdBody);
                cmdMsg.setTo("server2c");
                cmdMsg.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        
                        Log.e(": ", "onSuccess: " + "获取联系人信息发送成功");


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

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {

                Log.e("login", code + "登录聊天服务器失败！" + message);
                Log.e("login", "account=" + mMac + ",password=" + mPwd1);

                mLoadingDialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), "请在后台注册后，再登陆",
                                Toast.LENGTH_SHORT).show();

                    }
                });


            }

        });
    }


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

//            s = s.replace(":", "_");

            Log.e("getMac", s);


            return s;

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;


    }


    private String ssid;
    private String pwd;


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

    /**
     * java生成随机数字和字母组合
     *
     * @param
     * @return
     */
    public static String getCharAndNumr(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 输出字母还是数字
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 字符串
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 取得大写字母还是小写字母
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
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
            mIvQR.setImageBitmap(bitmap);


        } catch (WriterException e) {
            e.printStackTrace();
        }


    }
}
