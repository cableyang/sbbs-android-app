package com.gfan.sbbs.ui.main;

import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gfan.sbbs.bean.Attachment;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.ui.Abstract.BaseActivity;
import com.gfan.sbbs.ui.base.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class ImagePagerActivity extends BaseActivity{
	private ViewPager pager;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private int imagePosition;

	private static final String STATE_POSITION = "state_position";
	private static final String TAG = "ImagePagerActivity";
	
	@Override
	protected void processUnLogin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void _onCreate(Bundle savedInstanceState) {
		super._onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.image_pager_layout);
		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		imagePosition = bundle.getInt(PostHelper.EXTRA_POSITION);
//		if (savedInstanceState != null) {
//			imagePosition = savedInstanceState.getInt(STATE_POSITION);
//		}
//		
		imageLoader = ImageLoader.getInstance();

		@SuppressWarnings("unchecked")
		List<Attachment> attList = (List<Attachment>) bundle.getSerializable(PostHelper.EXTRA_ATT_LIST);
		ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(attList);
		
		options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.user_default_photo)
		.imageScaleType(ImageScaleType.EXACTLY)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		pager = (ViewPager) this.findViewById(R.id.image_pager);
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(imagePosition);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		imagePosition = savedInstanceState.getInt(STATE_POSITION);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().stop();
	}
	
	
	private class ImagePagerAdapter extends PagerAdapter{
		List<Attachment> attList ;
//		private LayoutInflater inflater;
		
		
		
		public ImagePagerAdapter(List<Attachment> attList) {
			this.attList = attList;
//			inflater = getLayoutInflater();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
		@Override
		public int getCount() {
			return null == attList?0:attList.size();
		}


		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.i(TAG, "The "+position+" one is downloading");
//			View imageLayout = inflater.inflate(R.layout.image_pager_item, container,false);
//			TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image_item);
//			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading_progress);
			TouchImageView imageView = new TouchImageView(ImagePagerActivity.this);
			imageView.setId(R.id.pager_full_image);
			final ProgressDialog progressdialog = new ProgressDialog(ImagePagerActivity.this);
			progressdialog.setMessage(getResources().getString(R.string.loading));
			
			String url = attList.get(position).getUrl();
			Log.i(TAG, "image url is "+url);
			imageLoader.displayImage(url, imageView,options,new SimpleImageLoadingListener(){
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					Log.i(TAG, "start downloading the image");
//					spinner.setVisibility(View.VISIBLE);
					progressdialog.setCanceledOnTouchOutside(false);
					progressdialog.show();
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					String message = "加载失败";
					switch (failReason.getType()) {
//						case IO_ERROR:
//							message = "Input/Output error";
//							break;
//						case DECODING_ERROR:
//							message = "Image can't be decoded";
//							break;
//						case NETWORK_DENIED:
//							message = "Downloads are denied";
//							break;
//						case OUT_OF_MEMORY:
//							message = "Out Of Memory error";
//							break;
//						case UNKNOWN:
//							message = "Unknown error";
//							break;
					}
					Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

//					spinner.setVisibility(View.GONE);
					progressdialog.dismiss();
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//					spinner.setVisibility(View.GONE);
					progressdialog.dismiss();
				}
			});
		
			container.addView(imageView, 0);
			return imageView;
		}

		/**
		 * important! this method must be overridden ,otherwise the image
		 * will not be displayed properly
		 */
		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			return view.equals(object);
		}
	
		
	}

}