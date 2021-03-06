package com.gfan.sbbs.ui.Abstract;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.othercomponent.MyApplication;

public abstract class BaseActivity extends SherlockActivity {

	private static final String TAG = "BaseActivity";
	protected String token;
	protected boolean isLogined;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!MyApplication.isNightMode) {
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_ForceOverflow);
		}else{
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_ForceOverflow);
		}
		// setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		_onCreate(savedInstanceState);
		MyApplication.getInstance().setActivity(this);
	}

	protected void _onCreate(Bundle savedInstanceState) {
		if (null != getSupportActionBar()) {
			getSupportActionBar().setHomeButtonEnabled(true);
		}
		MyApplication.getInstance();
		isLogined = MyApplication.checkLogin();
		if (isLogined) {
			Log.i(TAG, "login");
			token = MyApplication.getInstance().getToken();
			setup();
		} else {
			Log.i(TAG, "unlogin");
			processUnLogin();
		}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	protected abstract void processUnLogin();

	protected abstract void setup();

	protected String getToken() {
		return this.token;
	}

	protected boolean isLogined() {
		
		return this.isLogined;
	}

	protected boolean isTrue(Bundle savedInstance, String tag) {
		if (null != savedInstance) {
			boolean flag = savedInstance.getBoolean(tag, false);
			return flag;
		}
		return false;
	}
}
