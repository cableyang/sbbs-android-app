package com.gfan.sbbs.ui.main;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public class MailBody extends BaseActivity {
	private String num,errorCause;
	private int type;
	private Mail mail;
	private TextView authorView, timeView, titleView,contentView, quoterView, quoteView;

	private GenericTask mRetrieveTask, mDelTask;

	private static final int MENU_REPLY = 10;
	private static final int MENU_AUTHOR = 11;
	private static final int MENU_DELETE = 12;

	private static final String LAUNCH_ACTION = "com.yuchao.ui.MAIL";
	private static final String TAG = "MailBody";

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		private ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mRetrieveTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(MailBody.this);
			pdialog.setMessage(getResources().getString(R.string.loading));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			processResult(result);
		}

	};

	private TaskListener mDelTaskListener = new TaskAdapter() {
		private ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mDelTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(MailBody.this);
			pdialog.setMessage(getString(R.string.loading));
			pdialog.show();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			delMail(result);
		}

	};

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void doRetrieve() {
		mRetrieveTask = new RetrieveTask();
		String token = MyApplication.getInstance().getToken();
		String url = "http://bbs.seu.edu.cn/api/mail/get.json?type=" + type
				+ "&id=" + num + "&token=" + token;
		mRetrieveTask.setListener(mRetrieveTaskListener);
		mRetrieveTask.execute(url);
	}

	private void doDel() {
		mDelTask = new DelMailTask();
		mDelTask.setListener(mDelTaskListener);
		String url = "http://bbs.seu.edu.cn/api/mail/delete.json?type=0&id="
				+ mail.getNum() + "&token=" + MyApplication.getInstance().getToken();
		mDelTask.execute(url);
	}

	private void processResult(TaskResult result) {

		if (result == TaskResult.Failed) {
			Toast.makeText(this, errorCause, Toast.LENGTH_SHORT).show();
			return;
		}
		if (type == 0) {
			authorView.setText(mail.getFrom());
		} else if (type == 1) {
			authorView.setText(mail.getFrom());
		}
		titleView.setText(mail.getTitle());
		timeView.setText(mail.getDate());
		contentView.setText(mail.getContent());
		quoteView.setText(mail.getQuote());
		if (!TextUtils.isEmpty(mail.getQuote())) {
			quoterView.setText(mail.getFrom()+"提到");
		}else{
			quoteView.setVisibility(View.GONE);
			quoterView.setVisibility(View.GONE);
		}
	}
	

	private void initView() {
		authorView = (TextView) this.findViewById(R.id.post_text_author);
		timeView = (TextView) this.findViewById(R.id.post_text_time);
		titleView = (TextView) this.findViewById(R.id.PostTitle);
		contentView = (TextView)this.findViewById(R.id.post_text_content);
		quoterView = (TextView) this.findViewById(R.id.post_text_quoter);
		quoteView = (TextView) this.findViewById(R.id.post_text_quote);
		this.findViewById(R.id.post_att_label).setVisibility(View.GONE);
//		this.findViewById(R.id.post_att_link).setVisibility(View.GONE);
		this.findViewById(R.id.post_att).setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		if(null == mail){
			return false;
		}
		Bundle bundle = new Bundle();
		switch (item.getItemId()) {
		case MENU_REPLY:{
			
			Intent intent = new Intent(MailBody.this,WriteMail.class);
			bundle.putString(WriteMail.EXTRA_RECIEVER, mail.getFrom());
			bundle.putString(WriteMail.EXTRA_REID, mail.getNum());
			bundle.putString(WriteMail.EXTRA_TITLE, mail.getTitle());
			bundle.putString(WriteMail.EXTRA_CONTENT, mail.getContent());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_AUTHOR:{
			Intent intent = new Intent(MailBody.this, ViewProfileActivity.class);
			bundle.putString("userID", mail.getFrom());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
			}
		case MENU_DELETE:
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("标题");
			ab.setMessage("删除?");
			ab.setPositiveButton("是", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doDel();
				}
			});
			ab.setNegativeButton("否", null);
			ab.create();
			ab.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, MENU_REPLY, Menu.NONE, "reply")
				.setIcon(R.drawable.ic_menu_reply_inverse)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(1, MENU_AUTHOR, Menu.NONE, "author")
				.setIcon(R.drawable.ic_menu_profile_inverse)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(1, MENU_DELETE, Menu.NONE, "delete")
				.setIcon(R.drawable.ic_menu_delete_inverse)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	private void delMail(TaskResult result) {
		if (TaskResult.OK == result) {
			Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
			Intent intent = getIntent();
			int position = intent.getExtras().getInt("position");
			Bundle bundle = new Bundle();
			bundle.putInt("position", position);
			intent.putExtras(bundle);
			setResult(1, intent);
			finish();
			return;
		} else {
			Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	protected void onDestroy() {

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}
		if (mDelTask != null
				&& mDelTask.getStatus() == GenericTask.Status.RUNNING) {
			mDelTask.cancel(true);
		}
		super.onDestroy();
	}

	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			//				mail = SBBSSupport.getMailContentAPI(params[0]);
			try {
				mail = BBSOperator.getInstance().getMail(params[0]);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				Log.e(TAG, errorCause);
				return TaskResult.Failed;
			}
			return TaskResult.OK;
		}
	}

	private class DelMailTask extends GenericTask {

		private boolean result = false;

		@Override
		protected TaskResult _doInBackground(String... params) {
			//				result = SBBSSupport.delMail(params[0]);
			result = BBSOperator.getInstance().getBoolean(params[0]);
			if (result) {
				return TaskResult.OK;
			} else {
				return TaskResult.Failed;
			}
		}

	}

	@Override
	protected void processUnLogin() {
		Toast.makeText(this, R.string.unlogin_notice, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void setup() {
		setContentView(R.layout.singlepost);
		Bundle bundle = getIntent().getExtras();
		setTitle(bundle.getString("title"));
		num = bundle.getString("num");
		type = bundle.getInt("type");
		initView();
//		setupState();
		registerForContextMenu(contentView);
		doRetrieve();

	}
}
