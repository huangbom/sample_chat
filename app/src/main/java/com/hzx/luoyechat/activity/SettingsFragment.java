/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hzx.luoyechat.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.hzx.luoyechat.Constant;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.DemoHXSDKModel;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.applib.utils.HXPreferenceUtils;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 设置界面
 * 
 */
public class SettingsFragment extends Fragment implements OnClickListener {

	/**
	 * 设置新消息通知布局
	 */
	private RelativeLayout rl_switch_notification;
	/**
	 * 设置声音布局
	 */
	private RelativeLayout rl_switch_sound;
	/**
	 * 设置震动布局
	 */
	private RelativeLayout rl_switch_vibrate;

	View check_new_version;
//	View switch_themecolor;
	/**
	 * 设置扬声器布局
	 */

	/**
	 * 打开新消息通知imageView
	 * 关闭新消息通知imageview
	 */
	CheckBox switch_notification;

	/**
	 * 打开声音提示imageview
	 * 关闭声音提示imageview
	 */
	CheckBox switch_sound;
	/**
	 * 打开消息震动提示
	 * 关闭消息震动提示
	 */
	CheckBox switch_vibrate;
	/**
	 * 打开扬声器播放语音
	 */
	CheckBox switch_speaker;

	/**
	 * 声音和震动中间的那条线
	 */
	private TextView textview1, textview2;

	private LinearLayout blacklistContainer;
	
	private LinearLayout userProfileContainer;
	
	/**
	 * 退出按钮
	 */
	private Button logoutBtn;

//	CheckBox switch_room_owner_leave;
	View theme_color;
	View rl_switch_theme_color;
	
	private EMChatOptions chatOptions;
 
	/**
	 * 诊断
	 */
	private LinearLayout llDiagnose;
	/**
	 * iOS离线推送昵称
	 */
//	private LinearLayout pushNick;
	
	DemoHXSDKModel model;

	View rootView;

	int[] colors = {
			R.color.cyan,
			android.R.color.holo_blue_dark,
			android.R.color.holo_green_light,
			android.R.color.holo_red_light,
			android.R.color.holo_green_dark,
			android.R.color.holo_red_dark,
			android.R.color.holo_purple,
			android.R.color.holo_orange_light,
			android.R.color.holo_orange_dark,
			android.R.color.holo_blue_bright,
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView =  inflater.inflate(R.layout.fragment_conversation_settings, container, false);
		return rootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
		rl_switch_notification = (RelativeLayout) getView().findViewById(R.id.rl_switch_notification);
		rl_switch_sound = (RelativeLayout) getView().findViewById(R.id.rl_switch_sound);
		rl_switch_vibrate = (RelativeLayout) getView().findViewById(R.id.rl_switch_vibrate);
		check_new_version =  getView().findViewById(R.id.check_new_version);

		switch_notification = (CheckBox) getView().findViewById(R.id.switch_notification);

		switch_sound = (CheckBox) getView().findViewById(R.id.switch_sound);

		switch_vibrate = (CheckBox) getView().findViewById(R.id.switch_vibrate);

		switch_speaker = (CheckBox) getView().findViewById(R.id.switch_speaker);

//		switch_room_owner_leave = (CheckBox) getView().findViewById(R.id.switch_room_owner_leave);
		
		logoutBtn = (Button) getView().findViewById(R.id.btn_logout);
		if(!TextUtils.isEmpty(EMChatManager.getInstance().getCurrentUser())){
			logoutBtn.setText(getString(R.string.button_logout) + "(" + EMChatManager.getInstance().getCurrentUser() + ")");
		}

		textview1 = (TextView) getView().findViewById(R.id.textview1);
		textview2 = (TextView) getView().findViewById(R.id.textview2);
		
		blacklistContainer = (LinearLayout) getView().findViewById(R.id.ll_black_list);
		userProfileContainer = (LinearLayout) getView().findViewById(R.id.ll_user_profile);
		llDiagnose=(LinearLayout) getView().findViewById(R.id.ll_diagnose);
//		switch_themecolor = getView().findViewById(R.id.switch_themecolor);
//		pushNick=(LinearLayout) getView().findViewById(R.id.ll_set_push_nick);

		theme_color = getView().findViewById(R.id.theme_color);
		rl_switch_theme_color = getView().findViewById(R.id.rl_switch_theme_color);

		initSwitchColor();

		blacklistContainer.setOnClickListener(this);
		userProfileContainer.setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		llDiagnose.setOnClickListener(this);
		check_new_version.setOnClickListener(this);
//		switch_themecolor.setOnClickListener(this);

		chatOptions = EMChatManager.getInstance().getChatOptions();
		
		model = (DemoHXSDKModel) HXSDKHelper.getInstance().getModel();
		
		// 震动和声音总开关，来消息时，是否允许此开关打开
		// the vibrate and sound notification are allowed or not?
		switch_notification.setChecked(model.getSettingMsgNotification());
		
		// 是否打开声音
		// sound notification is switched on or not?
		switch_sound.setChecked(model.getSettingMsgSound());
		
		// 是否打开震动
		// vibrate notification is switched on or not?
		switch_vibrate.setChecked(model.getSettingMsgVibrate());

		// 是否打开扬声器
		switch_speaker.setChecked(model.getSettingMsgSpeaker());

		// 是否允许聊天室owner leave
//		switch_room_owner_leave.setChecked(model.isChatroomOwnerLeaveAllowed());

		initListener();
	}

	android.support.v7.app.AlertDialog.Builder dialog;
	private void initSwitchColor() {
		dialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
				.setAdapter(new BaseAdapter() {
					@Override
					public int getCount() {
						return colors.length;
					}

					@Override
					public Object getItem(int position) {
						return colors[position];
					}

					@Override
					public long getItemId(int position) {
						return position;
					}

					@Override
					public View getView(final int position, View convertView, ViewGroup parent) {
						if (convertView == null) {
							convertView = new View(getActivity());
							convertView.setLayoutParams(new AbsListView.LayoutParams(-1, 70));
						}

						convertView.setBackgroundColor(getResources().getColor((Integer) getItem(position)));
						return convertView;
					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
//						Snackbar.make(theme_color, i + ": ", 0).show();
						HXPreferenceUtils.getInstance().setThemeColor(i);
						HXPreferenceUtils.getInstance().setTabIndex(2);
						theme_color.setBackgroundResource(colors[i]);
						getActivity().finish();
						startActivity(new Intent(getActivity(), MainActivity.class));
					}
				});/**/
		rl_switch_theme_color.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.show();
			}
		});
		int colorindex = HXPreferenceUtils.getInstance().getThemeColor();
		theme_color.setBackgroundResource(colors[colorindex]);
	}

	private void initListener(){

		// 通知
		switch_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (b) {
					rl_switch_sound.setVisibility(View.VISIBLE);
					rl_switch_vibrate.setVisibility(View.VISIBLE);
					textview1.setVisibility(View.VISIBLE);
					textview2.setVisibility(View.VISIBLE);
					chatOptions.setNotificationEnable(true);
					EMChatManager.getInstance().setChatOptions(chatOptions);
					HXSDKHelper.getInstance().getModel().setSettingMsgNotification(true);
				} else {
					rl_switch_sound.setVisibility(View.GONE);
					rl_switch_vibrate.setVisibility(View.GONE);
					textview1.setVisibility(View.GONE);
					textview2.setVisibility(View.GONE);
					chatOptions.setNotificationEnable(false);
					EMChatManager.getInstance().setChatOptions(chatOptions);
					HXSDKHelper.getInstance().getModel().setSettingMsgNotification(false);
				}
		}});

		//打开声音提示
		switch_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					chatOptions.setNoticeBySound(b);
					EMChatManager.getInstance().setChatOptions(chatOptions);
					HXSDKHelper.getInstance().getModel().setSettingMsgSound(b);
			}
		});

		/*switch_room_owner_leave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				chatOptions.allowChatroomOwnerLeave(b);
				EMChatManager.getInstance().setChatOptions(chatOptions);
				model.allowChatroomOwnerLeave(b);
			}
		});*/

		// 打开扬声器播放语音
		switch_speaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				chatOptions.setUseSpeaker(b);
				EMChatManager.getInstance().setChatOptions(chatOptions);
				if (b){
					HXSDKHelper.getInstance().getModel().setSettingMsgVibrate(true);
					HXSDKHelper.getInstance().getModel().setSettingMsgSpeaker(true);}
				else
					HXSDKHelper.getInstance().getModel().setSettingMsgSpeaker(false);
			}
		});

		//  是否打开震动
		switch_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				chatOptions.setNoticedByVibrate(b);
				EMChatManager.getInstance().setChatOptions(chatOptions);
				HXSDKHelper.getInstance().getModel().setSettingMsgVibrate(b);
			}
		});
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_logout: //退出登陆
				logout();
				break;
			case R.id.ll_black_list: // 黑名单列表
				startActivity(new Intent(getActivity(), BlacklistActivity.class));
				break;
			case R.id.ll_diagnose:
				startActivity(new Intent(getActivity(), DiagnoseActivity.class));
				break;

			case R.id.ll_user_profile:
				startActivity(new Intent(getActivity(), UserProfileActivity.class).putExtra("setting", true));
				break;

			case R.id.check_new_version:
				checkeNewVersion();
				break;
			default:
				break;
		}
	}


	void checkeNewVersion(){
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
				switch (updateStatus) {
					case UpdateStatus.Yes: // has update
						UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
						break;
					case UpdateStatus.No: // has no update
						Snackbar.make(check_new_version, "当前版本是最新版本", Snackbar.LENGTH_SHORT).show();
						break;
					case UpdateStatus.NoneWifi: // none wifi
						Snackbar.make(check_new_version, "没有wifi连接， 只在wifi下更新", Snackbar.LENGTH_SHORT).show();
						break;
					case UpdateStatus.Timeout: // time out
						Snackbar.make(check_new_version, "连接超时", Snackbar.LENGTH_SHORT).show();
						break;
				}
			}
		});
		UmengUpdateAgent.update(getActivity());
	}

	void logout() {
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st = getResources().getString(R.string.Are_logged_out);
		pd.setMessage(st);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		DemoHXSDKHelper.getInstance().logout(true,new EMCallBack() {
			
			@Override
			public void onSuccess() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						if (pd.isShowing()) {
							pd.dismiss();
							// 重新显示登陆页面
							 getActivity().finish();
							startActivity(new Intent(getActivity(), LoginActivity.class));
						}
					}
				});
			}
			
			@Override
			public void onProgress(int progress, String status) {
				
			}
			
			@Override
			public void onError(int code, String message) {
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						pd.dismiss();
						Snackbar.make(check_new_version, "unbind devicetokens failed", Snackbar.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
        	outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
        	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }
}
