package com.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tgchat.AddActivity;
import com.tgchat.MainActivity;
import com.tgchat.WelcomeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tgchat.AddActivity.userQueryInfoList;
import static com.tgchat.FriendsFragment.friendsAccountList;
import static com.tgchat.MainActivity.messageList;
import static com.tgchat.WelcomeActivity.userInfo;

public class NetRequestUtils {

    private Context context;

    public NetRequestUtils(Context context) {
        this.context = context;
    }

    //定义服务器地址
    private static final String serverAddress = "http://112.74.168.99:8080";

    /**
     * 请求登录
     *
     * @param username->用户名
     * @param hashValue->经sha1加密的密码
     */
    public void requestLogin(final String username, final String hashValue) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/doLogin/";
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody body = new FormBody.Builder()
                        .add("username", username)
                        .add("hashValue", hashValue).build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    Log.e("test", Temp);
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String flag;
                        String errorMsg;
                        if (status.equals("success")) {
                            flag = "success";
                            errorMsg = "";

                            JSONObject jsonMsgObject = jsonObjectTemp.getJSONObject("msg");
                            String nickName = unicodeToString((String) jsonMsgObject.get("nickname"));
                            String headImage = (String) jsonMsgObject.get("head_image");

                            //将信息放入用户字典
                            userInfo.put("userName", username);
                            userInfo.put("password", hashValue);
                            userInfo.put("nickName", nickName);
                            userInfo.put("imageUrl", headImage);

                        } else {
                            flag = "failed";
                            errorMsg = "Login failed";
                        }
                        //发送广播
                        Intent mIntent = new Intent("com.services.Login");
                        mIntent.putExtra("Login", flag);
                        mIntent.putExtra("errorMsg", errorMsg);
                        mIntent.putExtra("username", username);
                        context.sendBroadcast(mIntent);

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 52<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    /**
     * 第二次请求注册
     *
     * @param username->用户名
     * @param hashValue->经sha1加密的密码
     */
    public void requestRegister(final String username, final String hashValue, final String salt,
                                final String e_mail, final String nickName, final String Image) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/doRegister/";
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody body = new FormBody.Builder()
                        .add("username", username)
                        .add("hashValue", hashValue)
                        .add("salt", salt)
                        .add("e-mail", e_mail)
                        .add("nickname", nickName)
                        .add("image", Image)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    Log.e("test", Temp);
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String msg = (String) jsonObjectTemp.get("msg");
                        String flag;
                        String errorMsg;
                        if (status.equals("success")) {
                            flag = "success";
                            errorMsg = "";

                        } else {
                            flag = "failed";
                            errorMsg = msg;
                        }
                        //发送广播
                        Intent mIntent = new Intent("com.services.Register");
                        mIntent.putExtra("requestKind", "secondRegister");
                        mIntent.putExtra("Register", flag);
                        mIntent.putExtra("errorMsg", errorMsg);
                        context.sendBroadcast(mIntent);

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 52<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    /**
     * 第一次请求注册->请求验证码
     *
     * @param Email->邮箱地址
     * @param filename->经sha1加密的密码
     */
    public void requestVerifyCode(final String Email, final String filename) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/doRegister/?e-mail="
                        + Email + "&filename=" + filename;

                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    Log.e("test", Temp);
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String flag;
                        String errorMsg;
                        String randomString;
                        String uploadToken;
                        if (status.equals("success")) {
                            flag = "success";
                            errorMsg = "";

                            JSONObject jsonMsgObject = jsonObjectTemp.getJSONObject("msg");
                            randomString = (String) jsonMsgObject.get("random_string");
                            uploadToken = (String) jsonMsgObject.get("upload_token");


                        } else {
                            flag = "failed";
                            errorMsg = "RequestVerifyCode failed";
                            randomString = "";
                            uploadToken = "";
                        }
                        //发送广播
                        Intent mIntent = new Intent("com.services.Register");
                        mIntent.putExtra("requestKind", "firstRegister");
                        mIntent.putExtra("Email", flag);
                        mIntent.putExtra("errorMsg", errorMsg);
                        mIntent.putExtra("randomString", randomString);
                        mIntent.putExtra("uploadToken", uploadToken);
                        context.sendBroadcast(mIntent);

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 52<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    /**
     * 上传图片
     *
     * @param file->图片文件字节流
     * @param filename->上传到云端的文件名
     * @param token->从业务服务器中获取到的token
     */
    public static void upload(byte[] file, String filename, String token) {

        UploadManager uploadManager = new UploadManager();

        uploadManager.put(file, filename, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                        if (info.isOK()) {
                            Log.e("test", "Upload Success");
                        } else {
                            Log.e("test", "Upload Fail");
                            //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                        }
                        Log.e("test", key + ",\r\n " + info + ",\r\n " + res);
                    }


                }, new UploadOptions(null, null, false,
                        new UpProgressHandler() {
                            public void progress(String key, double percent) {
                                Log.e("test", key + ": " + percent);
                            }
                        }, null));

    }

    /**
     * 请求获取其他用户信息*
     *
     * @param username 需要查询的用户名
     * @param type 查询类型  1->查询好友, 2->查询添加好友
     */
    public void requestUserInfo(final String username, final int type) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/queryUserInfo/?username=" + username;

                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String flag;
                        String errorMsg;
                        if (status.equals("success")) {
                            flag = "success";
                            errorMsg = "";
                            //解析数据
                            JSONObject jsonMsgObject = jsonObjectTemp.getJSONObject("msg");
                            String nickname = unicodeToString((String) jsonMsgObject.get("nickname"));
                            String headImageFileName = (String) jsonMsgObject.get("head_image");
                            if (type == 1) {
                                HashMap<String, String> friendsInfo = new HashMap<>();
                                friendsInfo.put("nickName", nickname);
                                friendsInfo.put("headImage", headImageFileName);
                                WelcomeActivity.friendsInfo.put(username, friendsInfo);
                            } else {
                                AddActivity.UserQueryInfo userQueryInfo =
                                        new AddActivity.UserQueryInfo(username, nickname, headImageFileName);
                                userQueryInfoList.add(userQueryInfo);
                            }

                        } else {
                            flag = "failed";
                            errorMsg = "Can not find the User!";
                        }
                        //发送广播
                        Intent mIntent = new Intent("com.services.Query");
                        mIntent.putExtra("Query", flag);
                        mIntent.putExtra("Type", String.valueOf(type));
                        mIntent.putExtra("errorMsg", errorMsg);
                        context.sendBroadcast(mIntent);

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 52<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    /**
     * 请求添加好友
     *
     * @param myAccount  自己的账号
     * @param hisAccount 需要添加的人的账号
     */
    public void addFriends(final String myAccount, final String hisAccount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/friends/?kind=1&my_account="
                        + myAccount + "&his_account=" + hisAccount;

                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String errorMsg = (String) jsonObjectTemp.get("msg");
                        if (status.equals("success")) {
                            Looper.prepare();
                            Toast.makeText(context, "添加成功啦！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else {
                            Looper.prepare();
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 52<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }

    /**
     * 请求查询好友列表*
     *
     * @param myAccount 自己的用户名
     */
    public void queryFriends(final String myAccount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = serverAddress + "/friends/?kind=2&my_account=" + myAccount;

                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    //解析返回的json数据
                    String Temp = response.body().string();
                    JSONObject jsonObjectTemp = null;

                    jsonObjectTemp = new JSONObject(Temp);

                    //取得返回的状态
                    try {
                        String status = (String) jsonObjectTemp.get("status");
                        String flag;
                        String errorMsg;
                        if (status.equals("success")) {
                            flag = "success";
                            errorMsg = "";
                            //解析数据
                            JSONArray jsonArray = jsonObjectTemp.getJSONArray("msg");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                friendsAccountList.add(jsonArray.getString(i));
                            }
                        } else {
                            flag = "failed";
                            errorMsg = "Can not find the User!";
                        }
                        //发送广播
                        Intent mIntent = new Intent("com.services.Query");
                        mIntent.putExtra("Query", flag);
                        mIntent.putExtra("Type", "3");
                        mIntent.putExtra("errorMsg", errorMsg);
                        context.sendBroadcast(mIntent);

                    } catch (JSONException e) {
                        Log.e("test", "Exception occured at Line 495<NetRequestUt" +
                                "ils>" + "\n" + Log.getStackTraceString(e));
                    }


                } catch (IOException e) {
                    Log.e("test", "Exception occured at Line 49<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                } catch (JSONException e) {
                    Log.e("test", "Exception occured at Line 52<NetRequestUtils>"
                            + "\n" + Log.getStackTraceString(e));
                }

            }
        }).start();
    }


    //unicode转utf8
    private static String unicodeToString(String string) {
        byte[] utf8;
        try {
            utf8 = string.getBytes("UTF-8");
            string = new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("test", "Exception occured at Line 155<NetRequestUtils>"
                    + "\n" + Log.getStackTraceString(e));
        }
        return string;

    }
}
