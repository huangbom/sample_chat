/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hzx.luoyechat.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hzx.luoyechat.R;
import com.hzx.luoyechat.applib.controller.HXSDKHelper;
import com.hzx.luoyechat.applib.utils.HXPreferenceUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BaseActivity extends AppCompatActivity {

    static ExecutorService ESe = Executors.newFixedThreadPool(4);

    android.support.v7.app.AlertDialog.Builder mDialog;


    protected Toolbar mToolbar;
    private ViewGroup mRootLayout;
    TextView mTvEmptyInfo;

    public final static int[] themeColors = {
            R.style.AppTheme,
            R.style.AppTheme1,
            R.style.AppTheme2,
            R.style.AppTheme3,
            R.style.AppTheme4,
            R.style.AppTheme5,
            R.style.AppTheme6,
            R.style.AppTheme7,
            R.style.AppTheme8,
            R.style.AppTheme9,
    };

    @Override
    protected void onCreate(Bundle arg0) {
        initTheme();
        super.onCreate(arg0);
        setContentView(R.layout.activity_base);

        mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
        mTvEmptyInfo = (TextView) findViewById(R.id.tv_empty_info);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    private void initTheme() {
        int themeColor = HXPreferenceUtils.getInstance(this).getThemeColor();
        setTheme(themeColors[themeColor]);
    }

    protected void inflateContentView(int layoutId){
        getLayoutInflater().inflate(layoutId, mRootLayout);
    }

    protected void inflateContentView(int layoutId,int resId,boolean showNavi){
        inflateContentView(layoutId, getString(resId), showNavi);
    }

    protected void inflateContentView(int layoutId,CharSequence title,boolean showNavi){
        getLayoutInflater().inflate(layoutId, mRootLayout);
        setTitleAndNavigation(title, showNavi);
    }

    protected void inflateContentView(View view){
        mRootLayout.addView(view);
    }

    public Toolbar getToolbar(){
        return mToolbar;
    }

    protected void enterActivity(Class clz){
        enterActivity(clz, false);
    }

    protected void enterActivity(Class clz,boolean isFinish){
        startActivity(new Intent(this, clz));
        if (isFinish)
            finish();
    }

    protected void setTitleAndNavigation(int resId,boolean showNavigation){
        setTitleAndNavigation(getString(resId), showNavigation);
    }

    protected void setTitleAndNavigation(CharSequence title,boolean showNavigation){
        setToolbarTitle(title);
        if (showNavigation){
            mToolbar.setNavigationIcon(R.mipmap.tb_ic_back);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    back(view);
                }
            });
        }
    }

    protected void setToolbarTitle(CharSequence title){
        if (!TextUtils.isEmpty(title))
            mToolbar.setTitle(title);
    }
    protected void setToolbarTitle(int resId){
        setToolbarTitle(getString(resId));
    }

    protected void setEmptyInfo(CharSequence text){
        if (mTvEmptyInfo.getVisibility() != View.VISIBLE)
            mTvEmptyInfo.setVisibility(View.VISIBLE);
        mTvEmptyInfo.setText(text);
    }

    /**返回*/
    public void back(View view) {
        finish();
    }

    protected void exeTask(Runnable runnable){
        ESe.execute(runnable);
    }

    protected void showDialog(int resId,DialogInterface.OnClickListener posiListener){
        showDialog(getString(resId), posiListener);
    }

    protected void showDialog(CharSequence msg,DialogInterface.OnClickListener posiListener){
        if (mDialog == null)
            mDialog = new android.support.v7.app.AlertDialog.Builder(this);
        mDialog.setMessage(msg)
                .setTitle("提示")
                .setCancelable(false)
                .setPositiveButton(R.string.ok, posiListener)
                .setNegativeButton(R.string.cancel,null)
                .show();
    }

    protected void showDialog(int msg,DialogInterface.OnClickListener cancelListen,DialogInterface.OnClickListener posiListener){
        showDialog(getString(msg),cancelListen,posiListener);
    }

    /**如果取消监听传入null，不会显示cancel按钮*/
    protected void showDialog(CharSequence msg,DialogInterface.OnClickListener cancelListen,DialogInterface.OnClickListener posiListener){
        if (mDialog == null)
            mDialog = new android.support.v7.app.AlertDialog.Builder(this);

        mDialog.setMessage(msg)
            .setTitle("提示")
            .setCancelable(false)
            .setPositiveButton(R.string.ok, posiListener);

        if (cancelListen != null)
            mDialog.setNegativeButton(R.string.cancel,cancelListen);

        mDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
        HXSDKHelper.getInstance().getNotifier().reset();

        // umeng
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // umeng
        MobclickAgent.onPause(this);
    }
}
