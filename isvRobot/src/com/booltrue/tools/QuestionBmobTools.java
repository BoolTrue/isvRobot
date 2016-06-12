package com.booltrue.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.booltrue.isvRobot.R;
import com.booltrue.modle.QuestionBmob;
import com.booltrue.ui.MainActivity;
import com.booltrue.utils.ToastUtil;

public class QuestionBmobTools {
	private final String APPLICATION_ID = "1a6f022a5c7fa9845622b10ed9424ccc";
	
	private final String TAG = "BmobTools";
	
	private Context mContext ;
	
	private MainActivity mainActivity;
	
	private List<QuestionBmob> resultList ;
	
	public void BmobInitialize(Context context){
		mainActivity = (MainActivity)context;
		mContext = context;
		Bmob.initialize(mContext, APPLICATION_ID);
	}
	
	/**
	 * 多行条件查询
	 * 
	 * @param 查询数组
	 * @param 列字段
	 * @return
	 */
	public void QueryBmobObject(String[] param , String columnKey ){
		
		if(resultList!=null){
			resultList.clear();
		}
		resultList = null;
		
		BmobQuery<QuestionBmob> mainQuery = new BmobQuery<QuestionBmob>();
		
		
		mainQuery.setLimit(50);
		
		//模糊查询
		List<BmobQuery<QuestionBmob>> andQueryList = new ArrayList<BmobQuery<QuestionBmob>>();
		
		for(String tmp : param){
			BmobQuery<QuestionBmob> andQuery = new BmobQuery<QuestionBmob>();
			
			andQuery.addWhereContains(columnKey, tmp);
			
			andQueryList.add(andQuery);
		}
		
		mainQuery.and(andQueryList);
		
		mainQuery.findObjects(mContext, new FindListener<QuestionBmob>() {
			
			@Override
			public void onSuccess(List<QuestionBmob> successList) {
				
				ToastUtil.showTip("总共查到"+ successList.size() + "条数据", mainActivity, Toast.LENGTH_SHORT);

				Log.d(TAG, "总共查到"+ successList.size() + "条数据");

				//首先停止说话
				mainActivity.stopSpeakAndRecord();

				if(successList.size()<=0){

					mainActivity.newThreadSpeak("暂时没有找到结果，试试其他关键词吧。");
				}
				else{
					mainActivity.newThreadSpeak("请选择你想查询的问题。");
				}

				/*ArrayAdapter<QuestionBmob> arrayAdapter = new ArrayAdapter<QuestionBmob>(mainActivity, R.layout.list_text, successList);

				mainActivity.bindListAdapter(arrayAdapter);*/
				
				List<Map<String,String>> resultList =  parseData(successList);
				
				SimpleAdapter listViewAdapter = new SimpleAdapter(mContext, resultList, R.layout.list_text,
										new String[]{QuestionBmob.QUESTION_TITLE},
										new int[]{R.id.listTextView});
				mainActivity.bindListAdapter(listViewAdapter,resultList);
			}
			
			@Override
			public void onError(int errorCode, String msg) {
				ToastUtil.showTip("BmobError :" + msg +"  errorCode:" + errorCode, mainActivity, Toast.LENGTH_SHORT);

				Log.e(TAG, msg);
			}
		});
		
	}

	/*@SuppressWarnings("unchecked")
	public T  QueryBmobObject(T param) throws InstantiationException, IllegalAccessException{
		
		BmobQuery<T> bmobQuery = new BmobQuery<T>();
	
		bmobQuery.getObject(mContext, null, new GetListener<T>() {

			@Override
			public void onFailure(int arg0, String arg1) {
				// 失败
				obj = null;
				Log.d(TAG, "Bmob云端获取失败");
			}

			@Override
			public void onSuccess(T arg0) {
				// 成功
				obj =  arg0;
			}
		});
		
		return obj==null?null:(T)obj;
		
	}*/
	
	
	public List<Map<String,String>> parseData(List<QuestionBmob> inData){
		
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		
		int dataNum = 0;
		
		for(QuestionBmob question: inData){
			
			dataNum++;
			
			Map<String,String> tempMap = new HashMap<String,String>();

			tempMap.put(QuestionBmob.QUESTION_TITLE,dataNum + "、" + question.getQuestionTitle());
			tempMap.put(QuestionBmob.QUESTION_ANSWER, question.getQuestionAnswer());
			
			resultList.add(tempMap);
		}
		
		return resultList;
	}
}
