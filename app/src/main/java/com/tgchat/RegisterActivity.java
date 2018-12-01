package com.tgchat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.utils.EncryptUtils;
import com.utils.ImageUtils;
import com.utils.NetRequestUtils;
import com.utils.RoundImageView;

import java.io.File;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    //定义一个广播接收器
    private BroadcastReceiver broadcastReceiver;
    //邮箱正则
    private final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z" +
            "0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    //验证码是否已发送
    private boolean isSendCode = false;
    //随机生成的图片文件名->上传到七牛云后显示的文件名
    private String randomFileName;
    //获得的验证码
    private String verifyCode;
    //获得的上传图片的token
    private String uploadToken;
    //获得选择文件的地址
    private byte[] file;
    //邮箱地址
    private String emailAddress;

    private EditText emailInput;
    private RoundImageView headImage;
    public final static int GALLERY_REQUEST_CODE = 1;
    public final static int PHOTO_REQUEST_CUT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //点击dismiss监听
        initDismissBtnListener();

        //注册广播监听
        registerBroadcastReceiver();

        //初始化邮箱输入框焦点监听
        initEmailInputFocusListener();

        //初始化点击头像事件监听
        initHeadImageClickListener();

        //确认按钮点击事件的监听
        initSubmitClickListener();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    //点击dismiss监听
    private void initDismissBtnListener() {
        ImageView dismissBtn = findViewById(R.id.dismiss_btn);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //点击邮箱输入框获得焦点
    private void initEmailInputFocusListener() {
        emailInput = findViewById(R.id.e_mail);
        emailInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获得输入框中内容
                String content = emailInput.getText().toString().trim();
                //如果验证码未发送
                if (!isSendCode) {
                    //如果获得焦点
                    if (!hasFocus) {
                        //如果输入框为邮箱地址
                        if (Pattern.matches(REGEX_EMAIL, content)) {
                            //发送第一次注册请求，返回验证码和上传图片的token
                            NetRequestUtils netRequestUtils = new NetRequestUtils(RegisterActivity.this);
                            //生成随机的图片地址
                            randomFileName = UUID.randomUUID().toString()
                                    .replace("-", "") + ".png";
                            //保存email地址
                            emailAddress = content;

                            //发起网络请求
                            netRequestUtils.requestVerifyCode(content, randomFileName);
                            isSendCode = true;
                        } else {
                            //输入框不为空
                            if (!content.equals("")) {
                                Toast.makeText(RegisterActivity.this, "邮箱格式不正确，请检查！", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }
            }
        });
    }

    //注册监听接收到消息的广播
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.services.Register");
        broadcastReceiver = new MessageChangeReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    //新建广播接收
    private class MessageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取请求
            String requestKind = intent.getStringExtra("requestKind");
            //注册第一步
            if (requestKind.equals("firstRegister")) {
                String requestResult = intent.getStringExtra("Email");
                String getVerifyCode = intent.getStringExtra("randomString");
                String getUploadToken = intent.getStringExtra("uploadToken");
                if (requestResult.equals("success")) {
                    //更新获得的验证码和上传图片的token
                    verifyCode = getVerifyCode;
                    uploadToken = getUploadToken;
                    Toast.makeText(RegisterActivity.this, "验证码已发送！", Toast.LENGTH_SHORT).show();
                    //更爱邮箱输入框显示
                    emailInput.setText("");
                    emailInput.setHint("请输入验证码");
                    emailInput.setHintTextColor(Color.parseColor("#ff0000"));
                    isSendCode = true;

                    Log.e("test", "verifyCode：" + verifyCode + "\nuploadToken："
                            + uploadToken + "randomFileName：" + randomFileName);
                } else {
                    Toast.makeText(RegisterActivity.this, "验证码发送失败，请重试！", Toast.LENGTH_SHORT).show();
                    isSendCode = false;
                }
            } else {
                //注册第二步
                String requestResult = intent.getStringExtra("Register");
                String errorMsg = intent.getStringExtra("errorMsg");
                if (requestResult.equals("success")) {
                    Toast.makeText(RegisterActivity.this, "恭喜你，注册成功啦！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    //点击头像触发事件
    private void initHeadImageClickListener() {
        headImage = findViewById(R.id.choose_head_image);
        headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {   //权限还没有授予，需要在这里写申请权限的代码
                    // 第二个参数是一个字符串数组，里面是你需要申请的权限 可以设置申请多个权限
                    // 最后一个参数是标志你这次申请的权限，该常量在onRequestPermissionsResult中使用到
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            GALLERY_REQUEST_CODE);

                } else { //权限已经被授予，在这里直接写要执行的相应方法即可
                    choosePhoto();
                }
            }
        });
    }

    //选择图片
    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("test", "");
        if (resultCode == RegisterActivity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                try {

                    //该uri是上一个Activity返回的
                    Uri imageUri = data.getData();
                    //裁剪图像
                    crop(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PHOTO_REQUEST_CUT) {
                // 从剪切图片返回的数据
                if (data != null) {
                    Bitmap bitmap = data.getParcelableExtra("data");
                    file = ImageUtils.bitmapToBytes(bitmap);
                    headImage.setImageBitmap(bitmap);
                }

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                // Permission Denied
                Toast.makeText(RegisterActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //确认按钮点击事件的监听
    private void initSubmitClickListener() {
        //初始化控件
        final EditText userName = findViewById(R.id.register_username);
        final EditText nickName = findViewById(R.id.register_nickname);
        final EditText password = findViewById(R.id.register_password);
        final EditText confirmPassword = findViewById(R.id.comfirm_password);
        final EditText emailInput = findViewById(R.id.e_mail);
        Button submitBtn = findViewById(R.id.register_submit);
        //监听点击事件
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入框中内容
                String userNameText = userName.getText().toString().trim();
                String nickNameText = nickName.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();
                String emailInputText = emailInput.getText().toString().trim();
                //判断输入框是否有空
                if (userNameText.equals("") || nickNameText.equals("") || passwordText.equals("")
                        || confirmPasswordText.equals("") || emailInputText.equals("")) {
                    //发出提示
                    Toast.makeText(RegisterActivity.this,
                            "请仔细检查哦，输入框都不能空的哦！", Toast.LENGTH_SHORT).show();
                }
                //输入框不为空
                else {
                    //密码和确认密码输入不相符
                    if (!passwordText.equals(confirmPasswordText)) {
                        //发出提示
                        Toast.makeText(RegisterActivity.this,
                                "请仔细检查哦，密码和确认密码不一样哦！", Toast.LENGTH_SHORT).show();
                    }
                    //密码和确认密码相符
                    else {
                        //验证码输入错误
                        if (!verifyCode.equals(emailInputText)) {
                            //发出提示
                            Toast.makeText(RegisterActivity.this,
                                    "验证码输入错误哦！", Toast.LENGTH_SHORT).show();
                        }
                        //验证码输入正确
                        else {
                            //没有选择头像
                            if (file == null) {
                                //发出提示
                                Toast.makeText(RegisterActivity.this,
                                        "头像不能不选哦！", Toast.LENGTH_SHORT).show();
                            }
                            //选择了头像
                            else {
                                //发出提示
                                Toast.makeText(RegisterActivity.this,
                                        "正在注册....", Toast.LENGTH_SHORT).show();

                                //发起第二次注册请求
                                secondRequestRegister(userNameText, passwordText, nickNameText);
                            }
                        }

                    }
                }

            }
        });

    }

    /*
     * 剪切图片
     */
    private void crop(Uri uri) {

        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//输出路径
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);

        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    //第二次注册操作
    private void secondRequestRegister(String username, String password, String nickName) {
        String hashValue = EncryptUtils.encryptBySha1(password);
        String salt = EncryptUtils.getSalt();
        //上传头像
        NetRequestUtils.upload(file, randomFileName, uploadToken);
        //带参数第二次登录
        NetRequestUtils netRequestUtils = new NetRequestUtils(this);
        netRequestUtils.requestRegister(username, hashValue, salt, emailAddress, nickName, randomFileName);
    }
}

