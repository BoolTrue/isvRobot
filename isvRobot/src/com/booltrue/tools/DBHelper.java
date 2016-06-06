package com.booltrue.tools;

import com.booltrue.modle.QuestionColumn;

import android.content.ContentValues;
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
			QuestionColumn.ID + " INTEGER PRIMARY KEY," +
			QuestionColumn.QuestionTile + " VARCHAR ," +
			QuestionColumn.QuestionAnswer +" VARCHAR)";

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
		
		ContentValues values = new ContentValues();
		//1
		values.put(QuestionColumn.QuestionTile, "本次营改增试点范围的纳税人有哪些");
		values.put(QuestionColumn.QuestionAnswer, "在中华人民共和国境内提供交通运输业（陆路运输服务、水路运输服务、航空运输服务、管道运输服务）和部分现代服务业服务（研发和技术服务、信息技术服务、文化创意服务、物流辅助服务、有形动产租赁服务、鉴证咨询服务）的单位和个人，为增值税纳税人。");
		db.insert("QUESTION", "ID", values);
		//2
		values.put(QuestionColumn.QuestionTile, "试点纳税人如何进行划分");
		values.put(QuestionColumn.QuestionAnswer, "试点纳税人分为一般纳税人和小规模纳税人。应税服务年销售额超过财政部和国家税务总局规定标准的纳税人为一般纳税人，未超过规定标准的纳税人为小规模纳税人。");
		db.insert("QUESTION", "ID", values);
		//3
		values.put(QuestionColumn.QuestionTile, "试点纳税人认定为一般纳税人的年应税销售额标准是多少");
		values.put(QuestionColumn.QuestionAnswer, "应税服务年销售额标准为500万元。财政部和国家税务总局可以根据试点情况对应税服务年销售额标准进行调整。");
		db.insert("QUESTION", "ID", values);
		//4
		values.put(QuestionColumn.QuestionTile, "试点纳税人什么情况下应申请一般纳税人资格认定");
		values.put(QuestionColumn.QuestionAnswer, "试点实施前应税服务年销售额超过500万元的试点纳税人，应向主管国税机关申请办理一般纳税人资格认定手续。除国家税务总局另有规定外，一经认定为一般纳税人后，不得转为小规模纳税人。");
		db.insert("QUESTION", "ID", values);
		//5
		values.put(QuestionColumn.QuestionTile, "试点纳税人试点实施前年应征增值税销售额与应税服务年销售额如何换算");
		values.put(QuestionColumn.QuestionAnswer, "试点实施前的应税服务年销售额按以下公式换算：应税服务年销售额＝连续不超过12个月应税服务营业额合计÷（1+3％）。按照现行营业税规定差额征收营业税的试点纳税人，其应税服务营业额按未扣除之前的营业额计算。");
		db.insert("QUESTION", "ID", values);
		//6
		values.put(QuestionColumn.QuestionTile, "哪些情形不属于在境内提供应税服务");
		values.put(QuestionColumn.QuestionAnswer, "下列情形不属于在境内提供应税服务 " +
				"1、境外单位或者个人向境内单位或者个人提供完全在境外消费的应税服务。" +
				"2、境外单位或者个人向境内单位或者个人出租完全在境外使用的有形动产。" +
				"3、财政部和国家税务总局规定的其他情形。");
		db.insert("QUESTION", "ID", values);
		//7
		values.put(QuestionColumn.QuestionTile, "试点纳税人哪些行为视同提供应税服务行为");
		values.put(QuestionColumn.QuestionAnswer, "下列行为视同提供应税服务行为。" +
				"1、向其他单位或者个人无偿提供交通运输业和部分现代服务业服务，但以公益活动为目的或者以社会公众为对象的除外。" +
				"2、财政部和国家税务总局规定的其他情形。");
		
		db.insert("QUESTION", "ID", values);
	}

}
