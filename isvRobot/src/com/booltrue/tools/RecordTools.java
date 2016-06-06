package com.booltrue.tools;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.booltrue.ui.MainActivity;
import com.booltrue.utils.ApkInstaller;
import com.booltrue.utils.FucUtil;
import com.booltrue.utils.JsonParser;
import com.booltrue.utils.SessionUtil;
import com.booltrue.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class RecordTools {
	// 语记安装助手类
	private ApkInstaller mInstaller ;
	//当前上下文
	private Context context;

	// 语音听写对象
	private SpeechRecognizer mIat;

	int ret = 0;//返回值

	private String TAG = "语音录入";

	public boolean isRcord = false;

	public String resultStr = "";

	private long startRecord = 0;



	public void initRecordParams(Context context){
		this.context = context;

		this.mInstaller = new  ApkInstaller((Activity)context);

		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

	}


	public void startRecord(){
		// 设置参数
		setParam();
		// 不显示听写对话框
		ret = mIat.startListening(mRecognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			
			ToastUtil.showTip("听写失败,错误码：" + ret, context, Toast.LENGTH_SHORT);
		} 
		else {
			ToastUtil.showTip("开始说话", context, Toast.LENGTH_SHORT);
		}
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				ToastUtil.showTip("初始化失败，错误码：" + code, context, Toast.LENGTH_SHORT);
			}
		}
	};

	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			Log.d(TAG, "开始说话-->" + (startRecord = System.currentTimeMillis()) );
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			isRcord = true;
		}

		@Override
		public void onError(SpeechError error) {
			Log.d(TAG, "出错    errorCode:" + error.getErrorCode());
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			isRcord = false;
		}

		@Override
		public void onEndOfSpeech() {
			Log.d(TAG, "结束说话-->" + (System.currentTimeMillis()-startRecord));
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			isRcord = false;
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, "结果:" + results.getResultString());
			//实时设置文本框的内容
			((MainActivity)context).handlerSendMessage("editText", translateResult(results));
			
			if (isLast) {
				// TODO 最后的结果
				Log.d(TAG, "最后的结果:"  +  results.getResultString());
				resultStr = translateResult(results);
				isRcord = false;
				Log.d(TAG, "resultStr:"  +  resultStr);
				
				//((MainActivity)context).handlerSendMessage("editText", resultStr);
			}

		}
		/**
		 * RecognizerResult 结果解析
		 * @param results
		 * @return
		 */
		public String translateResult(RecognizerResult results){
			String text = JsonParser.parseIatResult(results.getResultString());

			String sn = null;
			// 读取json结果中的sn字段
			try {
				JSONObject resultJson = new JSONObject(results.getResultString());
				sn = resultJson.optString("sn");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
			mIatResults.put(sn, text);

			StringBuffer resultBuffer = new StringBuffer();
			for (String key : mIatResults.keySet()) {
				resultBuffer.append(mIatResults.get(key));
			}
			return resultBuffer.toString();
		}


		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			//ToastUtil.showTip("当前正在说话，音量大小：" + volume, context, Toast.LENGTH_SHORT);
			//Log.d(TAG, "返回音频数据："+data.length);
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
	 * 
	 * @param param
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		if(SessionUtil.getNetStatus(context)){
			// 设置听写引擎  云端
			mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		}
		else{
			
			/**
			 * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
			 */
			if (!SpeechUtility.getUtility().checkServiceInstalled()) {
				((Activity)context).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						
						mInstaller.install();
					}
				});
			}
			else {
				String result = FucUtil.checkLocalResource();
				if (!TextUtils.isEmpty(result)) {

					ToastUtil.showTip(result, context, Toast.LENGTH_SHORT);
				}
			}
			
			
			// 设置听写引擎  本地
			mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
		}

		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS,"10000");

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS,"2000");

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, "1");

		// 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
		// 注：该参数暂时只对在线听写有效
		mIat.setParameter(SpeechConstant.ASR_DWA, "1");
	}

	public void stopRecording(){
		mIat.cancel();
		this.isRcord = false;
	}
}
