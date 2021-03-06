package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.base.HomeViewModel;

public class TabsAdapter extends FragmentPagerAdapter implements
		ActionBar.TabListener, ViewPager.OnPageChangeListener {
	private final SherlockFragmentActivity m_activity;
	private final ActionBar m_actionBar;
	private final ViewPager m_viewPager;
	private final ArrayList<TabInfo> m_tabs = new ArrayList<TabInfo>();
	private HomeViewModel mHomeViewModel;
	private boolean m_isInited = false;
	private static final String TAG = "TabsAdapter";

	static final class TabInfo {
		private final Class<?> clss;
		private final Bundle args;
		private final String tabName;

		TabInfo(Class<?> _class, String name,Bundle _args) {
			clss = _class;
			args = _args;
			tabName = name;
		}
	}
	
	public void finishInit() {
		m_isInited = true;
	}

	public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
		super(activity.getSupportFragmentManager());
		m_activity = activity;
		mHomeViewModel = ((MyApplication)m_activity.getApplication()).getmHomeViewModel();
		m_actionBar = activity.getSupportActionBar();
		m_viewPager = pager;
		m_viewPager.setAdapter(this);
		m_viewPager.setOnPageChangeListener(this);
	}

	public void addTab(ActionBar.Tab tab, Class<?> clss,String name, Bundle args) {
		TabInfo info = new TabInfo(clss, name,args);
		tab.setTag(info);
		tab.setTabListener(this);
		m_tabs.add(info);
		m_actionBar.addTab(tab);
		
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return m_tabs.size();
	}

	@Override
	public Fragment getItem(int position) {
		TabInfo info = m_tabs.get(position);
		return Fragment.instantiate(m_activity, info.clss.getName(), info.args);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (m_isInited) {
			Log.i(TAG, TAG+"-->position is "+position);
			mHomeViewModel.setCurrentTab("00"+(position + 1));
			if(0 == position){
				m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			}else{
				m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		
		Log.i(TAG, TAG+"-->state is "+state);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (m_isInited) {
			Object tag = tab.getTag();
			for (int i = 0; i < m_tabs.size(); i++) {
				if (m_tabs.get(i) == tag) {
					m_actionBar.setTitle("");
					m_viewPager.setCurrentItem(i);
					mHomeViewModel.setCurrentTab("00"+(i + 1));
					Log.i(TAG, "i is "+i);
					if(0 == i){
						m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					}else{
						m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
					}
					break;
				}
			}
			
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}
