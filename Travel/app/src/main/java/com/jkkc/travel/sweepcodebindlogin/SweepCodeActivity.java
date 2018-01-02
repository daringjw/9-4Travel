package com.jkkc.travel.sweepcodebindlogin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
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
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.NetUtils;
import com.jkkc.travel.R;
import com.jkkc.travel.UI.HomeActivity0;
import com.jkkc.travel.bean.VersionBean;
import com.jkkc.travel.systemmanagerment.view.LoadingDialog;
import com.jkkc.travel.utils.EncryptUntils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

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


public class SweepCodeActivity extends AppCompatActivity {

    private ImageView mIvQR;
    private TextView mTvDeviceId;
    private TextView mTvSSID;
    private TextView mTvPWD;
    private int REQUEST_CODE = 1001;

    private WifiManager wifiManager;
    private String mDeviceId;
    private String mLocalMacAddress;
    private Button mBtnLogin;
    private boolean mHasLogin;
    private String mMac;
    private String mPwd1;
    private String mPhoneNum;
    private LoadingDialog mLoadingDialog;


    private String mUserName;


    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
            Log.e(": ", "run: " + "账号已经登陆");
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
                        if (NetUtils.hasNetwork(SweepCodeActivity.this)) {
                            //连接不到聊天服务器
                            Log.e(": ", "run: " + "连接不到聊天服务器");
                        } else {
                            //当前网络不可用，请检查网络设置
                            Log.e(": ", "run: " + "当前网络不可用，请检查网络设置");
                        }
                    }
                }
            });
        }
    }


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


    public final static String TAG = "SweepCodeActivity";
    private WifiAutoConnectManager mWifiAutoConnectManager;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private double mLat;
    private double mLng;

    private String mCity;
    private String mCity1;

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


            PrefUtils.setString(SweepCodeActivity.this, "lat", location.getLatitude() + "");
            PrefUtils.setString(SweepCodeActivity.this, "lng", location.getLongitude() + "");

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

            // Log.i("BaiduLocationApiDem", sb.toString());
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

    private void checkVersion() {
        // http://192.168.1.103:8080/
        //http://git.oschina.net/darin1/tourist/raw/master/version.json


        try {

            localVersionName = getLocalVersionName();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        http://192.168.1.103:8080/

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
//                "http://" + Config.HOSTNAME + ":8080/version.json",
                "http://git.oschina.net/darin1/banbengengxin/raw/master/version.json",

                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
//                        testTextView.setText(current + "/" + total);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
//                        textView.setText(responseInfo.result);

                        String result = responseInfo.result;
                        Gson gson = new Gson();
                        VersionBean versionBean = gson.fromJson(result, VersionBean.class);
                        netVersionName = versionBean.versionName;
                        des = versionBean.des;
                        mApkSize = versionBean.apkSize;

                        Log.e(TAG, "mApkSize=" + mApkSize);

                        if (localVersionName.equals(netVersionName)) {

                            Toast.makeText(getApplicationContext(), "这是最新版本" + localVersionName + ",无需更新",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            //提示用户是否更新apk
                            showUpdateDialog();

                        }


                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                        Toast.makeText(getApplicationContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();


                    }
                });
    }


    /**
     * 弹出对话框,提示用户更新
     */
    protected void showUpdateDialog() {
        //对话框,是依赖于activity存在的
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置左上角图标
        builder.setIcon(R.mipmap.travel);
        builder.setTitle("版本更新");
        //设置描述内容
        builder.setMessage("当前版本是:" + localVersionName + des);

        //积极按钮,立即更新
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载apk,apk链接地址,downloadUrl
                downloadLatestApk();

            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消对话框,进入主界面
//                enterHome();
//                dialog.dismiss();
            }
        });

        //点击取消事件监听
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //即使用户点击取消,也需要让其进入应用程序主界面
//                enterHome();
                dialog.dismiss();
            }
        });

        builder.show();
    }


    private String path;

    //https://git.oschina.net/darin1/banbengengxin/raw/master/version.json
    String mDownloadUrl = "https://git.oschina.net/darin1/banbengengxin/raw/master/com.jkkc.travel.apk";

    private void downloadLatestApk() {


        //apk下载链接地址,放置apk的所在路径

        //1,判断sd卡是否可用,是否挂在上
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //2,获取sd路径
            path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "com.jkkc.travel.apk";

//            Log.e(TAG, "path==" + path);

            //3,发送请求,获取apk,并且放置到指定路径
            HttpUtils httpUtils = new HttpUtils();
            //4,发送请求,传递参数(下载地址,下载应用放置位置)
            httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //下载成功(下载过后的放置在sd卡中apk)
                    Log.i("lau", "下载成功");
                    File file = responseInfo.result;
                    //提示用户安装
                    installApkFile(getApplicationContext(), path);

                }

                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    Log.i("lau", "下载失败");
                    //下载失败
                }

                //刚刚开始下载方法
                @Override
                public void onStart() {
                    Log.i("lau", "刚刚开始下载");
                    super.onStart();
                }

                //下载过程中的方法(下载apk总大小,当前的下载位置,是否正在下载)
                @Override
                public void onLoading(long total, long current, boolean isUploading) {


                    mBuilder = new AlertDialog.Builder(SweepCodeActivity.this);
                    mBuilder.setTitle("请确保网络连接正常,下载完毕后直接安装即可,新版本下载中...");


                    //百分比
                    String result = String.valueOf(current * 100 / mApkSize);

                    //设置描述内容
                    mBuilder.setMessage("当前已经下载  " + result + "%");

                    mDialog = mBuilder.create();

                    mDialog.show();


                    super.onLoading(total, current, isUploading);
                }
            });

        }


    }


    public static void installApkFile(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "com.jkkc.travel.fileprovider", new File(filePath));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");

        } else {
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);

    }


    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sweep_code);

        checkVersion();


        getCurrentPosition();

        mWifiAutoConnectManager = new WifiAutoConnectManager(this);
        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //关闭热点
        setWifiApEnabled(false);
        //打开wifi
        mWifiAutoConnectManager.openWifi();


        EMMessageListener msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                Log.e(TAG, "onMessageReceived: " + messages.get(0).getFrom());
                Log.e(": ", "onMessageReceived: " + messages.get(0).getBody().toString());
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                Log.e(": ", "onCmdMessageReceived: " + "收到信息");

                String s = messages.get(0).getBody().toString();
                Log.e(": ", "onCmdMessageReceived: " + s);
                switch (s) {
                    case "cmd:\"1009\"":
                        try {
                            Log.e(": ", "onCmdMessageReceived: ");
                            Log.e(": ", "onCmdMessageReceived: " + messages.get(0).getBooleanAttribute("result"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "cmd:\"1013\"":
                        //报警返回
                        try {
                            Log.e(": ", "报警返回: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("msg").getBytes(), "ciatInteraction"));
                            Log.e(": ", "报警返回: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("result").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "cmd:\"1024\"":
                        //下发语音讲解
                        try {
                            Log.e(": ", "下发语音讲解: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("1A_MAC").getBytes(), "ciatInteraction"));
                            Log.e(": ", "下发语音讲解: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("1A_WIFI_SSID").getBytes(), "ciatInteraction"));
                            Log.e(": ", "下发语音讲解: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("PASSWORD").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "cmd:\"1026\"":
                        //服务器配置上传周期
                        try {
                            Log.e(": ", "服务器配置上传周期: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("round").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "cmd:\"1028\"":
                        //网络配置应急电话
                        try {
                            Log.e(": ", "服务器配置上传周期: " + EncryptUntils.decode3DesBase64(messages.get(0).getStringAttribute("phoneList").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "cmd:\"1005\"":
                        //网络配置应急电话
                        try {


                            Log.e(": ", "获取用户名: " +
                                    EncryptUntils.decode3DesBase64
                                            (messages.get(0).getStringAttribute("peopleName").getBytes(),
                                                    "ciatInteraction"));

                            mUserName = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("peopleName").getBytes(),
                                            "ciatInteraction");

                            PrefUtils.setString(getApplicationContext(), "mUserName", mUserName);

                            mLoadingDialog.dismiss();

//                            sweepCodeLogin();

                            startActivity(new Intent(getApplicationContext(), HomeActivity0.class));
                            finish();


                        } catch (HyphenateException e) {

                            e.printStackTrace();
                        }
                        break;

                }
                //收到透传消息
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
        EMClient.getInstance().addConnectionListener(new EMConnectionListener() {
            @Override
            public void onConnected() {
                Log.e(": ", "run: " + "账号已经登陆");
            }

            @Override
            public void onDisconnected(final int errorCode) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (errorCode == EMError.USER_REMOVED) {
                            // 显示帐号已经被移除
                            Log.e(": ", "run: " + "显示帐号已经被移除");
                        } else if (errorCode == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                            // 显示帐号在其他设备登录
                            Log.e(": ", "run: " + "显示帐号在其他设备登录");
                        } else {
                            if (NetUtils.hasNetwork(SweepCodeActivity.this)) {
                                //连接不到聊天服务器
                                Log.e(": ", "run: " + "连接不到聊天服务器");
                            } else {
                                //当前网络不可用，请检查网络设置
                                Log.e(": ", "run: " + "当前网络不可用，请检查网络设置");
                            }
                        }
                    }
                });
            }
        });

        mIvQR = (ImageView) findViewById(R.id.ivQR);


        mPhoneNum = getPhoneNum();

        Log.e("guide", "phone==" + mPhoneNum);

        PrefUtils.setString(getApplicationContext(), "phoneNum", mPhoneNum);

        invoke();


    }


    private void invoke() {

        mMac = readFile("/sdcard/" + "mMac.txt");
        mPwd1 = readFile("/sdcard/" + "mPwd1.txt");

        PrefUtils.setString(getApplicationContext(), "mac", mMac);
        PrefUtils.setString(getApplicationContext(), "pwd1", mPwd1);

//        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
//        mPwd1 = PrefUtils.getString(getApplicationContext(), "pwd1", null);


        if (TextUtils.isEmpty(mMac)) {

            try {

                mWifiAutoConnectManager.openWifi();
                mLocalMacAddress = getLocalMacAddress();
                mMac = mLocalMacAddress.toLowerCase();
                mMac = mMac.trim();
                saveFile(mMac, "/sdcard/" + "mMac.txt");

                PrefUtils.setString(getApplicationContext(), "mac", mMac);
                mPwd1 = EncryptUntils.encode3DesBase64(mMac.getBytes(),
                        "ciatInteraction");
                mPwd1 = mPwd1.trim();

                saveFile(mPwd1, "/sdcard/" + "mPwd1.txt");

                PrefUtils.setString(getApplicationContext(), "pwd1", mPwd1);
                Log.e(TAG, mMac + ",pwd1==" + mPwd1);

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(),
                        "客官你别急啊，重新打开页面，等待5秒", Toast.LENGTH_SHORT)
                        .show();


            }


        }

        ssid = PrefUtils.getString(getApplicationContext(), "ssid", null);
        pwd = PrefUtils.getString(getApplicationContext(), "pwd", null);

        createQRImage(mMac + "," + ssid + "," + pwd + "," + mPhoneNum);
        Log.e(TAG, mMac + "," + ssid + "," + pwd + "," + mPhoneNum);

        mBtnLogin = (Button) findViewById(R.id.btnLogin);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Login();

            }


        });


    }

    @SuppressLint("WifiManagerLeak")
    private void sweepCodeLogin() {
        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mIvQR = (ImageView) findViewById(R.id.ivQR);

        mTvSSID = (TextView) findViewById(R.id.tvSSID);


        setWifiApEnabled(true);


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

    private String mMac1;

    private void Login() {


        mLoadingDialog = new LoadingDialog(this);
        mLoadingDialog.setMessage("正在登陆，请稍后...");
        mLoadingDialog.show();

        //检查网络
        if (isNetworkConnected(getApplicationContext())) {

            huanXin();

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


                String regex = "(.{2})";
                mMac1 = mMac.replaceAll(regex, "$1 ");
                mMac1 = mMac1.toUpperCase();
                mMac1.trim();
                Log.e("account==", "account==" + mMac1);

                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                String action = "1004";//action可以自定义
                cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac1.getBytes(), "ciatInteraction"));
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
                s = s + String.format("%02X", hardwareAddress[i]);
            }

//            s = s.replace(":", "_");

            Log.e("getMac", s);


            return s;

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;


    }

    public static String getDeviceId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }

    private String ssid;
    private String pwd;


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

            boolean isSetAp = PrefUtils.getBoolean(getApplicationContext(), "isSetAp", false);

            if (!isSetAp) {
                //未设置密码
                //配置热点的名称(可以在名字后面加点随机数什么的)
                ssid = getCharAndNumr(8);
                apConfig.SSID = ssid;
                //配置热点的密码
                pwd = getCharAndNumr(8);
                apConfig.preSharedKey = pwd;
                //标记已经设置密码
                PrefUtils.setBoolean(getApplicationContext(), "isSetAp", true);
                PrefUtils.setString(getApplicationContext(), "ssid", ssid);
                PrefUtils.setString(getApplicationContext(), "pwd", pwd);

            } else {

                //已经设置密码
                //以缓存的密码进行设置
                ssid = PrefUtils.getString(getApplicationContext(), "ssid", "");
                apConfig.SSID = ssid;
                pwd = PrefUtils.getString(getApplicationContext(), "pwd", "");
                apConfig.preSharedKey = pwd;

            }


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
