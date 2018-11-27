package com.tgchat;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.services.MessageService;
import com.utils.ChatRecordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //创建一个用户信息
    public static HashMap<String, String> userInfo = new HashMap<>();
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

    private ChatRecordUtils chatRecordUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //创建CharRecordUtils实例
        chatRecordUtils = new ChatRecordUtils(this);

        Intent loginIntent = getIntent();
        //判断Intent对象是否含有传入的参数
        if (loginIntent.hasExtra("userInfo")) {
            @SuppressWarnings("unchecked")
            //获取传入的用户信息
                    HashMap<String, String> info = (HashMap<String, String>) loginIntent.getSerializableExtra("userInfo");
            userInfo = info;
            String msg = "账号为：" + userInfo.get("userName") + "，昵称为：" + userInfo.get("nickName");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

        //判断userInfo是否为空，空则返回登录界面
        if (userInfo.isEmpty()) {
            jumpToLogin();
        }
        else{
            //创建后台消息服务
            String username = userInfo.get("userName");
            Intent serviceIntent = new Intent();
            serviceIntent.putExtra("requestType", "requestLogin");
            serviceIntent.putExtra("requestContent", username);

            serviceIntent.setClass(MainActivity.this, MessageService.class);

            startService(serviceIntent);
            //初始化消息界面
            initMessageList();
        }

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

    //初始化messageList
    public void initMessageList() {
        /*创建本地的数据库记录聊天记录,包括Message对象中的所有属性，
        添加isRead字段(true, false):判断消息是否已读,默认为false;
        添加isListVisible字段(true, false):判断是否被用户划出消息列表，默认为false；
            ->根据时间对消息进行排序，发消息的人进行过滤，用户划出则对该人所有消息设置 已读 和 被划出;
                ->即isRead设置为true， isListVisible设置为true;
         */

        //读取数据库中的聊天记录
        boolean status = chatRecordUtils.queryUnRemovedData(userInfo.get("userName"));
        Log.d("test", "查询消息界面数据" + status);

        //判断消息列表长度是否为0
        if(messageList.size() != 0){
            removeSimilarObject();
        }

    }

    //列表去重
    public static void removeSimilarObject() {
        //对列表进行升序排列
        sortByDate(true);
        //列表去重
        int indexCount = 0;
        HashMap<String, String> hashMap = new HashMap<>();
        ArrayList<Integer> positionList = new ArrayList<>();
        for (Message message : messageList) {
            String sendAccount = message.getSendAccount();
            if (hashMap.get(sendAccount) != null) {
                String mixArgs = hashMap.get(sendAccount);
                Integer position = Integer.valueOf(mixArgs.split("@")[0]);
                Integer sameCount = Integer.valueOf(mixArgs.split("@")[1]);
                positionList.add(position);
                hashMap.remove(sendAccount);
                hashMap.put(sendAccount, "" + indexCount + "@" + (message.getCount()+sameCount));
                message.setCount(message.getCount()+sameCount);

            } else {
                hashMap.put(sendAccount, "" + indexCount + "@" + message.getCount());
            }
            indexCount++;
        }

        int elementRemoveCount = 0;
        for (int position : positionList) {
            messageList.remove(position - elementRemoveCount);
            elementRemoveCount++;
        }
        //对列表进行降序排列
        sortByDate(true);

    }

    //对数组进行升序或降序排列
    public static void sortByDate(final boolean type) {
        //type为true升序排列，否则为降序排列
        Collections.sort(messageList, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                Long time1 = o1.getSendTime();
                Long time2 = o2.getSendTime();

                if(time1.equals(time2)) return 0;

                if(type) {
                    return time1>time2 ? 1:-1;
                }
                else{
                    return time1>time2 ? -1:1;
                }
            }
        });

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

    //跳转到Login界面
    public void jumpToLogin(){
        Intent intent = new Intent();
        //跳转时销毁本页面
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(MainActivity.this ,LoginActivity.class);
        startActivity(intent);
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

    //消息对象
    public static class Message{
        //发送人账号
        private String sendAccount;
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

        public Message(String sendAccount, String nickName, String headImageUrl, String messageContent, Long sendTime){
            this.sendAccount = sendAccount;
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
    }

}
