package com.jkkc.travel.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.jkkc.travel.R;
import com.jkkc.travel.bean.UpdateInfo;
import com.jkkc.travel.config.Config;
import com.jkkc.travel.db.NewsHelper;
import com.jkkc.travel.http.UpdateAppHttpUtil;
import com.jkkc.travel.sweepcodebindlogin.BindActivity;
import com.jkkc.travel.sweepcodebindlogin.PrefUtils;
import com.jkkc.travel.systemmanagerment.utils.DataCleanManager;
import com.umeng.analytics.MobclickAgent;
import com.vector.update_app.UpdateAppManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2017/7/11.
 */

public class MineActivity extends AppCompatActivity {


    @BindView(R.id.btnBack)
    ImageView mBtnBack;
    @BindView(R.id.ivPersonalMsg)
    ImageView mIvPersonalMsg;
    @BindView(R.id.tvPersonalMsg)
    TextView mTvPersonalMsg;
    @BindView(R.id.ivExpandPerson)
    ImageView mIvExpandPerson;
    @BindView(R.id.rlPersonalMsg)
    RelativeLayout mRlPersonalMsg;
    @BindView(R.id.tvMyName)
    TextView mTvMyName;
    @BindView(R.id.tvMyNumber)
    TextView mTvMyNumber;
    @BindView(R.id.ivAboutUs)
    ImageView mIvAboutUs;
    @BindView(R.id.tvAboutUs)
    TextView mTvAboutUs;
    @BindView(R.id.ivExpandAboutUs)
    ImageView mIvExpandAboutUs;
    @BindView(R.id.rlAboutUs)
    RelativeLayout mRlAboutUs;
    @BindView(R.id.tvJinKun)
    TextView mTvJinKun;
    @BindView(R.id.tvCheckVersion)
    TextView mTvCheckVersion;
    @BindView(R.id.ivLogout)
    ImageView mIvLogout;
    @BindView(R.id.tvLogout)
    TextView mTvLogout;
    @BindView(R.id.rlLogout)
    RelativeLayout mRlLogout;
    @BindView(R.id.llPersonalMsg)
    LinearLayout mLlPersonalMsg;
    @BindView(R.id.llAboutUs)
    LinearLayout mLlAboutUs;

    private String localVersionName;


    public static final String TAG = "MineActivity";


    private String new_version;


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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mine);
        ButterKnife.bind(this);

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

    @OnClick({R.id.btnBack, R.id.rlPersonalMsg, R.id.rlAboutUs, R.id.rlLogout})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.btnBack:

                finish();

                break;
            case R.id.rlPersonalMsg:


                if (mLlPersonalMsg.getVisibility() == View.VISIBLE) {

                    mLlPersonalMsg.setVisibility(View.GONE);
                    mIvExpandPerson.setImageResource(R.mipmap.ic_expand_more);

                } else {

                    mLlPersonalMsg.setVisibility(View.VISIBLE);
                    mIvExpandPerson.setImageResource(R.mipmap.ic_expand_less);
                    String mUserName = PrefUtils.getString(getApplicationContext(), "mUserName", null);
                    mTvMyName.setText("姓名:" + mUserName);
                    if (!TextUtils.isEmpty(getPhoneNum())) {
                        mTvMyNumber.setText("本机号码:" + getPhoneNum());
                    } else {
                        mTvMyNumber.setText("抱歉由于运营商问题，获取不到本机号码");
                    }

                }


                break;

            case R.id.rlAboutUs:


                if (mLlAboutUs.getVisibility() == View.VISIBLE) {

                    mLlAboutUs.setVisibility(View.GONE);
                    mIvExpandAboutUs.setImageResource(R.mipmap.ic_expand_more);

                } else {

                    mLlAboutUs.setVisibility(View.VISIBLE);
                    mIvExpandAboutUs.setImageResource(R.mipmap.ic_expand_less);

                    mTvJinKun.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(getApplicationContext(), JinKunActivity.class));

                        }
                    });


                    try {

                        localVersionName = getLocalVersionName();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mTvCheckVersion.setText("当前版本：" + localVersionName);


                    mTvCheckVersion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {


                                @Override
                                protected String doInBackground(String... params) {

                                    String urlstr = params[0];
                                    HttpGet httpGet = new HttpGet(urlstr);
                                    DefaultHttpClient httpClient = new DefaultHttpClient();
                                    try {
                                        // 向服务器端发送请求
                                        HttpResponse httpResponse = httpClient.execute(httpGet);
                                        HttpEntity httpEntity = httpResponse.getEntity();
                                        InputStream in = httpEntity.getContent();
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader(in));
                                        String line = null;
                                        StringBuffer sb = new StringBuffer();
                                        while ((line = br.readLine()) != null) {
                                            sb.append(line);
                                        }

                                        Log.e(TAG, "json=" + sb.toString());

                                        Gson gson = new Gson();
                                        UpdateInfo updateInfo = gson.fromJson(sb.toString(),
                                                UpdateInfo.class);
                                        new_version = updateInfo.getNew_version();
                                        Log.e(TAG, "new_version=" + new_version);


                                        return sb.toString();

                                    } catch (ClientProtocolException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(String s) {

                                    if (s != null) {

                                        if (localVersionName.equals(new_version)) {

                                            Toast.makeText(getApplicationContext(), "已经是最新版本了",
                                                    Toast.LENGTH_SHORT).show();

                                        } else {

                                            new UpdateAppManager
                                                    .Builder()
                                                    //当前Activity
                                                    .setActivity(MineActivity.this)
                                                    //更新地址
                                                    .setUpdateUrl(Config.UPDATE_URL)
                                                    //实现httpManager接口的对象
                                                    .setHttpManager(new UpdateAppHttpUtil())
                                                    .build()
                                                    .update();
                                        }

                                    }


                                    super.onPostExecute(s);
                                }
                            };

                            asyncTask.execute(Config.UPDATE_URL);


                        }


                    });


                }

                break;


            case R.id.rlLogout:

                //对话框,是依赖于activity存在的
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //设置左上角图标
                builder.setIcon(R.mipmap.travel);
                builder.setTitle("是否要注销当前用户");
                //设置描述内容
//                builder.setMessage("当前版本是:" + localVersionName + des);

                //积极按钮,立即更新
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        EMClient.getInstance().logout(true, new EMCallBack() {


                            @Override
                            public void onSuccess() {
                                // TODO Auto-generated method stub

                                Log.e(TAG, "用户解绑退出");
                                NewsHelper.getDBHelper().delAll();
                                PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);

                                String filePath = getApplicationContext()
                                        .getFilesDir().getAbsolutePath();
                                DataCleanManager.cleanApplicationData(getApplicationContext(), filePath);
                                PrefUtils.setBoolean(getApplicationContext(), "hasLogin", false);
                                startActivity(new Intent(getApplicationContext(), BindActivity.class));
                                finish();



                            }

                            @Override
                            public void onProgress(int progress, String status) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onError(int code, String message) {
                                // TODO Auto-generated method stub

                            }
                        });


                    }
                });

                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取消对话框,进入主界面
//                enterHome();

                        dialog.dismiss();
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


                break;


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

}
