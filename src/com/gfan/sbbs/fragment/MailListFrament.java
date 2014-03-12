package com.gfan.sbbs.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.db.MailDAO;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.ActivityFragmentTargets;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.OnOpenActivityFragmentListener;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Adapter.MailAdapter;
import com.gfan.sbbs.ui.base.BaseViewModel;
import com.gfan.sbbs.ui.base.HomeViewModel;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.ui.main.WriteMail;
import com.gfan.sbbs.utils.MyListView;
import com.gfan.sbbs.utils.MyListView.OnLoadMoreDataListener;
import com.umeng.analytics.MobclickAgent;

public class MailListFrament extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver, OnLoadMoreDataListener {
	private LayoutInflater mInflater;
	private View mLayout;
	private HomeViewModel mHomeViewModel;
	private OnOpenActivityFragmentListener mOnOpenActivityListener;
	private MyListView mailListView;
	private MailAdapter myAdapter;
	private List<Mail> mailList, inList, sendList, trashList;

	private GenericTask doRetrieveTask;

	private View moreView;
	private TextView moreBtn;
	private LinearLayout progressbar;

	private boolean isFirstLoad = true, forceLoad = false, isLoaded = false,
			isLogined;
	private String mailUrl, errorCause;
	private int start = 0, inStart = 0, sendStart = 0, trashStart = 0,
			headPosition;
	private int nowBox, lastItem;
	private boolean hasMoreData = true;

	private static final int LOADNUM = 20;
	private static final int MAILBOX = 1;
	private static final int SENDBOX = 2;
	private static final int DELETEBOX = 3;
	private static final int MENU_NEW = 10;

//	private static final int OPENREQUESTCODE = 0;
//	private static final int DELMAIL = 1;

	private static final String TAG = "MailListFragment";

	private TaskListener mRetrieveListener = new TaskAdapter() {
		ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mRetrieveListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			if (isFirstLoad) {
				pdialog = new ProgressDialog(getSherlockActivity());
				pdialog.setMessage(getResources().getString(R.string.loading));
				pdialog.show();
			}
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (null != pdialog) {
				pdialog.cancel();
			}
			mailListView.onRefreshComplete();
			if (processResult(result)) {
				isFirstLoad = false;
				forceLoad = false;
				isLoaded = true;
				draw();
				goTop();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		inList = new ArrayList<Mail>();
		sendList = new ArrayList<Mail>();
		trashList = new ArrayList<Mail>();
		Log.i(TAG, "OnCreate");
	}

	
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MailListFragment");
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MailListFragment");
	}



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SherlockFragmentActivity parentActivity = getSherlockActivity();
		if (parentActivity instanceof OnOpenActivityFragmentListener) {
			mOnOpenActivityListener = (OnOpenActivityFragmentListener) parentActivity;
		}
		ActionBarSherlock mSherlock = ActionBarSherlock.wrap(parentActivity);
		mSherlock
				.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		initArgs();
		initEvents();

		if (null != mHomeViewModel.getCurrentTab()
				&& mHomeViewModel.getCurrentTab().equals(
						ActivityFragmentTargets.TAB_MAIL)) {
			Log.i(TAG, "MailListFragment doretrieve");
			if (isLogined) {
				doRetrieve();
			}
		}
		Log.i(TAG, "MailListFragment -->onActivityCreated");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mInflater = inflater;
		mLayout = mInflater.inflate(R.layout.list_without_header, null);
		mHomeViewModel = ((MyApplication) getSherlockActivity()
				.getApplication()).getmHomeViewModel();
		mHomeViewModel.registerViewModelChangeObserver(this);

		Log.i(TAG, "MailListFragment-->OnCreateView");

		return mLayout;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int position = item.getItemId();
		if (position == MENU_NEW) {
			Intent intent = new Intent(getSherlockActivity(),WriteMail.class);
//			intent.setClassName("com.yuchao.ui", "com.yuchao.ui.WriteMail");
			startActivity(intent);
			return true;
		} else if (position > 0 && position < 4) {
			Log.i(TAG,
					"current box is" + nowBox + ",newBox is "
							+ item.getItemId());
			changeBox(item.getItemId());
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add(1, MENU_NEW, Menu.NONE, "new mail")
				.setIcon(R.drawable.ic_compose_inverse)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu subMenu = menu.addSubMenu(1, 0, Menu.NONE, R.string.mail_box_switch);
		subMenu.add(0, MAILBOX, 0, R.string.in_box);
		subMenu.add(0, SENDBOX, 0, R.string.send_box);
		subMenu.add(0, DELETEBOX, 0, R.string.trash_box);

		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setIcon(R.drawable.ic_notification_post_read);
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void doRetrieve() {
		if (null != doRetrieveTask
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		String url = mailUrl + "&type=" + (nowBox - 1) + "&start=" + start
				+ "&limit=" + LOADNUM;
		doRetrieveTask = new RetrieveMailTask();
		doRetrieveTask.setListener(mRetrieveListener);
		doRetrieveTask.execute(url);
		Log.i(TAG, "doRetrieve");
	}

	private void doLoadMore() {
		forceLoad = true;
		moreBtn.setVisibility(View.GONE);
		progressbar.setVisibility(View.VISIBLE);
		doRetrieve();
	}

	private void initArgs() {
		mailUrl = SBBSConstants.MAILURL;
		isLogined = MyApplication.checkLogin();
		if (isLogined) {
			mailUrl = mailUrl.concat("?token=" + MyApplication.getInstance().getToken());
		}
		mailListView = (MyListView) mLayout.findViewById(R.id.my_list);
		myAdapter = new MailAdapter(mInflater);
		mailListView.setAdapter(myAdapter);

		moreView = mInflater.inflate(R.layout.moredata, null);
		moreBtn = (TextView) moreView.findViewById(R.id.load_more_btn);
		progressbar = (LinearLayout) moreView.findViewById(R.id.more_progress);
		mailListView.addFooterView(moreView);
		nowBox = MAILBOX;
	}

	private void initEvents() {
		mailListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				createMailActivity(position);
			}
		});
		mailListView.setLoadMoreListener(this);
		mailListView.setonRefreshListener(new MyListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				isFirstLoad = true;
				start = 0;
				forceLoad = true;
				doRetrieve();
			}
		});
		moreBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doLoadMore();
			}
		});

	}

	private Mail getContextItemMail(int position) {
		if (position >= 1 && position <= myAdapter.getCount()) {
			return (Mail) myAdapter.getItem(position - 1);
		}
		return null;
	}

	public int getStart(int box) {
		switch (box) {
		case MAILBOX:
			return inStart;
		case SENDBOX:
			return sendStart;
		case DELETEBOX:
			return trashStart;
		default:
			return 0;
		}
	}

	private void setStart(int box, int value) {
		switch (box) {
		case MAILBOX:
			inStart = value;
			break;
		case SENDBOX:
			sendStart = value;
			break;
		case DELETEBOX:
			trashStart = value;
			break;
		}
	}

	private List<Mail> getMailList(int box) {
		switch (box) {
		case MAILBOX:
			return inList;
		case SENDBOX:
			return sendList;
		case DELETEBOX:
			return trashList;
		default:
			return null;
		}
	}

	private void changeBox(int box) {
		if (box == nowBox) {
			Toast.makeText(getSherlockActivity(), R.string.mail_current_box_notice,
					Toast.LENGTH_SHORT).show();
			return;
		}
		List<Mail> nowList = getMailList(nowBox);
		nowList.clear();
		nowList.addAll(mailList);// save current list
		mailList = getMailList(box);// point mailList to the desired mail list
		setStart(nowBox, start);
		start = getStart(box);
		nowBox = box;
		setActivityTitle(nowBox);
		if (0 == mailList.size()) {
			isFirstLoad = true;
			doRetrieve();
		} else {
			draw();
		}
	}
	
	private void setActivityTitle(int box){
		switch(box){
		case MAILBOX:
			getSherlockActivity().setTitle(R.string.menu_mail);
			break;
		case SENDBOX:
			getSherlockActivity().setTitle(R.string.menu_sendbox);
			break;
		case DELETEBOX:
			getSherlockActivity().setTitle(R.string.menu_trashbox);
			break;
		default:
			getSherlockActivity().setTitle(R.string.menu_mail);
				
		}
	}

	private void createMailActivity(int position) {
		Mail mail = null;
		mail = getContextItemMail(position);
		if (null == mail) {
			return;
		}
		String num = mail.getNum();
		String author = mail.getFrom();
		String title = mail.getTitle();
		Bundle bundle = new Bundle();
		bundle.putString("num", num);
		bundle.putString("author", author);
		bundle.putString("title", title);
		bundle.putInt("position", position);
		bundle.putInt("type", nowBox - 1);
		mail.setUnRead(false);
		if (null != mOnOpenActivityListener) {
			mOnOpenActivityListener.onOpenActivityOrFragment(
					ActivityFragmentTargets.MAIL, bundle);
		}
	}

	private boolean processResult(TaskResult result) {
		moreBtn.setVisibility(View.VISIBLE);
		progressbar.setVisibility(View.GONE);
		if (TaskResult.IO_ERROR == result || TaskResult.Failed == result) {
			Toast.makeText(getSherlockActivity(), errorCause,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TaskResult.NO_DATA == result) {
			Toast.makeText(getSherlockActivity(), R.string.mail_no_data,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void draw() {
		myAdapter.refresh(mailList);
		setActivityTitle(nowBox);
	}

	private void goTop() {
		mailListView.setSelection(headPosition);
	}

	private void processUnLogin() {

		Toast.makeText(getSherlockActivity(), R.string.login_indicate,
				Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	public void onDestroy() {
		if (doRetrieveTask != null
				&& doRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			doRetrieveTask.cancel(true);
		}
		super.onDestroy();
	}

	private class RetrieveMailTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			MailDAO dao = new MailDAO(MyApplication.mContext);
			List<Mail> list = dao.fetchMail(nowBox - 1);
			if (0 == list.size() || forceLoad) {
				List<Mail> newList;
				try {
					newList = BBSOperator.getInstance().getMailList(params[0]);
				} catch (HttpException e) {
					e.printStackTrace();
					errorCause = e.getMessage();
					return TaskResult.Failed;
				}
				if (isFirstLoad) {
					headPosition = 1;
					mailList = newList;
					if (null == mailList || mailList.size() == 0) {
						return TaskResult.NO_DATA;
					}
				} else {
					headPosition = mailList.size();
					mailList.addAll(newList);
				}
				if (newList.size() < LOADNUM) {
					hasMoreData = false;
				}
				dao.deleteMail(nowBox - 1);
				dao.insertMail(mailList, nowBox - 1);
			} else {
				mailList = list;
				isFirstLoad = false;
			}
			start = mailList.size();
			if (mailList.size() == 0) {
				return TaskResult.NO_DATA;
			}
			return TaskResult.OK;

		}

	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (HomeViewModel.CURRENTTAB_PROPERTY_NAME.equals(changedPropertyName)) {
			if (isLogined) {
				if (!isLoaded
						&& mHomeViewModel.getCurrentTab().equals(
								ActivityFragmentTargets.TAB_MAIL)) {
					doRetrieve();

				} else if (isLoaded
						&& mHomeViewModel.getCurrentTab().equals(
								ActivityFragmentTargets.TAB_MAIL)) {
					draw();
				}
			}
		} else {
			processUnLogin();
		}

	}

	@Override
	public void onLoadMoreData() {
		lastItem = mailListView.getLastItemIndex();
		if (hasMoreData && myAdapter.getCount() == lastItem) {
			doLoadMore();
		}
	}
}
