package com.gfan.sbbs.ui.main;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.Adapter.BoardListAdapter;
import com.gfan.sbbs.ui.Adapter.TopicListAdapter;
import com.gfan.sbbs.utils.MyListView;
import com.gfan.sbbs.utils.MyListView.OnLoadMoreDataListener;
import com.umeng.analytics.MobclickAgent;

public class SearchResult extends BaseActivity implements OnPageChangeListener ,OnLoadMoreDataListener{
	private ViewPager viewPager;
	private TextView  boardLabel, topicLabel;
	private TextView[] labels;
	private List<View> viewList;
	private TextView moreBtn;
	private View moreView;
	private LinearLayout progressbar;

	private LayoutInflater inflater;
	private MyListView topicListView, boardListView;
	private List<Board> boardList;
	private List<Topic> topicList;
	private boolean isFirstLoad = true,hasMoreData = true;
	private int headPosition = 1, start = 0;
	
	private String keyWord, errorCause;
	private String searchTopicUrl,searchBoardUrl;
	
	private TopicListAdapter topicAdapter;
	private BoardListAdapter boardAdapter;
	private static final int LIMIT = 20;

	private GenericTask mSearchTopicTask, mSearchBoardTask;
	
	private static final String TAG = SearchResult.class.getName();
	
	private TaskListener mSearchBoardTaskListener = new TaskAdapter() {
		private ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mSearchBoardTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(SearchResult.this);
			pdialog.setMessage(getResources().getString(R.string.loading));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			pdialog.dismiss();
			boardListView.onRefreshComplete();
			if (TaskResult.OK == result) {
				boardAdapter.refresh(boardList);
			} else if (TaskResult.NO_DATA == result) {
				Toast.makeText(SearchResult.this, R.string.search_no_data,
						Toast.LENGTH_SHORT).show();
			}else if(TaskResult.Failed == result){
				Toast.makeText(SearchResult.this, errorCause, Toast.LENGTH_SHORT).show();
			}
		}
	};
	private TaskListener mSearchTopicListener = new TaskAdapter() {

		private ProgressDialog pdialog;
		@Override
		public String getName() {
			return "mSearchListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			if(isFirstLoad){
				pdialog = new ProgressDialog(SearchResult.this);
				pdialog.setMessage(getResources().getString(R.string.loading));
				pdialog.show();
				pdialog.setCanceledOnTouchOutside(false);
			}
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if(null != pdialog){
				pdialog.dismiss();
			}
			topicListView.onRefreshComplete();
			if (getResult(result)) {
				isFirstLoad = false;
				drawTopicView();
				goTop();
			}
		}
	};

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
//		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
//		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		keyWord = getIntent().getExtras().getString("keyword");
		initUrls();
		this.setContentView(R.layout.search_result);
		setTitle(R.string.search_result_title);
		initView();
		doSearchTask();
	}

	/**
	 * 初始化initViewPager
	 */
	private void initViewPager() {
		viewPager = (ViewPager) this.findViewById(R.id.viewPager);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewList = new ArrayList<View>();
		View boardView = inflater.inflate(R.layout.list_without_header, null);
		View topicView = inflater.inflate(R.layout.list_without_header, null);
		viewList.add(boardView);
		viewList.add(topicView);
		boardListView = (MyListView) boardView.findViewById(R.id.my_list);
		topicListView = (MyListView) topicView.findViewById(R.id.my_list);
		viewPager.setAdapter(new MyAdapter());
		boardAdapter = new BoardListAdapter(this);
		topicAdapter = new TopicListAdapter(this);
		boardListView.setAdapter(boardAdapter);
		topicListView.setAdapter(topicAdapter);
		topicListView.setLoadMoreListener(this);
	}

	/**
	 * 初始化搜索地址
	 */
	private void initUrls(){
		searchTopicUrl = SBBSConstants.BASE_API_URL+"/search/topics.json?keys="+keyWord;
		searchBoardUrl = SBBSConstants.BASE_API_URL+"/search/boards.json?name="+keyWord;
		
		if(MyApplication.getInstance().isLogined()){
			searchBoardUrl += "&token="+ MyApplication.getInstance().getToken();
			searchTopicUrl += "&token="+ MyApplication.getInstance().getToken();
		}
	}
	
	private void initView() {
		
		initViewPager();
		moreView = getLayoutInflater().inflate(R.layout.moredata, null);
		moreBtn = (TextView) moreView.findViewById(R.id.load_more_btn);
		progressbar = (LinearLayout) moreView.findViewById(R.id.more_progress);
		topicListView.addFooterView(moreView);

		boardLabel = (TextView) this.findViewById(R.id.search_board);
		topicLabel = (TextView) this.findViewById(R.id.search_topic);
		labels = new TextView[] { boardLabel, topicLabel};
		
		viewPager.setOnPageChangeListener(this);
		
		moreBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doLoadMore();
			}
		});

		boardLabel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(0);
			}
		});
		topicLabel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
			}
		});
		boardListView.setonRefreshListener(new MyListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				doSearchBoard();
			}
		});
		boardListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Board board = getContextItemBoard(position);
						enterBoard(board);
					}
				});
		topicListView.setonRefreshListener(new MyListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				start = 0;
				isFirstLoad = true;
				doSearchTopic();
			}
		});
		topicListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long id) {
						Intent intent = new Intent(SearchResult.this,
								SinglePostActivity.class);
						try {
							Topic topic = getContextItemTopic(position);
							if (null == topic) {
								return;
							}
							Bundle bundle = new Bundle();
							bundle.putString(PostHelper.EXTRA_BOARD, topic.getBoardName());
							bundle.putInt(PostHelper.EXTRA_ID, topic.getId());
							intent.putExtras(bundle);
							startActivity(intent);
						} catch (IndexOutOfBoundsException e) {
							doLoadMore();
						}
					}
				});
	}

	/**
	 * 获取版面条目
	 * @param position
	 * @return
	 */
	private Board getContextItemBoard(int position) {
		if (position > 0 && position <= boardAdapter.getCount()) {
			return (Board) boardAdapter.getItem(position - 1);
		}
		return null;
	}

	/**
	 * 获取帖子条目
	 * @param position
	 * @return
	 */
	private Topic getContextItemTopic(int position) {
		if (position >= 1 && position <= topicAdapter.getCount()) {
			return (Topic) topicAdapter.getItem(position - 1);
		}
		return null;
	}

	/**
	 * 打开版面界面
	 * @param board
	 */
	private void enterBoard(Board board) {
		if (null == board) {
			return;
		}
		Intent intent = new Intent(SearchResult.this, TopicList.class);
		Bundle bundle = new Bundle();
		bundle.putString("boardID", board.getId());
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		if (null != mSearchBoardTask && mSearchBoardTask.getStatus() == GenericTask.Status.RUNNING) {
			mSearchBoardTask.cancel(true);
		}
		if (null != mSearchTopicTask && mSearchTopicTask.getStatus() == GenericTask.Status.RUNNING) {
			
			mSearchTopicTask.cancel(true);
		}
		super.onDestroy();
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

	private void doSearchTask(){
		doSearchBoard();
		doSearchTopic();
	}
	
	private void doSearchBoard() {

		mSearchBoardTask = new SearchBoardTask();
		mSearchBoardTask.setListener(mSearchBoardTaskListener);
		mSearchBoardTask.execute(searchBoardUrl);
	}

	private void doSearchTopic() {
		String url = searchTopicUrl.concat("&start=" + start + "&limit=" + LIMIT);
		mSearchTopicTask = new SearchTopicTask();
		mSearchTopicTask.setListener(mSearchTopicListener);
		mSearchTopicTask.execute(url);
	}

	private void doLoadMore() {
		moreBtn.setVisibility(View.GONE);
		progressbar.setVisibility(View.VISIBLE);
		doSearchTopic();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
		}
		return true;
	}
	
	public boolean getResult(TaskResult result) {
		moreBtn.setVisibility(View.VISIBLE);
		progressbar.setVisibility(View.GONE);
		Log.i("SearchResult", "getResult " + result.toString());
		if (TaskResult.Failed == result) {
			Toast.makeText(this, errorCause, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (null == topicList || 0 == topicList.size()) {
			Toast.makeText(this, R.string.search_no_data, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void drawTopicView() {
		topicAdapter.refresh(topicList);
	}

	private void goTop() {
		topicListView.setSelection(headPosition);
	}

	private class MyAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return viewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(viewList.get(arg1));
		}

		@Override
		public Object instantiateItem(View view, int position) {
			Log.i("SearchResult", "instantiateItem,position is " + position);
			((ViewPager) view).addView(viewList.get(position), 0);
			return viewList.get(position);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		Log.i(TAG, "arg0 is "+arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		for (int i = 0, len = labels.length; i < len; i++) {
			if (arg0 == i) {
				labels[i].setBackgroundResource(R.color.gold);
			} else {
				labels[i].setBackgroundResource(R.color.aa_white);
			}
		}
		if (1 == arg0 && null == topicList) {
			doSearchTopic();
		}
	}

	private class SearchBoardTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			//				boardList = SBBSSupport.getFavListAPI(params[0]);
			try {
				boardList = BBSOperator.getInstance().getBoardList(params[0]);
			} catch (HttpException e) {
				e.printStackTrace();
				errorCause = e.getMessage();
				return TaskResult.Failed;
			}
			if (null == boardList || boardList.size() == 0) {
				return TaskResult.NO_DATA;
			}
			return TaskResult.OK;
		}

	}

	private class SearchTopicTask extends GenericTask {

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
				headPosition = 1;
				topicList = list;
			} else {
				headPosition = topicList.size();
				topicList.addAll(list);
			}
			if(list.size() < LIMIT){
				hasMoreData = false;
			}
			start += list.size();
			return TaskResult.OK;
		}

	}

	@Override
	public void onLoadMoreData() {
		int lastItemIndex = topicListView.getLastItemIndex();
		if(hasMoreData && lastItemIndex == topicAdapter.getCount()){
			doLoadMore();
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
