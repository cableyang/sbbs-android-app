package com.gfan.sbbs.othercomponent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.gfan.sbbs.bean.User;
import com.gfan.sbbs.ui.base.HomeViewModel;
import com.gfan.sbbs.utils.images.LazyImageLoader;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MyApplication extends Application {
	public static Context mContext;
	private static MyApplication globalContext = null;
	private Activity activity;
	public static LazyImageLoader mImageLoader;
	public static boolean isNightMode = false;
	private boolean isLogined = false;
	private boolean guestLogin = false;
	private boolean rememberMe;
	private boolean autoLogin;
	private boolean autoUpdate;
	private boolean update_wifi;
	private boolean one_topic;
	public static User loginUser;
	public static String userName;
	private String userPwd;
	private int updateInterval, startPage;
	private String token;
	public static int screenWidth = 480;
	public static SharedPreferences mPreference;
	String[] blackList;
	public static final HomeViewModel mHomeViewModel = new HomeViewModel();
	private static final String TAG = "MyApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		mPreference = PreferenceManager.getDefaultSharedPreferences(this);
		blackList = new String[20];
		mContext = this.getApplicationContext();
		mImageLoader = new LazyImageLoader();
		buildPrefs();
		globalContext = this;
		initImageLoader(getApplicationContext());
	}

	public static MyApplication getInstance(){
		return globalContext;
	}
	public Activity getActivity(){
		return this.activity;
	}
	
	public void setActivity(Activity activity){
		this.activity = activity;
	}
	public SharedPreferences getmPreference() {
		return mPreference;
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	
	@SuppressWarnings("deprecation")
	private void buildPrefs() {
		SharedPreferences.Editor editor = mPreference.edit();
		if (!mPreference.contains(Preferences.AUTOLOGIN)) {
			editor.putBoolean(Preferences.AUTOLOGIN, true);
		} else {
			autoLogin = mPreference.getBoolean(Preferences.AUTOLOGIN, true);
			Log.i(TAG, "autoLogin is "+autoLogin);
		}
		if (!mPreference.contains(Preferences.REMEMBER_ME)) {
			editor.putBoolean(Preferences.REMEMBER_ME, true);
		} else {
			rememberMe = mPreference.getBoolean(Preferences.REMEMBER_ME, true);
		}
		if (!mPreference.contains(Preferences.BLACKLIST)) {
			editor.putString(Preferences.BLACKLIST, "");
		} else {
			blackList = mPreference.getString(Preferences.BLACKLIST, "").split(
					"\\s+");
		}
		if (!mPreference.contains(Preferences.OneTopic)) {
			editor.putBoolean(Preferences.OneTopic, false);
		} else {
			setOne_topic(mPreference.getBoolean(Preferences.OneTopic, false));
		}
		if (!mPreference.contains(Preferences.AUTOUPDATE)) {
			editor.putBoolean(Preferences.AUTOUPDATE, true);
		} else {
			autoUpdate = mPreference.getBoolean(Preferences.AUTOUPDATE, true);
		}
		if (!mPreference.contains(Preferences.UPDATE_WIFI_ONLY)) {
			editor.putBoolean(Preferences.UPDATE_WIFI_ONLY, false);
		} else {
			update_wifi = mPreference.getBoolean(Preferences.UPDATE_WIFI_ONLY,
					false);
		}
		if (mPreference.contains(Preferences.UPDATE_INTERVAL)) {
			updateInterval = Integer.parseInt(mPreference.getString(
					Preferences.UPDATE_INTERVAL, "7").trim());
		} else {
			editor.putString(Preferences.UPDATE_INTERVAL, "1");
		}
		if (mPreference.contains(Preferences.USER_NAME)) {
			userName = mPreference.getString(Preferences.USER_NAME, "");
		}
		if (!mPreference.contains(Preferences.USER_TOKEN)) {
			editor.putString(Preferences.USER_TOKEN, "");
		} else {
			token = mPreference.getString(Preferences.USER_TOKEN, "");
			token = URLEncoder.encode(token);
		}
		if (!mPreference.contains(Preferences.CHECK_UPDATE)) {
			editor.putBoolean(Preferences.CHECK_UPDATE, true);
		}
		if (!mPreference.contains(Preferences.CHECK_UPDATE_INTERVAL)) {
			editor.putString(Preferences.CHECK_UPDATE_INTERVAL, "10");
		}

		if (!mPreference.contains(Preferences.SELECT_PAGE)) {
			editor.putString(Preferences.SELECT_PAGE, "1");
		} else {
			String page = mPreference.getString(Preferences.SELECT_PAGE, "1");
			if (!TextUtils.isEmpty(page)) {
				setStartPage(Integer.parseInt(page.trim()));
			} else {
				setStartPage(1);
			}
		}
		if(!mPreference.contains(Preferences.NIGHT_MODE)){
			editor.putBoolean(Preferences.NIGHT_MODE, false);
		}else{
			isNightMode = mPreference.getBoolean(Preferences.NIGHT_MODE, false);
		}
		editor.commit();
	}

	public boolean isLogined() {
		return isLogined;
	}

	public void setLogined(boolean isLogined) {
		this.isLogined = isLogined;
	}

	public boolean isGuestLogin() {
		return guestLogin;
	}

	public void setGuestLogin(boolean guestLogin) {
		this.guestLogin = guestLogin;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public String encrypt(String passwd) {
		byte[] array = passwd.getBytes();

		return array.toString();
	}

	public String[] getBlackList() {
		return this.blackList;
	}

	public void setBlackList(String[] blackList) {
		this.blackList = blackList;
	}

	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
	}

	public boolean isAutoLogin() {
		return autoLogin;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void setUpdate_wifi(boolean update_wifi) {
		this.update_wifi = update_wifi;
	}

	public boolean isUpdate_wifi() {
		return update_wifi;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public  String getToken() {
		if (mPreference != null && mPreference.contains(Preferences.USER_TOKEN)) {
			String token = mPreference.getString(Preferences.USER_TOKEN, "");
			try {
				token = URLEncoder.encode(token, SBBSConstants.SBBS_ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return "";
			}
			return token;
		} else {
			return "";
		}
	}

	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}

	public String getUserPwd() {
		return userPwd;
	}

	public void setOne_topic(boolean one_topic) {
		this.one_topic = one_topic;
	}

	public boolean isOne_topic() {
		return one_topic;
	}

	public static boolean checkLogin() {
		userName = mPreference.getString(Preferences.USER_NAME, "");
		if (TextUtils.isEmpty(userName)) {
			return false;
		}
		return true;
	}

	public HomeViewModel getmHomeViewModel() {
		return mHomeViewModel;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getStartPage() {
		return startPage;
	}

}
