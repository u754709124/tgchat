package com.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tgchat.ChatActivity;
import com.tgchat.MainActivity;
import com.tgchat.R;
import com.tgchat.WelcomeActivity;
import com.utils.ImageUtils;
import com.utils.RoundImageView;

import java.io.File;
import java.util.HashMap;

import static com.tgchat.ChatActivity.chatMessageArrayList;
import static com.tgchat.WelcomeActivity.userInfo;

public class ChatListAdapter extends BaseAdapter {

    private Activity mActivity;

    public ChatListAdapter(Activity activity) {
        this.mActivity = activity;
    }

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
        String myAccount = userInfo.get("userName");
        String revAccount = chatMessage.getRevAccount();
        String senAccount = chatMessage.getSendAccount();

        //如果接收人是我
        if (revAccount.equals(myAccount)) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.his_message_piece, null);
            //初始化控件
            TextView hisSendTextView = convertView.findViewById(R.id.his_send_text);
            RoundImageView hisHeadImageView = convertView.findViewById(R.id.his_head_image);

            //设置头像
            //如果能在好友信息的字典找到该账号
            if (WelcomeActivity.friendsInfo.get(senAccount) != null) {
                HashMap<String, String> info = WelcomeActivity.friendsInfo.get(senAccount);
                assert info != null;
                String headImageFileName = info.get("headImage");

                //如果图片存在, 则设置图片
                if (ImageUtils.fileIsExists(mActivity, headImageFileName)) {
                    String ImagePath = mActivity.getFilesDir().getAbsolutePath() + "/" + headImageFileName;
                    hisHeadImageView.setImageURI(Uri.fromFile(new File(ImagePath)));
                }

            }

            //设置聊天记录
            hisSendTextView.setText(chatMessage.getMessageContent());

        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.me_message_piece, null);

            //初始化控件
            TextView meSendTextView = convertView.findViewById(R.id.me_send_text);
            RoundImageView meHeadImageView = convertView.findViewById(R.id.me_head_image);

            String headImageFileName = userInfo.get("imageUrl");
            //设置头像
            if (ImageUtils.fileIsExists(mActivity, headImageFileName)) {
                String ImagePath = mActivity.getFilesDir().getAbsolutePath() + "/" + headImageFileName;
                meHeadImageView.setImageURI(Uri.fromFile(new File(ImagePath)));
            }

            //设置聊天记录
            meSendTextView.setText(chatMessage.getMessageContent());
        }
        return convertView;
    }
}
