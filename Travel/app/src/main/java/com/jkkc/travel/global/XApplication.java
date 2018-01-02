package com.jkkc.travel.global;

import android.app.Application;
import android.content.Context;

/**
 * Created by Xxyou on 2017/2/28.
 */

public class XApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();


    }

    public static Context getContext() {
        return mContext;
    }
}
