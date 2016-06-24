package com.booltrue.tools;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "question.db";

	private static final int VERSION = 1; 

	private String TAG = "数据库Helper";

	private final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS QUESTION (" +
			/*QuestionColumn.ID + " INTEGER PRIMARY KEY," +
			QuestionColumn.QuestionTile + " VARCHAR ," +
			QuestionColumn.QuestionAnswer +*/" VARCHAR)";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);


	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.d(TAG, "onCreate");

		db.execSQL(CREATE_TABLE_IF_NOT_EXISTS);

		dataBaseInitTest(db);

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM QUESTION", null);
		cursor.moveToNext();
		Log.d(TAG, "QUESTION_COUNT -->" + cursor.getString(0) );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


	}

	//仅供测试
	private void dataBaseInitTest(SQLiteDatabase db){

		/*ContentValues values = new ContentValues();
		//1
		values.put(QuestionColumn.QuestionTile, "本次营改增试点范围的纳税人有哪些");
		values.put(QuestionColumn.QuestionAnswer, "在中华人民共和国境内提供交通运输业（陆路运输服务、水路运输服务、航空运输服务、管道运输服务）和部分现代服务业服务（研发和技术服务、信息技术服务、文化创意服务、物流辅助服务、有形动产租赁服务、鉴证咨询服务）的单位和个人，为增值税纳税人。");
		db.insert("QUESTION", "ID", values);*/


	}

}
