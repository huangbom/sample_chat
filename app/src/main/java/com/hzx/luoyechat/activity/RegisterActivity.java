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
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.hzx.luoyechat.DemoApplication;
import com.hzx.luoyechat.R;
import com.easemob.exceptions.EaseMobException;
import com.hzx.luoyechat.utils.RegexTools;
import com.hzx.luoyechat.widget.FrameButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** 注册页  */
public class RegisterActivity extends BaseActivity  {

	@Bind(R.id.username)
	EditText userNameEditText;
	@Bind(R.id.password)
	EditText passwordEditText;
	@Bind(R.id.confirm_password)
	EditText confirmPwdEditText;

	@Bind(R.id.til_username) TextInputLayout mTilUsername;
	@Bind(R.id.til_password) TextInputLayout mTilPassword;
	@Bind(R.id.til_confirm_password) TextInputLayout mTilConfirmPassword;

	@BindString(R.string.Password_cannot_be_empty)
	String Password_cannot_be_empty;

	@BindString(R.string.User_name_cannot_be_empty)
	String User_name_cannot_be_empty;

	@BindString(R.string.Confirm_password_cannot_be_empty)
	String Confirm_password_cannot_be_empty;

	@BindString(R.string.Two_input_password)
	String Two_input_password;

	@BindString(R.string.Is_the_registered)
	String Is_the_registered;

	@Bind(R.id.register)
	FrameButton regist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflateContentView(R.layout.activity_register, R.string.register, true);

		ButterKnife.bind(this);

		mTilUsername.setHint(getString(R.string.user_phone_email));
		mTilPassword.setHint(getString(R.string.password));
		mTilConfirmPassword.setHint(getString(R.string.confirmpassword));

		regist.getFirstButton().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register(view);
			}
		});

	}

	/** 注册 */
	public void register(View view) {
		if (!checkUserPsw())
			return;

		regist.showLoadAndClearText();

		exeTask(new Runnable() {
			public void run() {
				try {
					// 调用sdk注册方法
					EMChatManager.getInstance().createAccountOnServer(username, pwd);
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing()) {

								// 保存用户名
								DemoApplication.getInstance().setUserName(username);
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
								finish();
							}
						}
					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing()){
							int errorCode = e.getErrorCode();
							if (errorCode == EMError.NONETWORK_ERROR) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.USER_ALREADY_EXISTS) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.UNAUTHORIZED) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
							} else if (errorCode == EMError.ILLEGAL_USER_NAME) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
							}}
							regist.showFirstBotton();
						}
					});
				}
			}
		});

	}

	String username,pwd;
	public boolean checkUserPsw(){
		username = userNameEditText.getText().toString().trim();
		pwd = passwordEditText.getText().toString().trim();

		String confirm_pwd = confirmPwdEditText.getText().toString().trim();

		if (TextUtils.isEmpty(username)) {
			mTilUsername.setError(User_name_cannot_be_empty);

			userNameEditText.requestFocus();
			return false;
		}else if (!(RegexTools.isMobile(username) || RegexTools.isEmail(username))){
			mTilUsername.setError("请输入手机号或邮箱");
			userNameEditText.requestFocus();
			return false;
		}else
			mTilUsername.setErrorEnabled(false);


		if (pwd.length() < 6 || pwd.length() > 18 ){
			mTilPassword.setError("密码长度在6-18位");
			passwordEditText.requestFocus();
			return false;
		}
		else if (TextUtils.isEmpty(pwd)) {
			mTilPassword.setError(Password_cannot_be_empty);
			passwordEditText.requestFocus();
			return false;
		} else
			mTilPassword.setErrorEnabled(false);

		if (TextUtils.isEmpty(confirm_pwd)) {
			mTilConfirmPassword.setError(Confirm_password_cannot_be_empty);
			confirmPwdEditText.requestFocus();
			return false;
		} else if (!pwd.equals(confirm_pwd)) {
			mTilConfirmPassword.setError(Two_input_password);
			confirmPwdEditText.requestFocus();
			return false;
		}else
			mTilConfirmPassword.setErrorEnabled(false);

		return true;
	}

}
