package com.booltrue.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.booltrue.isvRobot.R;
import com.booltrue.listener.SearchButtonAdapter;
import com.booltrue.listener.SearchEditTextListener;
import com.booltrue.tools.DBHelper;
import com.booltrue.tools.RecordTools;
import com.booltrue.tools.SpeakTools;
import com.booltrue.utils.SessionUtil;
import com.booltrue.utils.SessionUtil.SearchButtonStatus;
import com.iflytek.cloud.Setting;


public class MainActivity extends Activity {
	
	private String TAG = "主窗口";
	//基本UI
	private Button searchBtn = null;
	private EditText searchEditText = null;
	private ListView questionResult = null;
	private Toast mToast = null;
	//语音合成工具
	public SpeakTools speakTools = new SpeakTools();
	//语音录入
	public RecordTools recordTools = new RecordTools();
	
	private SearchButtonAdapter btnAdapter = null;
	private SearchEditTextListener textListener = null;
	
	//SQLite数据库
	private SQLiteDatabase questionDb = null;
	
	private DBHelper dbHelper = null;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//语音合成初始化
		speakTools.initSpeakParams(MainActivity.this);
		//语音录入初始化
		recordTools.initRecordParams(MainActivity.this);
		
		btnAdapter = new SearchButtonAdapter(this);
		textListener = new SearchEditTextListener(this);
		
		//控件初始化
		initUI();
		initData();
		
		SessionUtil.reSetSession();

		Setting.setShowLog(false);
		
		
	}
	//初始化数据库
	private void initData() {
		
		//questionDb = this.openOrCreateDatabase(DBHelper.DBName, Context.MODE_PRIVATE, null);
		//使用dbHelper创建数据库 和 表
		dbHelper = new DBHelper(this);
		
		Log.d(TAG, "dbHelper --> getWritableDatabase()");
		//获取只读数据库
		questionDb = dbHelper.getReadableDatabase();
		
		
		
	}

	@SuppressLint("ShowToast")
	private void initUI() {
		searchBtn = (Button)findViewById(R.id.searchBtn);
		searchEditText = (EditText)findViewById(R.id.searchEditText);
		questionResult = (ListView)findViewById(R.id.questionList);

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		//设置自定义按键监听
		searchBtn.setOnClickListener(btnAdapter);
		
		//设置文本框监听
		searchEditText.addTextChangedListener(textListener);
	}

	//新建Handler 控制Toast
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {

			Bundle bundleTmp =  msg.getData();

			if(bundleTmp.containsKey("showStr")){
				mToast.setText(bundleTmp.getString("showStr"));
				mToast.show();
			}
			else if(bundleTmp.containsKey("btnText")){
				searchBtn.setText(bundleTmp.getString("btnText"));
			}
			else if(bundleTmp.containsKey("editText")){
				//append操作
				searchEditText.setText(searchEditText.getText().append(bundleTmp.getString("editText")));
			}
			else if(bundleTmp.containsKey("editTextClear")){
				//append操作
				searchEditText.setText("");
			}
//			else if(bundleTmp.containsKey("questionList")){
//				ArrayList<String> searchList = bundleTmp.getStringArrayList("questionList");
//				
//				questionResult.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, searchList));
//				
//			}

		}
	};

	public void handlerSendMessage(String key,String value){
		Bundle mBundle = new Bundle();
		Message mMsg = new Message();

		mBundle.putString(key, value);
		mMsg.setData(mBundle);
		mHandler.sendMessage(mMsg);
	}
	
	public void bindListAdapter(ListAdapter listAdapter){
		questionResult.setAdapter(listAdapter);
	}
	
	
	public SQLiteDatabase getSQLiteDatabase(){
		if(questionDb==null){
			initData();
		}
		return questionDb;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//控制按钮文本
		/*new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					if(btnAdapter.getBtnStatus().equals(SearchButtonStatus.WAIT_TO_SPEAK)){
						handlerSendMessage("btnText","点我说话");
					}
					else if(btnAdapter.getBtnStatus().equals(SearchButtonStatus.IS_SPEAKING)){
						handlerSendMessage("btnText","请说话...");
					}
					else if(btnAdapter.getBtnStatus().equals(SearchButtonStatus.IS_SEARCHING)){
						handlerSendMessage("btnText","X");
					}
				}
				
			}
		}).start();*/
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		/*closeCamera();
		if (null != mAcc) {
			mAcc.stop();
		}
		mStopTrack = true;
		timer.cancel();*/

		// 停止说话和录音
		speakTools.stopSpeaking();
		recordTools.stopRecording();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/*// 销毁对象
		mFaceDetector.destroy();
		mStopTrack = true;
		closeCamera();

		timer.cancel();*/

		// 停止说话和录音
		speakTools.stopSpeaking();
		recordTools.stopRecording();
	}
}
