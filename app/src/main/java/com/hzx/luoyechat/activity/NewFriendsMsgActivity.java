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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hzx.luoyechat.Constant;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.adapter.NewFriendsMsgAdapter;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.db.InviteMessgeDao;
import com.hzx.luoyechat.domain.InviteMessage;

import java.util.List;

/**
 * 申请与通知
 */
public class NewFriendsMsgActivity extends BaseActivity {
	private ListView listView;
	List<InviteMessage> msgs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflateContentView(R.layout.activity_new_friends_msg, R.string.Application_and_notify,true);

		listView = (ListView) findViewById(R.id.list);
		InviteMessgeDao dao = new InviteMessgeDao(this);
		  msgs = dao.getMessagesList();
		if (null == msgs || msgs.isEmpty()){
			setEmptyInfo("没有新消息~");
			return;
		}
		//设置adapter
		NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(this, 1, msgs); 
		listView.setAdapter(adapter);
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME).setUnreadMsgCount(0);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				InviteMessage inviteMessage = msgs.get(i);
				Intent intent = new Intent(NewFriendsMsgActivity.this, UserProfileActivity.class);
				intent.putExtra("username", inviteMessage.getFrom());
				startActivity(intent);
			}	});
	}

}
