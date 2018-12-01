package com.utils;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserInfoDataBase extends SQLiteOpenHelper {

    public UserInfoDataBase(Context context) {
        super(context, "userInfo.db", null, 1);
    }

    /* 创建数据库
     * param:_id -> 自增
     * param:username -> 当前账号
     * param:password -> 密码 hash加密
     * param:isLogin -> 是否已登录

     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table userInfo(username varchar(16) primary key, " +
                "hashValue varchar(200), isLogin boolean)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
