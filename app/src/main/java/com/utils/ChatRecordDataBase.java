package com.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatRecordDataBase extends SQLiteOpenHelper {

    public ChatRecordDataBase(Context context){
        super(context, "chatRecord.db", null, 1);
    }

    /* 创建数据库
     * param:_id -> 自增
     * param:sendAccount -> 发送人账号 数字 限制长度16位
     * param:revAccount -> 接收人账号 数字 限制长度16位
     * param:messageContent -> 消息内容 utf-8 限制长度100位
     * param:sendTime -> 发送时间 限制长度20位

     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table chatRecord (_id Integer primary key autoincrement, sendAccount varchar(16), " +
                "revAccount varchar(20), messageContent varchar(300), sendTime Bigint, isRead boolean, isRemoved boolean)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
