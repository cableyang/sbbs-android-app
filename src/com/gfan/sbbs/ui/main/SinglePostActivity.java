package com.gfan.sbbs.ui.main;

import java.io.Serializable;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.gfan.sbbs.bean.Attachment;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.Adapter.AttachmentAdapter;
import com.gfan.sbbs.utils.MyGridView;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author Nine
 * 
 */
@SuppressWarnings("deprecation")
public class SinglePostActivity extends BaseActivity  {
	private Topic topic;
	private TextView postTitle;
	private TextView textTime;
	private TextView textAuthor;
	private TextView textContent;
	private TextView textQuote;
	private TextView textQuoter;
	private String boardID, url;
	private int id;
	private GenericTask doRetrieveTask;

	private MyGridView myGridView;
	private AttachmentAdapter attAdapter;

	private static final int MENU_SHARE = 0;
	private static final int MENU_REPLY = 1;
	private static final int MENU_AUTHOR = 2;
	private static final int MENU_MAIL = 3;
	private static final int MENU_MORE = 4;
	private static final int MENU_ONE_TOPIC = 5;
	private static final int MENU_COPY = 6;
	private static final int MENU_ONE_TOPIC_ALL = 7;
	private static final int MENU_EDIT = 8;

	private static final String LAUNCH_ACTION = "com.yuchao.ui.SINGLEPOST";
	private static final String TAG = "SinglePostActivity";

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		private ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mRetrieveTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(SinglePostActivity.this);
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

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout postLayout = (LinearLayout) inflater.inflate(
				R.layout.singlepost, null);
		this.setContentView(postLayout);
//		imgLayout = (LinearLayout) postLayout.findViewById(R.id.post_att);
//		getPhotoTaskList = new ArrayList<GenericTask>();
		initView();
		bindView();
		handleArgs();
	}


	/**
	 * handle topic args
	 */
	private void handleArgs(){
		topic = (Topic) getIntent().getExtras().getSerializable("topic");
		if(null != topic){
			id = topic.getId();
			boardID = topic.getBoardName();
			url = topic.getUrl();
			displayTopic();
			displayAttachment();
		}else{
			boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
			id = getIntent().getExtras().getInt(PostHelper.EXTRA_ID);
			doRetrieve();
		}
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

	/**
	 * retrieve method
	 */
	private void doRetrieve() {
		initUrl();
		if (null != doRetrieveTask
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		doRetrieveTask = new RetrieveTask();
		doRetrieveTask.setListener(mRetrieveTaskListener);
		doRetrieveTask.execute(url);
	}

	/**
	 * display the topic
	 */
	private void displayTopic(){
		setTitle(topic.getTitle());
		postTitle.setText(topic.getTitle());
		textAuthor.setText(topic.getAuthor());
		textTime.setText(topic.getTime());
		textContent.setText(topic.getContent());
		if (null != topic.getQuoter()) {
			textQuoter.setText("在 " + topic.getQuoter()+"的大作中提到");
		} else {
			textQuoter.setVisibility(View.GONE);
		}
		textQuote.setText(topic.getQuote());
		if (TextUtils.isEmpty(topic.getQuote())) {
			textQuote.setVisibility(View.GONE);
		}
		if(!topic.isHasAtt()){
			findViewById(R.id.post_att_label).setVisibility(View.GONE);
		}

	}
	/**
	 * displayAttachment
	 */
	private void displayAttachment() {
		if (topic.isHasAtt()) {
			attAdapter.refresh(topic.getAttList());
		}
		if(attAdapter.getCount() <3){
			myGridView.setNumColumns(attAdapter.getCount());
		}
	}

	/**
	 * 
	 */
	
	private void initUrl(){
		url = "http://bbs.seu.edu.cn/api/topic/" + boardID + "/" + id
		+ ".json?limit=1";
		if (isLogined()) {
			url = url.concat("&token=" + token);
			}
		}
	/**
	 * 
	 */
	private void initView() {
		postTitle = (TextView) this.findViewById(R.id.PostTitle);
		textAuthor = (TextView) this.findViewById(R.id.post_text_author);
		textTime = (TextView) this.findViewById(R.id.post_text_time);
		textContent = (TextView) this.findViewById(R.id.post_text_content);
		textQuote = (TextView) this.findViewById(R.id.post_text_quote);
		textQuoter = (TextView) this.findViewById(R.id.post_text_quoter);
		
//		SimpleImageLoader.mImageLoaderListener = this;

		myGridView = (MyGridView) this.findViewById(R.id.att_grid);
		attAdapter = new AttachmentAdapter(this);
		myGridView.setAdapter(attAdapter);
	}

	private void bindView() {
		textQuote.setClickable(true);
		textQuote.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SinglePostActivity.this,
						SinglePostActivity.class);
				Bundle bundle = new Bundle();
				int reid = topic.getReid();
				bundle.putString("boardID", boardID);
				bundle.putInt("id", reid);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		textQuote.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Intent intent = new Intent(SinglePostActivity.this,
							SinglePostActivity.class);
					Bundle bundle = new Bundle();
					int reid = topic.getReid();
					bundle.putString("boardID", topic.getBoardName());
					bundle.putInt("id", reid);
					intent.putExtras(bundle);
					startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});
		
		myGridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
//				Attachment att = (Attachment) attAdapter.getItem(position);
//				Intent intent = new Intent(SinglePostActivity.this,
//						FullImageView.class);
//				intent.putExtra(FullImageView.EXTRA_IMAGE_URL, att.getUrl());
//				startActivity(intent);
				Intent intent  = new Intent(SinglePostActivity.this, ImagePagerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable(PostHelper.EXTRA_ATT_LIST, (Serializable) topic.getAttList());
				bundle.putInt(PostHelper.EXTRA_POSITION, position);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			
		});

	}

	private void processResult(TaskResult result) {
		if (TaskResult.IO_ERROR == result || TaskResult.Failed == result) {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("标题");
			ab.setMessage(getString(R.string.reload_post));
			ab.setPositiveButton("是", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doRetrieve();
				}
			});
			ab.setNegativeButton("否", null);
			ab.create();
			ab.show();
			return;
		}
		displayTopic();
		// processAtt();
		displayAttachment();
	}


	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		if (null == topic) {
			item.setEnabled(false);
			return false;
		}
		switch (item.getItemId()) {
		case MENU_SHARE: {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/png");
			String link = "http://bbs.seu.edu.cn/r/post/" + boardID + "/" + id;
			intent.putExtra(Intent.EXTRA_TEXT,
					"#通过SBBS Android客户端分享#" + topic.getTitle() + ",链接:" + link);
			if (topic.isHasAtt()) {
				for (Attachment att : topic.getAttList()) {
					if (att.isImage()) {
						Uri u = Uri.parse(att.getUrl());
						intent.putExtra(Intent.EXTRA_STREAM, u);
						break;
					}
				}
			}
			startActivity(Intent.createChooser(intent, getTitle()));
			break;
		}
		case MENU_REPLY: {
			Bundle bundle = new Bundle();
			String title = topic.getTitle();
			if (!title.startsWith("Re: ")) {
				title = "Re: ".concat(title);
			}
			bundle.putString(PostHelper.EXTRA_TITLE, title);
			bundle.putString(PostHelper.EXTRA_BOARD, boardID);
			bundle.putInt(PostHelper.EXTRA_REID, id);
			bundle.putString(PostHelper.EXTRA_CONTENT, topic.getContent());
			bundle.putInt(PostHelper.EXTRA_TYPE, PostHelper.TYPE_REPLY);
			Intent intent = new Intent(SinglePostActivity.this, WritePost.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_AUTHOR: {
			String author = topic.getAuthor();
			Intent intent = new Intent(SinglePostActivity.this,
					ViewProfileActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(ViewProfileActivity.EXTRA_USER, author);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_MAIL: {
			String author = topic.getAuthor();
			Intent intent = new Intent(SinglePostActivity.this, WriteMail.class);
			Bundle bundle = new Bundle();
			bundle.putString(WriteMail.EXTRA_RECIEVER, author);
			bundle.putString(WriteMail.EXTRA_TITLE, topic.getTitle());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_ONE_TOPIC: {
			Intent intent = new Intent(this, ThreadList.class);
			Bundle bundle = new Bundle();
			String title = topic.getTitle();
			if (title.contains("Re:")) {
				title = title.replace("Re:", "").trim();
			}
			bundle.putString(PostHelper.EXTRA_TITLE, title);
			bundle.putString(PostHelper.EXTRA_BOARD, boardID);
			int id = topic.getId();
			bundle.putInt(PostHelper.EXTRA_ID, id);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			break;
		}
		case MENU_COPY: {
			ClipboardManager cm = (ClipboardManager) this
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setText(topic.getContent());
			break;
		}
		case MENU_ONE_TOPIC_ALL: {
			Intent intent = new Intent(this, ThreadList.class);
			Bundle bundle = new Bundle();
			String title = topic.getTitle();
			if (title.contains("Re:")) {
				title = title.replace("Re:", "").trim();
			}
			bundle.putString(PostHelper.EXTRA_TITLE, title);
			bundle.putString(PostHelper.EXTRA_BOARD, boardID);
			int gid = topic.getGid();
			bundle.putInt(PostHelper.EXTRA_ID, gid);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_EDIT: {
			Intent intent = new Intent(this, WritePost.class);
			Bundle bundle = new Bundle();
			bundle.putInt(PostHelper.EXTRA_TYPE, PostHelper.TYPE_EDIT);
			bundle.putString(PostHelper.EXTRA_BOARD, topic.getBoardName());
			bundle.putInt(PostHelper.EXTRA_ID, topic.getId());
			bundle.putString(PostHelper.EXTRA_TITLE, topic.getTitle());
			bundle.putString(PostHelper.EXTRA_CONTENT, topic.getContent());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!MyApplication.isNightMode) {

			menu.add(Menu.NONE, MENU_SHARE, Menu.NONE, "share")
					.setIcon(R.drawable.ic_menu_share_inverse)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_REPLY, Menu.NONE, "reply")
					.setIcon(R.drawable.ic_menu_topic_reply_inverse)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_AUTHOR, Menu.NONE, "author")
					.setIcon(R.drawable.ic_menu_profile_inverse)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_MAIL, Menu.NONE, "mail")
					.setIcon(R.drawable.ic_menu_new_mail_inverse)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			menu.add(Menu.NONE, MENU_SHARE, Menu.NONE, "share")
					.setIcon(R.drawable.ic_menu_share)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_REPLY, Menu.NONE, "reply")
					.setIcon(R.drawable.ic_menu_topic_reply)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_AUTHOR, Menu.NONE, "author")
					.setIcon(R.drawable.ic_menu_profile)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_MAIL, Menu.NONE, "mail")
					.setIcon(R.drawable.ic_menu_new_mail)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		
		if (getSDKVersion() >= 11) {
			Log.i(TAG, "SDKVersion no less than 11");
			SubMenu subMenu = menu.addSubMenu(Menu.NONE, MENU_MORE, Menu.NONE,
					"more");
			subMenu.add(Menu.NONE, MENU_ONE_TOPIC, Menu.NONE,
					R.string.post_expand_here);
			subMenu.add(Menu.NONE, MENU_ONE_TOPIC_ALL, Menu.NONE,
					R.string.post_expand);
			subMenu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.post_edit);
			subMenu.add(Menu.NONE, MENU_COPY, Menu.NONE, R.string.post_copy);
			MenuItem subMenuItem = subMenu.getItem();
			if (MyApplication.isNightMode) {
				subMenuItem.setIcon(R.drawable.ic_menu_more).setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM);
			} else {
				subMenuItem.setIcon(R.drawable.ic_menu_more_inverse)
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			}
		} else {
			Log.i(TAG, "SDKVersion less than 11");
			menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.post_edit)
					.setIcon(R.drawable.ic_menu_edit_inverse)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_COPY, Menu.NONE, R.string.post_copy)
					.setIcon(R.drawable.ic_menu_copy_holo_dark)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_ONE_TOPIC, Menu.NONE,
					R.string.post_expand_here).setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.add(Menu.NONE, MENU_ONE_TOPIC_ALL, Menu.NONE,
					R.string.post_expand).setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);

		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		if (doRetrieveTask != null
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			doRetrieveTask.cancel(true);
		}
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiscCache();
		super.onDestroy();
	}

	/**
	 * 
	 * @return
	 */
	private int getSDKVersion() {
		return Integer.valueOf(android.os.Build.VERSION.SDK_INT);
	}

	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			List<Topic> topicList = null;
			try {
				topicList = BBSOperator.getInstance().getTopicList(params[0]);
			} catch (HttpException e) {
				e.printStackTrace();
				return TaskResult.Failed;
			}
			topic = topicList.get(0);

			return TaskResult.OK;
		}
	}

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setup() {
		// TODO Auto-generated method stub

	}

}