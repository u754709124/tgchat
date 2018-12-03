package com.tgchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adapter.FriendsListAdapter;
import com.utils.ImageUtils;
import com.utils.NetRequestUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tgchat.WelcomeActivity.friendsInfo;
import static com.tgchat.WelcomeActivity.userInfo;

public class FriendsFragment extends Fragment{
    //定义好友账号列表
    public static ArrayList<String> friendsAccountList;
    //定义ListView
    private ListView listView;
    //定义一个adapter
    private FriendsListAdapter friendsListAdapter;
    //定义一个广播接收器
    private BroadcastReceiver broadcastReceiver;
    //定义一个handler
    private final FriendsHandler friendsHandler = new FriendsHandler();
    //定义一个NetRequestUtils
    private NetRequestUtils netRequestUtils;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_friends, container, false);
        //创建好友的账号列表
        friendsAccountList = new ArrayList<>();
        //获取ListView
        listView = contentView.findViewById(R.id.friends_list);
        //为ListView绑定adapter
        friendsListAdapter = new FriendsListAdapter(getActivity());
        listView.setAdapter(friendsListAdapter);
        //初始化NetRequestUtils
        netRequestUtils = new NetRequestUtils(getActivity());
        //初始化广播接收器
        initFriendsBroadcastReceiver();
        //初始化好友账号列表
        initFriendsAccountList();
        //初始化联系人点击事件
        initFriendsClickListener();

        return contentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //解除注册广播监听器
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    //初始化好友账号列表
    private void initFriendsAccountList() {
        netRequestUtils.queryFriends(userInfo.get("userName"));
    }

    //监听点击联系人事件
    private void initFriendsClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击消息对应的用户名
                String selectedAccount = friendsAccountList.get(position);
                String nickName;
                if (friendsInfo.get(selectedAccount) != null) {
                    HashMap<String, String> getInfo = friendsInfo.get(selectedAccount);
                    assert getInfo != null;
                    nickName = getInfo.get("nickName");
                } else {
                    nickName = "";
                }
                //创建一个新Intent启动聊天界面
                Intent intent = new Intent();
                intent.setClass(getActivity(), ChatActivity.class);
                intent.putExtra("selectedAccount", selectedAccount);
                intent.putExtra("nickName", nickName);
                startActivity(intent);
            }
        });
    }

    //初始化广播接收器
    private void initFriendsBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter("com.services.Query");
        broadcastReceiver = new FriendsBroadcastReceiver();
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    //创建一个广播监听类
    private class FriendsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("Type");
            String status = intent.getStringExtra("Query");
            //加载完好友账号
            if (type.equals("3")) {
                if (status.equals("success")) {
                    Message message = new Message();
                    message.what = 1;
                    friendsHandler.sendMessage(message);
                }
            }
            //加载完好友资料信息
            else if (type.equals("1")) {
                Message message = new Message();
                message.what = 2;
                friendsHandler.sendMessage(message);
            }
        }
    }

    //新建一个Handler
    @SuppressLint("HandlerLeak")
    private class FriendsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //加载完好友账号
            if (msg.what == 1) {
                for (String account : friendsAccountList) {
                    netRequestUtils.requestUserInfo(account, 1);
                }
                //加载完好友信息
            } else if (msg.what == 2) {
                friendsListAdapter.notifyDataSetChanged();
                for (String account : friendsAccountList) {
                    HashMap<String, String> info = friendsInfo.get(account);
                    while (info == null) {
                        info = friendsInfo.get(account);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String imageFileName = info.get("headImage");
                    ImageUtils imageUtils = new ImageUtils(getActivity());
                    imageUtils.downloadImage(imageFileName, friendsHandler, 3);
                }
                //头像图片加载完成
            } else if (msg.what == 3) {
                //通知messageListAdapter数据改变
                friendsListAdapter.notifyDataSetChanged();
            }
        }
    }

}
