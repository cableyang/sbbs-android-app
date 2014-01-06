package com.gfan.sbbs.file.service;

import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.file.utils.FileUtils;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.main.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * this service is used to upload attachments in background
 * 
 * TODO 
 * give notice the attachment is success or failed to upload to the server
 * or even back to the FileUploadActivity to serve the user the opportunity to upload the 
 * error-upload-files
 * 
 * @author Nine
 * 
 */
public class UpLoadService extends Service {

	private String boardID;
	private String token;
	private int id;
	private String addAttachmentUrl;
	private static final String TAG = UpLoadService.class.getName();
	private static final int NOTIFYID = 1;
	private TaskListener postWithAttachmentTaskListener = new TaskAdapter() {

		@Override
		public void onPreExecute(GenericTask task) {
			PendingIntent pendingIntent = PendingIntent.getActivity(
					UpLoadService.this, 0, new Intent(),
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification notification = new NotificationCompat.Builder(
					UpLoadService.this)
					.setSmallIcon(R.drawable.icon_notification)
					.setContentTitle(getString(R.string.uploading_att))
					.setTicker(getString(R.string.uploading_att))
					.setContentText("").setProgress(0, 100, true)
					.setContentIntent(pendingIntent).setAutoCancel(true)
					.build();
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFYID, notification);
			super.onPreExecute(task);
			Log.i(TAG, "onPreExecute");
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			Log.i(TAG, "onPostExecute");
			PendingIntent pendingIntent = PendingIntent.getActivity(
					UpLoadService.this, 0, new Intent(),
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification notification = new NotificationCompat.Builder(
					UpLoadService.this)
					.setSmallIcon(R.drawable.icon_notification)
					.setContentTitle(getString(R.string.uploading_att))
					.setTicker(getString(R.string.upload_att_finished))
					.setContentText("").setProgress(100, 100, true)
					.setContentIntent(pendingIntent).setAutoCancel(true)
					.build();
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFYID, notification);
			mNotificationManager.cancel(NOTIFYID);
			stopSelf();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			super.onProgressUpdate(task, param);
		}

		@Override
		public void onCancelled(GenericTask task) {
			// TODO Auto-generated method stub
			super.onCancelled(task);
			Log.i(TAG, "onCancelled");
		}

		@Override
		public String getName() {
			return postWithAttachmentTaskListener.getName();
		}
	};
//	private boolean isAnonymous;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return -1;
		boardID = intent.getExtras().getString(PostHelper.EXTRA_BOARD);
		token = intent.getExtras().getString("token");
//		isAnonymous = intent.getExtras().getBoolean("isAnonymous");
		id = intent.getExtras().getInt(PostHelper.EXTRA_ID);
		createPostUrl();
		postWithFile();
		return super.onStartCommand(intent, flags, startId);
	}

	private void createPostUrl() {
		addAttachmentUrl = SBBSConstants.BASE_API_URL
				+ "/attachment/add.json?board=" + boardID + "&token=" + token
				+ "&id=" + id;
		Log.i(TAG, addAttachmentUrl);
	}

	private void postWithFile() {
		PostWithAttachmentTask postWithAttachmentTask = new PostWithAttachmentTask();
		postWithAttachmentTask.setListener(postWithAttachmentTaskListener);
		postWithAttachmentTask.execute("");
		Log.i(TAG, "start posting the attachments");
	}

	@Override
	public void onDestroy() {
		FileUtils.getInstance().cleanQueues();
		Log.i(TAG, "service destroyed");
		super.onDestroy();
	}

	private class PostWithAttachmentTask extends GenericTask {
		private boolean isTerminated = false;

		/**
		 ** get the post's id before uploading the attachment in case that the
		 * attachments are uploaded to unwanted post
		 * 
		 */
		@Override
		protected TaskResult _doInBackground(String... params) {
			while (!isTerminated) {
				try {
					// TODO to be continued
					String attUrl = FileUtils.getInstance().takeFromAttUrl();
					if (null == attUrl) {
						isTerminated = true;
						continue;
					}
					if (attUrl.startsWith("file://")) {
						attUrl = attUrl.substring(7);
						Log.i(TAG, attUrl);
						BBSOperator.getInstance().doUploadAttachment(
								addAttachmentUrl, attUrl, null);
						Log.i(TAG, "addAttachmentUrl is "+addAttachmentUrl);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG, "InterruptedException");
					isTerminated = true;
					// stopSelf();
				} catch (HttpException e) {
					e.printStackTrace();
					Log.e(TAG, "HTTPException");
					isTerminated = true;
					// stopSelf();
				}
			}
			return TaskResult.OK;
		}
	}

}
