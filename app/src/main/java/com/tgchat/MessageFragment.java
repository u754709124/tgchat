package com.tgchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.adapter.MessageListAdapter;
import com.services.MessageService;
import com.utils.ChatRecordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.tgchat.MainActivity.messageList;
import static com.tgchat.MainActivity.userInfo;

public class MessageFragment extends Fragment implements MessageService.MessageUpdateUiInterface {
    protected Activity mActivity;
    public static MessageListAdapter messageListAdapter;
    private ChatRecordUtils chatRecordUtils;
    private int resumeCount = 0;

    //创建消息处理Handler个更新UI
    @SuppressLint("HandlerLeak")
    private final Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //输出提示消息
            if(msg.what == 0){
                String tips = (String)msg.obj;
                Toast.makeText(mActivity.getApplicationContext(), tips, Toast.LENGTH_SHORT).show();
            }
            //输出MainActivity.Message
            else if(msg.what == 1){
                MainActivity.Message messageInfo = (MainActivity.Message)msg.obj;
                messageList.add(messageInfo);
                removeSimilarObject();
                //通知adapter更新界面
                messageListAdapter.notifyDataSetChanged();

                //设置震动提醒
                Vibrator vibrator = (Vibrator)mActivity.getSystemService(Service.VIBRATOR_SERVICE);
                long[] patter = {0, 230, 220, 70};
                vibrator.vibrate(patter, -1);
            }

            super.handleMessage(msg);
        }
    };


    @Override
    public void onResume() {
        if (resumeCount != 0) {
            messageList.clear();
            initMessageList();
            messageListAdapter.notifyDataSetChanged();
        }
        resumeCount++;
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.fragment_message, container, false);
        //初始化ListView
        final ListView messageList = contentView.findViewById(R.id.message_list);

        //实例化ChatRecordUtil对象
        chatRecordUtils = new ChatRecordUtils(mActivity);

        //初始化消息列表
        initMessageList();

        //绑定adapter
        messageListAdapter = new MessageListAdapter();
        messageList.setAdapter(messageListAdapter);

        //为ListView绑定长按点击事件
        messageList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //创建 弹出式窗口
                onItemLongClicked(view, position);
                return false;
            }
        });
        //点击消息跳转聊天页面
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击消息对应的用户名
                MainActivity.Message info = MainActivity.messageList.get(position);
                String selectedAccount = info.getSendAccount();

                //设置消息为已读
                chatRecordUtils.setIsRead(selectedAccount, userInfo.get("userName"));
                chatRecordUtils.setIsRead(userInfo.get("userName"), selectedAccount);

                //创建一个新Intent启动聊天界面
                Intent intent = new Intent();
                intent.setClass(mActivity, ChatActivity.class);
                intent.putExtra("selectedAccount", selectedAccount);
                startActivity(intent);
            }
        });

        //设置消息回调接口
        MessageService.setUpdateUI(this);

        return contentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    //消息长按调出菜单
    private void onItemLongClicked(View view, final int position)
    {
        PopViewMenu menu = new PopViewMenu();
        menu.addMenuItem("删除", "remove");
        menu.addMenuItem("更多", "more");

        //menu.show(this, view, 0, 0);
        menu.show(mActivity, view, view.getWidth()/2, -view.getHeight()/2);

        menu.listener = new PopViewMenu.OnMenuItemClickedListener()
        {
            @Override
            public void onMenuItemClicked(String option, String value)
            {
                Log.d("test", "点中了菜单项: " + value);
                if(value.equals("remove")){
                    //获取当前选择的Message对象
                    MainActivity.Message currentMessage = messageList.get(position);
                    String sendAccount = currentMessage.getSendAccount();
                    if(!userInfo.isEmpty()){
                        String revAccount = userInfo.get("userName");
                        //在数据库中将此条消息标注为IsRemoved
                        chatRecordUtils.setIsRemoved(sendAccount, revAccount);
                        chatRecordUtils.setIsRemoved(revAccount, sendAccount);
                    }

                    messageList.remove(position);

                    messageListAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void updateUI(Message message) {
        mUIHandler.sendMessage(message);
    }

    //初始化messageList
    public void initMessageList() {
        /*创建本地的数据库记录聊天记录,包括Message对象中的所有属性，
        添加isRead字段(true, false):判断消息是否已读,默认为false;
        添加isListVisible字段(true, false):判断是否被用户划出消息列表，默认为false；
            ->根据时间对消息进行排序，发消息的人进行过滤，用户划出则对该人所有消息设置 已读 和 被划出;
                ->即isRead设置为true， isListVisible设置为true;
         */

        if (!userInfo.isEmpty()) {
            //读取数据库中的聊天记录
            chatRecordUtils.queryUnRemovedData(userInfo.get("userName"), false);
            chatRecordUtils.queryUnRemovedData(userInfo.get("userName"), true);

            //判断消息列表长度是否为0
            if (messageList.size() != 0) {
                removeSimilarObject();
            }
        }
    }

    //列表去重
    public void removeSimilarObject() {
        //对列表进行升序排列
        sortByDate(true);
        //列表去重
        int indexCount = 0;
        HashMap<String, String> hashMap = new HashMap<>();
        ArrayList<Integer> positionList = new ArrayList<>();
        for (MainActivity.Message message : messageList) {
            String sendAccount = message.getSendAccount();
            String revAccount = message.getRevAccount();
            //判断发送人是否为自己
            if (sendAccount.equals(userInfo.get("userName"))) {
                String temp = sendAccount;
                sendAccount = revAccount;
                revAccount = temp;
            }

            if (hashMap.get(sendAccount) != null) {
                String mixArgs = hashMap.get(sendAccount);
                Integer position = Integer.valueOf(mixArgs.split("@")[0]);
                Integer sameCount = Integer.valueOf(mixArgs.split("@")[1]);
                positionList.add(position);
                hashMap.remove(sendAccount);
                hashMap.put(sendAccount, "" + indexCount + "@" + (message.getCount() + sameCount));
                message.setSendAccount(sendAccount);
                message.setRevAccount(revAccount);
                message.setCount(message.getCount() + sameCount);

            } else {
                hashMap.put(sendAccount, "" + indexCount + "@" + message.getCount());
            }
            indexCount++;
        }

        //对positionList进行排序
        Collections.sort(positionList);
        int elementRemoveCount = 0;
        for (int position : positionList) {
            messageList.remove(position - elementRemoveCount);
            elementRemoveCount++;
        }
        //对列表进行降序排列
        sortByDate(false);

    }

    //对数组进行升序或降序排列
    public static void sortByDate(final boolean type) {
        //type为true升序排列，否则为降序排列
        Collections.sort(messageList, new Comparator<MainActivity.Message>() {
            @Override
            public int compare(MainActivity.Message o1, MainActivity.Message o2) {
                Long time1 = o1.getSendTime();
                Long time2 = o2.getSendTime();

                if (time1.equals(time2)) return 0;

                if (type) {
                    return time1 > time2 ? 1 : -1;
                } else {
                    return time1 > time2 ? -1 : 1;
                }
            }
        });

    }
}
