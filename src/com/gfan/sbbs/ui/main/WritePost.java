package com.gfan.sbbs.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.dao.topic.NewTopicSendDao;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.file.service.UpLoadService;
import com.gfan.sbbs.file.utils.FileUtils;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.utils.ActivityUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * TODO process the photos before uploading 
 * 
 * @author Nine
 * 
 */

public class WritePost extends BaseActivity {
	private EditText titleView, contentView;
	private String boardID;
	private Integer reid, id;
	private boolean isAnonymous = false;
	private int type = 0;

	private Topic resultTopic;


	private static final int MENU_SEND = Menu.FIRST;
	private static final int MENU_ADD_ATTACHMENT = Menu.FIRST + 1;
	private static final int MENU_ANONYMOUS = Menu.FIRST + 2;
	private static final int REQUEST_ADD_ATTACHMENT = 0;
	private static final String TAG = WritePost.class.getName();

	private TaskListener doPostListener = new TaskAdapter() {

		ProgressDialog pdialog;

		@Override
		public String getName() {
			return "doPostListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(WritePost.this);
			pdialog.setMessage(getString(R.string.sending_post));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			handleResult(result);
		}

	};

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setup() {

		Log.i(TAG, "this is setup method");
	}

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ActionBarSherlock actionBar = ActionBarSherlock.wrap(this);
		actionBar
				.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout newPostLayout = (LinearLayout) inflater.inflate(
				R.layout.newpost, null);
		this.setContentView(newPostLayout);
		initView();
		processForVarPostType();
		Log.i(TAG, "this is _onCreate method");
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	/**
	 * process args for different post type
	 */

	private void processForVarPostType() {
		type = getIntent().getExtras().getInt(PostHelper.EXTRA_TYPE);
		Log.i(TAG, "post type is "+type);
		switch (type) {
	
		case PostHelper.TYPE_NEW: {
			boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
			reid = 0;
			id = 0;
			setTitle(R.string.write_post);
			boolean hasDraft = MyApplication.getInstance().getmPreference().getBoolean(Preferences.HAS_DRAFT, false);
			if(hasDraft){
				String title = MyApplication.getInstance().getmPreference().getString(Preferences.DRAFT_TITLE, "");
				String content = MyApplication.getInstance().getmPreference().getString(Preferences.DRAFT_CONTENT, "");
				reid = MyApplication.getInstance().getmPreference().getInt(Preferences.DRAFT_REID, 0);
				titleView.setText(title);
				contentView.setText(content);
//				SharedPreferences.Editor editor = MyApplication.getInstance().getmPreference().edit();
//				editor.putBoolean(Preferences.HAS_DRAFT, false);
//				editor.commit();
				dropDraft();
			}
			break;
		}
		case PostHelper.TYPE_REPLY: {
			boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
			reid = getIntent().getExtras().getInt(PostHelper.EXTRA_REID);
			String title = getIntent().getExtras().getString(
					PostHelper.EXTRA_TITLE);
			titleView.setText(title);
			String hint = getIntent().getExtras().getString(
					PostHelper.EXTRA_CONTENT);
			contentView.setHint(hint);
			contentView.requestFocus();
			contentView.setSelection(0);
			setTitle(R.string.write_post);
			id = 0;// useless param for reply a topic,set it to zero directly
					// here
			break;
		}
		case PostHelper.TYPE_EDIT: {
			boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
			id = getIntent().getExtras().getInt(PostHelper.EXTRA_ID);
			String content = getIntent().getExtras().getString(
					PostHelper.EXTRA_CONTENT);
			contentView.setText(content);
			String title = getIntent().getExtras().getString(
					PostHelper.EXTRA_TITLE);
			titleView.setText(title);
			setTitle(R.string.edit_post);
			break;
		}
		}
	}

	/**
	 * create topic which is used as params of post action
	 * 
	 * @return
	 */
	private Topic createTopic() {
		Topic topic = new Topic();
		String title = titleView.getText().toString().trim();
		String content = contentView.getText().toString().trim();
		topic.setTitle(title).setContent(content).setBoardName(boardID);
		if (0 != id) {
			topic.setId(id);
		} else {
			topic.setReid(reid);
		}
		topic.setAnonymous(isAnonymous);
		return topic;
	}

	/**
	 * check this post has attachments or not
	 * 
	 * @return
	 */
	private boolean hasAttachment() {
		return !FileUtils.getInstance().isEmpty();

	}

	/**
	 * check if there are any errors
	 * 
	 * @return
	 */
	private boolean canSend() {
		String title = titleView.getText().toString();
		if (TextUtils.isEmpty(title)) {
			Toast.makeText(WritePost.this, R.string.post_title_null_alert,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * post with pics/files,start a background service
	 */
	private void postWithPic() {
		Intent intent = new Intent(this, UpLoadService.class);
		Bundle bundle = new Bundle();
		bundle.putString(PostHelper.EXTRA_BOARD, boardID);
		bundle.putInt(PostHelper.EXTRA_ID, resultTopic.getId());
		bundle.putString("token", token);
		bundle.putBoolean("isAnonymous", isAnonymous);
		intent.putExtras(bundle);
		startService(intent);
	}

	/**
	 * 
	 */
	private void postWithoutPic() {
		SendNewTopicTask sendTask = new SendNewTopicTask();
		sendTask.setListener(doPostListener);
		sendTask.execute("");
	}

	/**
	 * save draft when send topic failed 
	 */
	private void saveDraft() {
		SharedPreferences preferences =  MyApplication.getInstance().getmPreference();
		SharedPreferences.Editor editor = preferences.edit();
		
		String title = titleView.getText().toString();
		if(!TextUtils.isEmpty(title)){
			editor.putString(Preferences.DRAFT_TITLE, title);
		}
		String content = contentView.getText().toString();
		if(!TextUtils.isEmpty(content)){
			editor.putString(Preferences.DRAFT_CONTENT, content);
		}
		if(null != reid ){
			editor.putInt(Preferences.DRAFT_REID, reid);
		}
		if(!TextUtils.isEmpty(content+title)){
			editor.putBoolean(Preferences.HAS_DRAFT, true);
		}else{
			editor.putBoolean(Preferences.HAS_DRAFT, false);
		}
		editor.commit();
	}
	
	/**
	 * drop the draft by the users or after retrieve it from db
	 */
	private void dropDraft(){
		SharedPreferences preferences = MyApplication.getInstance().getmPreference();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(Preferences.HAS_DRAFT, false);
		editor.putString(Preferences.DRAFT_TITLE, "");
		editor.putString(Preferences.DRAFT_CONTENT, "");
		editor.commit();
	}

	private void handleResult(TaskResult result) {

		if (TaskResult.OK == result && !hasAttachment()) {
			Toast.makeText(this, R.string.post_success, Toast.LENGTH_SHORT).show();
			finish();
		} else if (TaskResult.OK == result && hasAttachment()) {
			postWithPic();
			finish();
		}else{
//			saveDraft();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initView() {
		titleView = (EditText) this.findViewById(R.id.newpostTitle);
		contentView = (EditText) this.findViewById(R.id.newpostcontent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "this is onCreateOptionsMenu method");
		menu.add(Menu.NONE, MENU_ADD_ATTACHMENT, Menu.NONE, "add attachment")
				.setIcon(R.drawable.toolbar_add_file)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		if ("psychology".equals(boardID.toLowerCase().trim())) {
			menu.add(Menu.NONE, MENU_ANONYMOUS, Menu.NONE, "anonymous")
					.setIcon(R.drawable.ic_toolbar_unanonymous)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			isAnonymous = true; //anonymous by default
		}
		menu.add(0, MENU_SEND, Menu.NONE, "post")
				.setIcon(R.drawable.ic_menu_send_holo_light_inverse)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		// boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:{
			saveDraft();
			Toast.makeText(this, R.string.save_draft, Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
		case MENU_SEND: {
			if (canSend()) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(contentView.getWindowToken(), 0);
				postWithoutPic();
			}
			break;
		}
		case MENU_ADD_ATTACHMENT: {
			startUploadActivity();
			break;
		}
		case MENU_ANONYMOUS: {
			if (isAnonymous) {
				isAnonymous = false;
				item.setIcon(R.drawable.ic_toolbar_anonymous);
				Toast.makeText(this, R.string.anonymous_status_off, Toast.LENGTH_SHORT).show();
			} else {
				isAnonymous = true;
				item.setIcon(R.drawable.ic_toolbar_unanonymous);
				Toast.makeText(this, R.string.anonymous_status_on, Toast.LENGTH_SHORT).show();
			}
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void startUploadActivity() {
		Intent intent = new Intent(this, FileUploadActivity.class);
		startActivityForResult(intent, REQUEST_ADD_ATTACHMENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO
		if (null == data) {
			return;
		}
		if (requestCode == REQUEST_ADD_ATTACHMENT && resultCode == RESULT_OK) {
			Log.i(TAG, "WritePost Activity starts from FileUploadActivity");
			return;
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "activity destroyed");
		super.onDestroy();
	}

	private class SendNewTopicTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			try {
				Topic topic = createTopic();
				if (type == PostHelper.TYPE_REPLY
						|| type == PostHelper.TYPE_NEW) {
					resultTopic = new NewTopicSendDao().sendNewTopic(topic);
				} else {
					resultTopic = new NewTopicSendDao().editTopicDao(topic);
				}
			} catch (HttpException e) {
				e.printStackTrace();
				ActivityUtils.showTips(e.getMessage());
				return TaskResult.Failed;
			}
			return TaskResult.OK;
		}
	}

}
