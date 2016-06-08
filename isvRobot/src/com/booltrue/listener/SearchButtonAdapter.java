package com.booltrue.listener;

import android.content.Context;
import android.view.View;
import com.booltrue.utils.SessionUtil.SearchButtonStatus;

import com.booltrue.ui.MainActivity;
import com.iflytek.thridparty.m;

public class SearchButtonAdapter implements View.OnClickListener {
	
	private MainActivity mainActivity;
	
	private SearchButtonStatus btnStatus = null;
	
	public SearchButtonAdapter(Context context){
		this.mainActivity = (MainActivity)context;
		//初始状态为等待说话
		btnStatus = SearchButtonStatus.WAIT_TO_SPEAK;
	}
	
	@Override 
	public void onClick(View v) {
		//隐藏答案区域
		mainActivity.answerBack();
		//停止说话
		mainActivity.stopSpeakAndRecord();
		
		//如果是正在说话的状态，则点击按钮直接停止说话
		if(btnStatus.equals(SearchButtonStatus.IS_SPEAKING)){
			
			if(mainActivity.speakTools.isPlay){
				mainActivity.speakTools.stopSpeaking();
			}
			if(mainActivity.recordTools.isRcord){
				mainActivity.recordTools.stopRecording();
			}
			
			//如果正在说话的状态下按下按钮，则直接进入搜索界面
			btnStatus = SearchButtonStatus.IS_SEARCHING;
			
			return;
		}
		//开始讲话
		btnStatus = SearchButtonStatus.IS_SPEAKING;
		
		//清空文本
		mainActivity.handlerSendMessage("editTextClear", "");
		
		mainActivity.recordTools.startRecord();
		
		btnStatus = SearchButtonStatus.WAIT_TO_SPEAK;
		
	}
	
	public SearchButtonStatus getBtnStatus(){
		return btnStatus;
	}
	

}
