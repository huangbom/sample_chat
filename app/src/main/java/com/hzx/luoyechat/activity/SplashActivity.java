package com.hzx.luoyechat.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.StateSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.platform.comapi.map.A;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.hzx.luoyechat.DemoHXSDKHelper;
import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.utils.HXPreferenceUtils;
import com.hzx.luoyechat.utils.SmileUtils;
import com.hzx.luoyechat.widget.ChangeColorText;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 开屏页
 */
public class SplashActivity extends BaseActivity {
	@Bind(R.id.splash_root) View rootLayout;

	@Bind(R.id.tv_version) TextView mTv_version;

	private static final int sleepTime = 1500;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_splash);

		ButterKnife.bind(this);

		mTv_version.setText(getVersion());


		AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
		animation.setDuration(1300);
		rootLayout.startAnimation(animation);

		UmengUpdateAgent.update(this);

		HXPreferenceUtils.getInstance(this).setTabIndex(0);

	}

	@Override
	protected void onStart() {
		super.onStart();

		new Thread(new Runnable() {
			public void run() {
				if (DemoHXSDKHelper.getInstance().isLogined()) {
					// ** 免登陆情况 加载所有本地群和会话
					//不是必须的，不加sdk也会自动异步去加载(不会重复加载)；
					//加上的话保证进了主页面会话和群组都已经load完毕
					long start = System.currentTimeMillis();
					EMGroupManager.getInstance().loadAllGroups();
					EMChatManager.getInstance().loadAllConversations();
					long costTime = System.currentTimeMillis() - start;
					//等待sleeptime时长
					if (sleepTime - costTime > 0) {
						try {
							Thread.sleep(sleepTime - costTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//进入主页面
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
				}else {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
					finish();
				}
			}
		}).start();

	}

	/** 获取当前应用程序的版本号 */
	private String getVersion() {
		String st = getResources().getString(R.string.Version_number_is_wrong);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo packinfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packinfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return st;
		}
	}
}
