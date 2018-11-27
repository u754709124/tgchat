package com.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tgchat.ChatActivity;
import com.tgchat.MainActivity;
import com.tgchat.R;

import static com.tgchat.ChatActivity.chatMessageArrayList;

public class ChatListAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        return chatMessageArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessageArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatActivity.ChatMessage chatMessage = (ChatActivity.ChatMessage) getItem(position);
        String myAccount = MainActivity.userInfo.get("userName");
        String revAccount = chatMessage.getRevAccount();

        //如果接收人是我
        if (revAccount.equals(myAccount)) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.his_message_piece, null);
            //初始化控件
            TextView hisSendTextView = convertView.findViewById(R.id.his_send_text);
            ImageView hisHeadImageView = convertView.findViewById(R.id.his_head_image);

            //设置头像
            //.....................暂略

            //设置聊天记录
            hisSendTextView.setText(chatMessage.getMessageContent());

        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.me_message_piece, null);

            //初始化控件
            TextView meSendTextView = convertView.findViewById(R.id.me_send_text);
            ImageView meHeadImageView = convertView.findViewById(R.id.me_head_image);

            //设置头像
            //.....................暂略

            //设置聊天记录
            meSendTextView.setText(chatMessage.getMessageContent());
        }
        return convertView;
    }
}
