package com.booltrue.utils;

public class StringUtils {
	/**
	 * 去除标点符号
	 * @param param
	 * @return
	 */
	public static String replaceAllPunctuate(String param){
		//去除中文符号
		param =  param.replaceAll("。|？|‘|“|；|：|！", "");
		//去除英文符号
		return param.replaceAll("|.|\\?|'|\"|;|:|!", "");
	}

	public static String[] translatEditText(String editText){

		editText = replaceAllPunctuate(editText);

		return editText.split("，|,");

	}
}
