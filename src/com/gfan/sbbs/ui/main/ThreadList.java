package com.gfan.sbbs.ui.main;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.Adapter.TopReplyAdapter;
import com.gfan.sbbs.utils.MyListView;
import com.gfan.sbbs.utils.MyListView.OnLoadMoreDataListener;
import com.umeng.analytics.MobclickAgent;

@SuppressWarnings("deprecation")
public class ThreadList extends BaseActivity implements OnLoadMoreDataListener {

//	public static final int REFRESH_REPLY = 1;
	public MyListView myListView;
	private String title, boardID;
	private int id, start = 0, headPosition = 0, firstVisibleItemIndex = 0;
	private String baseUrl, errorCause;
	private boolean isFirstLoad = true, hasMoreData = true;

	private static final int LOADNUM = 20;

	private static final int MENU_AUTHORINFO = Menu.FIRST;
	private static final int MENU_MAIL_AUTHOR = Menu.FIRST + 1;
	private static final int MENU_REPLY = Menu.FIRST + 2;
	private static final int MENU_SHARE = Menu.FIRST + 3;
	private static final int MENU_COPY = Menu.FIRST + 4;
	private static final int MENU_SINGLE_SHARE = Menu.FIRST + 5;
	private static final int MENU_ENTER_BOARD = Menu.FIRST + 6;
	private static final int MENU_QUICK_REPLY = Menu.FIRST + 7;

	private View moreView;
//	private View qRView;
	private TextView moreBtn;
	private LinearLayout progressbar;
//	private ImageView qRBtn;
	private EditText qREditText;
	private List<Topic> threadList;
	private TopReplyAdapter myAdapter;
	private GenericTask doRetrieveTask, mPostTask;
	private static final String TAG = ThreadList.class.getName();
	private static final String IS_RUNNING = "running";

	private static class State {
		public int headPosition;
		public int start;
		public boolean isFirstLoad;
		public List<Topic> threadList;

		State(ThreadList replyActivity) {
			headPosition = replyActivity.firstVisibleItemIndex;
			threadList = replyActivity.threadList;
			start = replyActivity.start;
			isFirstLoad = replyActivity.isFirstLoad;
		}
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
			if (isFirstLoad) {
				pdialog = new ProgressDialog(ThreadList.this);
				pdialog.setMessage(getResources().getString(R.string.loading));
				pdialog.show();
				pdialog.setCanceledOnTouchOutside(false);
			}
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (null != pdialog) {
				pdialog.dismiss();
			}
			myListView.onRefreshComplete();
			handleRetrieveResult(result);
		}
	};

	private TaskListener doPostListener = new TaskAdapter() {
		ProgressDialog pdialog;

		@Override
		public String getName() {
			return "doPostListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(ThreadList.this);
			pdialog.setMessage(getString(R.string.sending_post));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (null != pdialog) {
				pdialog.dismiss();
			}
			if (result == TaskResult.Failed) {
				Toast.makeText(ThreadList.this, errorCause, Toast.LENGTH_SHORT)
						.show();
			} 
//			else {
//				Toast.makeText(ThreadList.this, R.string.post_success, Toast.LENGTH_SHORT).show();
//				qREditText.setText("");
//				qREditText.clearFocus();
//			}
			
		}
	};

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.list_without_header);
		initArgs();
		setupState();
		registerForContextMenu(myListView);
		boolean wasRunning = isTrue(savedInstanceState, IS_RUNNING);
		State state = (State) getLastNonConfigurationInstance();
		if (null != state) {
			int lastPosition = state.headPosition;
			threadList = state.threadList;
			start = state.start;
			isFirstLoad = state.isFirstLoad;
			if (null != threadList && !threadList.isEmpty() && !wasRunning) {
				draw();
				myListView.setSelection(lastPosition);
			}
		} else {
			doRetrieve();
		}
	}

	private void initArgs() {
		id = getIntent().getExtras().getInt(PostHelper.EXTRA_ID);
		boardID = getIntent().getExtras().getString(PostHelper.EXTRA_BOARD);
		baseUrl = SBBSConstants.BASE_API_URL+"/topic/" + boardID + "/" + id
				+ ".json?limit=" + LOADNUM;
		title = getIntent().getStringExtra("title");
		setTitle(title);
		myListView = (MyListView) this.findViewById(R.id.my_list);
		moreView = getLayoutInflater().inflate(R.layout.moredata, null);
		moreBtn = (TextView) moreView.findViewById(R.id.load_more_btn);
		progressbar = (LinearLayout) moreView.findViewById(R.id.more_progress);
		myListView.addFooterView(moreView);
//		this.findViewById(R.id.re_devider).setVisibility(View.VISIBLE);
		if (isLogined) {
			baseUrl = baseUrl.concat("&token=" + token);
//			qRView = this.findViewById(R.id.quick_reply);
//			qRView.setVisibility(View.VISIBLE);
//			qREditText = (EditText) qRView.findViewById(R.id.quick_reply_txt);
//			qRBtn = (ImageView) qRView.findViewById(R.id.quick_reply_btn);
		}
		myAdapter = new TopReplyAdapter(this);
		myListView.setAdapter(myAdapter);
	}

	private void setupState() {
		myListView.setonRefreshListener(new MyListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				start = 0;
				isFirstLoad = true;
				doRetrieve();
			}
		});
		myListView.setLoadMoreListener(this);
		moreBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doLoadMore();
			}
		});

//		myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//			
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				switch(scrollState){
//				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:{
//					qRView.setVisibility(View.GONE);
//				}
//				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:{
//					qRView.setVisibility(View.VISIBLE);
//				}
//				}
//			}
//			
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		myListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Log.i("ThreadList", "position is " + position);
						Topic topic = getContextItemTopic(position);
						if (null != topic) {
							Intent intent = new Intent(ThreadList.this,
									SinglePostActivity.class);
							intent.putExtra("topic", topic);
							startActivity(intent);
						}
					}
				});
//		if (!isLogined) {
//			return;
//		}
//		qRBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View view) {
//				doPost();
//			}
//		});
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
	protected void onSaveInstanceState(Bundle outState) {
		if (null != doRetrieveTask
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(IS_RUNNING, true);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return createState();
	}

	private synchronized State createState() {
		return new State(this);
	}

	private void doRetrieve() {
		if (null != doRetrieveTask
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
			doRetrieveTask = new RetrieveTask();
			doRetrieveTask.setListener(mRetrieveTaskListener);
			String url = getBaseUrl().concat("&start=" + start);
			doRetrieveTask.execute(url);
		
	}

	public void doPost() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(qREditText.getWindowToken(), 0);
		mPostTask = new DoPostTask();
		mPostTask.setListener(doPostListener);
		String content = qREditText.getText().toString().trim();
		if (TextUtils.isEmpty(content)) {
			return;
		}
		String reTitle = title;
		if (!title.contains("Re:")) {
			reTitle = "Re: ".concat(title);
		}
		mPostTask.execute(boardID, reTitle, content, Integer.valueOf(id)
				.toString(), token);
	}

	private void doLoadMore() {
		moreBtn.setVisibility(View.GONE);
		progressbar.setVisibility(View.VISIBLE);
		doRetrieve();
	}

	private void draw() {
		myAdapter.refresh(threadList);
	}

	private void goTop() {
		myListView.setSelection(headPosition);
	}

	private Topic getContextItemTopic(int position) {
		Log.i("ReplyActivity", "position is " + position);
		if (position >= 1 && position <= myAdapter.getCount()) {
			return (Topic) myAdapter.getItem(position - 1);
		}
		return null;
	}
	
	private void reply(Topic topic){
		int id = topic.getId();
		Bundle bundle = new Bundle();
		String reTitle = topic.getTitle();
		if (!topic.getTitle().startsWith("Re: ")) {
			reTitle = "Re: ".concat(topic.getTitle());
		}
		bundle.putInt(PostHelper.EXTRA_TYPE, PostHelper.TYPE_REPLY);
		bundle.putString(PostHelper.EXTRA_TITLE, reTitle);
		bundle.putString(PostHelper.EXTRA_BOARD, boardID);
		bundle.putInt(PostHelper.EXTRA_REID, id);
		bundle.putString(PostHelper.EXTRA_CONTENT, topic.getContent());
		Intent intent = new Intent(this, WritePost.class);
		intent.putExtras(bundle);
		startActivity(intent);

	}


	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo lm = (AdapterContextMenuInfo) item.getMenuInfo();
		Topic topic = getContextItemTopic(lm.position);
		switch (item.getItemId()) {
		case MENU_AUTHORINFO:
		{
			String author = topic.getAuthor();
			Intent intent = new Intent(this, ViewProfileActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("userID", author);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_MAIL_AUTHOR:
		{
			String author = topic.getAuthor();
			Intent intent = new Intent(this, WriteMail.class);
			Bundle bundle = new Bundle();
			bundle.putString(WriteMail.EXTRA_RECIEVER, author);
			bundle.putString(WriteMail.EXTRA_TITLE, topic.getTitle());
			bundle.putString(WriteMail.EXTRA_CONTENT, topic.getContent());
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		case MENU_REPLY:
		{
			reply(topic);
			break;
		}
		case MENU_SINGLE_SHARE: {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			String link = "http://bbs.seu.edu.cn/r/post/" + boardID + "/" + id;
			intent.putExtra(Intent.EXTRA_SUBJECT, "");
			intent.putExtra(Intent.EXTRA_TEXT,
					getResources().getString(R.string.app_share_tag) + topic.getTitle() + getResources().getString(R.string.app_share_link) + link);
			startActivity(Intent.createChooser(intent, getTitle()));
			break;
		}
		case MENU_SHARE: {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			// http://bbs.seu.edu.cn/r/topic/ID/170527
			String link = "http://bbs.seu.edu.cn/r/topic/" + boardID + "/" + id;
			intent.putExtra(Intent.EXTRA_SUBJECT, "");
			intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_share_tag) + title
					+ getResources().getString(R.string.app_share_link) + link);
			startActivity(Intent.createChooser(intent, getTitle()));
			break;
		}
		case MENU_COPY: {
			ClipboardManager cm = (ClipboardManager) this
					.getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setText(topic.getContent());
			break;
		}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.app_menu);
		menu.add(0, MENU_AUTHORINFO, 0, R.string.thread_view_author);
		menu.add(0, MENU_MAIL_AUTHOR, 0, R.string.thread_mail_author);
		menu.add(0, MENU_REPLY, 0, R.string.thread_reply);
		menu.add(0, MENU_SINGLE_SHARE, 0, R.string.thread_share);
		menu.add(0, MENU_SHARE, 0, R.string.thread_share_all);
		menu.add(0, MENU_COPY, 0, R.string.thread_share_all);
	}

	@Override
	protected void onDestroy() {

		if (doRetrieveTask != null
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			doRetrieveTask.cancel(true);
		}
		super.onDestroy();
	}


	private void handleRetrieveResult(TaskResult result) {
		moreBtn.setVisibility(View.VISIBLE);
		progressbar.setVisibility(View.GONE);
		if (result == TaskResult.IO_ERROR || TaskResult.Failed == result) {
			loadFailure();
			return;
		}
		if(result == TaskResult.OK){
			isFirstLoad = false;
			draw();
			goTop();
		}
	}

	private void loadFailure() {
		Toast.makeText(this, errorCause, Toast.LENGTH_SHORT).show();
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ENTER_BOARD:{
			Intent intent = new Intent(ThreadList.this, TopicList.class);
			Bundle bundle = new Bundle();
			bundle.putString("boardID", boardID);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		case MENU_QUICK_REPLY :{
			reply(getContextItemTopic(1));
			return true;
		}
		default:		
			return super.onOptionsItemSelected(item);
		
		}
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(0, MENU_ENTER_BOARD, 0, "enter board")
				.setIcon(R.drawable.ic_notification_post_read)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(Menu.NONE, MENU_QUICK_REPLY, Menu.NONE, "reply")
		.setIcon(R.drawable.ic_menu_reply_inverse)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		return true;
	}

	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			List<Topic> list;
			try {
				list = BBSOperator.getInstance().getTopicList(params[0]);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				return TaskResult.Failed;
			}
			if (isFirstLoad) {
//			if(null == threadList || 0 == threadList.size()){
				threadList = list;
				headPosition = 0;
			} else {
				headPosition = threadList.size() - 1;
				if (0 == list.size()) {
					return TaskResult.NO_DATA;
				} else {
					threadList.addAll(list);
				}
			}
			if (list.size() < LOADNUM) {
				hasMoreData = false;
			}
			isFirstLoad = false;
			start = threadList.size();
			if (threadList.size() == 0) {
				return TaskResult.NO_DATA;
			}
			return TaskResult.OK;
		}

	}

	private class DoPostTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			// result = SBBSSupport.doPost(board, reid, post, title,
			// content);
			// topicAdapter = SBBSSupport.doPostAPI(params);
			String url = SBBSConstants.BASE_URL
					+ "/api/topic/post.json?type=1&token=" + token;
			try {
				BBSOperator.getInstance().doPost(url, params[0], params[1],
						params[2], params[3]);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				return TaskResult.Failed;
			}
			return TaskResult.OK;
		}
	}

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setup() {

	}

	@Override
	public void onLoadMoreData() {
		int lastItemIndex = myListView.getLastItemIndex();
		Log.i(TAG,
				"lastItem is " + lastItemIndex + "getCount is "
						+ myAdapter.getCount());
		if (hasMoreData && lastItemIndex == myAdapter.getCount()) {
			doLoadMore();
		}
	}
}
