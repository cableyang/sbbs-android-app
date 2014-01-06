package com.gfan.sbbs.utils.images;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.gfan.sbbs.bean.Attachment;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.utils.images.LazyImageLoader.ImageLoaderCallback;

public class SimpleImageLoader {
	private static final String TAG = "SimpleImageLoader";
	public static ImageLoaderListener mImageLoaderListener;

	public static void display(final ImageView imageView, Attachment att) {
		imageView.setTag(att);
		String url = att.getUrl();
		imageView.setPadding(5, 5, 5, 5);
		imageView.setBackgroundResource(R.drawable.image_bg);
		mImageLoaderListener.onPreImageLoad(imageView);
		imageView.setImageBitmap(MyApplication.mImageLoader.get(url,
				createImageViewCallback(imageView, url)));
	}

	public static ImageLoaderCallback createImageViewCallback(
			final ImageView imageView, String url) {
		return new ImageLoaderCallback() {
			@Override
			public void refresh(String url, Bitmap bitmap) {
				if(null == bitmap){
					Log.e(TAG, "bitmap is null");
					mImageLoaderListener.onImageLoadFailed(imageView);
					return;
				}
				String imageUrl = ((Attachment)imageView.getTag()).getUrl();
				if (url.equals(imageUrl)) {
					
					Bitmap newBitmap = ImageUtils.createRoundBitmap(bitmap, 10.0f);
					imageView.setImageBitmap(newBitmap);
					imageView.setAdjustViewBounds(true);
				}
				mImageLoaderListener.onImageLoadSuccess(imageView);
			}
		};
	}
	public interface ImageLoaderListener{
		public void onPreImageLoad(ImageView iv);
		public void onImageLoadFailed(ImageView iv);
		public void onImageLoadSuccess(ImageView iv);
	}
}
