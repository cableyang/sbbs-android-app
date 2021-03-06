package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.ui.main.R;

public class NoticeAdapter extends BaseAdapter {

	private List<Object> noticeList;
	private Context context;
	public NoticeAdapter(Context context){
		this.context = context;
		this.noticeList = new ArrayList<Object>();
	}
	@Override
	public int getCount() {
		return noticeList.size();
	}

	@Override
	public Object getItem(int position) {
		return noticeList.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void refresh(List<Object> list){
		this.noticeList = list;
		notifyDataSetChanged();
	}
	public void refresh(){
		notifyDataSetChanged();
	}
	@Override
	public View getView(int postion, View convertView, ViewGroup arg2) {
		Object object = noticeList.get(postion);
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView =  inflater.inflate(R.layout.notice_item, null);
			holder = new ViewHolder();
			holder.authorView = (TextView)convertView.findViewById(R.id.notice_author);
			holder.boardView = (TextView)convertView.findViewById(R.id.notice_board);
			holder.titleView = (TextView)convertView.findViewById(R.id.notice_title);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		if(object instanceof Mail){
			Mail mail = (Mail)object;
			holder.authorView.setText(mail.getFrom());
			holder.titleView.setText(mail.getTitle());
			holder.boardView.setText("");
		}else{
			Topic topic = (Topic)object;
			holder.authorView.setText(topic.getAuthor());
			holder.boardView.setText(topic.getBoardName());
			holder.titleView.setText(topic.getTitle());
		}
		return convertView;
	}
	private static class ViewHolder{
		TextView authorView;
		TextView boardView;
		TextView titleView;
	}
}
