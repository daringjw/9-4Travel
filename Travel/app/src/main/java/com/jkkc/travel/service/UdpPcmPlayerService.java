package com.jkkc.travel.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jkkc.travel.bean.MicBean;
import com.jkkc.travel.event.MicEvent;
import com.jkkc.travel.utils.PrefUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;


/**
 * Created by blvhop on 2017/8/7.
 */

public class UdpPcmPlayerService extends Service {

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
    // 接收到UDP语音数据包大小
    private int bufferSizeReceiveBytes = 2048; // 根据1A实际一次发送的数据包调整

    private AudioTrack mAudioTrack;
    private AudioPlayer mAudioPlayer = null;

    private static final int AudioPlayPort = 9999;
    private static final int RSCmdPort = 9998;

    private DatagramSocket AudioPlayDS;
    private DatagramSocket RSCmdDS;
    private String mac;

    // 正在播放的状态
    public enum PLAY_STATE {
        START,
        PLAYING,
        PAUSE,
        STOP,
    }

    // STOP状态: 30s收不到音频数据、收到stop情况下置为false
    public PLAY_STATE play_state = PLAY_STATE.START;

    private final int TimeoutTimesMax = 30;
    private int mTimeoutTimes = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("xxxxx onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("xxxxx onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        System.out.println("xxxxx onCreate");

        // 初始化
        // 获得缓冲区字节大小
        // bufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        bufferSizeInBytes = 16384;
        // 初始化播放设备
        // 获得构建对象的最小缓冲区大小b
        int minBufSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRateInHz,
                mChannelConfig, mAudioFormat, minBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();


        new Thread(new AudioReceiveThread()).start();
        new Thread(new SendCmdThread()).start();
        new Thread(new ReceiveCmdThread()).start();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("xxxxx onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("xxxxx onDestroy");

        mAudioTrack.stop();
        // 在商旅系统中，就不要stopService，让Service一直运行
        super.onDestroy();
    }

    private UdpPcmPlayerBinder binder = new UdpPcmPlayerBinder();

    public class UdpPcmPlayerBinder extends Binder {
        public UdpPcmPlayerService getService() {
            return UdpPcmPlayerService.this;
        }
    }

    public PLAY_STATE getPlayState() {
        System.out.println("xxxxx play_state = " + play_state);
        return play_state;
    }

    public void start() {
        System.out.println("xxxxx start");
        play_state = PLAY_STATE.START;
    }

    public synchronized void play() { // 必须加synchronized，否则可能出现线程中读出来的isPlaying是缓存数据
        System.out.println("xxxxx play");
        play_state = PLAY_STATE.PLAYING;
    }

    public void pause() {
        System.out.println("xxxxx pause");
        play_state = PLAY_STATE.PAUSE;
    }

    public void stop() {
        System.out.println("xxxxx stop");
        play_state = PLAY_STATE.STOP;
    }

    class AudioReceiveThread implements Runnable {
        @Override
        public void run() {

            try {
                AudioPlayDS = new DatagramSocket(AudioPlayPort);
                AudioPlayDS.setSoTimeout(1000);
            } catch (Exception e) { // 不能执行到这里
                e.printStackTrace();
                showLog(e.getMessage());
                play_state = PLAY_STATE.STOP;
                stopSelf();
                showLog("AudioReceiveThread 语音服务结束");
                return;
            }

            byte[] buf = new byte[bufferSizeInBytes];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            showLog("dtu receive begin >>>>>>>>>>>>>>>>>>>>>>>>");

            while (true) {
                try {
                    AudioPlayDS.receive(dp); // 阻塞式方法。通过receive方法将收到数据存入数据包中
                } catch (SocketTimeoutException e) {
                    showLog("dtu receive timeout mTimeoutTimes = " + mTimeoutTimes);
                    mTimeoutTimes++;
                    if (mTimeoutTimes > TimeoutTimesMax) {
                        play_state = PLAY_STATE.STOP;
                        showLog("dtu receive timeout! set play_state = PLAY_STATE.STOP;");
                        mTimeoutTimes = 0;
                    }
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                    showLog(e.getMessage());
                    continue;
                } finally {
                }

                if (dp != null) {
                    try {
                        if (play_state == PLAY_STATE.PLAYING) {
                            if (mAudioPlayer == null) {
                                mAudioPlayer = new AudioPlayer();
                            }

                            byte[] data = new byte[dp.getLength()];
                            System.arraycopy(dp.getData(), 0, data, 0, dp.getLength());
                            mAudioPlayer.putData(data);


                            MicBean micBean = new MicBean();
                            MicEvent micEvent = new MicEvent();
                            micEvent.setMicBean(micBean);
                            micEvent.getMicBean().mic1=data;

                            EventBus.getDefault().post(micEvent);




                        } else if (play_state == PLAY_STATE.STOP) {
                            mAudioPlayer.isWorking = false;
                            mAudioPlayer = null;
                            Thread.sleep(5000);
                        } // 不要在其他状态设置延时，否则会出现声音滞后播放的现象
                        showLog("dtu receive from " + dp.getAddress().getHostAddress() + " data len = " + dp.getLength());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLog(e.getMessage());
                    }
                    mTimeoutTimes = 0;
                }
            }
        }
    }

    class AudioPlayer {
        public boolean isWorking = true;
        private LinkedList<byte[]> linkedList;

        public AudioPlayer() {
            linkedList = new LinkedList<>();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    showLog("AudioPlayer thread " + Thread.currentThread().getId() + " BEGIN");
                    byte[] data;

                    while (isWorking) {
                        data = getData();
                        if (data == null) {
                            try {
                                Thread.sleep(50);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        if (linkedList.size() > 8) { // 2048 * 8 / 1024 /16 = 1s，延时不会超过1s，超过1s清空数据
                            linkedList.clear();
                        }

                        try {
                            // showLog("Player thread ID " + Thread.currentThread().getId() + " mAudioTrack.write data len  = " + data.length);
                            mAudioTrack.write(data, 0, data.length);
                        } catch (Exception e) {
                            showLog("Player thread mAudioTrack.write e = " + e.getMessage());
                        }
                    }
                    showLog("AudioPlayer thread " + Thread.currentThread().getId() + " END");
                }
            });
            thread.start();
        }

        public void putData(byte[] data) {
            synchronized (this) {
                linkedList.add(data);
            }
        }

        public byte[] getData() {
            synchronized (this) {
                if (linkedList.isEmpty()) {
                    return null;
                }
                byte[] data = linkedList.get(0);
                linkedList.remove(data);
                return data;
            }
        }
    }


    class SendCmdThread implements Runnable {

        @Override
        public void run() {
            try {
                RSCmdDS = new DatagramSocket(RSCmdPort);
            } catch (Exception e) { // 不能执行到这里
                e.printStackTrace();
                showLog(e.getMessage());

                play_state = PLAY_STATE.STOP;

                stopSelf();

                showLog("SendCmdThread EERRRROORR 语音服务结束");
                return;
            }

            while (true) {
                try {
                    String str_send = "0";
                    if (play_state == PLAY_STATE.PLAYING) {
                        str_send = "1";
                    }
					//mac
                    if (mac == null) {
                        mac = PrefUtils.getString(getApplicationContext(), "mac", null);
                    }
                    str_send = str_send + "," + mac + "\r\n";
                    DatagramPacket dp = new DatagramPacket(str_send.getBytes(), str_send.getBytes().length, InetAddress.getByName("192.168.99.1"), RSCmdPort);
                    RSCmdDS.send(dp);
                    Thread.sleep(5000);
                } catch (Exception e) {
                    showLog("SendCmdThread e = " + e.getMessage());
                }
            }
        }
    }

    class ReceiveCmdThread implements Runnable {

        @Override
        public void run() {
            byte[] buf = new byte[12];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            while (true) {

                if (RSCmdDS == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        continue;
                    }
                }

                try {
                    RSCmdDS.receive(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                    showLog(e.getMessage());
                    continue;
                } finally {
                }

                if (dp != null) {
                    try {
                        String str = new String(buf);
                        if (str.contains("start")) {
                        } else if (str.contains("stop")) {
                            play_state = PLAY_STATE.STOP;
                        }
                    } catch (Exception e) {
                        showLog("ReceiveCmdThread e = " + e.getMessage());
                    }
                }
            }
        }
    }


    private void showLog(String msg) {
        System.out.println("xxxxx " + msg);
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }
}
