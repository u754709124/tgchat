# 一个安卓的练手项目-仿QQ聊天App TGChat
## 主要实现功能
* 页面主要仿照QQ进行规划
* 账号登录
* 账号注册
* 主要的聊天功能
* 查找，添加好友

## 基本实现方法
### 账号注册
* 账号头像->通过七牛云储存图片;
* 填写邮箱->GET发送请求到服务器，服务器发送验证码，返回验证码和用于上传图片的token,邮箱输入框变为验证码输入框;
* 点击确认->POST发送请求到服务器，主要参数：账号，经sha1加密的密码，随机生成的16位salt，邮箱验证码，昵称以及发送到七牛云存储图片的文件名;

### 账号登录
    上传经sha1加密的密码，服务器将的到的密文加上服务器存储的salt进行sha1加密，与服务器端存储注册时与salt结合加密的密文比对;
    
### 基本聊天功能
    主要通过sock长连接实现。
    
### 使用的第三方库
* 七牛云官方上传图片的SDK
* okhttp3
* okio1.9

## 页面图片展示
### 登录页面
![login](http://tgchat.94loveyou.cn/login.jpg)
### 账号注册
![register](http://tgchat.94loveyou.cn/register.jpg)
### 消息界面
![message](http://tgchat.94loveyou.cn/message_list.jpg)
### 聊天界面
![chat](http://tgchat.94loveyou.cn/chat.jpg)
### 联系人界面
![friends](http://tgchat.94loveyou.cn/friends.jpg)
### 查找，添加好友
![add](http://tgchat.94loveyou.cn/add.jpg)
