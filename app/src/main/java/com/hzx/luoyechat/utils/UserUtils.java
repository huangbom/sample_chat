package com.hzx.luoyechat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzx.luoyechat.Constant;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.domain.User;
import com.squareup.picasso.Picasso;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     * @param username
     * @return
     */
    public static User getUserInfo(String username){
        User user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(username);
        if(user == null){
            user = new User(username);
        }

        if(user != null){
            //demo没有这些数据，临时填充
        	if(TextUtils.isEmpty(user.getNick()))
        		user.setNick(username);
        }
        return user;
    }

	/** ChatActivity专用*/
	public static void setUserAvatar(Context context, String username, ImageView imageView,int resId){
		User user = getUserInfo(username);
		if (Constant.CHAT_KEFU.equals(username)){
			imageView.setImageResource(R.mipmap.kefu);
		}else {
		if(user != null && user.getAvatar() != null){
			Picasso.with(context).load(user.getAvatar()).placeholder(resId).into(imageView);
		}else{
			Picasso.with(context).load(resId).into(imageView);
		}}
	}

    /** 设置用户头像 */
    public static void setUserAvatar(Context context, String username, ImageView imageView){

		setUserAvatar(context,username,imageView,R.mipmap.default_avatar);
    }
    
    /** 设置当前用户头像 */
	public static void setCurrentUserAvatar(Context context, ImageView imageView) {

		setCurrentUserAvatar(context,imageView,R.mipmap.default_avatar);
	}

	public static void setCurrentUserAvatar(Context context, ImageView imageView,int resId) {
		User user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
		if (user != null && user.getAvatar() != null) {
			Picasso.with(context).load(user.getAvatar()).placeholder(resId).into(imageView);
		} else {
			Picasso.with(context).load(resId).into(imageView);
		}
	}
    
    /** 设置用户昵称 */
    public static void setUserNick(String username,TextView textView){
    	User user = getUserInfo(username);
    	if(user != null){
    		textView.setText(user.getNick());
    	}else{
    		textView.setText(username);
    	}
    }
    
    /**
     * 设置当前用户昵称
     */
    public static void setCurrentUserNick(TextView textView){
    	User user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
    	if(textView != null){
    		textView.setText(user.getNick());
    	}
    }
    
    /**
     * 保存或更新某个用户
     */
	public static void saveUserInfo(User newUser) {
		if (newUser == null || newUser.getUsername() == null) {
			return;
		}
		((DemoHXSDKHelper) HXSDKHelper.getInstance()).saveContact(newUser);
	}
    
}
