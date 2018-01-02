package com.jkkc.travel.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.NetUtils;
import com.jkkc.travel.R;
import com.jkkc.travel.utils.EncryptUntils;
import com.jkkc.travel.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2017/6/28.
 */

public class JieKouActivity extends Activity {

    @BindView(R.id.btnSendAlarm)
    Button mBtnSendAlarm;
    @BindView(R.id.btnSendLoc)
    Button mBtnSendLoc;
    @BindView(R.id.btnGuideSpeakRe)
    Button mBtnGuideSpeakRe;
    @BindView(R.id.btnNetRatioRe)
    Button mBtnNetRatioRe;
    @BindView(R.id.btnContactsRe)
    Button mBtnContactsRe;

    private String mMac;
    private String mPwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_jiekou);
        ButterKnife.bind(this);


        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());


        receive();


    }


    //回复语音讲解
    private void anwserTheVoice() {

        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1023";//action可以自定义

        cmdMsg.setAttribute("result", true);
        cmdMsg.setAttribute("msg", "绑定失败的原因");
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);


    }


    private void receive() {

        EMMessageListener msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                Log.e("login", messages.get(0).toString());

            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {

                Log.e("login", "onCmdMessageReceived: " + messages.get(0).toString());
                //收到透传消息.
                String cmd = messages.get(0).getBody().toString();
                Log.e("login", "cmd==" + cmd);

                switch (cmd) {
                    case "cmd:\"1013\"":
                        //报警返回
                        try {

                            Log.e("login", "报警返回: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("msg").getBytes(), "ciatInteraction"));

                            Log.e("login", "报警返回: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("result").getBytes(), "ciatInteraction"));

                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "cmd:\"1024\"":
                        //语音讲解回复
                        try {
                            Log.e("login", "语音讲解回复: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("msg").getBytes(), "ciatInteraction"));
                            Log.e("login", "语音讲解回复: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("result").getBytes(), "ciatInteraction"));


                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }

                        anwserTheVoice();
                        break;
                    case "cmd:\"1026\"":
                        //服务器配置上传周期
                        try {
                            Log.e("login", "服务器配置上传周期: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("round").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "cmd:\"1028\"":
                        //网络配置应急电话
                        try {
                            Log.e("login", "服务器配置上传周期: " + EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("phoneList").getBytes(), "ciatInteraction"));
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "cmd:\"1018\"":
                        //信息推送
                        try {
                            //预警信息
                            final String msg = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("msg").getBytes(), "ciatInteraction");
                            String title = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("title").getBytes(), "ciatInteraction");
                            final String city = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("city").getBytes(), "ciatInteraction");
                            final String level = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("level").getBytes(), "ciatInteraction");

                            final String txt = EncryptUntils.decode3DesBase64
                                    (messages.get(0).getStringAttribute("txt").getBytes(), "ciatInteraction");


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


    @OnClick({R.id.btnSendAlarm, R.id.btnSendLoc, R.id.btnGuideSpeakRe, R.id.btnNetRatioRe, R.id.btnContactsRe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSendAlarm:

                sendHelpMsgToServer();
                Toast.makeText(getApplicationContext(), "报警", Toast.LENGTH_SHORT).show();

                break;
            case R.id.btnSendLoc:

                mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
                String lat = PrefUtils.getString(getApplicationContext(), "lat", null);
                String lng = PrefUtils.getString(getApplicationContext(), "lng", null);

                Log.e("login", mMac + ";" + lat + ";" + lng);


                EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
                String action = "1014";//action可以自定义
                cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac.getBytes(), "ciatInteraction"));
                Log.e("login", "onClick: " + EncryptUntils.decode3DesBase64(mMac.getBytes(), "ciatInteraction"));
                cmdMsg.setAttribute("latitude", EncryptUntils.encode3DesBase64(lat.getBytes(), "ciatInteraction"));
                Log.e("login", "lat=" + lat);
                cmdMsg.setAttribute("longitude", EncryptUntils.encode3DesBase64(lng.getBytes(), "ciatInteraction"));
                Log.e("login", "lng=" + lng);
                cmdMsg.setAttribute("coordinates", EncryptUntils.encode3DesBase64("GCJ02".getBytes(), "ciatInteraction"));
                cmdMsg.setAttribute("sendTime", EncryptUntils.encode3DesBase64("2017年6月28日 15:54".getBytes(), "ciatInteraction"));
                EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
                cmdMsg.addBody(cmdBody);
                cmdMsg.setTo(toUsername);
                EMClient.getInstance().chatManager().sendMessage(cmdMsg);


                break;
            case R.id.btnGuideSpeakRe:


                break;
            case R.id.btnNetRatioRe:


                break;
            case R.id.btnContactsRe:


                break;
        }
    }


    String toUsername = "server2c";//发送给某个人

    private void sendHelpMsgToServer() {


        mMac = PrefUtils.getString(getApplicationContext(), "mac", null);
        String lat = PrefUtils.getString(getApplicationContext(), "lat", null);
        String lng = PrefUtils.getString(getApplicationContext(), "lng", null);

        Log.e("login", mMac + ";" + lat + ";" + lng);


        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        String action = "1012";//action可以自定义
        cmdMsg.setAttribute("MAC", EncryptUntils.encode3DesBase64(mMac.getBytes(), "ciatInteraction"));
        Log.e("login", "onClick: " + EncryptUntils.decode3DesBase64(mMac.getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("latitude", EncryptUntils.encode3DesBase64(lat.getBytes(), "ciatInteraction"));
        Log.e("login", "lat=" + lat);
        cmdMsg.setAttribute("longitude", EncryptUntils.encode3DesBase64(lng.getBytes(), "ciatInteraction"));
        Log.e("login", "lng=" + lng);
        cmdMsg.setAttribute("coordinates", EncryptUntils.encode3DesBase64("GCJ02".getBytes(), "ciatInteraction"));
        cmdMsg.setAttribute("sendTime", EncryptUntils.encode3DesBase64("2017年6月28日 15:54".getBytes(), "ciatInteraction"));
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.addBody(cmdBody);
        cmdMsg.setTo(toUsername);
        EMClient.getInstance().chatManager().sendMessage(cmdMsg);


    }


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
                        if (NetUtils.hasNetwork(JieKouActivity.this)) {
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
}

