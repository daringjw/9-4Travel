package com.jkkc.travel.SOS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;

import com.jkkc.travel.R;
import com.umeng.analytics.MobclickAgent;


/**
 * Created by Guan on 2017/6/8.
 */

public class SosActivity1 extends Activity {


    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        sendCancelAlarmToServer();

    }

    SosDialog sosDialog;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(

                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |

                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |

                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_sos1);

        sosDialog = new SosDialog(this, R.style.CustomDialog);

        if (!sosDialog.isShowing()) {
            sosDialog.show();
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


    public static String getDeviceId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }

    private void sendCancelAlarmToServer() {


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

}
