package com.jkkc.travel.utils;

/**
 * Created by Guan on 2018/1/3.
 */

public class GetFenBei {

    /**
     * 获取分贝
     *
     * @param buffer  语音buffer
     * @return
     */
    public  double getDB(byte[] buffer) {
        long time = System.currentTimeMillis();
        short[] audioData = BytesTransUtil.getInstance().Bytes2Shorts(buffer);
        long v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < audioData.length; i++) {
            v += audioData[i] * audioData[i];
        }
        // 平方和除以数据总长度，得到音量大小。
        double mean = v / (double) buffer.length;
        double volume = 10 * Math.log10(mean);
        return volume;
    }





}
