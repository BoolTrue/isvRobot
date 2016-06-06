package com.booltrue.listener;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.booltrue.modle.Question;
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
		
		Cursor questionCursor = mSqlDB.query("QUESTION", new String[]{Question.QuestionTile,Question.QuestionAnswer}, 
				getSelectionStr(sqlParam.length), sqlParam, null, null, Question.ID);
		
		ArrayList<String> questionRuesult = new ArrayList<String>();
		
		if(questionCursor!=null){
			int column = questionCursor.getColumnIndex(Question.QuestionTile);
			while(questionCursor.moveToNext()){
				questionRuesult.add(questionCursor.getString(column));
			}
		}
		//把查询结果传给mainActivity
		mainActivity.handlerSendMessage("questionList", questionRuesult);
		
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
