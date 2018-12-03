package com.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tgchat.R;
import com.utils.ImageUtils;
import com.utils.RoundImageView;

import java.io.File;
import java.util.HashMap;

import static com.tgchat.FriendsFragment.friendsAccountList;
import static com.tgchat.WelcomeActivity.friendsInfo;

public class FriendsListAdapter extends BaseAdapter {

    private Activity mActivity;

    public FriendsListAdapter(Activity activity) {
        this.mActivity = activity;

    }

    @Override
    public int getCount() {
        return friendsAccountList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendsAccountList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_friends, null);
        }
        String account = (String) getItem(position);
        TextView nickNameTextView = convertView.findViewById(R.id.friends_nickname);
        RoundImageView headImageView = convertView.findViewById(R.id.friends_head_image);

        if (friendsInfo.get(account) != null) {
            HashMap<String, String> info = friendsInfo.get(account);
            assert info != null;
            //设置昵称
            nickNameTextView.setText(info.get("nickName"));
            String headImageFileName = info.get("headImage");

            //如果图片存在, 则设置图片
            if (ImageUtils.fileIsExists(mActivity, headImageFileName)) {
                String ImagePath = mActivity.getFilesDir().getAbsolutePath() + "/" + headImageFileName;
                headImageView.setImageURI(Uri.fromFile(new File(ImagePath)));
            }

        } else {
            nickNameTextView.setText(account);
        }
        return convertView;
    }
}
