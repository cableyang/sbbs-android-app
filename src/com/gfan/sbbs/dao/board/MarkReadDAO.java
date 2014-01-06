package com.gfan.sbbs.dao.board;

import java.util.List;

import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.ui.utils.ActivityUtils;
/**
 * 
 * @author Nine
 *
 */
public class MarkReadDAO {

	private Board board;
	private MarkBoardReadTask mMarkReadTask;
	private String url ="";
	
	private TaskListener mMarkBoardReadTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return mMarkBoardReadTaskListener.getClass().getName();
		}

		@Override
		public void onPreExecute(GenericTask task) {
			super.onPreExecute(task);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (TaskResult.OK == result) {
				ActivityUtils.showTips(R.string.post_mark_read_success);
			} else {
				ActivityUtils.showTips(R.string.post_mark_read_error);
			}
		}

	};

	public void markAsRead(){
		if (null == board) {
			return;
		}
		url += board.getId()+"/markread.json?token="+MyApplication.getInstance().getToken();
		
		if (board.isDirectory()) {
			List<Board> childList = board.getChildBoards();
			for (int i = 0, len = childList.size(); i < len; i++) {
				if (childList.get(i).isDirectory()) {
					this.board = childList.get(i);
					markAsRead();
				}
			}
		} else {
			mMarkReadTask = new MarkBoardReadTask();
			mMarkReadTask.setListener(mMarkBoardReadTaskListener);
			mMarkReadTask.execute(url);
		}

	}
	public  MarkReadDAO(Board board){
		this.board = board;
		url = SBBSConstants.BASE_API_URL+"/board/";
	}
	
	private class MarkBoardReadTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(String... params) {
			boolean result = BBSOperator.getInstance().getBoolean(params[0]);
			if (result) {
				return TaskResult.OK;
			}
			return TaskResult.Failed;
		}

	}
	
}
