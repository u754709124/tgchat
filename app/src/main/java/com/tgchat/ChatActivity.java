package com.tgchat;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.utils.SoftKeyBoardListener;

public class ChatActivity extends AppCompatActivity {
    TextView nickNameText;
    Button sendBtn;
    ImageView returnToList;
    EditText inputField;
    View topBar;
    View inputBar;
    View chatEachList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //全屏界面
        hideNavigationBar();

        //初始化view
        initView();

        //获取Intent传入参数
        Intent intent = getIntent();
        String selectedAccount = intent.getStringExtra("selectedAccount");
        nickNameText.setText(selectedAccount);

        //返回按钮点击事件
        returnToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });


        //右滑事件监听

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    //初始化view
    private void initView() {
        nickNameText = findViewById(R.id.nickname_text);
        sendBtn = findViewById(R.id.send_btn);
        returnToList = findViewById(R.id.return_btn);
        inputField = findViewById(R.id.input_field);
        topBar = findViewById(R.id.top_bar);
        inputBar = findViewById(R.id.input_bar);
        chatEachList = findViewById(R.id.chat_each_list);
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
}
