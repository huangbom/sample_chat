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
package com.hzx.luoyechat.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.db.InviteMessgeDao;
import com.hzx.luoyechat.domain.InviteMessage;
import com.hzx.luoyechat.domain.InviteMessage.InviteMesageStatus;
import com.hzx.luoyechat.widget.FrameButton;

import java.util.List;

public class NewFriendsMsgAdapter extends ArrayAdapter<InviteMessage> {

	private Context context;
	private InviteMessgeDao messgeDao;

	public NewFriendsMsgAdapter(Context context, int textViewResourceId, List<InviteMessage> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		messgeDao = new InviteMessgeDao(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.row_invite_msg, null);
			holder.avator = (ImageView) convertView.findViewById(R.id.avatar);
			holder.reason = (TextView) convertView.findViewById(R.id.message);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.groupContainer = (LinearLayout) convertView.findViewById(R.id.ll_group);
			holder.groupname = (TextView) convertView.findViewById(R.id.tv_groupName);
//			 holder.time = (TextView) convertView.findViewById(R.id.time);

			holder.status  = (FrameButton) convertView.findViewById(R.id.user_state);
			holder.refused = (FrameButton) convertView.findViewById(R.id.user_refused);
			holder.status.setLoadInfoVisible(View.GONE);
			holder.refused.setLoadInfoVisible(View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String str1 = context.getResources().getString(R.string.Has_agreed_to_your_friend_request);
		String str2 = context.getResources().getString(R.string.agree);
		String refus = context.getResources().getString(R.string.refuse);
		
		String str3 = context.getResources().getString(R.string.Request_to_add_you_as_a_friend);
		String str4 = context.getResources().getString(R.string.Apply_to_the_group_of);
		String str5 = context.getResources().getString(R.string.Has_agreed_to);
		String str6 = context.getResources().getString(R.string.Has_refused_to);
		final InviteMessage msg = getItem(position);
		if (msg != null) {
			if(msg.getGroupId() != null){ // 显示群聊提示
				holder.groupContainer.setVisibility(View.VISIBLE);
				holder.groupname.setText(msg.getGroupName());
			} else{
				holder.groupContainer.setVisibility(View.GONE);
			}

			holder.reason.setText(msg.getReason());
			holder.name.setText(msg.getFrom());
//			 holder.time.setText(DateUtils.getTimestampString(new
//					 Date(msg.getTime())));
			if (msg.getStatus() == InviteMesageStatus.BEAGREED) {
				holder.status.setVisibility(View.INVISIBLE);
				holder.refused.setVisibility(View.INVISIBLE);
				holder.reason.setText(str1);
			} else {
				if (msg.getStatus() == InviteMesageStatus.BEINVITEED || msg.getStatus() == InviteMesageStatus.BEAPPLYED) {
					holder.status.setVisibility(View.VISIBLE);
					holder.refused.setVisibility(View.VISIBLE);
					holder.status.getFirstButton().setEnabled(true);
					holder.refused.getFirstButton().setEnabled(true);

					holder.status.setFirstText(str2);
					if (msg.getStatus() == InviteMesageStatus.BEINVITEED) {
						if (msg.getReason() == null) {
							// 如果没写理由
							holder.reason.setText(str3);
						}
					} else { //入群申请
						if (TextUtils.isEmpty(msg.getReason())) {
							holder.reason.setText(str4 + msg.getGroupName());
						}
					}
					holder.refused.setFirstText(refus);
					// 设置点击事件
					holder.status.setOnFistBtnListener(new FrameButton.OnFirstBtnClickListener() {

						@Override
						public void onFirstClick(View view) {
							holder.refused.getFirstButton().setEnabled(false);
							holder.status.setLoadInfoVisible(View.GONE);
							// 同意别人发的好友请求
							acceptInvitation(holder.status, msg);
						}

					});
					holder.refused.setOnFistBtnListener(new FrameButton.OnFirstBtnClickListener() {
						@Override
						public void onFirstClick(View view) {
							holder.status.getFirstButton().setEnabled(false);
							refuseInvitation(holder.refused, msg);
						}

					});

				} else if (msg.getStatus() == InviteMesageStatus.AGREED) {
					holder.refused.setVisibility(View.INVISIBLE);
					holder.status.setFirstText(str5);
					holder.status.getFirstButton().setEnabled(false);
					holder.refused.getFirstButton().setEnabled(false);

				} else if (msg.getStatus() == InviteMesageStatus.REFUSED) {
					holder.status.setFirstText(str6);
					holder.status.getFirstButton().setEnabled(false);

					holder.refused.setVisibility(View.INVISIBLE);
					holder.refused.getFirstButton().setEnabled(false);
				}
			}

		}

		return convertView;
	}

	/**
	 * 同意好友请求或者群申请
	 */
	private void acceptInvitation(final FrameButton button, final InviteMessage msg) {
//		final ProgressDialog pd = new ProgressDialog(context);
		String str1 = context.getResources().getString(R.string.Are_agree_with);
		final String str2 = context.getResources().getString(R.string.Has_agreed_to);
		final String str3 = context.getResources().getString(R.string.Agree_with_failure);
//		pd.setMessage(str1);
//		pd.setCanceledOnTouchOutside(false);
//		pd.show();

		new Thread(new Runnable() {
			public void run() {
				// 调用sdk的同意方法
				try {
					if(msg.getGroupId() == null) //同意好友请求
						EMChatManager.getInstance().acceptInvitation(msg.getFrom());
					else //同意加群申请
					    EMGroupManager.getInstance().acceptApplication(msg.getFrom(), msg.getGroupId());
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
//							pd.dismiss();
							msg.setStatus(InviteMesageStatus.AGREED);
							// 更新db
							ContentValues values = new ContentValues();
							values.put(InviteMessgeDao.COLUMN_NAME_STATUS, msg.getStatus().ordinal());
							messgeDao.updateMessage(msg.getId(), values);

							button.setBackgroundDrawable(null);
							button.getFirstButton().setEnabled(false);
							button.setNewFirstText(str2);
							button.showFirstBotton();
						}
					});
				} catch (final Exception e) {
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
//							pd.dismiss();
							button.showFirstBotton();

							Toast.makeText(context, str3 + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});

				}
			}
		}).start();
	}

	private void refuseInvitation(final FrameButton button, final InviteMessage msg) {
		final String str2 = context.getResources().getString(R.string.Has_refused_to);
		final String str3 = "拒绝失败";

		new Thread(new Runnable() {
			public void run() {
				try {
					if(msg.getGroupId() == null)
						EMChatManager.getInstance().refuseInvitation(msg.getFrom());
					((Activity) context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							pd.dismiss();
							msg.setStatus(InviteMesageStatus.REFUSED);
							// 更新db
							ContentValues values = new ContentValues();
							values.put(InviteMessgeDao.COLUMN_NAME_STATUS, msg.getStatus().ordinal());
							messgeDao.updateMessage(msg.getId(), values);

							button.setNewFirstText(str2);
							button.getFirstButton().setEnabled(false);
							button.showFirstBotton();
						}
					});
				} catch (final Exception e) {
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							button.showFirstBotton();
							Toast.makeText(context, str3 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}


	private static class ViewHolder {
		ImageView avator;
		TextView name;
		TextView reason;
		LinearLayout groupContainer;
		TextView groupname;
//		 TextView time;
		FrameButton refused;
		FrameButton status;
	}

}
