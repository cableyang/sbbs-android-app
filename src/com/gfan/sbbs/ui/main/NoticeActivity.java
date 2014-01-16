package com.gfan.sbbs.ui.main;

import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.Adapter.MailAdapter;
import com.gfan.sbbs.ui.Adapter.TopicListAdapter;

public class NoticeActivity extends BaseActivity {

	private ListView mailListView, atsListView, reListView;
	private List<Mail> mailList;
	private List<Topic> atsList, reList;
	private HashMap<String, Object> noticeArray;
	private TopicListAdapter atsAdapter, reAdapter;
	private MailAdapter mailAdapter;
	private GenericTask doRetrieveTask, doClearTask;
	private String url, errorCause;
	private static final String LAUNCH_ACTION = "com.yuchao.ui.NOTICE";
	private static final int MENU_CLEAR = 0;
	private static final String TAG = "NoticeActivity";

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		return intent;
	}

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		private ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mRetrieveTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(NoticeActivity.this);
			pdialog.setMessage(getResources().getString(R.string.loading));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			processNotice(result);
		}

	};

	private TaskListener mClearTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "mClearTaskListener";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			clearNotification(result);
			draw();
		}
	};

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		mailListView = (ListView) this.findViewById(R.id.mail_notice_list);
		atsListView = (ListView) this.findViewById(R.id.ats_notice_list);
		reListView = (ListView) this.findViewById(R.id.re_notice_list);
		atsAdapter = new TopicListAdapter(this);
		reAdapter = new TopicListAdapter(this);
		atsListView.setAdapter(atsAdapter);
		reListView.setAdapter(reAdapter);
		mailAdapter = new MailAdapter(this);
		mailListView.setAdapter(mailAdapter);
	}

	private void init() {
		setTitle(R.string.notice_title);
		mailListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Mail mail = mailList.get(position);
						Intent intent = new Intent(NoticeActivity.this,
								MailBody.class);
						Bundle bundle = new Bundle();
						bundle.putString("title", mail.getTitle());
						bundle.putString("num", mail.getNum());
						bundle.putInt("type", 0);
						intent.putExtras(bundle);
						startActivity(intent);
						mailList.remove(mail);
						draw();
					}
				});
		atsListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Topic topic = atsList.get(position);
						createTopicIntent(topic);
						atsList.remove(topic);
						draw();
					}
				});
		reListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Topic topic = reList.get(position);
						createTopicIntent(topic);
						reList.remove(topic);
						draw();
					}
				});
	}

	private void createTopicIntent(Topic topic) {
		Intent intent = new Intent(NoticeActivity.this,
				SinglePostActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("boardID", topic.getBoardName());
		bundle.putInt("id", topic.getId());
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void draw() {
		mailAdapter.refresh(mailList);
		atsAdapter.refresh(atsList);
		reAdapter.refresh(reList);
		drawView();
	}

	private void drawView() {
		if (0 == mailList.size()) {
			mailListView.setVisibility(View.GONE);
		} else {
			mailListView.setVisibility(View.VISIBLE);
		}
		if (0 == atsList.size()) {
			atsListView.setVisibility(View.GONE);
		} else {
			atsListView.setVisibility(View.VISIBLE);
		}
		if (0 == reList.size()) {
			reListView.setVisibility(View.GONE);
		} else {
			reListView.setVisibility(View.VISIBLE);
		}
	}

	private void goTop() {
		// listView.setSelection(1);
	}

	private void doRetrieve() {
		doRetrieveTask = new RetrieveTask();
		doRetrieveTask.setListener(mRetrieveTaskListener);
		doRetrieveTask.execute(url);
	}

	private void doClear() {
		doClearTask = new DoClearTask();
		doClearTask.setListener(mClearTaskListener);
		String url = "http://bbs.seu.edu.cn/api/clear_notifications.json?token="
				+ MyApplication.getInstance().getToken();
		doClearTask.execute(url);
	}

	@SuppressWarnings("unchecked")
	private void processNotice(TaskResult result) {
		if (TaskResult.Failed == result) {
			Toast.makeText(this, errorCause, Toast.LENGTH_SHORT).show();
			return;
		}
		if (TaskResult.NO_DATA == result) {
			Toast.makeText(this, R.string.notice_no_data, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		clearNotification();
		
		mailList = (List<Mail>) noticeArray.get("mail");
		atsList = (List<Topic>) noticeArray.get("ats");
		reList = (List<Topic>) noticeArray.get("reply");
		
		if (mailList.size() == 0 && atsList.size() == 0 && reList.size() == 0) {
			Toast.makeText(this, R.string.notice_no_data, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		draw();
		goTop();
	}

	private void clearNotification(TaskResult result) {
		if (TaskResult.IO_ERROR == result) {
			Toast.makeText(this, R.string.notice_clear_network_err, Toast.LENGTH_SHORT).show();
			return;
		} else if (TaskResult.Failed == result) {
			Toast.makeText(this, R.string.notice_clear_failed, Toast.LENGTH_SHORT).show();
			return;
		} else if (result == TaskResult.OK) {
			Toast.makeText(this, R.string.notice_clear_ok, Toast.LENGTH_SHORT).show();
			clearNotification();
		}
		draw();
	}
	
	private void clearNotification(){
		if (null != mailList) {
			mailList.clear();
		}
		if (null != atsList) {
			atsList.clear();
		}
		if (null != reList) {
			reList.clear();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemID = item.getItemId();
		switch(itemID){
		case MENU_CLEAR:
			doClear();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, R.string.notice_clear).setIcon(R.drawable.ic_menu_delete_inverse).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		if (null != doRetrieveTask
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			doRetrieveTask.cancel(true);
		}
		if (null != doClearTask
				&& doClearTask.getStatus() == GenericTask.Status.RUNNING) {
			doClearTask.cancel(true);
		}
		clearNotification();
		super.onDestroy();
	}

	private class DoClearTask extends GenericTask {

		private boolean flag;

		@Override
		protected TaskResult _doInBackground(String... params) {
			flag = BBSOperator.getInstance().getBoolean(params[0]);
			if (flag) {
				return TaskResult.OK;
			} else {
				return TaskResult.Failed;
			}
		}
	}

	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			// noticeArray = SBBSSupport.getNotifications(params[0]);
			try {
				noticeArray = BBSOperator.getInstance().getNoticeList(params[0]);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				Log.e(TAG, errorCause);
				return TaskResult.Failed;
			}

			return TaskResult.OK;
		}

	}

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setup() {
		LayoutInflater inflate = getLayoutInflater();
		LinearLayout noticeaLayout = (LinearLayout) inflate.inflate(
				R.layout.notice, null);
		this.setContentView(noticeaLayout);
		url = "http://bbs.seu.edu.cn/api/notifications.json?token=" + token;
		this.initView();
		this.init();
		Bundle bundle = getIntent().getExtras();
		
		if (null != bundle && bundle.containsKey("newMentions")) {
			noticeArray = (HashMap<String, Object>) bundle
					.getSerializable("newMentions");
			
			processNotice(TaskResult.OK);

		} else {
			doRetrieve();
		}

	}
}
