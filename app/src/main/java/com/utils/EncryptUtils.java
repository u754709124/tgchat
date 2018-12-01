package com.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class EncryptUtils {

    //sha1加密
    public static String encryptBySha1(String string) {
        if (string.isEmpty()) {
            return "";
        }
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA1");
            byte[] bytes = hash.digest(string.getBytes("UTF-8"));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("test", "Exception occured at Line 16<EncryptUtils>"
                    + "\n" + Log.getStackTraceString(e));
        } catch (UnsupportedEncodingException e) {
            Log.e("test", "Exception occured at Line 19<EncryptUtils>"
                    + "\n" + Log.getStackTraceString(e));
        }
        return "";
    }

    //随机生成16位Salt
    public static String getSalt() {
        //生成盐的长度
        final int length = 16;
        StringBuilder result = new StringBuilder();
        for (int i = length; i > 0; i--) {
            Random random = new Random();
            String string = "abcdefghijklmhopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-=+*";
            int index = random.nextInt(62);
            String temp = String.valueOf(string.charAt(index));
            result.append(temp);
        }
        return result.toString();
    }
}
