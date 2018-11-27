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

import static com.tgchat.MainActivity.messageList;
import static com.tgchat.MainActivity.userInfo;

public class MessageFragment extends Fragment implements MessageService.MessageUpdateUiInterface {
    protected Activity mActivity;
    public static MessageListAdapter messageListAdapter;
    private ChatRecordUtils chatRecordUtils;

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
                MainActivity.removeSimilarObject();
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
        messageListAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.fragment_message, container, false);
        //初始化ListView
        final ListView messageList = contentView.findViewById(R.id.message_list);
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
                //创建一个新Intent启动聊天界面
                Intent intent = new Intent();
                intent.setClass(mActivity, ChatActivity.class);
                intent.putExtra("selectedAccount", selectedAccount);
                startActivity(intent);
            }
        });

        //设置消息回调接口
        MessageService.setUpdateUI(this);

        //实例化ChatRecordUtil对象
        chatRecordUtils = new ChatRecordUtils(mActivity);

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
}
