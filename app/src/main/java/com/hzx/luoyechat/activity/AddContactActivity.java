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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.hzx.luoyechat.DemoApplication;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.widget.FrameButton;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * 添加好友
 */
public class AddContactActivity extends BaseActivity{
	@Bind(R.id.edit_note)
	public EditText editText;
	@Bind(R.id.ll_user)
	public LinearLayout searchedUserLayout;
	@Bind(R.id.name)
	public TextView nameText;

	@Bind(R.id.avatar)
	public ImageView avatar;

	@Bind(R.id.indicator)
	FrameButton addContact;

	@BindString(R.string.Please_enter_a_username)
	String Please_enter_a_username;

	@BindString(R.string.send_successful)
	String sendSuccessful;
	@BindString(R.string.Request_add_buddy_failure)
	String Request_add_buddy_failure;

	@BindString(R.string.Add_a_friend)
	String Add_a_friend;

	private InputMethodManager inputMethodManager;
	private String toAddUsername;
	private ProgressDialog progressDialog;
	android.support.v7.app.AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflateContentView(R.layout.activity_add_contact,R.string.add_friend, true);

		ButterKnife.bind(this);

		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		dialog = new android.support.v7.app.AlertDialog.Builder(this)
				.setTitle("提示")
				.setNegativeButton(R.string.ok, null).create();

		addContact.setLoadInfoVisible(View.GONE);
		addContact.setOnFistBtnListener(new FrameButton.OnFirstBtnClickListener() {
			@Override
			public void onFirstClick(View view) {
				addContact(view);
			}
		});
	}
	
	/** 查找contact */
	public void searchContact( ) {
		final String name = editText.getText().toString().trim();

			if(TextUtils.isEmpty(name)) {
				editText.setError(Please_enter_a_username);
				searchedUserLayout.setVisibility(View.GONE);
				return;
			}
			toAddUsername = name;

			searchedUserLayout.setVisibility(View.VISIBLE);
			nameText.setText(toAddUsername);

	}


	/**
	 *  添加contact
	 */
	public void addContact(View view){
		if(DemoApplication.getInstance().getUserName().equals(nameText.getText().toString())){
			String str = getString(R.string.not_add_myself);
			dialog.setMessage(str);
			dialog.show();
			addContact.showFirstBotton();
			return;
		}

		if(((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().containsKey(nameText.getText().toString())){
			String strin = getString(R.string.This_user_is_already_your_friend);
		    //提示已在好友列表中，无需添加
		    if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
		        strin = "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可";
		    }

			dialog.setMessage(strin);
			dialog.show();
			addContact.showFirstBotton();
			return;
		}

		new Thread(new Runnable() {
			public void run() {
				Message message = mHandle.obtainMessage();

				try {
					//demo写死了个reason，实际应该让用户手动填入
					EMContactManager.getInstance().addContact(toAddUsername, Add_a_friend);
					message.obj = sendSuccessful;
				} catch (final Exception e) {
					message.obj = Request_add_buddy_failure;
				}

				mHandle.sendMessage(message);
			}
		}).start();
	}

	Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
			addContact.showFirstBotton();
			super.handleMessage(msg);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(R.string.button_search)
				.setIcon(R.mipmap.abc_ic_search_api_mtrl_alpha)
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						searchContact();
						return false;
					}
				})
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

}
