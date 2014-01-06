package com.gfan.sbbs.fragment;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.dao.board.MarkReadDAO;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.ActivityFragmentTargets;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.OnOpenActivityFragmentListener;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.Adapter.BoardListAdapter;
import com.gfan.sbbs.ui.base.BaseViewModel;
import com.gfan.sbbs.ui.base.HomeViewModel;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.ui.main.TopicList;
import com.gfan.sbbs.utils.MyListView;

public class FavFragment extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver {

	private MyListView favListView;
	private List<Board> favList;
	private boolean isLoaded = false, isLogined;
	private boolean onRoot = true;
	private String favUrl, errorCause;
	private BoardListAdapter myAdapter;
	private GenericTask  mRetrieveTask;
	private HomeViewModel mHomeViewModel;
	private OnOpenActivityFragmentListener mOnOpenActivityListener;

	private LayoutInflater mInflater;
	private View mLayout, mToUpperFolder;
	private TextView mUpFolder;

	private static final String TAG = "FavFragment";

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		ProgressDialog pdialog;

		@Override
		public String getName() {
			return "mRetrieveTaskListener";
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
			pdialog = new ProgressDialog(getSherlockActivity());
			pdialog.setMessage(getResources().getString(R.string.loading));
			pdialog.show();
			pdialog.setCanceledOnTouchOutside(false);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (null != pdialog) {
				pdialog.dismiss();
			}
			favListView.onRefreshComplete();
			processResult(result);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity parentActivity = getSherlockActivity();
		if (parentActivity instanceof OnOpenActivityFragmentListener) {
			mOnOpenActivityListener = (OnOpenActivityFragmentListener) parentActivity;
		}
		initArgs();
		initEvents();
		Log.i(TAG, "FavFragment createView");
		if (null != mHomeViewModel.getCurrentTab()
				&& ActivityFragmentTargets.TAB_FAV.equals(mHomeViewModel
						.getCurrentTab())) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);

			if (isLogined) {
				Log.i(TAG, "FavFragment do retrieve");
				doRetrieve();
			} else {
				processUnlogin();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mLayout = mInflater.inflate(R.layout.list_without_header, null);
		mHomeViewModel = ((MyApplication) getActivity().getApplication())
				.getmHomeViewModel();
		mHomeViewModel.registerViewModelChangeObserver(this);
		return mLayout;
	}

	private void doRetrieve() {
		if (null != mRetrieveTask
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mRetrieveTask = new RetrieveFavTask();
		mRetrieveTask.setListener(mRetrieveTaskListener);
		mRetrieveTask.execute(favUrl);
	}

	@SuppressWarnings("unused")
	private void markBoardRead(Board board) {
		new MarkReadDAO(board).markAsRead();
	}

	private void initArgs() {
		isLogined = MyApplication.checkLogin();
		Log.i(TAG, "isLogined is " + isLogined);
		favUrl = SBBSConstants.FAVURL;
		if (isLogined) {
			favUrl = favUrl.concat("?token=" + MyApplication.getInstance().getToken());
		}
		favListView = (MyListView) mLayout.findViewById(R.id.my_list);
		mToUpperFolder = mInflater.inflate(R.layout.fav_item, null);
		mToUpperFolder.setId(R.id.up_folder);
		favListView.addHeaderView(mToUpperFolder);
		mUpFolder = (TextView) mToUpperFolder.findViewById(R.id.fav_item);
		mUpFolder.setText(R.string.up_folder);
		myAdapter = new BoardListAdapter(mInflater);
		favListView.setAdapter(myAdapter);
	}

	private void initEvents() {
		favListView.setonRefreshListener(new MyListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				doRetrieve();
			}
		});
		favListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long id) {
						Board board = getContextItemBoard(position);
						if (null == board) {
							return;
						}
						if (board.isDirectory()) {
							List<Board> list = board.getChildBoards();
							if (null == list || list.size() == 0) {
								Toast.makeText(getSherlockActivity(), R.string.fav_empty_dir,
										Toast.LENGTH_SHORT).show();
								return;
							}
							board.setHasUnread(false);
							draw(list);
							onRoot = false;
						} else {
							Bundle bundle = new Bundle();
							boolean flag = MyApplication.mPreference
									.getBoolean(Preferences.OneTopic, false);
							int mode = 0;
							if (flag) {
								mode = 2;
							}
							bundle.putInt(TopicList.EXTRA_MODE, mode);
							bundle.putString(PostHelper.EXTRA_BOARD,
									board.getId());
							// bundle.putString("boardName", board.getTitle());
							mOnOpenActivityListener.onOpenActivityOrFragment(
									ActivityFragmentTargets.TOPICLIST, bundle);
							board.setHasUnread(false);
							notifyDataSetChanged();
						}
					}
				});
		mUpFolder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (onRoot) {
					return;
				}
				draw();
			}
		});
	}

	private Board getContextItemBoard(int position) {
		Log.i(TAG, "position is " + position);
		if (position >= 2 && position <= myAdapter.getCount() + 1) {
			return (Board) myAdapter.getItem(position - 2);
		} else {
			return null;
		}
	}

	private void processResult(TaskResult result) {
		if (TaskResult.Failed == result) {
			Toast.makeText(getSherlockActivity(), errorCause,
					Toast.LENGTH_SHORT).show();
			return;
		} else if (TaskResult.NO_DATA == result) {
			Toast.makeText(getSherlockActivity(), R.string.fav_no_data,
					Toast.LENGTH_SHORT).show();
			return;
		}
		isLoaded = true;
		draw();
		goTop();
	}

	private void draw() {
		myAdapter.refresh(favList);
		onRoot = true;
	}

	private void draw(List<Board> list) {
		myAdapter.refresh(list);
	}

	private void notifyDataSetChanged() {
		myAdapter.notifyDataSetChanged();
	}

	private void goTop() {
		favListView.setSelection(1);
	}

	private void processUnlogin() {
		//TODO sometimes(I don't know when) getSherlockActivity() returns null
		if (null != getActivity()) {
			Toast.makeText(getActivity(), R.string.unlogin_notice, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {

		if (null != mRetrieveTask
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}
		super.onDestroy();
	}

	private class RetrieveFavTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			try {
				favList = BBSOperator.getInstance().getFavList(params[0]);
			} catch (HttpException e) {
				errorCause = e.getMessage();
				return TaskResult.Failed;
			}
			if (null == favList || favList.size() == 0) {
				return TaskResult.NO_DATA;
			}
			return TaskResult.OK;
		}

	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {

		if (!isLogined) {
			processUnlogin();
			return;
		}
		if (HomeViewModel.CURRENTTAB_PROPERTY_NAME.equals(changedPropertyName)) {
			if (!isLoaded
					&& mHomeViewModel.getCurrentTab().equals(
							ActivityFragmentTargets.TAB_FAV)) {
				doRetrieve();
			} else if (isLoaded
					&& mHomeViewModel.getCurrentTab().equals(
							ActivityFragmentTargets.TAB_FAV)) {
				draw();
			}
		}
	}

}
