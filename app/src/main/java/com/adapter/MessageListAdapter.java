package com.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tgchat.MainActivity;
import com.tgchat.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MessageListAdapter extends BaseAdapter{
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
        ImageView headImage = convertView.findViewById(R.id.return_btn);

        //获取当前position的Message对象
        MainActivity.Message message = messageList.get(position);
        //设置发送人昵称
        sendNickname.setText(message.getNickName());
        //设置发送人头像, 关键词：drawable 网络头像
        //headImage.setImageDrawable();
        //设置发送的消息
        messageContent.setText(message.getMessageContent());
        //设置发送时间
        sendTime.setText(convertLongMillisToString(message.getSendTime()));
        //设置小红点显示消息数
        messageCount.setText(String.valueOf(message.getCount()));



        return convertView;
    }

    private String convertLongMillisToString(Long oldTimeMills){
        SimpleDateFormat totalDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.CHINA);
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM", Locale.CHINA);
        SimpleDateFormat dayDateFormat = new SimpleDateFormat("dd", Locale.CHINA);
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm", Locale.CHINA);

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
