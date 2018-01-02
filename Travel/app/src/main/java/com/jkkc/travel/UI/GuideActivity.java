package com.jkkc.travel.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.jkkc.travel.R;
import com.jkkc.travel.sweepcodebindlogin.BindActivity;
import com.jkkc.travel.utils.Constant;
import com.jkkc.travel.utils.PrefUtils;
import com.umeng.analytics.MobclickAgent;


/**
 * Created by Guan on 2017/6/14.
 * 启动页
 */

public class GuideActivity extends Activity {

    private WifiManager wifiManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (Settings.System.canWrite(this)) {
                    //检查返回结果
                    Toast.makeText(getApplicationContext(), "允许修改系统设置已经开启，恭喜，wifi热点可以使用",
                            Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), "允许修改系统设置没有开启，抱歉，wifi热点不能使用，请重新打开应用，并且开启权限",
                            Toast.LENGTH_LONG).show();


                }
            }
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide);

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        //初始化天气图标
        initMap();

        //获取wifi管理服务
        wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        //打开wifi
        wifiManager.setWifiEnabled(true);


        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 3);


            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED

                ) {


            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,

                    }, 1);

            return;


        } else {

            splash();

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

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            startActivity(new Intent(getApplicationContext(), BindActivity.class));
            finish();

        }


    }

    private void splash() {


        boolean hasLogin = PrefUtils.getBoolean(getApplicationContext(), "hasLogin", false);

        if (hasLogin) {

            startActivity(new Intent(getApplicationContext(), HomeActivity2.class));
            finish();

        } else {

            // 闪屏的核心代码
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {


                    Intent intent = new Intent(GuideActivity.this, BindActivity.class); //从启动动画ui跳转到主ui
                    startActivity(intent);
                    finish(); // 结束启动动画界面


                }
            }, 2000); //启动动画持续2秒钟


        }


    }


}

