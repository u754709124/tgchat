package com.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.tgchat.MainActivity;
import com.utils.ChatRecordUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;

import static com.tgchat.MainActivity.userInfo;


public class MessageService extends Service {

    private final String TAG = "test";
    private MessageSocket messageSocket;
    public static MessageUpdateUiInterface messageUpdateUiInterface;
    private String currentAccount;
    private ChatRecordUtils chatRecordUtils;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "服务创建");
        //创建MessageSocket实例
        messageSocket = new MessageSocket();

        //初始化Socket
        messageSocket.initMessageSocket();
        super.onCreate();

        //新建ChatRecordUtils实例
        chatRecordUtils = new ChatRecordUtils(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //请求方式 requestLogin-请求登录 requestSendMessage-请求发送消息
        String requestType = intent.getStringExtra("requestType");
        //请求内容 请求登录-服务器下发的token 请求发送消息-消息具体内容(发送对象@发送的信息)
        String requestContent = intent.getStringExtra("requestContent");

        if(requestType.equals("requestLogin")){
            Log.e(TAG, requestContent);
            currentAccount = requestContent;
            messageSocket.sendMessage(requestContent);

        }
        else if(requestType.equals("requestSendContent")){
            messageSocket.sendMessage(requestContent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "服务销毁");
        super.onDestroy();
    }

    //初始化Socket
    private class MessageSocket {
        private Socket socket;
        private final String host = "112.74.168.99";
        private final int port = 8888;
        //连接超时
        private final int connectTimeOut = 2000;
        boolean isPrime = true;
        int reconnectCount = 0;

        private MessageSocket() {

        }

        private void initMessageSocket() {
            socket = new Socket();
            //启动线程连接服务器
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        socket.connect(new InetSocketAddress(host, port));

                    } catch (IOException e) {
                        Log.e(TAG, String.format("连接到 %s[%d]超时，请检查网络连接！", host, port));
                        if(reconnectCount == 0){
                            Message tips = new Message();
                            tips.what = 0;
                            tips.obj = String.format(Locale.CHINA,"连接到 %s[%d]超时，请检查网络连接！", host, port);
                            messageUpdateUiInterface.updateUI(tips);
                            reconnectCount++;
                        }
                        try {
                            Thread.sleep(connectTimeOut);
                        } catch (InterruptedException e1) {
                            Log.e(TAG, Log.getStackTraceString(e1));
                        }
                        initMessageSocket();

                    }

                }
            }).start();

            new listenMessage().start();

        }

        /*监听消息线程
         */
        class listenMessage extends Thread {
            @Override
            public void run() {

                //等待socket连接
                while(!socket.isConnected()){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }

                super.run();

                if (socket != null) {
                    try {
                        // 读Sock里面的数据
                        InputStream s = socket.getInputStream();
                        byte[] buf = new byte[1024];
                        int len = 0;
                        while ((len = s.read(buf)) != -1) {
                            String msg = new String(buf, 0, len);
                            if (msg.equals("login success")) {
                                new sendHeartBeatThread().start();
                                Log.e(TAG, "登录成功！");
                            } else {
                                String sendAccount = msg.split("@")[0];
                                Long sendTime = Long.valueOf(msg.split("@")[1]);
                                String messageContent = msg.split("@")[2];

                                //将得到的聊天记录保存到本地数据库
                                chatRecordUtils.saveChatRecord(sendAccount, currentAccount ,
                                        messageContent, sendTime);

                                //通过服务器查询用户的头像以及昵称信息
                                String nickname = sendAccount;
                                String headImageUrl = "1.jpg";
                                if(messageUpdateUiInterface != null){

                                    //新建MainActivity.Message对象
                                    MainActivity.Message info = new MainActivity.Message(
                                            sendAccount, userInfo.get("userName"), nickname, headImageUrl,
                                            messageContent, sendTime);
                                    //回调到MainActivity通知更新UI
                                    Message msg1 = new Message();
                                    msg1.what = 1;
                                    msg1.obj = info;
                                    messageUpdateUiInterface.updateUI(msg1);
                                }

                                //发送广播
                                Intent mIntent = new Intent("com.services.receiveMessage");
                                mIntent.putExtra("requestType", "requestUpdateUI");
                                sendBroadcast(mIntent);

                                Log.e(TAG, String.format("[%s]: \"%s\" 给你发送了一条消息: %s",
                                        sendTime, sendAccount, messageContent));
                            }
                        }

                    } catch (IOException e) {
                        Log.e(TAG,Log.getStackTraceString(e));
                        Log.e(TAG, "监听消息是连接中断，请检查网络连接！");
                        //执行重新连接
                        initMessageSocket();
                    }
                } else {
                    Log.e(TAG, "Socket对象为空！");
                }
            }
        }

        /*发送消息方法
         *
         *通过Intent供外部调用
         */
        private void sendMessage(String message) {
            new sendMessThread(message).start();
        }

        /*发送消息线程

         */
        class sendMessThread extends Thread {
            String message;

            private sendMessThread(String message) {
                this.message = message;
            }

            @Override
            public void run() {

                //等待socket连接
                while(!socket.isConnected()){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG,Log.getStackTraceString(e));
                    }
                }
                super.run();
                Log.e(TAG, "sendMessage");
                //写操作
                OutputStream os = null;

                try {
                    os = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG,Log.getStackTraceString(e));
                }
                try {
                    assert os != null;
                    os.write(("" + message).getBytes());
                    os.flush();
                } catch (IOException e) {
                    Log.e(TAG,Log.getStackTraceString(e));
                    //检查是否socket连接中断
                    if(!socket.isConnected()){
                        //等待重连
                        Toast.makeText(MessageService.this, "网络连接中断，等待重连...", Toast.LENGTH_SHORT).show();
                        while(!socket.isConnected()){
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e1) {
                                Log.e(TAG,Log.getStackTraceString(e1));
                            }
                        }
                        //重新发送消息
                        sendMessage(message);
                    }
                }
            }
        }

        /*发送心跳包线程

         */
        class sendHeartBeatThread extends Thread {
            @Override
            public void run() {

                //等待socket连接
                while(!socket.isConnected()){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG,Log.getStackTraceString(e));
                    }
                }
                super.run();
                OutputStream os = null;
                try {
                    while (isPrime) {
                        os = socket.getOutputStream();
                        os.write(("-").getBytes());
                        os.flush();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e(TAG,Log.getStackTraceString(e));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    assert os != null;
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG,Log.getStackTraceString(e));
                }
            }
        }
    }

    /*消息回调接口

     */
    public interface MessageUpdateUiInterface{
        void updateUI(Message message);
    }

    public static void setUpdateUI(MessageUpdateUiInterface updateUIInterface){
        messageUpdateUiInterface = updateUIInterface;
    }

}
