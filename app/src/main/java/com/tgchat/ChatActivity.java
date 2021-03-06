package com.tgchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.adapter.ChatListAdapter;
import com.services.MessageService;
import com.utils.ChatRecordUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.tgchat.MainActivity.messageList;
import static com.tgchat.WelcomeActivity.userInfo;

public class ChatActivity extends AppCompatActivity {
    //定义控件
    TextView nickNameText;
    Button sendBtn;
    ImageView returnToList;
    EditText inputField;
    View topBar;
    View inputBar;
    ListView chatEachList;

    //定义ListView的adapter
    ChatListAdapter chatListAdapter;

    //定义ChatRecordUtils
    ChatRecordUtils chatRecordUtils;

    //定义对方账号
    String hisAccount;

    //定义聊天记录列表
    public static ArrayList<ChatMessage> chatMessageArrayList;

    //定义接收器
    BroadcastReceiver broadcastReceiver;

    //定义ListView滚动监听器的计数器
    int scrollCount = 0;


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
        String nickName = intent.getStringExtra("nickName");

        hisAccount = selectedAccount;
        //页面titile显示的文字
        String displayTitle;

        if (nickName.equals("")) {
            displayTitle = hisAccount;
        } else {
            displayTitle = nickName;
        }
        nickNameText.setText(displayTitle);

        //返回按钮点击事件
        returnToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        //右滑事件监听

        //新建ChatRecordUtils实例
        chatRecordUtils = new ChatRecordUtils(this);

        //创建聊天记录列表
        chatMessageArrayList = new ArrayList<>();

        //初始化聊天记录列表
        initChatMessageArrayList();

        //设置ListView的adapter
        setListViewAdapter();

        //设置发送按钮监听
        setSendBtnOnClickListen();

        //注册监听接收到消息的广播
        registerBroadcastReceiver();

        //监听ListView中消息条数，更改显示方式
        setScrollListener();

        //监听输入框是否有内容
        setInputDataChangeLister();

    }

    @Override
    protected void onDestroy() {
        //解除注册监听广播
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
        //关闭数据库连接
        chatRecordUtils.sqLiteDatabase.close();
    }

    //监听ListView中消息条数，更改显示方式
    public void setScrollListener() {
        chatEachList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //没有一次显示完
                if (visibleItemCount < totalItemCount) {
                    //滚动只需要执行一次
                    if (scrollCount == 0) {
                        chatEachList.setSelection(chatEachList.getCount() - 1);
                        scrollCount++;
                    }
                }
            }
        });
    }

    //监听输入框是否有内容
    private void setInputDataChangeLister() {
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputText = inputField.getText().toString().trim();

                //输入框输入不为空格
                if (!inputText.equals("")) {
                    //设置发送按钮颜色和文字颜色
                    sendBtn.setBackground(getDrawable(R.drawable.send_clickable_btn_shape));
                    sendBtn.setTextColor(Color.rgb(255, 255, 255));
                    //设置按钮可以点击
                    sendBtn.setEnabled(true);
                } else {
                    //设置发送按钮颜色和文字颜色
                    sendBtn.setBackground(getDrawable(R.drawable.send_unclickable_btn_shape));
                    sendBtn.setTextColor(Color.rgb(241, 242, 244));
                    //设置按钮不可以点击
                    sendBtn.setEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //设置ListView监听
    public void setListViewAdapter() {
        chatListAdapter = new ChatListAdapter(this);
        chatEachList.setAdapter(chatListAdapter);

    }

    //初始化聊天记录列表
    private void initChatMessageArrayList() {

        //查询与此人的所有聊天记录
        chatRecordUtils.queryAllChatRecord(hisAccount, userInfo.get("userName"));
        chatRecordUtils.queryAllChatRecord(userInfo.get("userName"), hisAccount);

        //设置为已读
        chatRecordUtils.setIsRead(hisAccount, userInfo.get("userName"));

        //对聊天记录列表按升序排序
        Collections.sort(chatMessageArrayList, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                Long time1 = o1.getSendTime();
                Long time2 = o2.getSendTime();
                if (time1.equals(time2)) {
                    return 0;
                } else {
                    return time1 > time2 ? 1 : -1;
                }
            }
        });
    }

    //初始化view
    private void initView() {
        nickNameText = findViewById(R.id.nickname_text);
        sendBtn = findViewById(R.id.send_btn);
        returnToList = findViewById(R.id.chat_head_image);
        inputField = findViewById(R.id.input_field);
        topBar = findViewById(R.id.top_bar);
        inputBar = findViewById(R.id.input_bar);
        chatEachList = findViewById(R.id.chat_each_list);
    }

    //设置发送按钮监听, 发送消息
    private void setSendBtnOnClickListen() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入
                String sendContent = inputField.getText().toString();
                // 清空输入框
                inputField.setText("");
                //关闭软键盘
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //替换输入中的@为/a3/t4
                String messageContent = sendContent.replaceAll("@", "/a3/t4");
                //拼接字符串
                String sendNeedContent = hisAccount + "@" + messageContent;

                //调用服务进行发送
                Intent serviceIntent = new Intent();
                serviceIntent.setClass(ChatActivity.this, MessageService.class);
                serviceIntent.putExtra("requestType", "requestSendContent");
                serviceIntent.putExtra("requestContent", sendNeedContent);
                startService(serviceIntent);

                //获取当前毫秒时间戳
                Long currentMillis = System.currentTimeMillis();

                //将发送的记录添加进数据库
                chatRecordUtils.saveChatRecord(WelcomeActivity.userInfo.get("userName"), hisAccount, sendContent, currentMillis);
                //设置为已读
                chatRecordUtils.setIsRead(WelcomeActivity.userInfo.get("userName"), hisAccount);

                //初始化聊天列表
                chatMessageArrayList.clear();
                initChatMessageArrayList();

                //通知adapter数据改变
                chatListAdapter.notifyDataSetChanged();

            }
        });
    }

    //聊天记录对象
    public static class ChatMessage {
        private String sendAccount;
        private String revAccount;
        private String messageContent;
        private String headImageUrl;
        private Long sendTime;

        public ChatMessage(String sendAccount, String revAccount, String messageContent, String headImageUrl, Long sendTime) {

            this.sendAccount = sendAccount;
            this.revAccount = revAccount;
            this.headImageUrl = headImageUrl;
            this.messageContent = messageContent;
            this.sendTime = sendTime;
        }

        public String getSendAccount() {
            return sendAccount;
        }

        public String getRevAccount() {
            return revAccount;
        }

        public String getHeadImageUrl() {
            return headImageUrl;
        }

        public Long getSendTime() {
            return sendTime;
        }

        public String getMessageContent() {
            return messageContent;
        }
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

    //注册监听接收到消息的广播
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.services.receiveMessage");
        broadcastReceiver = new MessageChangeReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    //新建广播接收
    public class MessageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取请求
            String requestType = intent.getStringExtra("requestType");
            if (requestType.equals("receiveMessage")) {
                //清空列表
                chatMessageArrayList.clear();
                //初始化消息列表
                initChatMessageArrayList();
                //通知adapter
                chatListAdapter.notifyDataSetChanged();

            }
        }
    }

}
