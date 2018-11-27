package com.tgchat;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    HashMap<String, String> userInfo = new HashMap<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("test", "登录页面销毁");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //界面全屏
        hideNavigationBar();

        //监听登录按钮
        Button loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录成功
                if(doLogin()){

                    Intent finishLogin = new Intent();
                    finishLogin.setClass(LoginActivity.this, MainActivity.class);
                    //Intent添加HashMap userInfo
                    finishLogin.putExtra("userInfo", userInfo);
                    finishLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(finishLogin);
                }
            }
        });

    }

    //全屏界面
    public void hideNavigationBar(){
        View decorView = getWindow().getDecorView();
        decorView .setSystemUiVisibility(

                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    //登录操作
    public boolean doLogin(){
        //获取输入框中账号
        TextView usernameInput = findViewById(R.id.username);
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
            return false;
        }

        //###发送账号密码到服务器端###
        //服务器返回响应
        //账号密码正确
        if(username.equals("123") && password.equals("123")){
            //服务器返回用户信息, 建立socket连接
            //账号 userName 昵称nickName 头像urlImage
            userInfo.put("userName", username);
            userInfo.put("nickName", "The Shy");
            userInfo.put("urlImage", "1.img");
            return true;
        }
        //账号或密码错误,返回false
        else{
            String msg = "账号或者密码错误！";
            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
