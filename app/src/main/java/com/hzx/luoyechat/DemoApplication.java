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
package com.hzx.luoyechat;

import android.app.Application;
import android.content.Context;
import android.util.TypedValue;

import com.easemob.EMCallBack;
import com.hzx.luoyechat.activity.BaseActivity;
import com.hzx.luoyechat.applib.utils.HXPreferenceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class DemoApplication extends Application {

	public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	
	/** 当前用户nickname,为了苹果推送不是userid而是昵称 */
	public static String currentUserNick = "";
	public static DemoHXSDKHelper hxSDKHelper = new DemoHXSDKHelper();

	@Override
	public void onCreate() {
		int themeColor = HXPreferenceUtils.getInstance(this).getThemeColor();
		setTheme(BaseActivity.themeColors[themeColor]);
		super.onCreate();

        applicationContext = this;
        instance = this;

        hxSDKHelper.onInit(applicationContext);

		CustomActivityOnCrash.install(this);
// Application取消注释 设置一个未捕获异常的处理器
//		Thread.currentThread().setUncaughtExceptionHandler( new MyUncaughtExceptionHandler());
	}

	private class MyUncaughtExceptionHandler implements
			Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			// 并没有处理异常 虚拟机挂掉了 死后的遗言
			System.out.println("发现了异常,但是被哥捕获了");
			PrintStream ps = null;
			try {
				File file = new File(getFilesDir(), "error.txt");
				ps = new PrintStream(file);
//				Toast.makeText(getBaseContext(),"错误日志目录："+file.getAbsolutePath(),0).show();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			ex.printStackTrace();
			ex.printStackTrace(ps); // 把错误日志 写入到一个文件中

			android.os.Process.killProcess(android.os.Process.myPid());// 自杀
		}
	}

	public static DemoApplication getInstance() {
		return instance;
	}
 

	/**
	 * 获取当前登陆用户名
	 */
	public String getUserName() {
	    return hxSDKHelper.getHXId();
	}

	/**
	 * 获取密码
	 */
	public String getPassword() {
		return hxSDKHelper.getPassword();
	}

	/**
	 * 设置用户名
	 */
	public void setUserName(String username) {
	    hxSDKHelper.setHXId(username);
	}

	/**
	 * 设置密码 下面的实例代码 只是demo，实际的应用中需要加password 加密后存入 preference 环信sdk
	 * 内部的自动登录需要的密码，已经加密存储了
	 */
	public void setPassword(String pwd) {
	    hxSDKHelper.setPassword(pwd);
	}

	/**
	 * 退出登录,清空数据
	 */
	public void logout(final boolean isGCM,final EMCallBack emCallBack) {
		// 先调用sdk logout，在清理app中自己的数据
	    hxSDKHelper.logout(isGCM, emCallBack);
	}

	public void getThemeAttribute(int resid, TypedValue outValue){
		getTheme().resolveAttribute(resid, outValue,true);
	}
}
