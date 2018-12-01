package com.utils;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.util.Log;

public class ImageUtils {
    final String fontUrl = "http://tgchat.94loveyou.cn/";
    Activity activity;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    //判断文件是否存在
    public static boolean fileIsExists(Activity activity, String strFile) {
        try {
            File f = new File(activity.getFilesDir(), strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 获取网络图片
     *
     * @param imageUrl 图片网络地址
     * @return Bitmap 返回位图
     */
    private Bitmap GetImageInputStream(String imageUrl) {
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //bitmap转字节流
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, b);
        return b.toByteArray();
    }

    /**
     * 下载图片
     *
     * @param imageUrl 图片文件名
     * @param handler  UI更新
     * @param kind     2为个人图片获取完成， 3为好友图片获取完成
     */
    public void downloadImage(String imageUrl, Handler handler, Integer kind) {
        DownloadImagesThread downloadImagesThread = new DownloadImagesThread(imageUrl, handler, kind);
        downloadImagesThread.start();

    }

    //下载图片线程
    class DownloadImagesThread extends Thread {
        String imageUrl;
        Handler handler;
        Integer kind;

        public DownloadImagesThread(String imageUrl, Handler handler, Integer kind) {
            this.imageUrl = imageUrl;
            this.handler = handler;
            this.kind = kind;
        }

        @Override
        public void run() {
            super.run();
            //判断文件是否存在
            if (!fileIsExists(activity, imageUrl)) {
                Bitmap head_image = GetImageInputStream(fontUrl + imageUrl);
                SaveImage(head_image, imageUrl);
            }
            Message msg = new Message();
            msg.what = kind;
            handler.sendMessage(msg);
        }
    }

    /**
     * 保存位图到本地
     *
     * @param bitmap
     * @param imageUrl 文件名
     */
    private void SaveImage(Bitmap bitmap, String imageUrl) {
        FileOutputStream fileOutputStream;
        String path = activity.getFilesDir().getAbsolutePath();
        try {
            fileOutputStream = new FileOutputStream(path + "/" + imageUrl);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
