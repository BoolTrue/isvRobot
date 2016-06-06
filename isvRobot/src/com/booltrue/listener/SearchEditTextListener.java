package com.booltrue.listener;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

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
		
		String[] sqlParam = translatEditText(editText);
		
		Cursor questionCursor = mSqlDB.query("QUESTION", new String[]{QuestionColumn.QuestionTile,QuestionColumn.QuestionAnswer}, 
				getSelectionStr(sqlParam.length), sqlParam, null, null, QuestionColumn.ID);
		
		ListAdapter listAdapter = new SimpleCursorAdapter(mainActivity, android.R.layout.simple_list_item_1, 
				questionCursor, new String[]{QuestionColumn.QuestionTile}, new int[]{android.R.id.text1},0);
		
		//把查询结果传给mainActivity
		mainActivity.bindListAdapter(listAdapter);
		
	}
	
	private String[] translatEditText(String editText){
		editText = editText.replaceAll("。|?|‘|“|；|：", editText);
		
		return editText.split("，");
	}
	
	private String getSelectionStr(int paramLength){
		
		String result = "";
		for(int i=0;i<paramLength;i++){
			result += " questionTile like %?% or ";
		}
		
		return result += " 1=1 ";
	}
	
	
	
}
