package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.ui.main.R;

public class TopTenAdapter extends BaseAdapter {
	private List<Topic> topList;
	private Context context;
	private LayoutInflater mInflater;

	public TopTenAdapter(Context context) {
		this.topList = new ArrayList<Topic>();
		this.context = context;
	}
	
	public TopTenAdapter(LayoutInflater mInflater){
		this.topList = new ArrayList<Topic>();
		this.mInflater = mInflater;
	}

	@Override
	public int getCount() {
		return topList.size();
	}

	@Override
	public Object getItem(int position) {
		return topList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void refresh(List<Topic> list) {
		this.topList = list;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == mInflater) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listview_item_top_ten,
					null);
			holder.txt_author = (TextView) convertView
					.findViewById(R.id.list_top_ten_textAuthor);
			holder.txt_title = (TextView) convertView
					.findViewById(R.id.list_top_ten_textTitle);
			holder.txt_board = (TextView) convertView
					.findViewById(R.id.list_top_ten_textBoard);
			holder.readView = (TextView) convertView
					.findViewById(R.id.list_top_ten_pop);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Topic topic = topList.get(position);
		holder.txt_author.setText(topic.getAuthor());
		holder.txt_title.setText(topic.getTitle());
		holder.txt_board.setText(topic.getBoardName()+"版");
		holder.readView.setText(topic.getReplies() + "/"
				+ topic.getPopularity());
		return convertView;
	}

	private static class ViewHolder {
		TextView txt_author;
		TextView txt_title;
		TextView txt_board;
		TextView readView;
	}
}