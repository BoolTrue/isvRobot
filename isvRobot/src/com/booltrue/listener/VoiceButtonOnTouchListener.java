package com.booltrue.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.booltrue.base.BaseActivity;
import com.booltrue.isvRobot.R;
import com.booltrue.ui.MainActivity;
import com.booltrue.utils.SessionUtil;

public class VoiceButtonOnTouchListener implements OnTouchListener {

	private Context mContext;
	
	private BaseActivity baseActivity;
	
	public VoiceButtonOnTouchListener(Context context){
		this.mContext = context;
		
		this.baseActivity = (BaseActivity)mContext;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View searchBtn, MotionEvent event) {
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			
			searchBtn.setBackgroundResource(R.drawable.voice_button);
			
			//隐藏答案区域
			((MainActivity)mContext).answerBack();
			//停止说话
			baseActivity.stopSpeakAndRecord();
			
			//震动提醒
			baseActivity.vibratorService(100l);
			
			//开始讲话
			//清空文本
			baseActivity.handlerSendMessage("editTextClear", "");
			
			baseActivity.recordTools.startRecord();
			
			SessionUtil.reSetTimeOut();
			
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			
			searchBtn.setBackgroundResource(R.drawable.voice_button_touch);
			
			baseActivity.stopSpeakAndRecord();
		}
		
		return true;//使onClick事件生效  true则onClick不生效
	}

}
