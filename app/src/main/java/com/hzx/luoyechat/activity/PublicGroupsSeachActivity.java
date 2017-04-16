package com.hzx.luoyechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.hzx.luoyechat.R;
import com.easemob.exceptions.EaseMobException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PublicGroupsSeachActivity extends BaseActivity{
    @Bind(R.id.rl_searched_group)
    RelativeLayout containerLayout;

    @Bind(R.id.et_search_id)
    EditText idET;

    public static EMGroup searchedGroup;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        inflateContentView(R.layout.activity_public_groups_search, R.string.search_pubic_group,true);
        ButterKnife.bind(this);

        searchedGroup = null;
    }
    
    /** 搜索 */
    public void searchGroup( ){
        if(TextUtils.isEmpty(idET.getText())){
            return;
        }
        
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.searching));
        pd.setCancelable(false);
        pd.show();
        
        new Thread(new Runnable() {

            public void run() {
                try {
                    searchedGroup = EMGroupManager.getInstance().getGroupFromServer(idET.getText().toString());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            containerLayout.setVisibility(View.VISIBLE);
                            setToolbarTitle(searchedGroup.getGroupName());
                        }
                    });
                    
                } catch (final EaseMobException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            searchedGroup = null;
                            containerLayout.setVisibility(View.GONE);
                            if(e.getErrorCode() == EMError.GROUP_NOT_EXIST){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.group_not_existed), 0).show();
                            }else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.group_search_failed) + " : " + getString(R.string.connect_failuer_toast), 0).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    /**点击搜索到的群组进入群组信息页面*/
    public void enterToDetails(View view){
        startActivity(new Intent(this, GroupSimpleDetailActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.search)
                .setIcon(R.mipmap.abc_ic_search_api_mtrl_alpha)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        searchGroup();
                        return true;
                    }
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }
}
