package com.hzx.luoyechat.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.hzx.luoyechat.Constant;
import com.hzx.luoyechat.DemoApplication;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.domain.User;
import com.hzx.luoyechat.utils.UserUtils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
*用户简介
 */
public class UserProfileActivity extends BaseActivity implements OnClickListener{

	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private static final int REQUESTCODE_NICK = 3;

	@Bind(R.id.user_head_avatar)
	ImageView headAvatar;

	@Bind(R.id.ic_right_arrow)
	ImageView iconRightArrow;


	@Bind(R.id.user_nickname)
	TextView tvNickName;

	@Bind(R.id.rl_user_head_avatar)
	RelativeLayout rlUserHead;

	@Bind(R.id.user_username)
	TextView tvUsername;

	private ProgressDialog dialog;

	@Bind(R.id.rl_nickname)
	RelativeLayout rlNickName;

	@Bind(R.id.add_friend)
	Button add_friend;

	@Bind(R.id.send_msg)
	Button send_msg;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		inflateContentView(R.layout.activity_user_profile, R.string.title_user_profile,true);
		ButterKnife.bind(this);
		initListener();
	}

	private void initListener() {
		Intent intent = getIntent();
		final String username = intent.getStringExtra("username");
		boolean enableUpdate = intent.getBooleanExtra("setting", false);
		if (enableUpdate) {
			iconRightArrow.setVisibility(View.VISIBLE);

			rlNickName.setOnClickListener(this);
			rlUserHead.setOnClickListener(this);
//			headAvatar.setOnClickListener(this);
		} else {
			iconRightArrow.setVisibility(View.INVISIBLE);
		}
		if (username == null) {
			tvUsername.setText(EMChatManager.getInstance().getCurrentUser());
			UserUtils.setCurrentUserNick(tvNickName);
			UserUtils.setCurrentUserAvatar(this, headAvatar);
		} else if(Constant.CHAT_KEFU.equals(username)){
			tvUsername.setText(R.string.robot_chat);
			tvNickName.setText(R.string.robot_chat);
			headAvatar.setImageResource(R.mipmap.kefu);
		}else if (username.equals(EMChatManager.getInstance().getCurrentUser())) {
			tvUsername.setText(EMChatManager.getInstance().getCurrentUser());
			UserUtils.setCurrentUserNick(tvNickName);
			UserUtils.setCurrentUserAvatar(this, headAvatar);
		} else {
			tvUsername.setText(username);
			UserUtils.setUserNick(username, tvNickName);
			UserUtils.setUserAvatar(this, username, headAvatar);
			asyncFetchUserInfo(username);

			User user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(username);
			if (user == null){
				add_friend.setVisibility(View.VISIBLE);
				add_friend.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						addContact(view,username);
					}
				});
			}else {
				send_msg.setVisibility(View.VISIBLE);
				send_msg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(new Intent(UserProfileActivity.this, ChatActivity.class).putExtra("userId", username));
						finish();
					}
				});
			}
		}
	}

	private void addContact(View view, final String username){
		if(DemoApplication.getInstance().getUserName().equals(username)){
			String str = getString(R.string.not_add_myself);
			dialog.setMessage(str);
			dialog.show();
			return;
		}

		if(((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().containsKey(username)){
			String strin = getString(R.string.This_user_is_already_your_friend);
			//提示已在好友列表中，无需添加
			if(EMContactManager.getInstance().getBlackListUsernames().contains(username)){
				strin = "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可";
			}

			dialog.setMessage(strin);
			dialog.show();
			return;
		}

		progressDialog = new ProgressDialog(this);
		String stri = getResources().getString(R.string.Is_sending_a_request);
		progressDialog.setMessage(stri);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();

		new Thread(new Runnable() {
			public void run() {

				try {
					//demo写死了个reason，实际应该让用户手动填入
					String s = getResources().getString(R.string.Add_a_friend);
					EMContactManager.getInstance().addContact(username, s);
					runOnUiThread(new Runnable() {
						public void run() {
							if (progressDialog.isShowing())
								progressDialog.dismiss();
							String s1 = getResources().getString(R.string.send_successful);
							Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							if (progressDialog.isShowing())
								progressDialog.dismiss();
							String s2 = getResources().getString(R.string.Request_add_buddy_failure);
							Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
	ProgressDialog progressDialog;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.user_head_avatar:
		case R.id.rl_user_head_avatar:
			uploadHeadPhoto();
			break;
		case R.id.rl_nickname:
			Intent intent = new Intent(this, EditActivity.class)
					.putExtra("title",getString(R.string.setting_nickname))
					.putExtra("data",tvNickName.getText().toString().trim());

			startActivityForResult(intent,REQUESTCODE_NICK);

//			final EditText editText = new EditText(this);
//			new android.support.v7.app.AlertDialog.Builder(this)
//					.setTitle()
//					.setView(editText)
//					.setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							String nickString = editText.getText().toString();
//							if (TextUtils.isEmpty(nickString)) {
//								Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
//								return;
//							}
//							updateRemoteNick(nickString);
//						}
//					}).setNegativeButton(R.string.dl_cancel, null).show();
			break;
		default:
			break;
		}

	}
	
	public void asyncFetchUserInfo(String username){
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<User>() {

			@Override
			public void onSuccess(User user) {
				if (user != null) {
					tvNickName.setText(user.getNick());
					if (!TextUtils.isEmpty(user.getAvatar())) {
						Picasso.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.mipmap.default_avatar).into(headAvatar);
					} else {
						Picasso.with(UserProfileActivity.this).load(R.mipmap.default_avatar).into(headAvatar);
					}
					UserUtils.saveUserInfo(user);
				}

			}


			@Override
			public void onError(int error, String errorMsg) {
			}
		});
	}
	
	
	
	private void uploadHeadPhoto() {
	android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
		builder.setTitle(R.string.dl_title_upload_photo);
		builder.setItems(new String[] { getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload) },
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
									Toast.LENGTH_SHORT).show();
							break;
						case 1:
							Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
							pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(pickIntent, REQUESTCODE_PICK);
							break;
						default:
							break;
						}
					}
				});

		builder.create().show();
	}
	
	

	private void updateRemoteNick(final String nickName) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean updatenick = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().updateParseNickName(nickName);
				if (UserProfileActivity.this.isFinishing()) {
					return;
				}
				if (!updatenick) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
									.show();
							dialog.dismiss();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
									.show();
							tvNickName.setText(nickName);
						}
					});
				}
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_PICK:
			if (data == null || data.getData() == null) {
				return;
			}
			startPhotoZoom(data.getData());
			break;
		case REQUESTCODE_CUTTING:
			if (data != null) {
				setPicToView(data);
			}
			break;

		case REQUESTCODE_NICK:
			if (resultCode == RESULT_OK){
				String nick = data.getStringExtra("data");
				updateRemoteNick(nick);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}
	
	/**
	 * newGroup the picture data
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(getResources(), photo);
			headAvatar.setImageDrawable(drawable);
			uploadUserAvatar(Bitmap2Bytes(photo));
		}

	}
	
	private void uploadUserAvatar(final byte[] data) {
		dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String avatarUrl = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().uploadUserAvatar(data);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						if (avatarUrl != null) {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
									Toast.LENGTH_SHORT).show();
						}

					}
				});

			}
		}).start();

		dialog.show();
	}
	
	
	public byte[] Bitmap2Bytes(Bitmap bm){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
}
