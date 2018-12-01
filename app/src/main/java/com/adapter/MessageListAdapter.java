package com.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tgchat.MainActivity;
import com.tgchat.R;
import com.tgchat.WelcomeActivity;
import com.utils.ImageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MessageListAdapter extends BaseAdapter{

    private Activity mActivity;

    public MessageListAdapter(Activity activity) {
        this.mActivity = activity;
    }
    //获取消息列表
    private ArrayList<MainActivity.Message> messageList = MainActivity.messageList;

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message,null);
        }

        //初始化内部控件
        TextView sendNickname = convertView.findViewById(R.id.send_nickname);
        TextView messageContent = convertView.findViewById(R.id.message_content);
        TextView sendTime = convertView.findViewById(R.id.send_time);
        TextView messageCount = convertView.findViewById(R.id.message_count);
        ImageView headImage = convertView.findViewById(R.id.chat_head_image);

        //获取当前position的Message对象
        MainActivity.Message message = messageList.get(position);


        //如果能在好友信息的字典找到该账号
        if (WelcomeActivity.friendsInfo.get(message.getSendAccount()) != null) {
            HashMap<String, String> info = WelcomeActivity.friendsInfo.get(message.getSendAccount());
            assert info != null;
            String headImageFileName = info.get("headImage");
            String nickName = info.get("nickName");

            //如果图片存在, 则设置图片
            if (ImageUtils.fileIsExists(mActivity, headImageFileName)) {
                String ImagePath = mActivity.getFilesDir().getAbsolutePath() + "/" + headImageFileName;
                headImage.setImageURI(Uri.fromFile(new File(ImagePath)));
            }

            //设置好友昵称
            sendNickname.setText(nickName);
        } else {
            sendNickname.setText(message.getSendAccount());
        }

        //设置发送的消息
        messageContent.setText(message.getMessageContent());
        //设置发送时间
        sendTime.setText(convertLongMillisToString(message.getSendTime()));
        //设置小红点显示消息数
        if (message.getCount() == 0) {
            messageCount.setVisibility(View.GONE);
        } else {
            messageCount.setVisibility(View.VISIBLE);
            messageCount.setText(String.valueOf(message.getCount()));
        }



        return convertView;
    }

    private String convertLongMillisToString(Long oldTimeMills){
        SimpleDateFormat totalDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM", Locale.CHINA);
        SimpleDateFormat dayDateFormat = new SimpleDateFormat("dd", Locale.CHINA);
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

        Long nowTimeMills = System.currentTimeMillis();

        Integer nowYear = Integer.valueOf(yearDateFormat.format(nowTimeMills));
        Integer oldYear = Integer.valueOf(yearDateFormat.format(oldTimeMills));

        Integer nowMonth = Integer.valueOf(monthDateFormat.format(nowTimeMills));
        Integer oldMonth = Integer.valueOf(monthDateFormat.format(oldTimeMills));

        Integer nowDay = Integer.valueOf(dayDateFormat.format(nowTimeMills));
        Integer oldDay = Integer.valueOf(dayDateFormat.format(oldTimeMills));

        String oldTime = timeDateFormat.format(oldTimeMills);
        String totalTime = totalDateFormat.format(oldTimeMills);
        if(nowYear.equals(oldYear)&&nowMonth.equals(oldMonth)){
            if(nowDay.equals(oldDay)){
                return (oldTime);
            }
            else if(nowDay.equals(oldDay+1)){
                return ("昨天 " + oldTime);
            }
            else if(nowDay.equals(oldDay+2)){
                return ("前天 " + oldTime);
            }
        }
        return totalTime;

    }
}
