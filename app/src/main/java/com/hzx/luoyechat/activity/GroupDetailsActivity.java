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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.NetUtils;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.utils.UserUtils;
import com.hzx.luoyechat.widget.ExpandGridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**群组详情*/
public class GroupDetailsActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "GroupDetailsActivity";
	private static final int REQUEST_CODE_ADD_USER = 0;
	private static final int REQUEST_CODE_EDIT_GROUPNAME = 5;

	String longClickUsername = null;

	private String groupId;
	@Bind(R.id.progressBar)
	ProgressBar loadingPB;
	@Bind(R.id.btn_exit_grp)
	Button exitBtn;
	@Bind(R.id.btn_exitdel_grp)
	Button deleteBtn;
	EMGroup group;
	GridAdapter adapter;
//	private int referenceWidth;
//	private int referenceHeight;
	private ProgressDialog progressDialog;

	@Bind(R.id.switch_groupmsg)
	SwitchCompat switch_groupmsg;

	public static GroupDetailsActivity instance;

	@BindString(R.string.people)
	String st ;

	@Bind(R.id.gridview)
	ExpandGridView userGridview;
	// 清空所有聊天记录
	@Bind(R.id.clear_all_history)
	RelativeLayout clearAllHistory;

	@Bind(R.id.rl_blacklist)
	RelativeLayout blacklistLayout;

	@Bind(R.id.rl_change_group_name) RelativeLayout changeGroupNameLayout;
    @Bind(R.id.rl_group_id) RelativeLayout idLayout;
    @Bind(R.id.tv_group_id_value) TextView idText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // 获取传过来的groupid
        groupId = getIntent().getStringExtra("groupId");
        group = EMGroupManager.getInstance().getGroup(groupId);

        if(group == null){
            finish();
            return;
        }

		inflateContentView(R.layout.activity_group_details, null, true);

		ButterKnife.bind(this);

		instance = this;

		idLayout.setVisibility(View.VISIBLE);

		idText.setText(groupId);
		if (group.getOwner() == null || "".equals(group.getOwner())
				|| !group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
			exitBtn.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.GONE);
			blacklistLayout.setVisibility(View.GONE);
			changeGroupNameLayout.setVisibility(View.GONE);
		}
		// 如果自己是群主，显示解散按钮
		if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
			exitBtn.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.VISIBLE);
		}
		
		setToolbarTitle(group.getGroupName() + "(" + group.getAffiliationsCount() + st);

		List<String> members = new ArrayList<>();
		members.addAll(group.getMembers());
		
		adapter = new GridAdapter(this, R.layout.grid, members);
		userGridview.setAdapter(adapter);

		// 保证每次进详情看到的都是最新的group
		updateGroup();

		// 设置OnTouchListener
		userGridview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (adapter.isInDeleteMode) {
							adapter.isInDeleteMode = false;
							adapter.notifyDataSetChanged();
							return true;
						}
						break;
					default:
						break;
				}
				return false;
			}
		});

		clearAllHistory.setOnClickListener(this);
		blacklistLayout.setOnClickListener(this);
		changeGroupNameLayout.setOnClickListener(this);

		mDialog = new android.support.v7.app.AlertDialog.Builder(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String st1 = getResources().getString(R.string.being_added);
		final String st6 = getResources().getString(R.string.Modify_the_group_name_successful);
		final String st7 = getResources().getString(R.string.change_the_group_name_failed_please);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_ADD_USER:// 添加群成员
				final String[] newmembers = data.getStringArrayExtra("newmembers");
				showProgressDialog(st1);
				addMembersToGroup(newmembers);
				break;

			case REQUEST_CODE_EDIT_GROUPNAME: //修改群名称
				final String returnData = data.getStringExtra("data");
				if(!TextUtils.isEmpty(returnData)){

					showProgressDialog(R.string.is_modify_the_group_name);

					new Thread(new Runnable() {
						public void run() {
							try {
							    EMGroupManager.getInstance().changeGroupName(groupId, returnData);
								runOnUiThread(new Runnable() {
									public void run() {
										setToolbarTitle(returnData + "(" + group.getAffiliationsCount()
												+ st);
										progressDialog.dismiss();
										Toast.makeText(GroupDetailsActivity.this, st6, Toast.LENGTH_SHORT).show();
									}
								});

							} catch (EaseMobException e) {
								e.printStackTrace();
								runOnUiThread(new Runnable() {
									public void run() {
										progressDialog.dismiss();
										Toast.makeText(GroupDetailsActivity.this, st7, Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					}).start();
				}
				break;
			default:
				break;
			}
		}
	}

	void showProgressDialog(String msg){
		if (progressDialog == null) {
			String st1 = getResources().getString(R.string.being_added);
			progressDialog = new ProgressDialog(GroupDetailsActivity.this);
			progressDialog.setMessage(st1);
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.setMessage(msg);
		progressDialog.show();
	}
	void showProgressDialog(int resId){
		String string = getResources().getString(resId);
		showProgressDialog(string);
	}

	void addToBalcklist(){
		String st8 = getResources().getString(R.string.Are_moving_to_blacklist);
		showProgressDialog(st8);
		new Thread(new Runnable() {
			public void run() {
				try {
					EMGroupManager.getInstance().blockUser(groupId, longClickUsername);
					runOnUiThread(new Runnable() {
						public void run() {
							if (progressDialog.isShowing()) {
								refreshMembers();
								progressDialog.dismiss();
								Toast.makeText(GroupDetailsActivity.this,R.string.Move_into_blacklist_success,Toast.LENGTH_SHORT).show();
							}
						}
					});
				} catch (EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							if (progressDialog.isShowing()) {
								progressDialog.dismiss();
								Toast.makeText(GroupDetailsActivity.this,R.string.failed_to_move_into,Toast.LENGTH_SHORT).show();

							}
						}
					});
				}
			}
		}).start();
	}

	private void refreshMembers(){
	    adapter.clear();
        
        List<String> members = new ArrayList<String>();
        members.addAll(group.getMembers());
        adapter.addAll(members);
        
        adapter.notifyDataSetChanged();
	}

	@OnClick(R.id.switch_groupmsg)
	public void onSwitchClicked(View view){

		final boolean isChecked = switch_groupmsg.isChecked();
		if (isChecked) {

			showProgressDialog(R.string.group_is_blocked);

			exeTask(new Runnable() {
				public void run() {
					try {
						EMGroupManager.getInstance().blockGroupMessage(groupId);
						runOnUiThread(new Runnable() {
							public void run() {
								progressDialog.dismiss();
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
							public void run() {
									progressDialog.dismiss();
									Toast.makeText(GroupDetailsActivity.this,R.string.group_of_shielding,Toast.LENGTH_SHORT).show();
								switch_groupmsg.setChecked(!isChecked);
							}
						});
					}
				}
			});

		} else {
			showProgressDialog(R.string.Is_unblock);
			exeTask(new Runnable() {
				public void run() {
					try {
						EMGroupManager.getInstance().unblockGroupMessage(groupId);
						runOnUiThread(new Runnable() {
							public void run() { progressDialog.dismiss();  }
						});
					} catch (Exception e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
							public void run() {
									progressDialog.dismiss();
									Toast.makeText(GroupDetailsActivity.this, R.string.remove_group_of, Toast.LENGTH_SHORT).show();
									switch_groupmsg.setChecked(!isChecked);
							}
						});
					}
				}
			});
		}
	}

	/**
	 * 点击退出群组按钮
	 */
	public void exitGroup(View view) {
		showDialog(R.string.exit_group_hint, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				 showProgressDialog(R.string.is_quit_the_group_chat);

				exitGrop();
			}
		});
	}


	/**
	 * 点击解散群组按钮
	 */
	public void exitDeleteGroup(View view) {
		showDialog(R.string.dissolution_group_hint, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				showProgressDialog(R.string.chatting_is_dissolution);
				deleteGrop();
			}
		});
	}


	/**
	 * 退出群组
	 */
	private void exitGrop() {
		new Thread(new Runnable() {
			public void run() {
				try {
				    EMGroupManager.getInstance().exitFromGroup(groupId);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
							if(ChatActivity.activityInstance != null)
							    ChatActivity.activityInstance.finish();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Exit_the_group_chat_failure) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	/**
	 * 解散群组
	 */
	private void deleteGrop() {
		final String st5 = getResources().getString(R.string.Dissolve_group_chat_tofail);
		new Thread(new Runnable() {
			public void run() {
				try {
				    EMGroupManager.getInstance().exitAndDeleteGroup(groupId);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
							if(ChatActivity.activityInstance != null)
							    ChatActivity.activityInstance.finish();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(getApplicationContext(), st5 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	/**
	 * 增加群成员
	 */
	private void addMembersToGroup(final String[] newmembers) {
		final String st6 = getResources().getString(R.string.Add_group_members_fail);
		new Thread(new Runnable() {
			
			public void run() {
				try {
					// 创建者调用add方法
					if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
					    EMGroupManager.getInstance().addUsersToGroup(groupId, newmembers);
					} else {
						// 一般成员调用invite方法
					    EMGroupManager.getInstance().inviteUser(groupId, newmembers, null);
					}
					runOnUiThread(new Runnable() {
						public void run() {
						    refreshMembers();
							setToolbarTitle(group.getGroupName() + "(" + group.getAffiliationsCount()
									+ st);
							progressDialog.dismiss();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();

							Toast.makeText(getApplicationContext(), st6 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.clear_all_history: // 清空聊天记录
			showDialog(R.string.sure_to_empty_this, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					showProgressDialog(R.string.are_empty_group_of_news);

					EMChatManager.getInstance().clearConversation(group.getGroupId());
					progressDialog.dismiss();
				}
			});
			break;

		case R.id.rl_blacklist: // 黑名单列表
			startActivity(new Intent(GroupDetailsActivity.this, GroupBlacklistActivity.class).putExtra("groupId", groupId));
			break;

		case R.id.rl_change_group_name:
			startActivityForResult(new Intent(this, EditActivity.class).putExtra("data", group.getGroupName()), REQUEST_CODE_EDIT_GROUPNAME);
			break;

		default:
			break;
		}

	}


	/**
	 * 群组成员gridadapter
	 */
	private class GridAdapter extends ArrayAdapter<String> {

		private int res;
		public boolean isInDeleteMode;
		private List<String> objects;

		public GridAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
			res = textViewResourceId;
			isInDeleteMode = false;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
		    ViewHolder holder = null;
			if (convertView == null) {
			    holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(res, null);
				holder.imageView = (ImageView) convertView.findViewById(R.id.iv_avatar);
				holder.textView = (TextView) convertView.findViewById(R.id.tv_name);
				holder.badgeDeleteView = (ImageView) convertView.findViewById(R.id.badge_delete);
				convertView.setTag(holder);
			}else{
			    holder = (ViewHolder) convertView.getTag();
			}
			final LinearLayout button = (LinearLayout) convertView.findViewById(R.id.button_avatar);
			// 最后一个item，减人按钮
			if (position == getCount() - 1) {
			    holder.textView.setText("");
				// 设置成删除按钮
			    holder.imageView.setImageResource(R.drawable.smiley_minus_btn);
//				button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.smiley_minus_btn, 0, 0);
				// 如果不是创建者或者没有相应权限，不提供加减人按钮
				if (!group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
					// if current user is not group admin, hide add/remove btn
					convertView.setVisibility(View.INVISIBLE);
				} else { // 显示删除按钮
					if (isInDeleteMode) {
						// 正处于删除模式下，隐藏删除按钮
						convertView.setVisibility(View.INVISIBLE);
					} else {
						// 正常模式
						convertView.setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
					}
					final String st10 = getResources().getString(R.string.The_delete_button_is_clicked);
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							EMLog.d(TAG, st10);
							isInDeleteMode = true;
							notifyDataSetChanged();
						}
					});
				}
			} else if (position == getCount() - 2) { // 添加群组成员按钮
			    holder.textView.setText("");
			    holder.imageView.setImageResource(R.drawable.smiley_add_btn);
//				button.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.smiley_add_btn, 0, 0);
				// 如果不是创建者或者没有相应权限
				if (!group.isAllowInvites() && !group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
					// if current user is not group admin, hide add/remove btn
					convertView.setVisibility(View.INVISIBLE);
				} else {
					// 正处于删除模式下,隐藏添加按钮
					if (isInDeleteMode) {
						convertView.setVisibility(View.INVISIBLE);
					} else {
						convertView.setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
					}
					final String st11 = getResources().getString(R.string.Add_a_button_was_clicked);
					button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							EMLog.d(TAG, st11);
							// 进入选人页面
							startActivityForResult(
									(new Intent(GroupDetailsActivity.this, GroupPickContactsActivity.class).putExtra("groupId", groupId)),
									REQUEST_CODE_ADD_USER);
						}
					});
				}
			} else { // 普通item，显示群组成员
				final String username = getItem(position);
				convertView.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);
//				Drawable avatar = getResources().getDrawable(R.drawable.default_avatar);
//				avatar.setBounds(0, 0, referenceWidth, referenceHeight);
//				button.setCompoundDrawables(null, avatar, null, null);
				holder.textView.setText(username);
				UserUtils.setUserAvatar(getContext(), username, holder.imageView,R.mipmap.default_avatar_black);
				// demo群组成员的头像都用默认头像，需由开发者自己去设置头像
				if (isInDeleteMode) {
					// 如果是删除模式下，显示减人图标
					convertView.findViewById(R.id.badge_delete).setVisibility(View.VISIBLE);
				} else {
					convertView.findViewById(R.id.badge_delete).setVisibility(View.INVISIBLE);
				}
				final String st12 = getResources().getString(R.string.not_delete_myself);
				final String st13 = getResources().getString(R.string.Are_removed);
				final String st14 = getResources().getString(R.string.Delete_failed);
				final String st15 = getResources().getString(R.string.confirm_the_members);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isInDeleteMode) {
							// 如果是删除自己，return
							if (EMChatManager.getInstance().getCurrentUser().equals(username)) {
								showDialog(st12, null);
								return;
							}
							if (!NetUtils.hasNetwork(getApplicationContext())) {
								Toast.makeText(GroupDetailsActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
								return;
							}
							EMLog.d("group", "remove user from group:" + username);
							deleteMembersFromGroup(username);
						} else {
							Intent intent = new Intent(GroupDetailsActivity.this,UserProfileActivity.class);
							intent.putExtra("username",getItem(position));
							startActivity(intent);
						}
					}

					/**
					 * 删除群成员
					 */
					protected void deleteMembersFromGroup(final String username) {
						final ProgressDialog deleteDialog = new ProgressDialog(GroupDetailsActivity.this);
						deleteDialog.setMessage(st13);
						deleteDialog.setCanceledOnTouchOutside(false);
						deleteDialog.show();
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									// 删除被选中的成员
								    EMGroupManager.getInstance().removeUserFromGroup(groupId, username);
									isInDeleteMode = false;
									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											deleteDialog.dismiss();
											refreshMembers();

											setToolbarTitle(group.getGroupName() + "("
													+ group.getAffiliationsCount() + st);
										}
									});
								} catch (final Exception e) {
									deleteDialog.dismiss();
									runOnUiThread(new Runnable() {
										public void run() {
											Toast.makeText(getApplicationContext(), st14 + e.getMessage(), Toast.LENGTH_LONG).show();
										}
									});
								}

							}
						}).start();
					}
				});

				button.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
					    if(EMChatManager.getInstance().getCurrentUser().equals(username))
					        return true;
						if (group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
							showDialog(st15, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									addToBalcklist();
								}
							});

							longClickUsername = username;
						}
						return false;
					}
				});
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return super.getCount() + 2;
		}
	}

	protected void updateGroup() {
		new Thread(new Runnable() {
			public void run() {
				try {
					final EMGroup returnGroup = EMGroupManager.getInstance().getGroupFromServer(groupId);
					// 更新本地数据
					EMGroupManager.getInstance().createOrUpdateLocalGroup(returnGroup);

					runOnUiThread(new Runnable() {
						public void run() {
							setToolbarTitle(group.getGroupName() + "(" + group.getAffiliationsCount()
									+ ")");
							loadingPB.setVisibility(View.INVISIBLE);
							refreshMembers();
							if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
								// 显示解散按钮
								exitBtn.setVisibility(View.GONE);
								deleteBtn.setVisibility(View.VISIBLE);
							} else {
								// 显示退出按钮
								exitBtn.setVisibility(View.VISIBLE);
								deleteBtn.setVisibility(View.GONE);
							}

							// update block
							EMLog.d(TAG, "group msg is blocked:" + group.getMsgBlocked());
								switch_groupmsg.setChecked(group.isMsgBlocked());
						}
					});

				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							loadingPB.setVisibility(View.INVISIBLE);
						}
					});
				}
			}
		}).start();
	}

	public void back(View view) {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}
	
	private static class ViewHolder{
	    ImageView imageView;
	    TextView textView;
	    ImageView badgeDeleteView;
	}

}
