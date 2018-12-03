package com.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tgchat.AddActivity;
import com.tgchat.R;
import com.utils.ImageUtils;
import com.utils.NetRequestUtils;
import com.utils.RoundImageView;

import java.io.File;

import static com.tgchat.AddActivity.userQueryInfoList;
import static com.tgchat.WelcomeActivity.userInfo;

public class AddListAdapter extends BaseAdapter {

    Activity addActivity;

    public AddListAdapter(Activity activity) {
        this.addActivity = activity;
    }

    @Override
    public int getCount() {
        return userQueryInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return userQueryInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_add_friend, null);
        }
        final AddActivity.UserQueryInfo userQueryInfo = (AddActivity.UserQueryInfo) getItem(position);

        String nickName = userQueryInfo.getNickname();
        String headImageFileName = userQueryInfo.getHeadImageFileName();

        //设置昵称
        TextView nickNameTextView = convertView.findViewById(R.id.add_nickname);
        nickNameTextView.setText(nickName);

        //设置头像
        if (ImageUtils.fileIsExists(addActivity, headImageFileName)) {
            RoundImageView headImageView = convertView.findViewById(R.id.add_head_image);
            String imageAbsolutePath = addActivity.getFilesDir().getAbsolutePath() + "/" + headImageFileName;
            headImageView.setImageURI(Uri.fromFile(new File(imageAbsolutePath)));
        }
        //设置添加好友按钮点击监听事件
        Button addBtn = convertView.findViewById(R.id.submit_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //向服务器发送请求
                NetRequestUtils netRequestUtils = new NetRequestUtils(addActivity);
                netRequestUtils.addFriends(userInfo.get("userName"), userQueryInfo.getAccount());
                addActivity.finish();
            }
        });

        return convertView;
    }
}
