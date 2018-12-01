package com.tgchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.services.MessageService;
import com.utils.UserInfoUtils;

import static com.tgchat.WelcomeActivity.userInfo;

public class SettingFragment extends Fragment{
    //定义一个UserInfoUtils
    UserInfoUtils userInfoUtils;
    View contentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_setting, container, false);
        //初始化userInfoUtils
        userInfoUtils = new UserInfoUtils(getActivity());
        //初始化点击登出按钮事件的监听
        initLogOutButtonClickListener();

        return contentView;
    }


    //点击登出按钮事件的监听
    private void initLogOutButtonClickListener() {
        Button logOutButton = contentView.findViewById(R.id.log_out);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消数据库自动登录
                userInfoUtils.deleteUser(userInfo.get("userName"));
                //清除内存中userinfo
                userInfo.clear();
                //跳转到欢迎页
                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                startActivity(intent);

                //关闭服务
                Intent intent1 = new Intent(getActivity(), MessageService.class);
                intent1.setAction(MessageService.ACTION);
                getActivity().stopService(intent1);
                //销毁MainActivity
                getActivity().finish();

            }
        });
    }

    @Override
    public void onDestroy() {
        //关闭数据库连接
        userInfoUtils.sqLiteDatabase.close();
        super.onDestroy();
    }
}
