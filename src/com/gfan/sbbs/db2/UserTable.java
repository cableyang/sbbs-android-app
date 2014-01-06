package com.gfan.sbbs.db2;

import com.gfan.sbbs.bean.User;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

public class UserTable implements BaseColumns {
	private static final String TAG = "UserTable";

	public static final String TABLE_NAME = "user_table";
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";
	public static final String CURRENT_STATUS = "user_status";
	public static final String SOURCE_IP = "source_ip";
	public static final String ASTROLOLGY = "astrology";
	public static final String LOGIN_TIMES = "login_time";
	public static final String POST_TIMES = "post_time";
	public static final String LIFE_VALUE = "life_value";
	public static final String PERFORM_VALUE = "perform_value";
	public static final String EXPERIENCE = "experience";
	public static final String IDENTITY = "identity";
	public static final String MEDAL_NUM = "medal_num";
	public static final String GENDER = "gender";
	public static final String LAST_LOGIN_TIME = "last_login_time";

	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
			+ "(" + _ID + " INTEGER PRIMARY KEY," + USER_ID + " TEXT NOT NULL,"
			+ USER_NAME + " TEXT NOT NULL" + CURRENT_STATUS + " TEXT,"
			+ SOURCE_IP + " TEXT," + ASTROLOLGY + " TEXT," + LOGIN_TIMES
			+ " TEXT," + POST_TIMES + " TEXT," + LIFE_VALUE + " TEXT,"
			+ PERFORM_VALUE + " TEXT," + EXPERIENCE + " TEXT," + IDENTITY
			+ " TEXT,"+MEDAL_NUM+" TEXT,"+GENDER+" TEXT,"+LAST_LOGIN_TIME+" TEXT)";
	public static final String DROP_TABLE = "DROP TABLE "+TABLE_NAME;
	
	public static User parseCursor(Cursor cursor){
		
		if(null == cursor || 0 == cursor.getCount()){
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		}else if(-1 == cursor.getPosition()){
			cursor.moveToFirst();
		}
		User user = new User();
		String user_ID = cursor.getString(cursor.getColumnIndex(USER_ID));
		String user_name = cursor.getString(cursor.getColumnIndex(USER_NAME));
		
		return user;
	}
}
