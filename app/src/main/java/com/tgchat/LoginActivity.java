package com.tgchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.utils.EncryptUtils;
import com.utils.NetRequestUtils;
import com.utils.UserInfoUtils;

import static com.tgchat.WelcomeActivity.userInfo;

public class LoginActivity extends AppCompatActivity {

    //定义一个广播接收器
    BroadcastReceiver broadcastReceiver;
    //定义一个NetRequestUtils
    NetRequestUtils netRequestUtils;
    //定义一个UserInfoUtils
    UserInfoUtils userInfoUtils;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("test", "登录页面销毁");
        //解除注册广播监听
        unregisterReceiver(broadcastReceiver);
        userInfoUtils.sqLiteDatabase.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //界面全屏
        hideNavigationBar();
        //新建UserInfoUtils
        userInfoUtils = new UserInfoUtils(LoginActivity.this);
        //新建NetRequestUtils
        netRequestUtils = new NetRequestUtils(this);
        //注册监听
        registerBroadcastReceiver();

        //初始化发送按钮监听
        initLoginBtnListener();

        //初始化注册按钮监听
        initRegisterBtnListener();
    }

    //全屏界面
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView .setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    //注册按钮监听
    private void initRegisterBtnListener() {
        TextView registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到注册界面
                Intent mIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(mIntent);
            }
        });
    }

    //监听登录按钮
    private void initLoginBtnListener() {
        Button loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    //登录操作
    private void doLogin() {
        //获取输入框中账号
        TextView usernameInput = findViewById(R.id.register_username);
        String username = usernameInput.getText().toString().trim();
        //获取输入框中密码
        TextView passwordInput = findViewById(R.id.passwd);
        String password = passwordInput.getText().toString().trim();

        //判断输入框账号密码是否为空
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            String msg = "账号或者密码不能为空！";
            //显示提示
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            //清空输入框内容
            usernameInput.setText("");
            passwordInput.setText("");
        } else {
            //###发送账号密码到服务器端###
            //服务器返回响应
            //账号密码正确
            String hashValue = EncryptUtils.encryptBySha1(password);
            Log.e("test", "Login界面hashValue:" + hashValue);
            netRequestUtils.requestLogin(username, hashValue);
        }
    }

    //注册监听接收到消息的广播
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.services.Login");
        broadcastReceiver = new MessageChangeReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    //新建广播接收
    private class MessageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取请求
            String requestType = intent.getStringExtra("Login");
            if (requestType.equals("success")) {
                Intent finishLogin = new Intent();
                finishLogin.setClass(LoginActivity.this, MainActivity.class);
                finishLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(finishLogin);
                //保存账户信息
                userInfoUtils.saveUserInfo(userInfo.get("userName"), userInfo.get("password"));
            } else {
                String msg = "账号或者密码错误！";
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
