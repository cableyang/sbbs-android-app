package com.gfan.sbbs.utils.images;

import android.graphics.Bitmap;

import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.main.R;

public interface ImageCache {
	public static Bitmap mDefaultBitmap = ImageManager
			.drawableToBitmap(MyApplication.mContext.getResources()
					.getDrawable(R.drawable.user_default_photo));
	public static Bitmap mDefaultErrorBitmap = ImageManager.drawableToBitmap(MyApplication.mContext.getResources().getDrawable(R.drawable.load_pic_error));
	public Bitmap get(String url);

	public void put(String url, Bitmap bitmap);
}
