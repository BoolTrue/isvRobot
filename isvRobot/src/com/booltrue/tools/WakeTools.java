package com.booltrue.tools;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.booltrue.isvRobot.R;
import com.booltrue.ui.MainActivity;
import com.booltrue.utils.ToastUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

public class WakeTools {

	private final String TAG = "WakeTools";

	private Context mContext;

	private MainActivity mainActivity;

	// 语音唤醒对象
	private VoiceWakeuper mIvw;
	
	//参数
	private int curThresh = 10;
	private String keep_alive = "1";
    private String ivwNetMode = "0";


	public void initWakeTools(Context context){
		this.mContext = context;
		this.mainActivity = (MainActivity)context;

		// 初始化唤醒对象
		mIvw = VoiceWakeuper.createWakeuper(mContext, mWakeUpInitListener);
	}
	
	public void startWakeListener(){
		mIvw = VoiceWakeuper.getWakeuper();
		if(mIvw != null) {
			
			// 清空参数
			mIvw.setParameter(SpeechConstant.PARAMS, null);
			// 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
			mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ curThresh);
			// 设置唤醒模式
			mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
			// 设置持续进行唤醒
			mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
			// 设置闭环优化网络模式
			mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
			// 设置唤醒资源路径
			mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
			// 启动唤醒
			int ret = mIvw.startListening(mWakeuperListener);
			if(ret != 0) {
				ToastUtil.showTip("语音唤醒失败,错误码:"+ ret, mContext, Toast.LENGTH_SHORT);
			} else {
				ToastUtil.showTip("语音唤醒启动成功", mContext, Toast.LENGTH_SHORT);
			}
		} else {
			ToastUtil.showTip("唤醒未初始化", mContext, Toast.LENGTH_SHORT);
		}
	}
	
	private String getResource() {
		return ResourceUtil.generateResourcePath(mContext,
			RESOURCE_TYPE.assets, "ivw/"+mContext.getString(R.string.app_id)+".jet");
	}
	
	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			Log.d(TAG, "onResult");
			try {
				String text = result.getResultString();
				JSONObject object;
				object = new JSONObject(text);
				StringBuffer buffer = new StringBuffer();
				buffer.append("【RAW】 "+text);
				buffer.append("\n");
				buffer.append("【操作类型】"+ object.optString("sst"));
				buffer.append("\n");
				buffer.append("【唤醒词id】"+ object.optString("id"));
				buffer.append("\n");
				buffer.append("【得分】" + object.optString("score"));
				buffer.append("\n");
				buffer.append("【前端点】" + object.optString("bos"));
				buffer.append("\n");
				buffer.append("【尾端点】" + object.optString("eos"));
				
				int score = Integer.valueOf(object.optString("score"));
				
				if(score>=curThresh){
					mainActivity.showMainLayout();
				}

				Log.d(TAG,  buffer.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onError(SpeechError error) {
			ToastUtil.showTip(error.getPlainDescription(true), mContext, Toast.LENGTH_SHORT);
		}

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {

		}

		@Override
		public void onVolumeChanged(int volume) {
			
		}
	};
	

	//初始化监听器（文本到语义）。
	private InitListener mWakeUpInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "mWakeUpInitListener  init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				ToastUtil.showTip("初始化失败,错误码：" + code, mContext, Toast.LENGTH_SHORT);
			}
		}
	};
	
	public void stopWeakUp(){
		mIvw.cancel();
		mIvw.stopListening();
	}
	
	public void destory(){
		mIvw.destroy();
	}
}
