package com.jkkc.travel.SOS;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.jkkc.travel.R;
import com.jkkc.travel.utils.EncryptUntils;
import com.jkkc.travel.utils.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;


/**
 * Created by Guan on 2017/5/5.
 */

public class SOSReceiver extends BroadcastReceiver {

    private String launcherPackageName;


    @Override
    public void onReceive(Context context, Intent intent) {

//        abortBroadcast(); // 中断广播，不会再响比它有优先级低得广播再传播下去了

        launcherPackageName = getLauncherPackageName(context);
        Log.e("sos", "launcherPackageName=" + launcherPackageName);

        String type = intent.getExtras().getString("type");

        try {

            final String SOS_TYPE_NOMAL = "nomal";
            final String SOS_TYPE_URGENT = "urgent";

            Log.i("xxxxx", "blvhop 接收到sos按键按下消息 type: " + type);

            if (type.equals(SOS_TYPE_NOMAL)) {

                if (launcherPackageName.equals("com.jkkc.travel")
                        || (launcherPackageName.equals("com.android.launcher3")
                        && (isRunningForeground(context)))) {

                    Intent intent1 = new Intent(context, SosActivity1.class);
                    context.startActivity(intent1);

                } else {

                }


            } else if (type.equals(SOS_TYPE_URGENT)) {

                if (launcherPackageName.equals("com.jkkc.travel")
                        || (launcherPackageName.equals("com.android.launcher3")
                        && (isRunningForeground(context)))) {

                    Intent i = new Intent(context, SosActivity.class);
                    context.startActivity(i);

                } else {

                    sendHelpMsgToServer(context);
                }


            } else {

                Log.e("xxxxx", "blvhop sos?????");

            }
        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    private void viberateUser(Context context) {

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat
                        .Builder(context)
                        .setSmallIcon(R.mipmap.sos_marker)
                        .setContentTitle("报警提示")
                        .setContentText("报警成功")
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(System.currentTimeMillis());


        Intent resultIntent = new Intent(context, SosActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();

        int mNotificationId = 001;

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        mNotifyMgr.notify(mNotificationId, notification);

    }


    String mMac;
    String toUsername = "server2c";//发送给某个人

    private void sendHelpMsgToServer(final Context context) {


        mMac = PrefUtils.getString(context, "mac", null);
        String lat = PrefUtils.getString(context, "lat", null);
        String lng = PrefUtils.getString(context, "lng", null);
        Log.e("报警",lat+"+"+lng);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss     ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);


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

                viberateUser(context);


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


    }

    //判断当前应用是否在前台运行
    public boolean isRunningForeground(Context context) {
        String packageName = getPackageName(context);
        String topActivityClassName = getTopActivityName(context);
        System.out.println("packageName=" + packageName + ",topActivityClassName=" + topActivityClassName);
        if (packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName)) {
            System.out.println("---> isRunningForeGround");
            return true;
        } else {
            System.out.println("---> isRunningBackGround");
            return false;
        }
    }


    public String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager =
                (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();
        }
        return topActivityClassName;
    }

    public String getPackageName(Context context) {
        String packageName = context.getPackageName();
        return packageName;
    }


    public String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            return "";
        }
        //如果是不同桌面主题，可能会出现某些问题，这部分暂未处理
        if (res.activityInfo.packageName.equals("android")) {
            return "";
        } else {
            return res.activityInfo.packageName;
        }
    }

    private boolean isBackgroundRunning(Context context) {
        String processName = "com.jkkc.travel";

        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

        if (activityManager == null) return false;
        // get running application processes
        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (process.processName.startsWith(processName)) {
                boolean isBackground = process.importance != IMPORTANCE_FOREGROUND
                        && process.importance != IMPORTANCE_VISIBLE;
                boolean isLockedState = keyguardManager.inKeyguardRestrictedInputMode();
                if (isBackground || isLockedState) return true;
                else return false;
            }
        }
        return false;
    }


}
