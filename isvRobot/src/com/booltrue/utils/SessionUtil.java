package com.booltrue.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SessionUtil {
	public static long doNotAnyActionTime = 0;
	
	public static boolean isAnswer = false;
	
	public enum SearchButtonStatus{
		WAIT_TO_SPEAK,IS_SPEAKING,IS_SEARCHING
	}

	
	public static void reSetTimeOut(){
		doNotAnyActionTime =  System.currentTimeMillis();
	}

	/**
	 * 判断是否联网了
	 * @param context
	 * @return
	 */
	public static boolean getNetStatus(Context context){
		ConnectivityManager cwjManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cwjManager.getActiveNetworkInfo(); 
		if (info != null && info.isAvailable()){ 
			return true;
		}
		else
		{
			return false;
		}
	}
}
