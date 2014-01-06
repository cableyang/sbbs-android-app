package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.main.R;

public class TopicListAdapter extends BaseAdapter {
	private List<Topic> topicList;
	private Context context;
	private static final String TAG = "TopicListAdapter";

	public TopicListAdapter(Context context) {
		super();
		this.topicList = new ArrayList<Topic>();
		this.context = context;
	}

	@Override
	public int getCount() {
		return topicList.size();
	}

	@Override
	public Object getItem(int position) {
		return topicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "current position is " + position);
		ViewHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listview_topic_list_item,
					null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView
					.findViewById(R.id.topic_title);
			// holder.num = (TextView) convertView.findViewById(R.id.topic_num);
			holder.authorView = (TextView) convertView
					.findViewById(R.id.topic_author);
			holder.timeView = (TextView) convertView
					.findViewById(R.id.topic_time);
			holder.readView = (TextView) convertView
					.findViewById(R.id.topic_popularity);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Topic topic = topicList.get(position);
		// holder.num.setText(topic.getNum());
		holder.authorView.setText(topic.getAuthor());
		holder.timeView.setText(topic.getTime());
		holder.readView.setText("(" + topic.getPopularity() + ")");
		if (topicList.get(position).isOnTop()) {
			Log.i(TAG, topicList.get(position).getTitle());
			holder.title.setText("[置顶]" + topic.getTitle());
		} else {
			if (topic.getId() == topic.getReid()) {
				holder.title.setText("●".concat(topic.getTitle()));
			} else {
				holder.title.setText(topic.getTitle());
			}
		}
		Log.i(TAG, "SBBSupport:topic unread is " + topic.isUnRead());
		// TODO distinguish the unread item under night mode
		if (!MyApplication.isNightMode) {
			if (topic.isUnRead()) {
				convertView.setBackgroundColor(0xffF5F5F5);
				// holder.title.getPaint().setFakeBoldText(true);
			} else {
				convertView.setBackgroundColor(0xffE4E4E4);
				// holder.title.getPaint().setFakeBoldText(false);
			}
		}
		return convertView;
	}

	public void refresh(List<Topic> list) {
		this.topicList = list;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	private static class ViewHolder {
		// TextView num;
		TextView authorView;
		TextView timeView;
		TextView title;
		TextView readView;
	}
}