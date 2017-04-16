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
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupInfo;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.widget.FrameButton;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GroupSimpleDetailActivity extends BaseActivity {
	@Bind(R.id.btn_add_to_group)
	FrameButton btn_add_group;

	@Bind(R.id.tv_admin)
	TextView tv_admin;

	@Bind(R.id.name)
	TextView tv_name;

	@Bind(R.id.tv_introduction)
	TextView tv_introduction;

	EMGroup group;

	String groupid;

	@Bind(R.id.loading)
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflateContentView(R.layout.activity_group_simle_details, R.string.Group_chat_information, true);

		ButterKnife.bind(this);

		EMGroupInfo groupInfo = (EMGroupInfo) getIntent().getSerializableExtra("groupinfo");
		String groupname = null;
		if(groupInfo != null){
		    groupname = groupInfo.getGroupName();
		    groupid = groupInfo.getGroupId();
		}else{
		    group = PublicGroupsSeachActivity.searchedGroup;
		    if(group == null)
		        return;
		    groupname = group.getGroupName();
		    groupid = group.getGroupId();
		}
		
		tv_name.setText(groupname);
		
		
		if(group != null){
		    showGroupDetail();
		    return;
		}
		new Thread(new Runnable() {

			public void run() {
				//从服务器获取详情
				try {
					group = EMGroupManager.getInstance().getGroupFromServer(groupid);
					runOnUiThread(new Runnable() {
						public void run() {
							showGroupDetail();
						}
					});
				} catch (final EaseMobException e) {
					e.printStackTrace();
					final String st1 = getResources().getString(R.string.Failed_to_get_group_chat_information);
					runOnUiThread(new Runnable() {
						public void run() {
							progressBar.setVisibility(View.INVISIBLE);
							Snackbar.make(tv_name, st1 + e.getMessage(), Snackbar.LENGTH_LONG).show();
						}
					});
				}
				
			}
		}).start();

		btn_add_group.setOnFistBtnListener(new FrameButton.OnFirstBtnClickListener() {
			@Override
			public void onFirstClick(View view) {
				addToGroup(view);
			}
		});
	}
	
	//加入群聊
	public void addToGroup(View view){
		String st1 = getResources().getString(R.string.Is_sending_a_request);
		final String st2 = getResources().getString(R.string.Request_to_join);
		final String st3 = getResources().getString(R.string.send_the_request_is);
		final String st4 = getResources().getString(R.string.Join_the_group_chat);
		final String st5 = getResources().getString(R.string.Failed_to_join_the_group_chat);

		btn_add_group.setLoadInfoText(st1);
		new Thread(new Runnable() {
			public void run() {
				try {
					//如果是membersOnly的群，需要申请加入，不能直接join
					if(group.isMembersOnly()){
					    EMGroupManager.getInstance().applyJoinToGroup(groupid, st2);
					}else{
					    EMGroupManager.getInstance().joinGroup(groupid);
					}
					runOnUiThread(new Runnable() {
						public void run() {

							if(group.isMembersOnly())
								Snackbar.make(tv_name,st3,Snackbar.LENGTH_SHORT).show();
							else
								Snackbar.make(tv_name,st4,Snackbar.LENGTH_SHORT).show();

							btn_add_group.showFirstBotton();
							Button firstButton = btn_add_group.getFirstButton();
							firstButton.setEnabled(false);

						}
					});
				} catch (final EaseMobException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							btn_add_group.showFirstBotton();
							Snackbar.make(tv_name,st5+e.getMessage(),Snackbar.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}
	
     private void showGroupDetail() {
         progressBar.setVisibility(View.INVISIBLE);
         //获取详情成功，并且自己不在群中，才让加入群聊按钮可点击
         if(!group.getMembers().contains(EMChatManager.getInstance().getCurrentUser()))
             btn_add_group.setVisibility(View.VISIBLE);

         tv_name.setText(group.getGroupName());
         tv_admin.setText(group.getOwner());
         tv_introduction.setText(group.getDescription());
     }
}
