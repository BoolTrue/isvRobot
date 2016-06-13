package com.booltrue.tools;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.booltrue.modle.QuestionBmob;
import com.booltrue.ui.MainActivity;
import com.booltrue.utils.JsonParser;
import com.booltrue.utils.StringUtils;
import com.booltrue.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;


public class UnderstandTools {
	private static String TAG = "UnderstandTools";

	// 语义理解对象（文本到语义）。
	private TextUnderstander mTextUnderstander;

	private Context mContext;

	private MainActivity mainActivity;

	int ret = 0;// 函数调用返回值

	private String understandStr = "";

	public void initUnderstandParams(Context context){
		this.mContext = context;
		this.mainActivity = (MainActivity)context;
		//设置文本理解监听
		mTextUnderstander = TextUnderstander.createTextUnderstander(
				context, mTextUdrInitListener);
	}

	/**
	 * 文本语义理解
	 * 
	 * @param questionStr
	 */
	public void understandText(String questionStr){

		//得到输入的字符串
		understandStr = questionStr;

		ToastUtil.showTip(questionStr, mContext, Toast.LENGTH_SHORT);

		if (mTextUnderstander.isUnderstanding()) {
			/*mTextUnderstander.cancel();
			ToastUtil.showTip("取消文本理解", mContext, Toast.LENGTH_SHORT);*/
		}
		else if(!mainActivity.recordTools.isRcord){
			//字符串处理 去除标点符号
			String unPunctuateStr = StringUtils.replaceAllPunctuate(understandStr);
			
			Log.d(TAG, "语义理解字符串（去标点） -->" + unPunctuateStr );
			ret = mTextUnderstander.understandText(unPunctuateStr, mTextUnderstanderListener);


			if (ret != 0) {
				ToastUtil.showTip("语义理解失败,错误码:" + ret, mContext, Toast.LENGTH_SHORT);
			}
		}
	}

	//语义理解回调监听
	private TextUnderstanderListener mTextUnderstanderListener = new TextUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				// 显示
				String resultText = result.getResultString();
				if (!TextUtils.isEmpty(resultText)) {
					
					Log.d(TAG, "jsonResult" + resultText);
					
					//理解完毕
					//json解析
					String answer = JsonParser.parseUnderstandRusult(resultText);
					
					Log.d(TAG, "解析得到的结果" + answer);
					
					if(answer==null||answer.equals("")){
						//Bmob云端解析
						BmobDealWith();
					}
					else{
						resultDealWith(answer);
					}
				}
			}
			else {
				Log.d(TAG, "understander result:null");
				ToastUtil.showTip("讯飞语义识别结果不正确。", mContext, Toast.LENGTH_SHORT);
				//Bmob云端解析
				BmobDealWith();
			}
		}

		@Override
		public void onError(SpeechError error) {
			// 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
			ToastUtil.showTip("讯飞语义onError Code：" + error.getErrorCode(), mContext, Toast.LENGTH_SHORT);
			
			Log.d(TAG, "讯飞语义onError Code：" + error.getErrorCode());
			//Bmob云端解析
			//BmobDealWith();
		}
	};

	//初始化监听器（文本到语义）。
	private InitListener mTextUdrInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				ToastUtil.showTip("初始化失败,错误码：" + code, mContext, Toast.LENGTH_SHORT);
			}
		}
	};
	
	/**
	 * Bmob 云端数据查询答案处理
	 */
	public void BmobDealWith(){
		//如果讯飞语义没有查到结果，则转为Bmob云数据库查询

		String[] sqlParam = StringUtils.translatEditText(understandStr);

		QuestionBmobTools mQuestionBmobTools = mainActivity.getBmobTools();

		mQuestionBmobTools.QueryBmobObject(sqlParam, QuestionBmob.QUESTION_TITLE);

		mainActivity.reSetTimeOut();//重置超时
	}
	/**
	 * 讯飞答案处理
	 * @param answer
	 */
	public void resultDealWith(String answer){
		
		mainActivity.showAndSpeakAnswer(answer);
	}
	

	public void stopUnderstand(){
		mTextUnderstander.cancel();
	}

	public void destory(){
		mTextUnderstander.destroy();
	}

}
