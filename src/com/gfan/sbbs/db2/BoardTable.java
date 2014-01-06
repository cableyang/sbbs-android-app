package com.gfan.sbbs.db2;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.gfan.sbbs.bean.Board;

public class BoardTable implements BaseColumns {
	private static final String TAG= "BoardTable";

	public static final String TABLE_NAME = "board_table";
	public static final String BOARD_ID = "board_id";
	public static final String BOARD_NAME = "board_name";
	public static final String BOARD_FOLDER = "board_folder";
	public static final String BOARD_COUNT = "board_count";//����������
	public static final String BOARD_USER = "board_user";//���������û���
	
	public static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"("+_ID+" INTEGER PRIMARY KEY,"
											+BOARD_ID+" TEXT NOT NULL,"+BOARD_NAME+" TEXT NOT NULL,"
											+BOARD_FOLDER+" TEXT NOT NULL,"+BOARD_COUNT+" TEXT,"
											+BOARD_USER+" TEXT "+")";
	
	public static final String DROP_TABLE = "DROP TABLE "+TABLE_NAME;
	
	public static Board parseBoard(Cursor cursor){
		if (null == cursor || 0 == cursor.getCount()) {
			Log.w(TAG, "Cann't parse Cursor, bacause cursor is null or empty.");
			return null;
		} else if (-1 == cursor.getPosition()) {
			cursor.moveToFirst();
		}
		Board board = new Board();
		String id = cursor.getString(cursor.getColumnIndex(BOARD_ID));
		String name = cursor.getString(cursor.getColumnIndex(BOARD_NAME));
		board.setId(id).setTitle(name);
		return board;
	}
}
