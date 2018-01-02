package com.jkkc.travel.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 2016/11/10.
 */

public class EncryptUntils {

    //定义加密算法，有DES、DESede(即3DES)、Blowfish
    private static final String Algorithm = "DESede";

    //MD5加密
    public static String md5encode(byte[] input){
        byte[] digestedValue = null;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input);
            digestedValue = md.digest();
        }catch(Exception e){
            e.printStackTrace();
        }
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < digestedValue.length; offset++) {
            i = digestedValue[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

    /**
     * 3DES加密方法
     * @param src 源数据的字节数组
     * @return
     */
    public static byte[] encryptMode(byte[] src,String key) {
        try {
            SecretKey deskey = new SecretKeySpec(build3DesKey(key), Algorithm);    //生成密钥
            Cipher c1 = Cipher.getInstance("DESede");    //实例化负责加密/解密的Cipher工具类
            c1.init(Cipher.ENCRYPT_MODE, deskey);    //初始化为加密模式
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }
    //3DES解密
    public static byte[] decrypt(byte[] data,String key){
        try {
            SecretKey deskey = new SecretKeySpec(build3DesKey(key), Algorithm);    //生成密钥
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.DECRYPT_MODE,deskey);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            //解密失败，打日志
            ex.printStackTrace();
        }
        return null;
    }

    /**
     *  3DES加密Base64编码处理方法
     * @param bytes 字符串转的数组
     * @param key 用于3DES加密解密的密钥
     * @return  经过3DES+Base64加密后的字符串
     */
    public static String encode3DesBase64(byte[] bytes,String key){
        byte [] base = encryptMode(bytes,key);
        return  Base64.encodeToString(base,0);
    }

    /**
     * 将3DES+Base64加密后的byte数组进行解密
     * @param bytes 先3DES+Base64加密后的 byte数组
     * @param key  用于3DES加密解密的密钥
     * @return 未加密前的字符串
     */
    public static String decode3DesBase64(byte[] bytes,String key){
        byte[] b = null;
        String result = null;
        try {
            b = decrypt(Base64.decode(bytes,0),key);
            result = new String(b, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    //构建3DES密钥
    public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
        byte[] key = new byte[24];    //声明一个24位的字节数组，默认里面都是0
        byte[] temp = keyStr.getBytes("UTF-8");    //将字符串转成字节数组
         /*
          * 执行数组拷贝
          * System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
          */
        if(key.length > temp.length){
            //如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, temp.length);
        }else{
            //如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        return key;
    }

    // Base64编码
    public static String getBase64(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = Base64.encodeToString(b,0);
        }
        return s;
    }

    //Base64解码
    public static String getFromBase64(String s) {
        byte[] b = null;
        String result = null;
        if (s != null) {
            try {
                b = Base64.decode(s,0);
                result = new String(b, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
