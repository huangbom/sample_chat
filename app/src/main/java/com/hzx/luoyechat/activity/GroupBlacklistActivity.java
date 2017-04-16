package com.hzx.luoyechat.activity;

import java.util.Collections;
import java.util.List;

import com.easemob.chat.EMGroupManager;
import com.hzx.luoyechat.R;
import com.easemob.exceptions.EaseMobException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

/**群聊黑名单列表*/
public class GroupBlacklistActivity extends BaseActivity {
	@Bind(R.id.list) ListView listView;

	@Bind(R.id.progressBar) ProgressBar progressBar;

	private BlacklistAdapter adapter;
	private String groupId;

	@BindString(R.string.get_failed_please_check)
	String st1;

	final static int UPDATE_ADAPTER = 1;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		inflateContentView(R.layout.activity_group_blacklist, R.string.blacklist,true);

		ButterKnife.bind(this);

		groupId = getIntent().getStringExtra("groupId");
		// 注册上下文菜单
		registerForContextMenu(listView);

		exeTask(new Runnable() {
			public void run() {
				Message message = mHandle.obtainMessage();

				try {
							List<String> blockedList = EMGroupManager.getInstance().getBlockedUsers(groupId);
							if (blockedList != null && !blockedList.isEmpty()) {
								Collections.sort(blockedList);
								adapter = new BlacklistAdapter(GroupBlacklistActivity.this, 1, blockedList);
								message.what = UPDATE_ADAPTER;

							} else {
 								message.what = 0;
								message.obj = "没有黑名单";
							}

						} catch (EaseMobException e) {
							message.what = 0;
							message.obj = st1;
						}
						mHandle.sendMessage(message);
					}
				});
	}

	Handler mHandle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (UPDATE_ADAPTER == msg.what){
				listView.setAdapter(adapter);
			}else {
				String str = msg.obj.toString();
				setEmptyInfo(str);
			}
				progressBar.setVisibility(View.GONE);
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.remove_from_blacklist, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.remove) {
			final String tobeRemoveUser = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			// 移出黑名单
			removeOutBlacklist(tobeRemoveUser);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	/**
	 * 移出黑民单
	 */
	void removeOutBlacklist(final String tobeRemoveUser) {
		final String st2 = getResources().getString(R.string.Removed_from_the_failure);
		try {
			// 移出黑民单
		    EMGroupManager.getInstance().unblockUser(groupId, tobeRemoveUser);
			adapter.remove(tobeRemoveUser);
		} catch (EaseMobException e) {
			e.printStackTrace();
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), st2, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	/**
	 * adapter
	 */
	private class BlacklistAdapter extends ArrayAdapter<String> {

		public BlacklistAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.row_contact, null);
			}

			TextView name = (TextView) convertView.findViewById(R.id.name);
			name.setText(getItem(position));

			return convertView;
		}

	}
	
}
