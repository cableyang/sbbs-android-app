package com.gfan.sbbs.ui.main;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.Preferences;

public class Preference extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener,OnPreferenceClickListener {
	private CheckBoxPreference rememberBox, autoLoginBox,isNightBox;
	private EditTextPreference blackListSettings;
	private ListPreference mStartPage;
	private ListPreference mFontAjustPreference;
//	private MyApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.pref_title));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.preference);
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		rememberBox = (CheckBoxPreference) this
				.findPreference("pref_remember_me");
		autoLoginBox = (CheckBoxPreference) this
				.findPreference("pref_autologin");
		isNightBox = (CheckBoxPreference) this.findPreference(Preferences.NIGHT_MODE);
		
		mFontAjustPreference = (ListPreference) this.findPreference(Preferences.FONT_SIZE_ADJUST);
		blackListSettings = (EditTextPreference)this.findPreference(Preferences.BLACKLIST);
		mStartPage = (ListPreference) this.findPreference(Preferences.SELECT_PAGE);
		blackListSettings.setOnPreferenceChangeListener(this);
		mStartPage.setOnPreferenceChangeListener(this);
		mStartPage.setOnPreferenceClickListener(this);
		isNightBox.setOnPreferenceChangeListener(this);
		isNightBox.setOnPreferenceClickListener(this);
	}


	@Override
	public boolean onPreferenceChange(android.preference.Preference preference,
			Object newValue) {
		if(preference.getKey().equals(Preferences.NIGHT_MODE)){
			MyApplication.isNightMode = (Boolean) newValue;
		}
		if(preference.getKey().equals(Preferences.FONT_SIZE_ADJUST)){
			SharedPreferences prefs = MyApplication.getInstance().getmPreference();
			Editor editor = prefs.edit();
			editor.putString(Preferences.FONT_SIZE_ADJUST, (String)newValue);
			editor.commit();
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceClick(android.preference.Preference preference) {
		return true;
	}

}
