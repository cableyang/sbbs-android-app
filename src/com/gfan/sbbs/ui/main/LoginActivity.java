package com.gfan.sbbs.ui.main;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.gfan.sbbs.bean.User;
import com.gfan.sbbs.file.utils.FileUtils;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends SherlockFragmentActivity {

	private EditText userNameText, passwdText;
	private Button mLoginButton, mGuestButton;
	private GenericTask mLoginTask;

	private String mUserName, mPasswd, mToken,errorCause;
	private SharedPreferences mPreferences;
	private MyApplication application;
	private User user;
	private static final String TAG = "LoginActivity";

	private TaskListener mLoginTaskListener = new TaskAdapter() {
		ProgressDialog pdialog;
		@Override
		public String getName() {
			
			return "mLoginTaskListener";
		}
		
		@Override
		public void onPreExecute(GenericTask task) {
			pdialog = new ProgressDialog(LoginActivity.this);
			pdialog.setMessage(getString(R.string.login_message));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			onLoginComplete(result);
		}
		

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		application = (MyApplication) getApplication();
		Log.i(TAG, "myapplication start");
		this.setContentView(R.layout.login);
		initArgs();
		initEvents();
		FileUtils.getInstance().cleanQueues();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initArgs(){
		userNameText = (EditText) this.findViewById(R.id.txt_username);
		passwdText = (EditText) this.findViewById(R.id.txt_password);
		mLoginButton = (Button) this.findViewById(R.id.btn_login);
		mGuestButton = (Button) this.findViewById(R.id.btn_guestLogin);

		mPreferences = MyApplication.mPreference;
		
	}
	
	private void initEvents(){
		boolean remember = mPreferences.getBoolean(Preferences.REMEMBER_ME,
				false);

		if (remember) {
			mUserName = mPreferences.getString(Preferences.USER_NAME, "");
			mPasswd = mPreferences.getString(Preferences.USER_PWD, "");
			userNameText.setText(mUserName);
			passwdText.setText(mPasswd);
		}
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doLogin();
			}
		});
		mGuestButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onLoginFinish(false);
			}
		});
		boolean isLogined = MyApplication.checkLogin();
		if (isLogined) {
			Log.i(TAG, "isLogined is true");
			boolean autoLogin = application.isAutoLogin();
			Log.i(TAG, "autoLogin is "+autoLogin);
			if (autoLogin) {
				MyApplication.loginUser = new User(mUserName,mPasswd);
				onLoginFinish(false);
			}
		}
		passwdText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN){
					doLogin();
					return true;
				}
				return false;
			}
		});
	}
	
	
	private void showSuccess() {
		Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
	}

	private void showFailure() {
		Toast.makeText(this, errorCause, Toast.LENGTH_SHORT).show();
	}

	private void doLogin() {
		mUserName = userNameText.getText().toString().trim();
		mPasswd = passwdText.getText().toString().trim();
		if (validate(mUserName, mPasswd)) {
			mLoginTask = new LoginTask();
			mLoginTask.setListener(mLoginTaskListener);
			mLoginTask.execute(mUserName, mPasswd);
		}
	}

	private boolean validate(String userID, String passwd) {
		if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(passwd)) {
			return false;
		}
		return true;
	}


	private void onLoginComplete(TaskResult taskResult) {
		if (TaskResult.Failed == taskResult) {
			showFailure();
			return;
		}
			showSuccess();
			mToken = user.getToken();
			onLoginFinish(true);
	}
	
	private void onLoginFinish(boolean isLogined){
		
		Intent intent = new Intent(this,Home.class);
		if(isLogined){
			updateInfo();
		}
		startActivity(intent);
		finish();
	}

	private void updateInfo() {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(Preferences.USER_NAME, mUserName);
		editor.putString(Preferences.USER_PWD, mPasswd);
		editor.putString(Preferences.USER_TOKEN, mToken);
		editor.commit();
		MyApplication.userName = mUserName;
		application.setToken(mToken);
		application.setLogined(true);
		MyApplication.loginUser = new User(mUserName,mPasswd);
		Log.i(TAG, mUserName+" login success");
	}

	@Override
	protected void onDestroy() {
		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			mLoginTask.cancel(true);
		}
		super.onDestroy();
	}

	private class LoginTask extends GenericTask{
		

		@Override
		protected TaskResult _doInBackground(String... params) {
			//				result = SBBSSupport.doLogin(mUserName, mPasswd);
			try {
				user = BBSOperator.getInstance().doLogin(mUserName, mPasswd);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				return TaskResult.Failed;
			}
			return TaskResult.OK;
			
		}
	}

}
