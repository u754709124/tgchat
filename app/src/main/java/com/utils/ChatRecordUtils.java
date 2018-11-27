package com.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tgchat.MainActivity;

import java.util.ArrayList;

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
//        ContentValues values = new ContentValues();
//        values.put("isRead", true);
//        values.put("isRemoved")
        String sql = "update chatRecord  set IsRead=1, IsRemoved=1 where sendAccount="
                + sendAccount + " AND revAccount=" + revAccount + " AND sendTime<="
                + currentMillis + ";";
        Log.e("test", sql);
        sqLiteDatabase.execSQL("update chatRecord  set IsRead=1, IsRemoved=1 where sendAccount="
                + sendAccount + " AND revAccount=" + revAccount + " AND sendTime<="
                + currentMillis + ";");

    }

    /* 查询未被Removed数据
     *
     * 展示在消息页面的数据
     */
    public boolean queryUnRemovedData(String revAccount){
        String[] selectColumn = new String[]{"sendAccount", "messageContent", "sendTime", "isRead"};
        Cursor cursor = sqLiteDatabase.query("chatRecord", selectColumn, "isRemoved=0 AND revAccount=?", new String[]{revAccount}, null, null, null);
        //判断是否存在IsRemoved=False项
        if(cursor == null) return false;

        if(cursor.moveToFirst()){
            do{
                String sendAccount = cursor.getString(cursor.getColumnIndex("sendAccount"));
                String messageContent = cursor.getString(cursor.getColumnIndex("messageContent"));
                Long sendTime = cursor.getLong(cursor.getColumnIndex("sendTime"));
                Integer isRead = cursor.getInt(cursor.getColumnIndex("isRead"));

                int count;

                if (isRead == 0) {
                    count = 1;
                } else {
                    count = 0;
                }
                //根据sendAccount查询用户nickname和headImageUrl
                String nickname = "123";
                String headImageUrl = "123.png";
                MainActivity.Message message = new MainActivity.Message(sendAccount, nickname, headImageUrl, messageContent, sendTime);
                message.setCount(count);
                MainActivity.messageList.add(message);

            }while(cursor.moveToNext());
        }
        cursor.close();
        return true;
    }

    /* 查询相互的聊天记录
     *
     * 展示在聊天页面的数据
     * 查询sendAccount=this.sendAccount, revAccount=this.revAccount
     *     和sendAccount=this.revAccount, revAccount=this.sendAccount的数据
     *     返回一个关于聊天记录的对象
     */
    public ArrayList<String> queryAllChatRecord(String sendAccount, String revAccount){

        String[] selectColumn = new String[]{"messageContent", "sendTime"};
        Cursor cursor = sqLiteDatabase.query("chatRecord", selectColumn, "sendAccount=? AND revAccount=?", new String[]{sendAccount, revAccount}, null, null, null);
        //判断是否存在IsRemoved=False项
        if(cursor == null) return null;

        if(cursor.moveToFirst()){
            do{
                String messageContent = cursor.getString(cursor.getColumnIndex("messageContent"));
                Long sendTime = cursor.getLong(cursor.getColumnIndex("sendTime"));
                Integer isRead = cursor.getInt(cursor.getColumnIndex("isRead"));

                int count;

                if (isRead == 0) {
                    count = 1;
                } else {
                    count = 0;
                }
                //根据sendAccount查询用户nickname和headImageUrl
                String nickname = "123";
                String headImageUrl = "123.png";
                MainActivity.Message message = new MainActivity.Message(sendAccount, nickname, headImageUrl, messageContent, sendTime);
                message.setCount(count);
                MainActivity.messageList.add(message);

            }while(cursor.moveToNext());
        }
        cursor.close();
        return null;
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
