package com.booltrue.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.booltrue.tools.BmobTools;
import com.booltrue.tools.RecordTools;
import com.booltrue.tools.RecordUITools;
import com.booltrue.tools.SpeakTools;
import com.booltrue.utils.SessionUtil;

@SuppressLint("HandlerLeak")
public abstract class BaseActivity extends Activity implements OnTouchListener {

	//语音合成工具
	public SpeakTools speakTools = new SpeakTools();
	//语音录入
	public RecordTools recordTools = new RecordTools();
	
	protected BmobTools bmobTools = new BmobTools();;
	
	public RecordUITools recordUITools = new RecordUITools();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//语音合成初始化
		speakTools.initSpeakParams(this);
		//语音录入初始化
		recordTools.initRecordParams(this);
		
		//UI语音录入初始化
		recordUITools.initRecordParams(this);
	}
	
	/**
	 * 停止说话和录音
	 */
	public void stopSpeakAndRecord(){
		speakTools.stopSpeaking();
		recordTools.stopRecording();
	}
	/**
	 * 根据条件查询数据
	 * @param param
	 * @param columnKey
	 */
	public abstract void QueryBmobObjects(String[] param , String columnKey );

	/**
	 * 震动
	 * @param time
	 */
	public void vibratorService(long time){

		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		vibrator.vibrate(time);
	}

	public void newThreadSpeak(final String speakStr){

		new Thread(new Runnable() {

			@Override
			public void run() {
				speakTools.speakSomething(speakStr);

			}
		}).start();
	}
	
	protected Handler mHandler = new Handler(){};
	
	public void handlerSendMessage(String key,String value){
		Bundle mBundle = new Bundle();
		Message mMsg = new Message();

		mBundle.putString(key, value);
		
		
		mMsg.setData(mBundle);
		
		mHandler.sendMessage(mMsg);
		
	}

	public boolean isPlay(){
		return speakTools.isPlay;
	}
	public boolean isRecord(){
		return recordTools.isRcord;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		SessionUtil.reSetTimeOut();

		return true;
	}
}
