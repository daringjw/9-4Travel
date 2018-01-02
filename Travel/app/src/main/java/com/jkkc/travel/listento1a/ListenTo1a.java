package com.jkkc.travel.listento1a;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jkkc.travel.R;
import com.jkkc.travel.sweepcodebindlogin.PrefUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ListenTo1a extends AppCompatActivity {
    // 音频获取源
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int mSampleRateInHz = 8000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; // CHANNEL_IN_MONO
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private Button mButtonStart;
    private Button mButtonStop;
    private boolean isWorking = false; // 设置正在录制的状态

    AudioTrack mAudioTrack;

    private static final int ReceivePort = 9999;

    DatagramSocket ReceiveDS;

    EditText editTextTargetIP;
    TextView textViewMyIP;
    String targetIP;

    String mDebugPath;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    private static final int REQUEST_RECORD = 1;
    private static String[] PERMISSIONS_RECORD = {
            Manifest.permission.RECORD_AUDIO
    };

    public static void verifyRecordPermissions(Activity activity) {
        // Check if we have record permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            // 提示用户开户权限音频
            ActivityCompat.requestPermissions(activity, PERMISSIONS_RECORD, REQUEST_RECORD);


        }
    }

    private long mEnd;
    private long mStart;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       /* getWindow().setFormat(PixelFormat.TRANSLUCENT);// 让界面横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉界面标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        // 重新设置界面大小

        setContentView(R.layout.activity_listen_to_1a);


        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permission1 != PackageManager.PERMISSION_GRANTED ||
                permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, new String[]{

                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO

            }, 1);

            return ;

        } else {

            mStart = System.currentTimeMillis();
            init();
            mDebugPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }


    }

    private void init() {

        mButtonStart = (Button) this.findViewById(R.id.start);
        mButtonStop = (Button) this.findViewById(R.id.stop);
        mButtonStart.setOnClickListener(new TestAudioListener());
        mButtonStop.setOnClickListener(new TestAudioListener());

        mButtonStart.setEnabled(true); // 让一个按键可以被用户按，或者不可按
        mButtonStop.setEnabled(false);
        // mButtonStart.setClickable(true); // 这个函数的意思是让按键按一下。模拟让按键自己按一下，而不是手动去按

        // 初始化
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);

        // 初始化播放设备
        // 获得构建对象的最小缓冲区大小
        int minBufSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRateInHz, mChannelConfig, mAudioFormat, minBufSize, AudioTrack.MODE_STREAM);

        editTextTargetIP = (EditText) findViewById(R.id.editTextTargetIP);
        textViewMyIP = (TextView) findViewById(R.id.textViewMyIP);

        @SuppressLint("WifiManagerLeak")
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        textViewMyIP.setText(ip.toCharArray(), 0, ip.length());
        editTextTargetIP.setText(ip.toCharArray(), 0, ip.length());
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    class TestAudioListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == mButtonStart) {
                mButtonStart.setEnabled(false);
                mButtonStop.setEnabled(true);
                start();
            }
            if (v == mButtonStop) {
                mButtonStart.setEnabled(true);
                mButtonStop.setEnabled(false);
                stop();
            }
        }
    }

    private void start() {
        try {
            mAudioTrack.play();
            targetIP = editTextTargetIP.getText().toString();
            isWorking = true;
            // 开启播放线程
            new Thread(new AudioPlayThread()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop() {

        isWorking = false;
        mAudioTrack.stop();
    }

    class AudioPlayThread implements Runnable {
        @Override
        public void run() {
            InetAddress targetAddr = null;

            try {
                // 接收程序
                ReceiveDS = new DatagramSocket(ReceivePort);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] buf = new byte[bufferSizeInBytes]; // 开辟一个二进制数组用于存储数据。
            DatagramPacket dp = new DatagramPacket(buf, buf.length); // 创建一个长度为buf.length的数据包

            while (isWorking) // 用一个循环来接收数据包
            {
                try {
                    ReceiveDS.receive(dp); // 阻塞式方法。通过receive方法将收到数据存入数据包中
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (dp != null) {
                    try {

                        //final byte[] chunk = dp.getData();

                        mAudioTrack.write(dp.getData(), 0, dp.getLength());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    showLog("dtu receive from " + dp.getAddress().getHostAddress() + " data len = " + dp.getLength());
                }

            }

            ReceiveDS.close();
        }
    }

    private void showLog(String msg) {
        Log.w("xxxxx", msg);
    }

    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
//        return days + "日" + hours + "小时" + minutes + "分"
//                + seconds + "秒";
        return minutes + "分" + seconds + "秒";

    }

    @Override
    protected void onDestroy() {

        mEnd = System.currentTimeMillis();
        long disTime = mEnd - mStart;
        String usedTime = formatDuring(disTime);
        PrefUtils.setString(getApplicationContext(), "usedTime", usedTime);

        if (isWorking) {
            isWorking = false;
            mAudioTrack.stop();
        }

        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }

        super.onDestroy();

    }
}


