package com.gfan.sbbs.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;

public class About extends com.gfan.sbbs.ui.Abstract.BaseActivity {
	private Button btn_quit,btn_advice;

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.about);
		setTitle(R.string.app_name);
		initView();
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initView() {
		btn_quit = (Button)this.findViewById(R.id.about_quit);
		btn_advice = (Button)this.findViewById(R.id.about_advice);
	}
	private void init(){
		btn_quit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				return;
			}
		});
		btn_advice.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				  FeedbackAgent agent = new FeedbackAgent(About.this);
				  agent.startFeedbackActivity();
			}
		});
	}

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		
	}

}
