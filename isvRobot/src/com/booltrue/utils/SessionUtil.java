package com.booltrue.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SessionUtil {
	private static String defaultSpeakStr = "你好！欢迎光临！我是迎宾机器人小李，有什么想问我的，就按屏幕上的按键，跟我说话吧！";
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
		else if(speakStr.contains("本次营改增试点范围的纳税人有哪些")||speakStr.contains("试点范围")||speakStr.contains("纳税人有哪些")||speakStr.contains("税人有哪些")||speakStr.contains("人有哪些")){
			return "在中华人民共和国境内提供交通运输业（陆路运输服务、水路运输服务、航空运输服务、管道运输服务）和部分现代服务业服务（研发和技术服务、信息技术服务、文化创意服务、物流辅助服务、有形动产租赁服务、鉴证咨询服务）的单位和个人，为增值税纳税人。";
		}
		else if(speakStr.contains("试点纳税人如何进行划分")||speakStr.contains("划分")||speakStr.contains("如何进行")||speakStr.contains("进行")){
			return "试点纳税人分为一般纳税人和小规模纳税人。应税服务年销售额超过财政部和国家税务总局规定标准的纳税人为一般纳税人，未超过规定标准的纳税人为小规模纳税人。";
		}
		else if(speakStr.contains("试点纳税人认定为一般纳税人的年应税销售额标准是多少")||speakStr.contains("销售额标准是多少")||speakStr.contains("标准是多少")||speakStr.contains("销售额")){
			return "应税服务年销售额标准为500万元。财政部和国家税务总局可以根据试点情况对应税服务年销售额标准进行调整。";
		}
		else if(speakStr.contains("试点纳税人什么情况下应申请一般纳税人资格认定")||speakStr.contains("纳税人资格认定")||speakStr.contains("纳税人资格")||speakStr.contains("资格认定")){
			return "试点实施前应税服务年销售额超过500万元的试点纳税人，应向主管国税机关申请办理一般纳税人资格认定手续。除国家税务总局另有规定外，一经认定为一般纳税人后，不得转为小规模纳税人。";
		}
		else if(speakStr.contains("试点纳税人试点实施前年应征增值税销售额与应税服务年销售额如何换算")||speakStr.contains("增值税销售额与应税服务年销售额")||speakStr.contains("增值税销售额")||speakStr.contains("年销售额")||speakStr.contains("税服务年销售额")||speakStr.contains("换算")){
			return "试点实施前的应税服务年销售额按以下公式换算：应税服务年销售额＝连续不超过12个月应税服务营业额合计÷（1+3％）。按照现行营业税规定差额征收营业税的试点纳税人，其应税服务营业额按未扣除之前的营业额计算。";
		}
		else if(speakStr.contains("哪些情形不属于在境内提供应税服务")||speakStr.contains("不属于在境内提供应税服务")||speakStr.contains("境内提供应税")||speakStr.contains("不属于在境内")||speakStr.contains("境内提供")){
			return "下列情形不属于在境内提供应税服务 " +
					"1、境外单位或者个人向境内单位或者个人提供完全在境外消费的应税服务。" +
					"2、境外单位或者个人向境内单位或者个人出租完全在境外使用的有形动产。" +
					"3、财政部和国家税务总局规定的其他情形。";
		}
		else if(speakStr.contains("试点纳税人哪些行为视同提供应税服务行为")||speakStr.contains("提供应税服务行为")||speakStr.contains("提供应税")){
			return "下列行为视同提供应税服务行为。" +
					"1、向其他单位或者个人无偿提供交通运输业和部分现代服务业服务，但以公益活动为目的或者以社会公众为对象的除外。" +
					"2、财政部和国家税务总局规定的其他情形。";
		}
		else if(speakStr.contains("试点纳税人哪些应税服务适用于零税率")||speakStr.contains("零税率")||speakStr.contains("零")||speakStr.contains("适用")){
			return "试点地区的单位和个人提供的国际运输服务、向境外单位提供的研发服务和设计服务，适用零税率。";
		}
		else if(speakStr.contains("试点纳税人的增值税税率是如何规定的")||speakStr.contains("增值税税率")||speakStr.contains("税率")||speakStr.contains("规定")){
			return "试点纳税人的增值税税率为。" +
					"1、提供有形动产租赁服务，税率为17%。" +
					"2、提供交通运输业服务，税率为11%。" +
					"3、提供现代服务业服务（有形动产租赁服务除外），税率为6%。" +
					"4、财政部和国家税务总局规定的应税服务，税率为零。";
		}
		else if(speakStr.contains("小规模纳税人可以自己开增值税普票和专票吗")||speakStr.contains("小规模纳税人")||speakStr.contains("自己开")||speakStr.contains("小规模")||speakStr.contains("普票")||speakStr.contains("专票")){
			return "小规模纳税人可以自己开增值税普票，如果需要开增值税专用发票，则需要凭销售合同向当地税务局申请代开发票。";
		}
		else if(speakStr.contains("简易征收的纳税人能不能开出增值税专用发票")||speakStr.contains("简易征收")||((speakStr.contains("增值税专用发票")||speakStr.contains("增票"))&&speakStr.contains("征收"))||speakStr.contains("专用发票")){
			return "能否开出增值税专用发票取决于纳税人的税务资质，跟是否采取简易征收无关。是否采用简易征收则取决于对新老项目的认定。也就是说具有一般纳税人资格但因为老项目选择了简易征收的纳税人是可以开具增值税专用发票的。";
		}
		else if(speakStr.contains("请问个人可以到税务机关申请代开增值税专用发票吗")||(speakStr.contains("个人")&&speakStr.contains("代开"))||speakStr.contains("代开")||speakStr.contains("代开增")){
			return "是不可以的。根据《国家税务总局关于印发〈税务机关代开增值税专用发票管理办法(试行)〉的通知》(国税发【2004】153号)规定，已办理税务登记的小规模纳税人(包括个体经营者)发生增值税应税行为，需要开具专用发票时，可向其主管税务机关申请代开。对于已办理税务登记的个体工商户可以申请代开增值税专用发票，但是其他个人不得申请代开增值税专用发票。";
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
