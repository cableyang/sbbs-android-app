package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.Preferences;
import com.gfan.sbbs.ui.main.R;
import com.gfan.sbbs.ui.utils.ActivityUtils;

public class BoardListAdapter extends BaseAdapter {
	private List<Board> boardList;
	private Context context;
	private LayoutInflater mInflater;

	public BoardListAdapter(Context context) {
		super();
		boardList = new ArrayList<Board>();
		this.context = context;
	}

	public BoardListAdapter(LayoutInflater mInflater) {
		boardList = new ArrayList<Board>();
		this.mInflater = mInflater;
	}

	@Override
	public int getCount() {
		return boardList.size();
	}

	@Override
	public Object getItem(int position) {
		return boardList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void refresh(List<Board> list) {
		this.boardList = list;
		notifyDataSetChanged();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (null == mInflater) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.fav_item, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView
					.findViewById(R.id.fav_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (boardList.get(position).isDirectory()) {
			holder.textView.setText("[目录]" + boardList.get(position).getId());
		} else {
			holder.textView.setText("[版面]" + boardList.get(position).getTitle()
					+ "(" + boardList.get(position).getId() + ")");
		}
		if (!MyApplication.isNightMode) {
			if (boardList.get(position).isHasUnread()) {
				convertView.setBackgroundColor(0xffF6F6F6);
				// holder.textView.getPaint().setFakeBoldText(true);
			} else {
				convertView.setBackgroundColor(0xffE4E4E4);
				// holder.textView.getPaint().setFakeBoldText(false);
			}
		}
		String fontSize = MyApplication.getInstance().getmPreference().getString(Preferences.FONT_SIZE_ADJUST, "Normal");
		if("Normal".equals(fontSize)){
			holder.textView.setTextAppearance(MyApplication.getInstance().getActivity(), R.style.FavItemText_Normal);
		}else if("Large".equals(fontSize)){
			holder.textView.setTextAppearance(MyApplication.getInstance().getActivity(), R.style.FavItemText_Large);
		}else{
			holder.textView.setTextAppearance(MyApplication.getInstance().getActivity(), R.style.FavItemText_Small);
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView textView;
	}
}
