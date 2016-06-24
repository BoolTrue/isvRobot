package com.booltrue.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.booltrue.utils.ApkInstaller;
import com.booltrue.utils.SessionUtil;
import com.booltrue.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

public class SpeakTools {
	// 语音合成对象
	private SpeechSynthesizer mTts;
	
	private Context context;

	// 语记安装助手类
	private ApkInstaller mInstaller ;

	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;

	//是否正在播放
	public boolean isPlay = false;


	public void initSpeakParams(Context context){
		this.context = context;
		this.mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
		this.mInstaller = new  ApkInstaller((Activity)context);
	}


	//Speek语音合成
	public void speakSomething(String speakStr){

		this.isPlay = true;

		//设置参数
		setParam();
		
		if(speakStr==null||speakStr.equals("")){
			return;
		}

		int code = mTts.startSpeaking(speakStr, mTtsListener);

		if (code != ErrorCode.SUCCESS) {
			if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
				//未安装则跳转到提示安装页面
				installHelp();
			}else {
				ToastUtil.showTip("语音合成失败,错误码: " + code, context, Toast.LENGTH_SHORT);
			}
		}
	}


	private void installHelp(){
		((Activity)context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mInstaller.install();
			}
		});
	}


	/**
	 * 初始化监听。
	 */
	public InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d("语音合成", "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				ToastUtil.showTip("初始化失败,错误码："+code, context, Toast.LENGTH_SHORT);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}		
		}
	};

	/**
	 * 语音合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			isPlay = true;
			ToastUtil.showTip("开始播放", context, Toast.LENGTH_SHORT);
		}

		@Override
		public void onSpeakPaused() {
			isPlay = false;
			ToastUtil.showTip("暂停播放", context, Toast.LENGTH_SHORT);
		}

		@Override
		public void onSpeakResumed() {
			isPlay = true;
			ToastUtil.showTip("继续播放", context, Toast.LENGTH_SHORT);
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
			//ToastUtil.showTip(String.format(context.getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying), context, Toast.LENGTH_SHORT);
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
			//ToastUtil.showTip(String.format(context.getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying), context, Toast.LENGTH_SHORT);
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				ToastUtil.showTip("播放完成", context, Toast.LENGTH_SHORT);

			} else if (error != null) {
				ToastUtil.showTip(error.getPlainDescription(true), context, Toast.LENGTH_SHORT);
			}
			isPlay = false;
			SessionUtil.reSetTimeOut();
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};

	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);

		if(SessionUtil.getNetStatus(context)){
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
			//设置合成语速
			mTts.setParameter(SpeechConstant.SPEED, "50");
			//设置合成音调
			mTts.setParameter(SpeechConstant.PITCH, "50");
			//设置合成音量
			mTts.setParameter(SpeechConstant.VOLUME, "50");
		}
		else{

			/**
			 * 选择本地合成
			 * 判断是否安装语记,未安装则跳转到提示安装页面
			 */
			if (!SpeechUtility.getUtility().checkServiceInstalled()) {
				installHelp();
			}

			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			/**
			 * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
			 * 开发者如需自定义参数，请参考在线合成参数设置
			 */
			mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
		}

	}

	public void stopSpeaking(){
		mTts.stopSpeaking();
		this.isPlay = false;
	}
	
	public void destory(){
		mTts.destroy();
	}

}
