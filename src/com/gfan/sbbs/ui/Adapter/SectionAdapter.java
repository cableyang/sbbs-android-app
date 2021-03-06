package com.gfan.sbbs.ui.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.ui.main.R;

public class SectionAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<List<Board>> boardList;

	public SectionAdapter(Context context) {
		this.context = context;
		this.boardList = new ArrayList<List<Board>>();
	}

	public void refresh() {
		notifyDataSetChanged();
	}

	public void refresh(List<List<Board>> list) {
		this.boardList = list;
		notifyDataSetChanged();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return this.boardList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean arg2, View convertView, ViewGroup arg4) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView tv;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.fav_item, null);
			tv = (TextView) convertView.findViewById(R.id.fav_item);
			convertView.setTag(tv);
		}else{
			tv = (TextView) convertView.getTag();
		}
		Board board = boardList.get(groupPosition).get(childPosition);
		tv.setText(board.getTitle()+"("+board.getId()+")");
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return boardList.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return this.boardList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return this.boardList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		return getpView(groupPosition);
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private TextView getpView(int groupPosition) {
		TextView tv = new TextView(this.context);
		LayoutParams layoutParams = new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(layoutParams);
		tv.setPadding(60, 5, 0, 5);
		tv.setTextSize(20);
//		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
		switch (groupPosition) {
		case 0:
			tv.setText(R.string.section_0);
			break;
		case 1:
			tv.setText(R.string.section_1);
			break;
		case 2:
			tv.setText(R.string.section_2);
			break;
		case 3:
			tv.setText(R.string.section_3);
			break;
		case 4:
			tv.setText(R.string.section_4);
			break;
		case 5:
			tv.setText(R.string.section_5);
			break;
		case 6:
			tv.setText(R.string.section_6);
			break;
		case 7:
			tv.setText(R.string.section_7);
			break;
		case 8:
			tv.setText(R.string.section_8);
			break;
		case 9:
			tv.setText(R.string.section_9);
			break;
		case 10:
			tv.setText(R.string.section_a);
			break;
		case 11:
			tv.setText(R.string.section_b);
			break;
		}
		return tv;
	}
}
