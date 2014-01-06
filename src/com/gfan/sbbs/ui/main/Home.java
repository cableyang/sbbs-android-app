package com.gfan.sbbs.ui.main;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.fragment.FavFragment;
import com.gfan.sbbs.fragment.FriendListFragment;
import com.gfan.sbbs.fragment.HotFragment;
import com.gfan.sbbs.fragment.MailListFrament;
import com.gfan.sbbs.othercomponent.ActivityFragmentTargets;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.OnOpenActivityFragmentListener;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.othercomponent.SBBSConstants;
import com.gfan.sbbs.service.SBBSService;
import com.gfan.sbbs.ui.Adapter.TabsAdapter;
import com.gfan.sbbs.ui.base.HomeViewModel;
import com.gfan.sbbs.ui.base.BaseViewModel.OnTabIndexChangeListener;
import com.korovyansk.android.slideout.SlideoutHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;

public class Home extends SherlockFragmentActivity implements
		OnOpenActivityFragmentListener, OnTabIndexChangeListener {
	private ViewPager mHomePager;
	private TabsAdapter pagerAdapter;
	private HomeViewModel mHomeViewModel;
	private long exitTime;
	private boolean onSearch = false;
	private static final int MENU_SEARCH = 100000;// assign it a big number, in
													// case conflict with others
	private MyApplication application;

	private static final String TAG = "HomeActivity";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!MyApplication.isNightMode) {
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		}else{
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		}
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.home);
		application = (MyApplication) getApplication();
		
		MobclickAgent.onError(this);// error feedback
		schedule();// background data check
		createShortCut();
		manageAppUpdate();

		mHomeViewModel = ((MyApplication) getApplication()).getmHomeViewModel();
		mHomeViewModel.setOnTabIndexChangeListener(this);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		MyApplication.screenWidth = getWindowManager().getDefaultDisplay()
				.getWidth();

		initPager();
		application.setActivity(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!MyApplication.isNightMode) {
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		}else{
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initPager() {
		ActionBar actionBar = getSupportActionBar();
		mHomePager = (ViewPager) this.findViewById(R.id.home_pager);
		pagerAdapter = new TabsAdapter(this, mHomePager);
		pagerAdapter
				.addTab(actionBar.newTab(), HotFragment.class, "topten", null);
		pagerAdapter.addTab(actionBar.newTab(), FavFragment.class, "fav", null);
		pagerAdapter
				.addTab(actionBar.newTab(), MailListFrament.class, "", null);
		pagerAdapter.addTab(actionBar.newTab(), FriendListFragment.class,
				"friends", null);
		pagerAdapter.finishInit();
		// TODO
//		mHomePager.setOffscreenPageLimit(0);
		Log.i(TAG, "start page is " + application.getStartPage());
		if (MyApplication.checkLogin() && application.getStartPage() == 1) {
			mHomePager.setCurrentItem(1);
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
			mHomeViewModel.setCurrentTab(ActivityFragmentTargets.TAB_FAV);
			Log.i(TAG, "fav_tab selected");
		} else {
			mHomeViewModel.setCurrentTab(ActivityFragmentTargets.TAB_HOT);
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
		}

	}

	private void createTipsDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.tips_title);
		ab.setMessage(R.string.tips);
		ab.setPositiveButton("知道了~~", null);
		ab.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, "search")
				.setIcon(R.drawable.ic_menu_search_inverse)
				.setActionView(R.layout.collapsible_edittext)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			int width = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
							.getDisplayMetrics());
			SlideoutHelper.prepare(this, R.id.inner_home, width);
			startActivity(new Intent(Home.this,
					com.gfan.sbbs.menu.MenuActivity.class));
			overridePendingTransition(0, 0);

			return true;

		} else if (id == MENU_SEARCH) {
			item.setActionView(R.layout.collapsible_edittext);
			final EditText searchInput = (EditText) item.getActionView()
					.findViewById(R.id.search_input);
			item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					searchInput.post(new Runnable() {

						@Override
						public void run() {
							onSearch = true;
							searchInput.requestFocus();
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(searchInput,
									InputMethodManager.SHOW_IMPLICIT);
						}
					});
					return true;
				}

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					onSearch = false;
					return true;
				}
			});
			searchInput.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View view, int keyCode, KeyEvent event) {
					if (KeyEvent.KEYCODE_ENTER == keyCode
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
						String input = searchInput.getText().toString();
						doSearch(input);
						onSearch = true;
						return true;
					}
					return false;
				}
			});
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	protected void schedule() {
		boolean isUpdateEnabled = MyApplication.mPreference.getBoolean(
				Preferences.CHECK_UPDATE, false);
		boolean isLogined = MyApplication.checkLogin();
		if (isUpdateEnabled && isLogined) {
			SBBSService.schedule(this);
		}
	}

	private void doSearch(String input) {
		if (TextUtils.isEmpty(input.trim())) {
			return;
		}
		try {
			input = URLEncoder
					.encode(input.trim(), SBBSConstants.SBBS_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(this,SearchResult.class);
		Bundle bundle = new Bundle();
		bundle.putString("keyword", input);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onOpenActivityOrFragment(String target, Bundle bundle) {
		Intent intent = new Intent();
		if (target.equals(ActivityFragmentTargets.ON_TOPIC)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.ThreadList");
		}
		if (target.equals(ActivityFragmentTargets.TOPICLIST)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.TopicList");
		}
		if (target.equals(ActivityFragmentTargets.MAIL)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.MailBody");
		}
		if (target.equals(ActivityFragmentTargets.USER)
				|| target.equals(ActivityFragmentTargets.TAB_PROFILE)) {
			intent.setClassName("com.gfan.sbbs.ui.main",
					"com.gfan.sbbs.ui.main.ViewProfileActivity");
		}
		if (target.equals(ActivityFragmentTargets.TAB_NOTICE)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.NoticeActivity");
		}
		if (target.equals(ActivityFragmentTargets.TAB_SETTINGS)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.Preference");
		}
		if (target.equals(ActivityFragmentTargets.TAB_ABOUT)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.About");
		}
		if (target.equals(ActivityFragmentTargets.NEW_MAIL)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.WriteMail");
		}
		if (target.equals(ActivityFragmentTargets.TAB_SECTIONS)) {
			intent.setClassName("com.gfan.sbbs.ui.main", "com.gfan.sbbs.ui.main.Sections");
		}
		if (null != bundle) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
	}


	/**
	 * create desktop shortcut for first-install-user
	 */
	private void createShortCut() {
		boolean firstRun = MyApplication.mPreference.getBoolean(
				Preferences.FIRST_RUN, true);
		if (!firstRun) {
			return;
		}
		createTipsDialog();
		Intent intent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");// action
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				getString(R.string.app_name));
		intent.putExtra("duplicate", false); 
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.icon);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);// icon
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
				getApplicationContext(), Logo.class));
		sendBroadcast(intent);
		MyApplication.mPreference.edit()
				.putBoolean(Preferences.FIRST_RUN, false).commit();
	}

	/**
	 * app update
	 */
	private void manageAppUpdate() {
		MyApplication application = (MyApplication) getApplication();
		if (application.isAutoUpdate()) {
			long updateTime = ((long) application.getUpdateInterval()) * 1000
					* 60 * 60 * 24;
			UmengUpdateAgent.update(this, updateTime);
			Log.i("MyApplication",
					"update Interval is " + application.getUpdateInterval());
		}
		UmengUpdateAgent.setUpdateOnlyWifi(application.isUpdate_wifi());
		UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

			@Override
			public void OnDownloadEnd(int result) {
				if (1 == result) {
					Toast.makeText(Home.this, R.string.update_app_success, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(Home.this, R.string.update_app_error, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (onSearch) {
				onSearch = false;
				return super.dispatchKeyEvent(event);
			} else if (System.currentTimeMillis() - exitTime > 2000) {
				Toast.makeText(this, R.string.exit_indicate, Toast.LENGTH_SHORT)
						.show();
				exitTime = System.currentTimeMillis();
			} else {
				exit();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		ImageLoader.getInstance().clearDiscCache();
		super.onDestroy();
	}

	private void exit() {
		finish();
		MyApplication.mImageLoader.getImageManager().clear();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onTabIndexChange(int tabIndex) {
		if (tabIndex < 4) {
			mHomePager.setCurrentItem(tabIndex);
		} else if (tabIndex == 9) {
			mHomeViewModel.doLogout(this);
		} else {
			String target = "00" + (tabIndex + 5);
			this.onOpenActivityOrFragment(target, null);
		}

	}

}
