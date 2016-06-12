package com.booltrue.listener;

import com.booltrue.ui.MainActivity;

import cn.bmob.v3.listener.GetListener;

public class BmobGetListener<T> extends GetListener<T> {

	private MainActivity mainActivity;
	
	private final String TAG = "BmobGetListener";
	
	
	@Override
	public void onFailure(int arg0, String arg1) {
		
		
	}

	@Override
	public void onSuccess(T arg0) {
		
		
	}

}
