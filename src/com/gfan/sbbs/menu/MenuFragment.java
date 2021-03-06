package com.gfan.sbbs.menu;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.ui.Adapter.MenuItemAdapter;
import com.gfan.sbbs.ui.base.HomeViewModel;
import com.gfan.sbbs.ui.main.R;

public class MenuFragment extends ListFragment {
	private List<MenuItem> menuItems;
	private MenuItemAdapter myAdapter;
	private HomeViewModel mHomeViewModel;
//	private MyApplication application;

	private static final String TAG = "MenuFragment";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setCacheColorHint(0);
		View headerView = LayoutInflater.from(MyApplication.mContext).inflate(
				R.layout.menu_item, null);
		headerView.setId(R.id.current_user);
		String currentUser = "guest";
		if (MyApplication.checkLogin()) {
			currentUser = MyApplication.userName;
		}
		((TextView) headerView.findViewById(R.id.rbm_item_text))
				.setText("当前用户:" + currentUser);
		headerView.findViewById(R.id.rbm_item_icon).setVisibility(View.GONE);
		getListView().addHeaderView(headerView);
		getListView().setBackgroundResource(R.drawable.menu_bg);
		parseXml(R.menu.ribbon_menu);
		myAdapter = new MenuItemAdapter(getActivity());
		mHomeViewModel = MyApplication.mHomeViewModel;
		setListAdapter(myAdapter);
		draw();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mHomeViewModel.setCurrentTabIndex(position - 1);
		Log.i(TAG, "position is " + position);
		((MenuActivity) getActivity()).getSlideoutHelper().close();
	}

	private void draw() {
		myAdapter.draw(menuItems);
		Log.i(TAG, "mList draw,mList.size is " + myAdapter.getCount());
	}

	private void parseXml(int menu) {
		menuItems = new ArrayList<MenuItem>();
		try {
			XmlResourceParser xpp = getResources().getXml(menu);
			xpp.next();
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String elemName = xpp.getName();
					if (elemName.equals("item")) {
						String textId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"title");
						String iconId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"icon");
						String resId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android",
								"id");
						int id = Integer.valueOf(resId.replace("@", ""));
						String title = resourceIdToString(textId);
						int icon = Integer.valueOf(iconId.replace("@", ""));

						MenuItem item = new MenuItem(id, icon, title);
						menuItems.add(item);
					}
				}

				eventType = xpp.next();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String resourceIdToString(String text) {
		if (!text.contains("@")) {
			return text;
		} else {

			String id = text.replace("@", "");
			return getResources().getString(Integer.valueOf(id));
		}

	}

}
