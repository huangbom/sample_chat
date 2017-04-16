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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.chat.EMGroupManager;
import com.hzx.luoyechat.R;
import com.easemob.exceptions.EaseMobException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 新建群组
 */
public class NewGroupActivity extends BaseActivity {
	@Bind(R.id.edit_group_name)
	EditText groupNameEditText;

	ProgressDialog progressDialog;

	@Bind(R.id.edit_group_introduction)
	EditText introductionEditText;

	@Bind(R.id.cb_public)
	CheckBox checkBox;

	@Bind(R.id.cb_member_inviter)
	CheckBox memberCheckbox;

	@Bind(R.id.ll_open_invite)
	LinearLayout openInviteContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflateContentView(R.layout.activity_new_group, R.string.The_new_group_chat, true);

		ButterKnife.bind(this);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					openInviteContainer.setVisibility(View.INVISIBLE);
				}else{
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		final String st2 = getResources().getString(R.string.Failed_to_create_groups);
		if (resultCode == RESULT_OK) {
			//新建群组
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(st1);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					// 调用sdk创建群组方法
					String groupName = groupNameEditText.getText().toString().trim();
					String desc = introductionEditText.getText().toString();
					String[] members = data.getStringArrayExtra("newmembers");
					try {
						if(checkBox.isChecked()){
							//创建公开群，此种方式创建的群，可以自由加入
							//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						    EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
						}else{
							//创建不公开群
						    EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
						}
						runOnUiThread(new Runnable() {
							public void run() {
								progressDialog.dismiss();
								setResult(RESULT_OK);
								finish();
							}
						});
					} catch (final EaseMobException e) {
						runOnUiThread(new Runnable() {
							public void run() {
								progressDialog.dismiss();
								Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
							}
						});
					}
					
				}
			}).start();
		}
	}


	@OnClick(R.id.btn_new_group)
	public void newGroup(View view) {
		String name = groupNameEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
			groupNameEditText.setError(str6);

		} else {
			// 进通讯录选人
			startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
		}
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.newGroup)
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						newGroup();
						return true;
					}
				})
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}*/
}
