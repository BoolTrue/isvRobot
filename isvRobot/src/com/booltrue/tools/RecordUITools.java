package com.booltrue.tools;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.booltrue.ui.MainActivity;
import com.booltrue.utils.ApkInstaller;
import com.booltrue.utils.JsonParser;
import com.booltrue.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

public class RecordUITools {
	// 语记安装助手类
	private ApkInstaller mInstaller ;
	//当前上下文
	private Context context;
	int ret = 0;//返回值
	private String TAG = "语音录入";
	public boolean isRcord = false;
	public String resultStr = "";

	// 语音听写UI
	private RecognizerDialog mIatDialog;

	public void initRecordParams(Context context){
		this.context = context;

		this.mInstaller = new  ApkInstaller((Activity)context);

		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		mIatDialog = new RecognizerDialog(context, mInitListener);
		
	}

	public void startRecord(){
		isRcord = true;

		// 显示听写对话框
		mIatDialog.setListener(mRecognizerDialogListener);
		mIatDialog.show();
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
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			isRcord = false;
			
			//实时设置文本框的内容
			((MainActivity)context).handlerSendMessage("editText", translateResult(results));
			
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			ToastUtil.showTip(error.getPlainDescription(true), context, Toast.LENGTH_SHORT) ;
		}

	};
	
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
}
