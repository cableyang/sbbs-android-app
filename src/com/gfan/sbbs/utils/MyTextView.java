package com.gfan.sbbs.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

public class MyTextView extends EditText {

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public MyTextView(Context context){
		super(context);
		init();
	}

	@Override
	protected boolean getDefaultEditable() {
		return false;
	}
	
	private void init(){
		setGravity(Gravity.TOP);
		setBackgroundColor(Color.WHITE);
	}
	

}
