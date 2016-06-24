package com.booltrue.listener;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.booltrue.modle.QuestionBmob;
import com.booltrue.tools.BmobTools;
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
		
		Log.d(TAG,"start-->" + start + "  before-->" + before + "  count-->" + count);
		
		if(editText.trim().equals("")){
			return;
		}
		
		mainActivity.stopSpeakAndRecord();
		
		//首先进行讯飞语义查询
		mainActivity.understandTools.understandText(editText);
		
	}
	
}
