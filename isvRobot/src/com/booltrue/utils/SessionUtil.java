package com.booltrue.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SessionUtil {
	private static String defaultSpeakStr = "你好我是小爱，请点击按钮向我问问题吧";
	public static boolean isSayHello = false;
	public static boolean isSayWarn = false;
	public static boolean isSayBye = false;
	public static long findFaceTime = 0;
	public static String speakStr  = defaultSpeakStr;
	
	public enum SearchButtonStatus{
		WAIT_TO_SPEAK,IS_SPEAKING,IS_SEARCHING
	}


	public static void reSetSession(){
		isSayHello = false;
		isSayWarn = false;
		isSayBye = false;
		findFaceTime = 0;
		speakStr  = defaultSpeakStr;
	}


	public static String getReserve(String speakStr){

		if(speakStr.contains("凯博是一家什么样的公司")||speakStr.contains("什么样的公司")||speakStr.contains("凯博是")||speakStr.contains("什么公司")){
			return "凯博机器人研发智造中心是国内最早从事智能服务机器人研发和制造的高新技术企业。公司生产的迎宾广告机器人是最具实用价值的智能服务机器人之一";
		}
		else if(speakStr.contains("迎宾广告机器人有哪些功能")||speakStr.contains("机器人有哪些功能")||speakStr.contains("广告机器人")){
			return "迎宾广告机器人具有人脸侦测、语音识别、自动应答、自动播报、自然语义对话、液晶屏广告播放、触摸查询等实用功能。凯博迎宾广告机器人，不但能为您吸引人气，招揽生意，还可以节省人工费用。如果客户有需要，还可以为您做定制开发。";
		}
		else if(speakStr.contains("迎宾广告机器人多少钱一台")||speakStr.contains("机器人多少钱")||speakStr.contains("多少钱一台")){
			return "迎宾广告机器人的全国统一市场零售价是29800元。";
		}
		else if(speakStr.contains("凯博机器人公司提供哪些服务")||speakStr.contains("哪些服务")||speakStr.contains("什么服务")||speakStr.contains("公司提供")){
			return "除国家规定的三包售后服务之外，凯博机器人公司还可以免费提供一次广告内容编辑与制作服务。如果客户需要，公司还可以提供上门安装培训售后等服务。总之购买凯博的迎宾广告机器人，您可以高枕无忧啦！";
		}
		else{
			return null;
		}
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
