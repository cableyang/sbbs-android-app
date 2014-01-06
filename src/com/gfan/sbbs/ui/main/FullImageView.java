package com.gfan.sbbs.ui.main;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.base.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class FullImageView extends BaseActivity implements ImageLoadingListener {
	private TouchImageView fullImageView;
	private Bitmap bitmap;
	private String mImageUrl;
	public static final String EXTRA_IMAGE_URL = "image_url";
	private ProgressDialog progressdialog = null;
	private ImageLoader imageLoader = null;

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		progressdialog = new ProgressDialog(this);
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));
		initArgs();
		doRetrievePhoto();
	}

	private void initArgs() {
		fullImageView = new TouchImageView(this);
		fullImageView.setId(R.id.full_image);
		setContentView(fullImageView);
		mImageUrl = getIntent().getExtras().getString(EXTRA_IMAGE_URL);
	}

	private void doRetrievePhoto() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.displayer(new FadeInBitmapDisplayer(300)).build();
		imageLoader.displayImage(mImageUrl, fullImageView, options, this);
	}

	@Override
	protected void onDestroy() {
		if (null != bitmap) {
			bitmap.recycle();
		}
		super.onDestroy();
	}

	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setup() {

	}

	@Override
	public void onLoadingCancelled(String arg0, View arg1) {
		// TODO Auto-generated method stub
		progressdialog.dismiss();
	}

	@Override
	public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
		// TODO Auto-generated method stub
		progressdialog.dismiss();
	}

	@Override
	public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
		// TODO Auto-generated method stub
		progressdialog.dismiss();
	}

	@Override
	public void onLoadingStarted(String arg0, View arg1) {
		progressdialog.setMessage(getResources().getString(R.string.loading));
		progressdialog.show();
	}

}
