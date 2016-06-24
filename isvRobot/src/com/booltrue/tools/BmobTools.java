package com.booltrue.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;

import com.booltrue.modle.QuestionBmob;

public class BmobTools {
	private final String APPLICATION_ID = "1a6f022a5c7fa9845622b10ed9424ccc";
	
	private Context mContext ;
	
	public void BmobInitialize(Context context){
		mContext = context;
		Bmob.initialize(mContext, APPLICATION_ID);
	}
	
	
	
	public List<Map<String,Object>> parseData(List<? extends BmobObject> inData){
		 
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		
		if(inData==null||inData.size()==0){
			return resultList;
		}
		
		if(inData.get(0) instanceof QuestionBmob){
			int dataNum = 0;
			
			for(BmobObject question: inData){
				
				dataNum++;
				
				Map<String,Object> tempMap = new HashMap<String,Object>();

				tempMap.put(QuestionBmob.QUESTION_TITLE,dataNum + "、" + ((QuestionBmob) question).getQuestionTitle());
				tempMap.put(QuestionBmob.QUESTION_ANSWER, ((QuestionBmob) question).getQuestionAnswer());
				tempMap.put(QuestionBmob.IS_IMG_MSG, ((QuestionBmob) question).getIsImgMsg());
				tempMap.put(QuestionBmob.OBJECT_ID, ((QuestionBmob) question).getObjectId());
				resultList.add(tempMap);
			}
		}
		
		return resultList;
	}
	
	public <T> BmobQuery<T> getBmobQuery(Class<T> objClass){
		
		BmobQuery<T> bmobQuery =  new BmobQuery<T>();
		
		return  bmobQuery;
	}
	
}
