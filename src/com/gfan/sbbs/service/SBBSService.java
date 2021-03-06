package com.gfan.sbbs.service;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.task.GenericTask;
import com.gfan.sbbs.task.TaskAdapter;
import com.gfan.sbbs.task.TaskListener;
import com.gfan.sbbs.task2.TaskResult;
import com.gfan.sbbs.ui.main.NoticeActivity;
import com.gfan.sbbs.ui.main.R;

public class SBBSService extends Service {

	private static final String TAG = "SBBSService";
	private HashMap<String, Object> noticeArray;

	private NotificationManager mNotificationManager;
	private ArrayList<Object> mNewMentions;

	private WakeLock mWakeLock;
	private GenericTask mRetrieveTask;
	private TaskListener mRetrieveTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return mRetrieveTaskListener.getClass().getName();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			if (TaskResult.OK != result) {
				stopSelf();
				return;
			}
			boolean needCheck = MyApplication.mPreference.getBoolean(
					Preferences.CHECK_UPDATE, true);
			// boolean needCheck = true;

			if (needCheck) {
				processNewMetions();
			}
			stopSelf();
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// mPreference = application.getmPreference();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();

		boolean needCheck = MyApplication.mPreference.getBoolean(
				Preferences.CHECK_UPDATE, false);
		Log.i(TAG, "Service reCreate,needCheck is " + needCheck);
		if (!needCheck) {
			Log.i(TAG, "check update pref is false");
			stopSelf();
			return;
		}
		// if (application.isLogined()) {
		// Log.i(TAG, "guest login");
		// stopSelf();
		// return;
		// }
		schedule(SBBSService.this);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNewMentions = new ArrayList<Object>();
		if (null != mRetrieveTask) {
			return;
		} else {
			mRetrieveTask = new RetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute();
		}
	}

	@Override
	public void onDestroy() {
		if (null != mRetrieveTask
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}
		mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public static void schedule(Context context) {

		boolean needCheck = MyApplication.mPreference.getBoolean(
				Preferences.CHECK_UPDATE, false);
		// boolean needCheck = true;
		if (!needCheck) {
			Log.d(TAG, "Check update preference is false.");
			return;
		}

		String intervalPref = MyApplication.mPreference
				.getString(
						Preferences.CHECK_UPDATE_INTERVAL,
						context.getString(R.string.pref_check_updates_interval_default));
		int interval = Integer.parseInt(intervalPref);
		// int interval = 1; //for debug

		Intent intent = new Intent(context, SBBSService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar c = new GregorianCalendar();
		c.add(Calendar.MINUTE, interval);

		DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		Log.d(TAG, "Schedule, next run at " + df.format(c.getTime()));

		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		if (needCheck) {
			alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
		}
		// else {
		// // only for widget
		// alarm.set(AlarmManager.RTC, c.getTimeInMillis(), pending);
		// }
	}

	private void processNewMetions() {
		Log.i(TAG, "before process mentions");
		int count = mNewMentions.size();
		if (count <= 0) {
			return;
		}

		String title;
		String text;

		title = getString(R.string.service_new_mention_updates);
		text = getString(R.string.service_x_new_mentions);
		text = MessageFormat.format(text, count);
		Intent sendIntent = NoticeActivity.createIntent(this);
		sendIntent.putExtra("newMentions", noticeArray);
		/**
		 * PendingIntent.FLAG_UPDATE_CURRENT  !important
		 * used for update the data contained in the sendIntent
		 */
		PendingIntent intent = PendingIntent
				.getActivity(this, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notify(intent, 0, R.drawable.icon_notification, title, title, text);
		Log.i(TAG, "After process mentions");
	}

	@SuppressWarnings("deprecation")
	private void notify(PendingIntent intent, int notificationId,
			int notifyIconId, String tickerText, String title, String text) {
		Notification notification = new Notification(notifyIconId, tickerText,
				System.currentTimeMillis());

		notification.setLatestEventInfo(this, title, text, intent);

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONLY_ALERT_ONCE
				| Notification.FLAG_SHOW_LIGHTS;

		notification.ledARGB = 0xFF84E4FA;
		notification.ledOnMS = 5000;
		notification.ledOffMS = 5000;

		String ringtoneUri = MyApplication.mPreference.getString(
				Preferences.RINGTONE_KEY, null);

		if (ringtoneUri == null) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		} else {
			notification.sound = Uri.parse(ringtoneUri);
		}

		if (MyApplication.mPreference
				.getBoolean(Preferences.VIBRATE_KEY, false)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		mNotificationManager.notify(notificationId, notification);
	}

	public static void unschedule(Context context) {
		Intent intent = new Intent(context, SBBSService.class);
		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Log.d(TAG, "Cancelling alarms.");
		alarm.cancel(pending);
	}

	private class RetrieveTask extends GenericTask {

		@SuppressWarnings("unchecked")
		@Override
		protected TaskResult _doInBackground(String... params) {
			String url = "http://bbs.seu.edu.cn/api/notifications.json?token="
					+ MyApplication.getInstance().getToken();
			Log.i(TAG, url);
			try {
				noticeArray = BBSOperator.getInstance().getNoticeList(url);
			} catch (HttpException e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
				return TaskResult.Failed;
			}
			mNewMentions.addAll((List<Mail>) noticeArray.get("mail"));
			mNewMentions.addAll((List<Topic>) noticeArray.get("ats"));
			mNewMentions.addAll((List<Topic>) noticeArray.get("reply"));
			if (mNewMentions.size() == 0) {
				Log.i(TAG, "No Data");
				return TaskResult.NO_DATA;
			}
			return TaskResult.OK;
		}

	}
}
