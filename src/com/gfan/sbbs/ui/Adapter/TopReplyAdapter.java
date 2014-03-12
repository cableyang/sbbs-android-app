package com.gfan.sbbs.ui.Adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.dao.topic.PostHelper;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.main.ImagePagerActivity;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.utils.MyGridView;

/**
 * 
 * @author Nine
 * 
 */
public class TopReplyAdapter extends BaseAdapter {
	public List<Topic> threadList;
	public Context context;

	public TopReplyAdapter(Context context) {
		threadList = new ArrayList<Topic>();
		this.context = context;
	}

	@Override
	public int getCount() {
		return threadList.size();
	}

	@Override
	public Object getItem(int position) {
		return threadList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void refresh(List<Topic> list) {
		this.threadList = list;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.listview_item_reply, null);
			holder = new ViewHolder();
			holder.txt_content = (TextView) convertView
					.findViewById(R.id.list_reply_text_content);
			holder.txt_author = (TextView) convertView
					.findViewById(R.id.list_reply_text_author);
			holder.txt_quoter = (TextView) convertView
					.findViewById(R.id.list_reply_text_quoter);
			holder.txt_quote = (TextView) convertView
					.findViewById(R.id.list_reply_text_quote);
			holder.txt_time = (TextView) convertView
					.findViewById(R.id.list_reply_text_time);
//			holder.txt_att = (TextView) convertView
//					.findViewById(R.id.list_reply_att_link);
//			holder.txt_att_label = (TextView) convertView
//					.findViewById(R.id.list_reply_att_label);
			holder.myGridView = (MyGridView) convertView
					.findViewById(R.id.list_att_grid);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//
		final Topic topic = threadList.get(position);
		holder.userInfo = topic.getAuthor();
		holder.content = topic.getContent();
		holder.txt_author.setText(holder.userInfo);
		// Spanned content = Html.fromHtml(holder.content);
		holder.txt_content.setText(holder.content);
		Linkify.addLinks(holder.txt_content, Linkify.ALL);
		holder.txt_content.setMovementMethod(null);
		if (null != topic.getQuoter()) {
			holder.txt_quoter.setVisibility(View.VISIBLE);
			holder.txt_quoter.setText("在 " + topic.getQuoter() + " 的大作中提到：");
			holder.txt_quote.setText(topic.getQuote());
		} else {
			holder.txt_quoter.setVisibility(View.GONE);
			holder.txt_quote.setVisibility(View.GONE);
		}
		
		holder.txt_time.setText(topic.getTime());
//
//		ImageLoader imageLoader = ImageLoader.getInstance();
//		DisplayImageOptions options = new DisplayImageOptions.Builder()
//				.showImageOnLoading(R.drawable.user_default_photo)
//				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
//				.bitmapConfig(Bitmap.Config.RGB_565).build();

//		if (topic.isHasAtt()) {
//			holder.txt_att.setVisibility(View.VISIBLE);
//			holder.txt_att_label.setVisibility(View.VISIBLE);
//			StringBuffer sb = new StringBuffer();
//			List<Attachment> attList = topic.getAttList();
//			for (int i = 0, len = attList.size(); i < len; i++) {
//				Attachment att = attList.get(i);
//				 sb.append("<a href='").append(att.getUrl()).append("'>");
//				 sb.append(att.getFileName()).append("</a><br/><br/>");
//
//			}
//			holder.txt_att.setText(Html.fromHtml(sb.toString()));
//			holder.txt_att.setMovementMethod(LinkMovementMethod.getInstance());
//		} else {
//			holder.txt_att.setVisibility(View.GONE);
//			holder.txt_att_label.setVisibility(View.GONE);
//		}
		if(topic.isHasAtt()){
			AttachmentAdapter attAdapter = new AttachmentAdapter(context);
			holder.myGridView.setAdapter(attAdapter);
			attAdapter.refresh(topic.getAttList());
			if(attAdapter.getCount() <3){
				holder.myGridView.setNumColumns(attAdapter.getCount());
			}
			holder.myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable(PostHelper.EXTRA_ATT_LIST, (Serializable) topic.getAttList());
					bundle.putInt(PostHelper.EXTRA_POSITION, position);
					intent.putExtras(bundle);
					intent.setClass(context, ImagePagerActivity.class);
					MyApplication.getInstance().getActivity().startActivity(intent);
					
				}
			});
		}else{
			holder.myGridView.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	/**
	 * 模型
	 * 
	 * @author Nine
	 * 
	 */

	private static class ViewHolder {
		TextView txt_content;
		TextView txt_author;
		TextView txt_time;
		TextView txt_quoter;
		TextView txt_quote;
//		TextView txt_att;
//		TextView txt_att_label;
		String userInfo;
		String content;
		MyGridView myGridView;
	}
}