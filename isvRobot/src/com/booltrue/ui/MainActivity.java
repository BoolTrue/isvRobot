package com.booltrue.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.booltrue.isvRobot.R;
import com.booltrue.listener.SearchButtonAdapter;
import com.booltrue.listener.SearchEditTextListener;
import com.booltrue.modle.QuestionColumn;
import com.booltrue.tools.DBHelper;
import com.booltrue.tools.RecordTools;
import com.booltrue.tools.SpeakTools;
import com.booltrue.utils.SessionUtil;
import com.iflytek.cloud.Setting;


public class MainActivity extends Activity {
	
	private String TAG = "主窗口";
	//基本UI
	//private Button searchBtn = null;
	private EditText searchEditText = null;
	private ListView questionResult = null;
	private ImageButton imgVoiceBtn = null;
	
	
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
	
	//答案区域
	private LinearLayout answerArea = null;
	private Button answerBack = null;
	private TextView questionAnswer = null;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
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
		//searchBtn = (Button)findViewById(R.id.searchBtn);
		searchEditText = (EditText)findViewById(R.id.searchEditText);
		questionResult = (ListView)findViewById(R.id.questionList);
		imgVoiceBtn = (ImageButton)findViewById(R.id.imgVoiceBtn);
		
		//答案区域
		answerArea = (LinearLayout)findViewById(R.id.answerArea);
		answerBack = (Button)findViewById(R.id.answerBack);
		questionAnswer = (TextView)findViewById(R.id.questionAnswer);
		

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		//设置自定义按键监听
		//searchBtn.setOnClickListener(btnAdapter);
		
		//设置文本框监听
		searchEditText.addTextChangedListener(textListener);
		
		//设置ImageButton监听
		imgVoiceBtn.setOnClickListener(btnAdapter);
		
		//设置答案区域返回按钮监听
		answerBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopSpeakAndRecord();
				answerBack();
			}
		});
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
				//searchBtn.setText(bundleTmp.getString("btnText"));
			}
			else if(bundleTmp.containsKey("editText")){
				//append操作
				searchEditText.setText(searchEditText.getText().append(bundleTmp.getString("editText")));
			}
			else if(bundleTmp.containsKey("editTextClear")){
				//append操作
				searchEditText.setText("");
			}

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
		
		questionResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				//stopSpeakAndRecord();
				
				Log.d(TAG, "ItemClickId -->" + String.valueOf(id));
				
				//questionDb.execSQL("select " + QuestionColumn.QuestionAnswer + " from Question where " + QuestionColumn.ID + "=" + id);
				
				String args[] = {String.valueOf(id)};
				Cursor cursor =  questionDb.rawQuery("select " + QuestionColumn.QuestionAnswer 
								+ " from Question where " 
								+ QuestionColumn.ID + "= ?", args);
				
				
				//有答案
				if(cursor.moveToNext()){
					String questionAnswerStr = cursor.getString(0);
					
					questionAnswer.setText(questionAnswerStr);
					
					//显示答案
					showAnswer();
					
					speakTools.speakSomething(questionAnswerStr);
				}
			
			}
			
		});
	}
	
	
	public SQLiteDatabase getSQLiteDatabase(){
		if(questionDb==null){
			initData();
		}
		return questionDb;
	}
	
	public void showAnswer(){
		questionResult.setVisibility(View.GONE);
		answerArea.setVisibility(View.VISIBLE);
		
	}
	public void answerBack(){
		questionResult.setVisibility(View.VISIBLE);
		answerArea.setVisibility(View.GONE);
	}
	
	public void stopSpeakAndRecord(){
		speakTools.stopSpeaking();
		recordTools.stopRecording();
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
