package com.gfan.sbbs.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.main.LoginActivity;
import com.gfan.sbbs.ui.main.R;


public class HomeViewModel extends BaseViewModel {
	private String currentTab;
	public static final String CURRENTTAB_PROPERTY_NAME = "currentTab";
	
	public HomeViewModel(){
		
	}
	
	public String getCurrentTab() {
		return currentTab;
	}
	public void setCurrentTab(String currentTab) {
		this.currentTab = currentTab;
		notifyViewModelChange(this, CURRENTTAB_PROPERTY_NAME);
	}
	
	public void setCurrentTabIndex(int tabIndex){
		mOnTabIndexChangeListener.onTabIndexChange(tabIndex);
	}
	
	public void doLogout(final Activity activity){
		AlertDialog.Builder ab = new AlertDialog.Builder(activity);
		ab.setTitle(R.string.user_logout);
		
		ab.setMessage(R.string.user_logout_confirm);
		ab.setNegativeButton(R.string.user_logout_negative, null);
		ab.setPositiveButton(R.string.user_logout_positive,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				final ProgressDialog pdialog = new ProgressDialog(activity);
				pdialog.setMessage(activity.getResources().getString(R.string.user_logout_indicate));
				pdialog.show();
				Thread td = new Thread(){

					@Override
					public void run() {
						pdialog.dismiss();
						MyApplication.mPreference.edit().clear().commit();
						Intent intent = new Intent(activity,LoginActivity.class);
						activity.startActivity(intent);
						activity.finish();
					}};
					td.start();
			}
			
		});
		ab.create();
		ab.show();
	}
}
