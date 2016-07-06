package com.booltrue.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.booltrue.adapter.SearchButtonAdapter;
import com.booltrue.base.BaseActivity;
import com.booltrue.isvRobot.R;
import com.booltrue.listener.SearchEditTextListener;
import com.booltrue.listener.VoiceButtonOnTouchListener;
import com.booltrue.modle.QuestionBmob;
import com.booltrue.tools.BmobTools;
import com.booltrue.tools.UnderstandTools;
import com.booltrue.tools.WakeTools;
import com.booltrue.ui.view.TypegifView;
import com.booltrue.utils.SessionUtil;
import com.booltrue.utils.ToastUtil;


public class MainActivity extends BaseActivity implements OnTouchListener {

	private String TAG = "主窗口";
	//基本UI
	//private Button searchBtn = null;
	private EditText searchEditText = null;
	private ListView questionResult = null;
	private ImageButton imgVoiceBtn = null;


	private Toast mToast = null;

	//语义理解
	public UnderstandTools understandTools = new UnderstandTools();
	//语音唤醒
	public WakeTools wakeTools = new WakeTools();
	

	private SearchButtonAdapter btnAdapter = null;
	private SearchEditTextListener textListener = null;
	private VoiceButtonOnTouchListener btnOnTouchListener = null;

	//SQLite数据库
	/*private SQLiteDatabase questionDb = null;

	private DBHelper dbHelper = null;*/

	//答案区域
	private ScrollView answerArea = null;
	private Button answerBack = null;
	private TextView questionAnswer = null;
	private TextView imgMsgLink = null;



	//待机图片
	//private ImageView waitImg = null;
	private TypegifView gifView = null;
	private LinearLayout mainLayout = null;
	
	//播放音效
	private SoundPool soundPool;
	
	private Button billCheck;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		//语音合成初始化
		speakTools.initSpeakParams(this);
		//语音录入初始化
		recordTools.initRecordParams(this);
		//语义理解初始化
		understandTools.initUnderstandParams(this);
		//语音唤醒初始化
		wakeTools.initWakeTools(this);
		
		//监听初始化
		btnAdapter = new SearchButtonAdapter(this);
		textListener = new SearchEditTextListener(this);
		
		btnOnTouchListener = new VoiceButtonOnTouchListener(this);
		
		
		//人脸识别 
		/*nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		buffer = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		mAcc = new Accelerometer(MainActivity.this);

		mFaceDetector = FaceDetector.createDetector(MainActivity.this, null);*/

		//控件初始化
		initUI();
		initData();

		wakeTools.startWakeListener();
		
	}
	//初始化数据库
	@SuppressLint("HandlerLeak")
	private void initData() {
		
		if(bmobTools==null){
			bmobTools = new BmobTools();
		}
		
		//Bmob数据初始化
		bmobTools.BmobInitialize(this);
		

		/*//questionDb = this.openOrCreateDatabase(DBHelper.DBName, Context.MODE_PRIVATE, null);
		//使用dbHelper创建数据库 和 表
		dbHelper = new DBHelper(this);

		Log.d(TAG, "dbHelper --> getWritableDatabase()");
		//获取只读数据库
		questionDb = dbHelper.getReadableDatabase();*/
		
		//Handler初始化
		mHandler = new Handler(){
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
				else if(bundleTmp.containsKey("showWaitImg")){
					waitImgBack();
				}
				else if(bundleTmp.containsKey("questionAnswer")){
					questionAnswer.setText("    " + bundleTmp.getString("questionAnswer").trim().replaceAll("/?", ""));
				}
			}
		};
	}

	@SuppressLint({ "ShowToast", "ClickableViewAccessibility" })
	private void initUI() {
		//searchBtn = (Button)findViewById(R.id.searchBtn);
		searchEditText = (EditText)findViewById(R.id.searchEditText);
		questionResult = (ListView)findViewById(R.id.questionList);
		imgVoiceBtn = (ImageButton)findViewById(R.id.imgVoiceBtn);
		
		//声音播放
		soundPool= new SoundPool(10,AudioManager.STREAM_SYSTEM,5);
		soundPool.load(this,R.raw.unlock,1);

		
		//待机页与主界面切换
		//waitImg = (ImageView)findViewById(R.id.waitImg);
		mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
		gifView = (TypegifView)findViewById(R.id.gifView);
		//播放动画
		gifView.setStart();
		/*gifView.setScaleX(0.5f);
		gifView.setScaleY(0.5f);*/
		
		gifView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showMainLayout();
				
			}
		});
		

		//答案区域
		answerArea = (ScrollView)findViewById(R.id.answerArea);
		answerBack = (Button)findViewById(R.id.answerBack);
		questionAnswer = (TextView)findViewById(R.id.questionAnswer);
		//设置答案区域返回按钮监听
		answerBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopSpeakAndRecord();
				answerBack();
				SessionUtil.reSetTimeOut();//重置超时
			}
		});
		imgMsgLink = (TextView)findViewById(R.id.imgMsgLink);

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		//设置自定义按键监听
		//searchBtn.setOnClickListener(btnAdapter);

		//设置文本框监听
		searchEditText.addTextChangedListener(textListener);

		//设置ImageButton监听
		imgVoiceBtn.setOnClickListener(btnAdapter);
		
		/*imgVoiceBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View searchBtn, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					
					searchBtn.setBackgroundResource(R.drawable.voice_button);
					
					//隐藏答案区域
					((MainActivity)mContext).answerBack();
					//停止说话
					baseActivity.stopSpeakAndRecord();
					
					//震动提醒
					baseActivity.vibratorService(100l);
					
					//开始讲话
					//清空文本
					baseActivity.handlerSendMessage("editTextClear", "");
					
					baseActivity.recordTools.startRecord();
					
					SessionUtil.reSetTimeOut();
					
				}
				else if(event.getAction() == MotionEvent.ACTION_UP){
					
					searchBtn.setBackgroundResource(R.drawable.voice_button_touch);
					
					baseActivity.stopSpeakAndRecord();
				}
				
				
				
				return false;
			}
		});*/
		
		//imgVoiceBtn.setOnTouchListener(btnOnTouchListener);
		
		//票据识别
		//billCheck = (Button)findViewById(R.id.billCheck);
	}

	public void bindListAdapter(SimpleAdapter arrayAdapter,final List<Map<String,Object>> resultList ){
		
		questionResult.setAdapter(arrayAdapter);

		questionResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				stopSpeakAndRecord();

				Log.d(TAG, "ItemClickId -->" + String.valueOf(id));
				Log.d(TAG, "ItemClickPosition -->" + String.valueOf(position));

				Map<String,Object> result = resultList.get(position);
				
				String questionId = result.get(QuestionBmob.OBJECT_ID).toString().trim();
				String answer = result.get(QuestionBmob.QUESTION_ANSWER).toString().trim();
				Boolean isImgMsg = (Boolean) result.get(QuestionBmob.IS_IMG_MSG);
				
				if(isImgMsg==null){
					isImgMsg = false;
				}
				
				//有答案
				if(answer !=null && !answer.equals("")){
					
					showAndSpeakAnswer(answer,isImgMsg,questionId);
					
				}//没有答案
				else{
					
					newThreadSpeak("未找到该问题的答案。");
					
				}
			}
		});
	}
	
	/**
	 * 多行条件查询
	 * 
	 * @param 查询数组
	 * @param 列字段
	 * @return
	 */
	public void QueryBmobObjects(String[] param , String columnKey ){
		
		BmobQuery<QuestionBmob> mainQuery =bmobTools.getBmobQuery(QuestionBmob.class);
		
		mainQuery.setLimit(50);
		
		//模糊查询
		List<BmobQuery<QuestionBmob>> andQueryList = new ArrayList<BmobQuery<QuestionBmob>>();
		
		for(String tmp : param){
			BmobQuery<QuestionBmob> andQuery = new BmobQuery<QuestionBmob>();
			
			andQuery.addWhereContains(columnKey, tmp);
			
			andQueryList.add(andQuery);
		}
		
		mainQuery.and(andQueryList);
		
		mainQuery.findObjects(this, new FindListener<QuestionBmob>() {
			
			@Override
			public void onSuccess(List<QuestionBmob> successList) {
				
				ToastUtil.showTip("总共查到"+ successList.size() + "条数据", MainActivity.this, Toast.LENGTH_SHORT);

				Log.d(TAG, "总共查到"+ successList.size() + "条数据");

				//首先停止说话
				stopSpeakAndRecord();

				if(successList.size()<=0){

					newThreadSpeak("暂时没有找到结果，试试其他关键词吧。");
				}
				else{
					newThreadSpeak("请选择你想查询的问题。");
				}

				List<Map<String,Object>> resultList =  bmobTools.parseData(successList);
				
				SimpleAdapter listViewAdapter = new SimpleAdapter(MainActivity.this, resultList, R.layout.question_list_item,
										new String[]{QuestionBmob.QUESTION_TITLE},
										new int[]{R.id.listTextView});
				
				
				bindListAdapter(listViewAdapter,resultList);
				
			}
			
			@Override
			public void onError(int errorCode, String msg) {
				ToastUtil.showTip("BmobError :" + msg +"  errorCode:" + errorCode, MainActivity.this, Toast.LENGTH_SHORT);

				Log.e(TAG, msg);
			}
		});
		
	}


	public void showAnswer(){
		questionResult.setVisibility(View.GONE);
		answerArea.setVisibility(View.VISIBLE);

	}
	public void answerBack(){
		questionResult.setVisibility(View.VISIBLE);
		answerArea.setVisibility(View.GONE);
	}
	
	public void showMainLayout(){
		gifView.setVisibility(View.GONE);
		mainLayout.setVisibility(View.VISIBLE);
		
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		float volumnRatio = volumnCurrent / audioMaxVolumn;
		
		soundPool.play(1,volumnRatio, volumnRatio, 0, 0, 1);
		
		SessionUtil.reSetTimeOut();//重置超时
		
		wakeTools.stopWeakUp();
	}
	public void waitImgBack(){
		
		stopSpeakAndRecord();
		
		mainLayout.setVisibility(View.GONE);
		gifView.setVisibility(View.VISIBLE);
		
		wakeTools.startWakeListener();
	}
	
	
	
	public void showAndSpeakAnswer(String answer){
		showAndSpeakAnswer(answer,false,null);
	}
	
	public void showAndSpeakAnswer(String answer,Boolean isImgMsg,final String objectId){
		
		handlerSendMessage("questionAnswer",answer);
		//说出答案
		newThreadSpeak(answer);
		//显示答案
		showAnswer();
		
		if(isImgMsg){
			imgMsgLink.setVisibility(View.VISIBLE);
			
			//跳转到图文答案界面
			imgMsgLink.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(),AnswerActivity.class);
					intent.putExtra("objectId", objectId);
					startActivity(intent);
				}
			});
		}
		
		SessionUtil.reSetTimeOut();
	}
	
	

	

	public boolean isPlay(){
		return speakTools.isPlay;
	}
	public boolean isRecord(){
		return recordTools.isRcord;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		SessionUtil.reSetTimeOut();
		
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		//检测用户操作是否超时（一定时间不操作则自动进入等待界面）
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while(true){
					//如果主界面正在显示
					if(mainLayout.getVisibility() != View.GONE){
						try {
							//每隔一秒做一次检查
							Thread.sleep(1000l);
							long timeOut = System.currentTimeMillis() - SessionUtil.doNotAnyActionTime;
							//如果时间超时15秒则返回待机界面
							if(timeOut >= 20000&&!speakTools.isPlay&&!recordTools.isRcord){
								
								handlerSendMessage("showWaitImg","");
								
							}
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			}
		}).start();

	}

	@Override
	protected void onPause() {
		super.onPause();

		// 停止说话和录音
		speakTools.stopSpeaking();
		recordTools.stopRecording();
		understandTools.stopUnderstand();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		//销毁语音语义对象
		speakTools.destory();
		recordTools.destory();
		understandTools.destory();
	}
	
	
}
