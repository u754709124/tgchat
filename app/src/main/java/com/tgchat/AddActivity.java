package com.tgchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.adapter.AddListAdapter;
import com.utils.ImageUtils;
import com.utils.NetRequestUtils;

import java.util.ArrayList;

import static com.tgchat.WelcomeActivity.userInfo;

public class AddActivity extends AppCompatActivity {
    //引用一个广播接收器
    private BroadcastReceiver broadcastReceiver;
    //引用一个NetRequestUtils
    private NetRequestUtils netRequestUtils;
    //新建一个查询到的用户的列表
    public static ArrayList<UserQueryInfo> userQueryInfoList;
    //定义一个ListView
    private ListView query_result_list;
    //定义一个adapter
    AddListAdapter addListAdapter;

    //新建一个AddHandler
    @SuppressLint("HandlerLeak")
    public final Handler addHandler = new AddHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        //初始化查询到的用户列表
        userQueryInfoList = new ArrayList<>();

        //创建一个NetRequestUtils
        netRequestUtils = new NetRequestUtils(this);
        //创建一个AddListAdapter
        addListAdapter = new AddListAdapter(this);
        //为ListView绑定一个Adapter
        query_result_list = findViewById(R.id.query_result_list);
        query_result_list.setAdapter(addListAdapter);

        //全屏界面
        hideNavigationBar();

        //初始化广播监听器
        initQueryBroadcastReceiver();
        //监听查询按钮点击事件
        initQueryBtnOnClickListener();
        //返回按钮点击监听
        initReturnBtnOnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册广播监听器
        unregisterReceiver(broadcastReceiver);
        //清空查询用户列表
        userQueryInfoList.clear();
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

    //返回按钮点击监听
    private void initReturnBtnOnClickListener() {
        ImageView returnBtn = findViewById(R.id.add_return);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //查询按钮监听
    private void initQueryBtnOnClickListener() {
        final Button queryBtn = findViewById(R.id.submit_query);
        final EditText queryInput = findViewById(R.id.query_number);
        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userQueryInfoList.clear();
                String queryNumber = queryInput.getText().toString().trim();
                queryInput.setText("");
                if (queryNumber.equals(userInfo.get("userName"))) {
                    Toast.makeText(AddActivity.this, "这是你自己呀！", Toast.LENGTH_SHORT).show();
                } else {
                    netRequestUtils.requestUserInfo(queryNumber, 2);
                }
            }
        });
    }

    //新建一个广播监听器
    private class QueryBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("Type");
            String status = intent.getStringExtra("Query");
            if (type.equals("2")) {
                if (status.equals("success")) {
                    Message message = new Message();
                    message.what = 1;
                    addHandler.sendMessage(message);
                } else {
                    Toast.makeText(context, "未查询到该用户信息！", Toast.LENGTH_SHORT).show();
                    Log.e("test", "AddActivity 查询用户信息失败!");
                }
            }
        }
    }

    //初始化广播接收器
    private void initQueryBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter("com.services.Query");
        broadcastReceiver = new QueryBroadcastReceiver();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    //创建一个UIHandler类
    @SuppressLint("HandlerLeak")
    private class AddHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //查询用户成功
            if (msg.what == 1) {
                //通知addListAdapter更新界面
                addListAdapter.notifyDataSetChanged();
                //开始下载头像图片
                ImageUtils imageUtils = new ImageUtils(AddActivity.this);
                for (UserQueryInfo userQueryInfo : userQueryInfoList) {
                    String headImageFileName = userQueryInfo.getHeadImageFileName();
                    imageUtils.downloadImage(headImageFileName, addHandler, 2);
                }
            }
            //下载图片成功
            if (msg.what == 2) {
                //通知addListAdapter更新界面
                addListAdapter.notifyDataSetChanged();
            }
        }
    }

    //查询到的用户信息对象
    public static class UserQueryInfo {

        private String account;
        private String nickname;
        private String headImageFileName;

        public UserQueryInfo(String account, String nickname, String headImageFileName) {
            this.account = account;
            this.headImageFileName = headImageFileName;
            this.nickname = nickname;
        }

        public String getAccount() {
            return account;
        }

        public String getNickname() {
            return nickname;
        }

        public String getHeadImageFileName() {
            return headImageFileName;
        }
    }
}
