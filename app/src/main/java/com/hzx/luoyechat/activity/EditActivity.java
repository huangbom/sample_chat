package com.hzx.luoyechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.hzx.luoyechat.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditActivity extends BaseActivity{

	@Bind(R.id.edittext) EditText editText;
	@Bind(R.id.til_edittext) TextInputLayout mTilEditText;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		inflateContentView(R.layout.activity_edit, R.string.Change_the_group_name,true);

		ButterKnife.bind(this);

		String title = getIntent().getStringExtra("title");
		String data = getIntent().getStringExtra("data");
		if(title != null)
			setToolbarTitle(title);
		if(data != null) {
			editText.setText(data);
		mTilEditText.setHint(data);
		}

		editText.setSelection(editText.length());
		
	}

	@OnClick(R.id.btn_save)
	public void save(View view){
		String trim = editText.getText().toString().trim();
		if (TextUtils.isEmpty(trim)) {
			mTilEditText.setError("不能为空");
			return;
		}else mTilEditText.setErrorEnabled(false);

		setResult(RESULT_OK, new Intent().putExtra("data", trim));
		finish();
	}

}
