package com.gfan.sbbs.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDAO {
	private static final String TAG = "UserDAO";
	private SQLiteOpenHelper mSQLiteOpenHelper;
	
	public UserDAO(Context context){
		mSQLiteOpenHelper = SBBSDataBase.getInstance(context).getmDataBaseHelper();
	}
	
//	public long insertUser(User user){
//		SQLiteDatabase db = mSQLiteOpenHelper.getWritableDatabase();
//		return db.insert(table, nullColumnHack, values)
//	}
}
