package com.gfan.sbbs.ui.main;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class Logo extends Activity{
	public static final int YOUR_BIRTHDAY_MONTH = 9;
	public static final int YOUR_BIRTHDAY_DAY = 1;
	private ImageView iv;
	private static final String TAG = "Logo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    this.setContentView(R.layout.logo);
	    iv = (ImageView) this.findViewById(R.id.logo_bg);
	    hpBirthDay();
	}


	@Override
	protected void onResume() {
		super.onResume();

	    AlphaAnimation aa = new AlphaAnimation(0.1f,1.0f);
	    aa.setDuration(3000);
	    iv.startAnimation(aa);
	    aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				Intent intent = new Intent(Logo.this,LoginActivity.class);
				startActivity(intent);
			    finish();
			}
		});
	}
	/**
	 * This method will change the startup screen at Sep 1st.And,it will do it once a year.
	 * Just dedicate it to my beloved girl,happy birthday!
	 */
	private void hpBirthDay(){
		GregorianCalendar ca  = new GregorianCalendar();
		int month = ca.get(Calendar.MONTH) + 1;
		int day = ca.get(Calendar.DATE);
		Log.i(TAG, "Today is "+month+"."+day);
		if(month == YOUR_BIRTHDAY_MONTH && day == YOUR_BIRTHDAY_DAY){
			Log.i(TAG, "Happy Birthday!");
			iv.setImageResource(R.drawable.birthday_splash);
		}
	}

}
