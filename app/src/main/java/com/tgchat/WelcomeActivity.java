package com.tgchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.utils.UserInfoUtils;

import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity {
    //创建一个用户信息
    public static HashMap<String, String> userInfo;
    //定义一个好友相关的信息表
    public static HashMap<String, HashMap<String, String>> friendsInfo;
    //定义接收器
    BroadcastReceiver broadcastReceiver;
    //定义一个UserInfoUtils对象
    UserInfoUtils userInfoUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //全屏界面
        hideNavigationBar();
        //初始化个人信息表
        userInfo = new HashMap<>();
        //初始化好友信息表
        friendsInfo = new HashMap<>();
        //创建一个UserInfoUtils对象
        userInfoUtils = new UserInfoUtils(this);
        //注册监听接收到消息的广播
        registerBroadcastReceiver();
        //判断是否有记住密码账号，以广播形式返回
        userInfoUtils.queryLoginUser();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除注册监听广播
        unregisterReceiver(broadcastReceiver);
    }

    //注册监听接收到消息的广播
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.services.Login");
        broadcastReceiver = new MessageChangeReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    //新建广播接收
    public class MessageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取请求
            String requestType = intent.getStringExtra("Login");
            String errorMsg = intent.getStringExtra("errorMsg");
            String username = intent.getStringExtra("username");
            if (requestType.equals("success")) {
                jumpToMainActivity();
            } else {
                if (errorMsg.equals("Login failed")) {
                    userInfoUtils.deleteUser(username);
                }
                jumpToLogin();
            }
        }
    }

    //跳转到Login界面
    public void jumpToLogin() {
        //暂停3秒
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.e("test", "Exception occured at Line 60<WelcomeActivity"
                            + "\n" + Log.getStackTraceString(e));
                }
                Intent intent = new Intent();
                //跳转时销毁本页面
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(WelcomeActivity.this, LoginActivity.class);
                //跳转到登录页面
                startActivity(intent);
            }
        }).start();

    }

    //跳转到主页面
    public void jumpToMainActivity() {
        Intent intent1 = new Intent(WelcomeActivity.this, MainActivity.class);
        //跳转时销毁本页面
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
    }

    //全屏界面
    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}
