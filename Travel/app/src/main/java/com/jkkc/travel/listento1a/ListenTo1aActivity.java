package com.jkkc.travel.listento1a;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jkkc.travel.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class ListenTo1aActivity extends AppCompatActivity {


    private final String TAG = "blvhop";
    /**
     * 用来解码
     */
    private MediaCodec mMediaCodec;
    /**
     * 用来读取音频文件
     */
    private MediaExtractor extractor;
    private MediaFormat format;
    private String mime = null;
    private int sampleRate = 0, channels = 0;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageView mBtnBack;
    private Button mBtnPauseOn;
    private boolean mPauseOn;
    private Button mBtnStop;




    /**
     * 将raw里的文件copy到sd卡下
     *
     * @param name
     */
    public void copyResToSdcard(String name) {//name为sd卡下制定的路径
        Field[] raw = R.raw.class.getFields();
        for (Field r : raw) {
            try {
                //     System.out.println("R.raw." + r.getName());
                int id = getResources().getIdentifier(r.getName(), "raw", getPackageName());
                if (!r.getName().equals("allapps")) {
                    String path = name + "/" + r.getName() + ".mp3";
                    BufferedOutputStream bufEcrivain = new BufferedOutputStream((new FileOutputStream(new File(path))));
                    BufferedInputStream VideoReader = new BufferedInputStream(getResources().openRawResource(id));
                    byte[] buff = new byte[20 * 1024];
                    int len;
                    while ((len = VideoReader.read(buff)) > 0) {
                        bufEcrivain.write(buff, 0, len);
                    }
                    bufEcrivain.flush();
                    bufEcrivain.close();
                    VideoReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to1a);


        mBtnBack = (ImageView) findViewById(R.id.btnBack);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();


            }
        });

        mPauseOn = true;
        mBtnPauseOn = (Button) findViewById(R.id.btnPauseOn);
        mBtnPauseOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mPauseOn) {
                    mPauseOn = false;
                    mBtnPauseOn.setText("继续");

                    myTask.cancel(true);


                } else {
                    mPauseOn = true;
                    mBtnPauseOn.setText("暂停");


                }


            }
        });


        mBtnStop = (Button) findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myTask.cancel(true);


                finish();




            }
        });


        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);

            return;

        } else {

            File f = new File("/storage/emulated/0/audio_info.mp3");
            if (!f.exists()) {
                copyResToSdcard("/storage/emulated/0/");
            }

            decodeVoice();
        }


    }




    AsyncTask<Void, Void, Void> myTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

//                haoShi();

            }

            return null;

        }


    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void haoShi() {

        int mFrequency = 44100;// 采样率
        int mChannel = AudioFormat.CHANNEL_OUT_STEREO;// 声道
        int mSampBit = AudioFormat.ENCODING_PCM_16BIT;// 采样精度
        AudioTrack mAudioTrack;

        // 获得构建对象的最小缓冲区大小
        int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency, mChannel, mSampBit, minBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.setVolume(0.5f);
        mAudioTrack.play();

        extractor = new MediaExtractor();
        try {
            extractor.setDataSource("/storage/emulated/0/audio_info.mp3");
        } catch (Exception e) {
            Log.e(TAG, " 设置文件路径错误" + e.getMessage());
        }

        // 音频文件信息
        format = extractor.getTrackFormat(0);
        // mime = format.getString(MediaFormat.KEY_MIME);
        // mime = "audio/ffmpeg";
        mime = "audio/mpeg";

        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        // if duration is 0, we are probably playing a live stream

        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
        } catch (RuntimeException e) {
            Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
        } finally {
        }

        if (mMediaCodec == null) {
            Log.e(TAG, "创建解码器失败！");
            return;
        }
        mMediaCodec.configure(format, null, null, 0);

        mMediaCodec.start();

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();// 用来存放目标文件的数据
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();// 解码后的数据
        extractor.selectTrack(0);

        // ==========开始解码=============
        final long kTimeOutUs = 10;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        long currentTimeMillis;
        byte[] buf_udp = new byte[256]; //开辟一个二进制数组用于存储数据。

        try {
            DatagramSocket ds = new DatagramSocket(6667);//创建一个DatagramSocket对象，并指定一个端口号

            while (true)//用一个循环来接收数据包
            {
                while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    Log.v(TAG, String.valueOf(currentTimeMillis));

                    DatagramPacket udpPacket = new DatagramPacket(buf_udp, buf_udp.length);//创建一个长度为buf.length的数据包
                    try {
                        ds.receive(udpPacket);//阻塞式方法。通过服务的receive方法将收到数据存入数据包中。

                        if (udpPacket != null) {
                            try {
                                int inputBufIndex = mMediaCodec.dequeueInputBuffer(kTimeOutUs);
                                if (inputBufIndex >= 0) {
                                    ByteBuffer dstBuf = inputBuffers[inputBufIndex];

                                    dstBuf.put(buf_udp, 0, udpPacket.getLength());
                                    int sampleSize = udpPacket.getLength();

                                    if (sampleSize < 0)
                                        sampleSize = 0;

                                    mMediaCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, 0, 0);
                                } else {
                                    Log.e(TAG, "inputBufIndex " + inputBufIndex);
                                }

                                // decode to PCM and push it to the AudioTrack player
                                int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

                                if (res >= 0) {
                                    int outputBufIndex = res;
                                    ByteBuffer buf = outputBuffers[outputBufIndex];
                                    final byte[] chunk = new byte[info.size];
                                    buf.get(chunk);
                                    buf.clear();
                                    if (chunk.length > 0) {
                                        try {
                                            mAudioTrack.write(chunk, 0, chunk.length);
                                            //    break;
                                        } catch (Exception e) {
                                            Log.i(TAG, "catch exception...");
                                        }
                                    }
                                    mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
                                }
                            } catch (RuntimeException e) {
                                Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        // =================================================================================
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
        // clear source and the other globals
        mime = null;
        sampleRate = 0;
        channels = 0;

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }


    private void decodeVoice() {

        if (myTask != null) {

            myTask.execute();

        }



/*
        thread = new Thread() {

            @Override
            public void run() {

                int mFrequency = 44100;// 采样率
                int mChannel = AudioFormat.CHANNEL_OUT_STEREO;// 声道
                int mSampBit = AudioFormat.ENCODING_PCM_16BIT;// 采样精度
                AudioTrack mAudioTrack;

                // 获得构建对象的最小缓冲区大小
                int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency, mChannel, mSampBit, minBufSize, AudioTrack.MODE_STREAM);
                mAudioTrack.setVolume(0.5f);
                mAudioTrack.play();

                extractor = new MediaExtractor();
                try {
                    extractor.setDataSource("/storage/emulated/0/audio_info.mp3");
                } catch (Exception e) {
                    Log.e(TAG, " 设置文件路径错误" + e.getMessage());
                }

                // 音频文件信息
                format = extractor.getTrackFormat(0);
                // mime = format.getString(MediaFormat.KEY_MIME);
                // mime = "audio/ffmpeg";
                mime = "audio/mpeg";

                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                // if duration is 0, we are probably playing a live stream

                try {
                    mMediaCodec = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
                } catch (RuntimeException e) {
                    Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
                } finally {
                }

                if (mMediaCodec == null) {
                    Log.e(TAG, "创建解码器失败！");
                    return;
                }
                mMediaCodec.configure(format, null, null, 0);

                mMediaCodec.start();

                ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();// 用来存放目标文件的数据
                ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();// 解码后的数据
                extractor.selectTrack(0);

                // ==========开始解码=============
                final long kTimeOutUs = 10;
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                long currentTimeMillis;
                byte[] buf_udp = new byte[256]; //开辟一个二进制数组用于存储数据。

                try {
                    DatagramSocket ds = new DatagramSocket(6667);//创建一个DatagramSocket对象，并指定一个端口号

                    while (true)//用一个循环来接收数据包
                    {
                        while (true) {
                            currentTimeMillis = System.currentTimeMillis();
                            Log.v(TAG, String.valueOf(currentTimeMillis));

                            DatagramPacket udpPacket = new DatagramPacket(buf_udp, buf_udp.length);//创建一个长度为buf.length的数据包
                            try {
                                ds.receive(udpPacket);//阻塞式方法。通过服务的receive方法将收到数据存入数据包中。

                                if (udpPacket != null) {
                                    try {
                                        int inputBufIndex = mMediaCodec.dequeueInputBuffer(kTimeOutUs);
                                        if (inputBufIndex >= 0) {
                                            ByteBuffer dstBuf = inputBuffers[inputBufIndex];

                                            dstBuf.put(buf_udp, 0, udpPacket.getLength());
                                            int sampleSize = udpPacket.getLength();

                                            if (sampleSize < 0)
                                                sampleSize = 0;

                                            mMediaCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, 0, 0);
                                        } else {
                                            Log.e(TAG, "inputBufIndex " + inputBufIndex);
                                        }

                                        // decode to PCM and push it to the AudioTrack player
                                        int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

                                        if (res >= 0) {
                                            int outputBufIndex = res;
                                            ByteBuffer buf = outputBuffers[outputBufIndex];
                                            final byte[] chunk = new byte[info.size];
                                            buf.get(chunk);
                                            buf.clear();
                                            if (chunk.length > 0) {
                                                try {
                                                    mAudioTrack.write(chunk, 0, chunk.length);
                                                    //    break;
                                                } catch (Exception e) {
                                                    Log.i(TAG, "catch exception...");
                                                }
                                            }
                                            mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
                                        }
                                    } catch (RuntimeException e) {
                                        Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }

                // =================================================================================
                if (mMediaCodec != null) {
                    mMediaCodec.stop();
                    mMediaCodec.release();
                    mMediaCodec = null;
                }
                if (extractor != null) {
                    extractor.release();
                    extractor = null;
                }
                // clear source and the other globals
                mime = null;
                sampleRate = 0;
                channels = 0;

                if (mAudioTrack != null) {
                    mAudioTrack.stop();
                    mAudioTrack.release();
                }
            }
        };

        thread.start();

        */

    }


}
