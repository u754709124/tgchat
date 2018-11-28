package com.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tgchat.ChatActivity;
import com.tgchat.MainActivity;

import java.util.ArrayList;

import static com.tgchat.ChatActivity.chatMessageArrayList;
import static com.tgchat.MainActivity.userInfo;

public class ChatRecordUtils {

    private Context context;

    SQLiteDatabase sqLiteDatabase;

    public ChatRecordUtils(Context context){
        this.context = context;
        init();

    }

    /*初始化

     */
    private void init(){
        ChatRecordDataBase chatRecordDataBase = new ChatRecordDataBase(context);
        sqLiteDatabase = chatRecordDataBase.getWritableDatabase();

    }

    /*设置消息已读
     *
     * 用户点击消息Item触发
     * 设置isRead=True
     */
    public void setIsRead(String sendAccount, String revAccount){
        Long currentMillis = System.currentTimeMillis();

        sqLiteDatabase.execSQL("update chatRecord  set IsRead=1 where sendAccount="
                + sendAccount + " AND revAccount=" + revAccount + " AND sendTime<="
                + currentMillis + ";");
    }

    /*设置消息被移除
    *
    * 用户长按Item点击删除时触发
    * 设置isRead=True , isRemoved=True
    * 筛选条件:比当前选中项毫秒时间戳小的
    */
    public void setIsRemoved(String sendAccount, String revAccount){
        Long currentMillis = System.currentTimeMillis();

        sqLiteDatabase.execSQL("update chatRecord  set IsRead=1, IsRemoved=1 where sendAccount="
                + sendAccount + " AND revAccount=" + revAccount + " AND sendTime<="
                + currentMillis + ";");
        sqLiteDatabase.execSQL("update chatRecord  set IsRead=1, IsRemoved=1 where sendAccount="
                + revAccount + " AND revAccount=" + sendAccount + " AND sendTime<="
                + currentMillis + ";");

    }

    /* 查询未被Removed数据
     *
     * 展示在消息页面的数据
     * param:String revAccount->账号
     * param:boolean reverse->true则查询发送人=账号的消息，false则查询接收人=账号的消息；
     */
    public void queryUnRemovedData(String Account, boolean reverse) {
        Cursor cursor;
        String[] selectColumn = new String[]{"sendAccount", "revAccount", "messageContent", "sendTime", "isRead"};
        if (!reverse) {
            cursor = sqLiteDatabase.query("chatRecord", selectColumn, "isRemoved=0 AND revAccount=?", new String[]{Account}, null, null, null);
        } else {
            cursor = sqLiteDatabase.query("chatRecord", selectColumn, "isRemoved=0 AND sendAccount=?", new String[]{Account}, null, null, null);
        }
        //判断是否存在IsRemoved=False项
        if (cursor == null) return;

        if(cursor.moveToFirst()){
            do{
                String sendAccount = cursor.getString(cursor.getColumnIndex("sendAccount"));
                String messageContent = cursor.getString(cursor.getColumnIndex("messageContent"));
                Long sendTime = cursor.getLong(cursor.getColumnIndex("sendTime"));
                Integer isRead = cursor.getInt(cursor.getColumnIndex("isRead"));
                String revAccount = cursor.getString(cursor.getColumnIndex("revAccount"));

                //将/a3/t4替换为@
                messageContent = messageContent.replace("/a3/t4", "@");

                int count;

                if (isRead == 0) {
                    count = 1;
                } else {
                    count = 0;
                }
                //根据sendAccount查询用户nickname和headImageUrl
                String nickname = "默认昵称";
                String headImageUrl = "123.png";
                MainActivity.Message message = new MainActivity.Message(sendAccount, revAccount, nickname, headImageUrl, messageContent, sendTime);
                message.setCount(count);
                MainActivity.messageList.add(message);

            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    /* 查询相互的聊天记录
     *
     * 展示在聊天页面的数据
     * 查询sendAccount=this.sendAccount, revAccount=this.revAccount
     *     和sendAccount=this.revAccount, revAccount=this.sendAccount的数据
     *     返回一个关于聊天记录的对象
     */
    public void queryAllChatRecord(String sendAccount, String revAccount) {

        String[] selectColumn = new String[]{"messageContent", "sendTime"};
        Cursor cursor = sqLiteDatabase.query("chatRecord", selectColumn, "sendAccount=? AND revAccount=?", new String[]{sendAccount, revAccount}, null, null, null);
        //判断是否存在IsRemoved=False项
        if (cursor == null) return;

        if(cursor.moveToFirst()){
            do{
                String messageContent = cursor.getString(cursor.getColumnIndex("messageContent"));
                Long sendTime = cursor.getLong(cursor.getColumnIndex("sendTime"));
                //将/a3/t4替换为@
                messageContent = messageContent.replace("/a3/t4", "@");
                //根据sendAccount查询用户nickname和headImageUrl
                String headImageUrl = "123.png";
                chatMessageArrayList.add(new ChatActivity.ChatMessage(sendAccount, revAccount, messageContent, headImageUrl, sendTime));


            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    /* 存入聊天记录
     *
     * 存储聊天的数据
     *
     */
    public void saveChatRecord(String sendAccount, String revAccount, String messageContent, Long sendTime ){

        ContentValues values = new ContentValues();
        values.put("sendAccount", sendAccount);
        values.put("revAccount", revAccount);
        values.put("messageContent", messageContent);
        values.put("sendTime", sendTime);
        values.put("isRead",false);
        values.put("isRemoved",false);

        sqLiteDatabase.insert("chatRecord", "isRead", values);

    }



}
