package com.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.tgchat.WelcomeActivity.userInfo;

public class UserInfoUtils {

    private Context context;

    public SQLiteDatabase sqLiteDatabase;
    private NetRequestUtils netRequestUtils;


    public UserInfoUtils(Context context) {
        this.context = context;
        init();

    }

    /*初始化

     */
    private void init() {
        UserInfoDataBase userInfoDataBase = new UserInfoDataBase(context);
        sqLiteDatabase = userInfoDataBase.getWritableDatabase();

        netRequestUtils = new NetRequestUtils(context);

    }


    /* 查询已登录的账户
     *
     */
    public void queryLoginUser() {

        String[] selectColumn = new String[]{"username", "hashValue"};
        Cursor cursor = sqLiteDatabase.query("userInfo", selectColumn, "isLogin=1", null, null, null, null);
        //判断是否存在IsLogin=True项
        if (cursor.getCount() == 0) {
            Log.e("test", "未查询到");
            //发送广播
            String errorMsg = "Query failed";
            String username = "";
            Intent mIntent = new Intent("com.services.Login");
            mIntent.putExtra("Login", "failed");
            mIntent.putExtra("errorMsg", errorMsg);
            mIntent.putExtra("username", username);
            context.sendBroadcast(mIntent);
            return;
        }

        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String hashValue = cursor.getString(cursor.getColumnIndex("hashValue"));

            netRequestUtils.requestLogin(username, hashValue);

        }
        cursor.close();
    }

    /* 存入用户记录
     * 登录操作
     */
    public void saveUserInfo(String username, String password) {

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("hashValue", password);
        values.put("isLogin", true);

        sqLiteDatabase.insert("userInfo", "isLogin", values);

    }

    /* 设置用户取消保存密码
     * 退出登录操作
     */
    public void setIsNotLogin(String username) {

        sqLiteDatabase.execSQL("update userInfo  set IsLogin=0 where username="
                + username + ";");

    }

    /* 删除用户
     */
    public void deleteUser(String username) {

        sqLiteDatabase.execSQL("delete from userInfo where username="
                + username + ";");

    }
}
