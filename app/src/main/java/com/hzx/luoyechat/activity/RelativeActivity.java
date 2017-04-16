package com.hzx.luoyechat.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.hzx.luoyechat.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RelativeActivity extends BaseActivity {

    @Bind(R.id.relative_wechat)
    Button mBtWechat;

    @Bind(R.id.relative_alipay)
    Button mBtAlipay;

    @Bind(R.id.relative_iv)
    ImageView mIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateContentView(R.layout.activity_relative, R.string.relative, true);

        ButterKnife.bind(this);

    }

    @OnClick(R.id.relative_wechat)
    public void alipay(View view){
        mIv.setImageResource(R.mipmap.wechat);
    }

    @OnClick(R.id.relative_alipay)
    public void wechat(View view){
        mIv.setImageResource(R.mipmap.alipay);
    }
}
