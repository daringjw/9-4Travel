package com.jkkc.travel.SOS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.jkkc.travel.R;
import com.jkkc.travel.utils.EncryptUntils;
import com.jkkc.travel.utils.PrefUtils;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.hyphenate.chat.EMGCMListenerService.TAG;


/**
 * Created by Guan on 2017/6/8.
 */

public class SosActivity extends Activity {

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        sendHelpMsgToServer();

    }


    SendSuccessDialog mSendSuccessDialog;
    SosNetDialog mSosNetDialog;
    SosNoUserDialog mSosNoUserDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sos);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (isNetworkConnected(this)) {

            boolean hasLogin = PrefUtils.getBoolean(getApplicationContext(), "hasLogin", false);
            if (hasLogin) {

                //发送求救消息给服务器，并且提示用户
                sendHelpMsgToServer();

            } else {

                mSosNoUserDialog = new SosNoUserDialog(this, R.style.CustomDialog);

                if (!mSosNoUserDialog.isShowing()) {

                    mSosNoUserDialog.show();

                }


            }

        } else {

            mSosNetDialog = new SosNetDialog(this, R.style.CustomDialog);

            if (!mSosNetDialog.isShowing()) {

                mSosNetDialog.show();

            }


        }


        //呼叫,拨打全球华人领事馆电话
        findViewById(R.id.ivCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("tel:008612308");
                Intent it = new Intent("android.intent.action.CALL", uri);
                startActivity(it);

            }
        });

    }

    private PopupWindow popupWindow;

    private void shouPopuWindow() {

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.dialog_send_success, null);
        Button button = (Button) view.findViewById(R.id.btnCancelAlarm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialDialog.Builder(SosActivity.this)
                        .content("是否取消报警？")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Toast.makeText(SosActivity.this, "已经取消报警求助", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).show();

            }
        });

        ImageView ivDel = (ImageView) view.findViewById(R.id.ivDel);
        ivDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "popupwindow==点击dissmiss");
                popupWindow.dismiss();

            }
        });

        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(false);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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


    String mMac;
    String toUsername = "server2c";//发送给某个人

    private void sendHelpMsgToServer() {


        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
        String lat = PrefUtils.getString(getApplicationContext(), "lat", null);
        String lng = PrefUtils.getString(getApplicationContext(), "lng", null);

        Log.e("报警", mMac + ";" + lat + ";" + lng);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss     ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);

        Log.e("sos", str);


        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1012";//action可以自定义
        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac.getBytes(), "ciatInteraction"));
        Log.e("login", "lat=" + lat);
        cmdMsg.setAttribute("latitude", EncryptUntils.encode3DesBase64(lat.getBytes(), "ciatInteraction"));
        Log.e("login", "lat=" + lat);
        cmdMsg.setAttribute("longitude", EncryptUntils.encode3DesBase64(lng.getBytes(), "ciatInteraction"));
        Log.e("login", "lng=" + lng);
        cmdMsg.setAttribute("coordinates", EncryptUntils.encode3DesBase64("WGS84".getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("sendTime", EncryptUntils.encode3DesBase64(str.getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        cmdMsg.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.e(": ", "onSuccess: " + "报警信息发送成功");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        shouPopuWindow();



                    }
                });
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext("s");
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(": ", "onError: " + e.getMessage());

                            }

                            @Override
                            public void onNext(String s) {

                            }
                        });

            }

            @Override
            public void onError(int code, String error) {

                Log.e(": ", "onError: 服务器出错");

            }

            @Override
            public void onProgress(int progress, String status) {
                Log.e(": ", "onProgress: ");
            }

        });

        EMClient.getInstance().chatManager().sendMessage(cmdMsg);


        new SweetAlertDialog(SosActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("报警发送成功")
                .setContentText("取消报警？")
                .setConfirmText("确定")
                .setCancelText("否")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        sDialog.dismissWithAnimation();
                        Toast.makeText(getApplicationContext(), "已经取消报警", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })

                .show();

    }


}
