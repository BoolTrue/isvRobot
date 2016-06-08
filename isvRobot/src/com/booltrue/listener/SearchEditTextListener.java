package com.booltrue.listener;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import com.booltrue.adapter.QuestionCursorAdapter;
import com.booltrue.isvRobot.R;
import com.booltrue.modle.QuestionColumn;
import com.booltrue.ui.MainActivity;


public class SearchEditTextListener implements TextWatcher {

	private MainActivity mainActivity;
	
	private final String TAG = "搜索框";
	
	public SearchEditTextListener(Context context){
		this.mainActivity = (MainActivity)context;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}
	@Override
	public void afterTextChanged(Editable s) {
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//获取到当前EditText 内容
		String editText = s.toString();
		
		Log.d(TAG, editText);
		
		SQLiteDatabase mSqlDB = mainActivity.getSQLiteDatabase();
		
		if(editText.trim().equals("")){
			return;
		}
		
		
		String[] sqlParam = translatEditText(editText);
		
		//查询游标
		Cursor questionCursor = mSqlDB.query("QUESTION", new String[]{QuestionColumn.ID,QuestionColumn.QuestionTile,QuestionColumn.QuestionAnswer}, 
				getSelectionStr(sqlParam.length), sqlParam, null, null, null);
		
		Log.d(TAG, "搜索结果条数 -->" + questionCursor.getCount());
		
		ListAdapter listAdapter = new SimpleCursorAdapter(mainActivity, R.layout.list_text, 
				questionCursor, new String[]{QuestionColumn.QuestionTile}, new int[]{R.id.listEditText},0);
		
		/*ListAdapter listAdapter = new QuestionCursorAdapter(mainActivity, R.layout.list_text, 
				questionCursor, new String[]{QuestionColumn.QuestionTile}, new int[]{R.id.listEditText},0);*/
		
		
		
		//把查询结果传给mainActivity
		mainActivity.bindListAdapter(listAdapter);
		
	}
	
	private String[] translatEditText(String editText){
		
		editText = editText.replaceAll("。|？|‘|“|；|：", "");
		
		String[] resultArray = editText.split("，|,");
		
		for(int i=0;i<resultArray.length;i++){
			resultArray[i] = "%" + resultArray[i].trim() + "%";
		}
		
		return resultArray;
		
	}
	
	private String getSelectionStr(int paramLength){
		
		String result = "";
		
		for(int i=0;i<paramLength;i++){
			result += QuestionColumn.QuestionTile + " like ? and ";
		}
		
		Log.d(TAG, "SQL --> " + result);
		
		return result += "1=1";
	}
	
}
