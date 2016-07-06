package com.booltrue.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ToastUtil {

	private static Toast mToast = null;
	
	private static Handler mHandler = null;

	public static void showTip(String str,final Context context , final int toastLength) {
		
		Bundle bundle = new Bundle();
		bundle.putString("showStr", str);


		final Message msg = new Message();
		msg.setData(bundle);

		((Activity)context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mToast==null){
					mToast = Toast.makeText(context, "", toastLength);
				}
				
				if(mHandler==null){
					//新建Handler 控制Toast
					mHandler = new Handler(){
						@Override
						public void handleMessage(final Message msg) {

							mToast.setText(msg.getData().getString("showStr"));
							mToast.show();
						}
					};
				}
				mHandler.sendMessage(msg);
			};
		});
	}
}
