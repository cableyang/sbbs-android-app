package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gfan.sbbs.bean.Attachment;
import com.gfan.sbbs.ui.main.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AttachmentAdapter extends BaseAdapter {
	private List<Attachment> attList;
	private Context context;

	public AttachmentAdapter(Context context) {
		this.context = context;
		attList = new ArrayList<Attachment>();
	}

	@Override
	public int getCount() {
		return attList.size();
	}

	@Override
	public Object getItem(int position) {
		return attList.get(position);
	}
	
	public String getAttachmentUrl(int position){
		Attachment att = (Attachment)getItem(position);
		return att.getUrl();
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void refresh(List<Attachment> list) {
		this.attList = list;
		this.notifyDataSetChanged();
	}

	public void refresh() {
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		ImageLoader imageLoader = ImageLoader.getInstance();
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.user_default_photo)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.att_item, null);
			holder = new ViewHolder();
			holder.nameView = (TextView) convertView
					.findViewById(R.id.att_name);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.att_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Attachment att = (Attachment) getItem(position);
		if (att.isImage()) {

//			ImageLoader.getInstance().displayImage(att.getUrl(),
//					holder.imageView);
			imageLoader.displayImage(att.getUrl(), holder.imageView, options);
			// Bitmap bitmap = BitmapFactory.decodeFile(att.getUrl());
			// holder.imageView.setImageBitmap(bitmap);
			holder.imageView.setVisibility(View.VISIBLE);
		} else {
			holder.nameView.setText(att.getFileName());
			holder.nameView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView nameView;
		ImageView imageView;
	}

}
