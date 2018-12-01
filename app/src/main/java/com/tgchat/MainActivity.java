package com.tgchat;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.services.MessageService;
import com.utils.ChatRecordUtils;
import com.utils.RoundImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.tgchat.WelcomeActivity.userInfo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MessageFragment.CallBackListener {
    //创建一个消息列表
    public static ArrayList<Message> messageList = new ArrayList<>();

    //底部栏按钮
    private View messageBtn;
    private View friendsBtn;
    private View settingBtn;

    //底部栏图像
    private ImageView messageImage;
    private ImageView friendsImage;
    private ImageView settingImage;

    //底部栏文字
    private TextView messageText;
    private TextView friendsText;
    private TextView settingText;

    //顶部栏文字
    private TextView topTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //提示欢迎词
        Toast.makeText(this, userInfo.get("nickName") + "，欢迎回来！", Toast.LENGTH_SHORT).show();

        //创建后台消息服务
        String username = userInfo.get("userName");
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra("requestType", "requestLogin");
        serviceIntent.putExtra("requestContent", username);

        serviceIntent.setClass(MainActivity.this, MessageService.class);

        startService(serviceIntent);


        super.onCreate(savedInstanceState);
        Log.e("test", "MainActivity创建！");
        setContentView(R.layout.activity_main);
        //全屏界面
        hideNavigationBar();

        //初始化界面
        initViews();
    }

    //activity销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清空messageList
        messageList.clear();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message_btn:
                setSelectedStatus(0);
                break;
            case R.id.friends_btn:
                setSelectedStatus(1);
                break;
            case R.id.setting_btn:
                setSelectedStatus(2);
                break;
        }
    }

    //初始化所有view
    public void initViews() {
        //初始化底部栏按钮
        messageBtn = findViewById(R.id.message_btn);
        friendsBtn = findViewById(R.id.friends_btn);
        settingBtn = findViewById(R.id.setting_btn);

        //初始化底部栏图像
        messageImage = findViewById(R.id.message_image);
        friendsImage = findViewById(R.id.friends_image);
        settingImage = findViewById(R.id.setting_image);

        //初始化底部栏文字
        messageText = findViewById(R.id.message_text);
        friendsText = findViewById(R.id.friends_text);
        settingText = findViewById(R.id.setting_text);

        //初始化顶部栏标题
        topTitle = findViewById(R.id.topTitle);

        //底部栏按钮设置监听
        messageBtn.setOnClickListener(this);
        friendsBtn.setOnClickListener(this);
        settingBtn.setOnClickListener(this);

        //默认显示为 消息列表 页面
        setSelectedStatus(0);
    }


    //底部栏按钮点击->设置选中状态
    public void setSelectedStatus(int index){
        //定义Drawable图片
        Drawable messageSelectedDrawable = getResources().getDrawable(R.mipmap.message_selected);
        Drawable messageUnselectedDrawable = getResources().getDrawable(R.mipmap.message_unselected);
        Drawable friendsSelectedDrawable = getResources().getDrawable(R.mipmap.friends_selected);
        Drawable friendsUnselectedDrawable = getResources().getDrawable(R.mipmap.friends_unselected);
        Drawable settingSelectedDrawable = getResources().getDrawable(R.mipmap.setting_selected);
        Drawable settingUnselectedDrawable = getResources().getDrawable(R.mipmap.setting_unselected);

        switch (index){
            case 0:
                messageImage.setImageDrawable(messageSelectedDrawable);
                messageText.setTextColor(Color.parseColor("#47bafd"));

                friendsImage.setImageDrawable(friendsUnselectedDrawable);
                friendsText.setTextColor(Color.parseColor("#8a8a8a"));

                settingImage.setImageDrawable(settingUnselectedDrawable);
                settingText.setTextColor(Color.parseColor("#8a8a8a"));

                topTitle.setText("消息列表");
                createFragment(new MessageFragment());

                break;

            case 1:
                messageImage.setImageDrawable(messageUnselectedDrawable);
                messageText.setTextColor(Color.parseColor("#8a8a8a"));

                friendsImage.setImageDrawable(friendsSelectedDrawable);
                friendsText.setTextColor(Color.parseColor("#47bafd"));

                settingImage.setImageDrawable(settingUnselectedDrawable);
                settingText.setTextColor(Color.parseColor("#8a8a8a"));

                topTitle.setText("联系人");
                createFragment(new FriendsFragment());

                break;

            case 2:
                messageImage.setImageDrawable(messageUnselectedDrawable);
                messageText.setTextColor(Color.parseColor("#8a8a8a"));

                friendsImage.setImageDrawable(friendsUnselectedDrawable);
                friendsText.setTextColor(Color.parseColor("#8a8a8a"));

                settingImage.setImageDrawable(settingSelectedDrawable);
                settingText.setTextColor(Color.parseColor("#47bafd"));

                topTitle.setText("设置界面");
                createFragment(new SettingFragment());

                break;
        }


    }

    //创建fragment
    public void createFragment(android.app.Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_body, fragment);
        transaction.commit();
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

    @Override
    public void setImage(String path) {
        RoundImageView chatHeadImage = findViewById(R.id.chat_head_image);
        chatHeadImage.setImageURI((Uri.fromFile(new File(path))));
    }

    //消息对象
    public static class Message{
        //发送人账号
        private String sendAccount;
        //接收人账号
        private String revAccount;
        //发送人昵称
        private String nickName;
        //头像地址
        private String headImageUrl;
        //发送内容
        private String messageContent;
        //发送时间
        private Long sendTime;
        //重复计数
        private Integer count;

        public Message(String sendAccount, String revAccount, String nickName, String headImageUrl, String messageContent, Long sendTime) {
            this.sendAccount = sendAccount;
            this.revAccount = revAccount;
            this.nickName = nickName;
            this.headImageUrl = headImageUrl;
            this.messageContent = messageContent;
            this.sendTime = sendTime;
            this.count = 1;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "nickName='" + nickName + '\'' +
                    ", messageContent='" + messageContent + '\'' +
                    '}';
        }

        public String getSendAccount() {
            return sendAccount;
        }

        public String getNickName() {
            return nickName;
        }

        public String getHeadImageUrl() {
            return headImageUrl;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public Long getSendTime() {
            return sendTime;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getRevAccount() {
            return revAccount;
        }

        public void setSendAccount(String sendAccount) {
            this.sendAccount = sendAccount;
        }

        public void setRevAccount(String revAccount) {
            this.revAccount = revAccount;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public void setHeadImageUrl(String headImageUrl) {
            this.headImageUrl = headImageUrl;
        }
    }

}
