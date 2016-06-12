package com.booltrue.ui;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
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

import com.booltrue.isvRobot.R;
import com.booltrue.listener.SearchButtonAdapter;
import com.booltrue.listener.SearchEditTextListener;
import com.booltrue.modle.QuestionBmob;
import com.booltrue.tools.QuestionBmobTools;
import com.booltrue.tools.RecordTools;
import com.booltrue.tools.SpeakTools;
import com.booltrue.tools.UnderstandTools;
import com.booltrue.ui.view.TypegifView;
import com.booltrue.utils.SessionUtil;
import com.iflytek.cloud.FaceDetector;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.util.Accelerometer;


public class MainActivity extends Activity implements OnTouchListener {

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
	//语义理解
	public UnderstandTools understandTools = new UnderstandTools();

	private SearchButtonAdapter btnAdapter = null;
	private SearchEditTextListener textListener = null;

	//SQLite数据库
	/*private SQLiteDatabase questionDb = null;

	private DBHelper dbHelper = null;*/

	//答案区域
	private ScrollView answerArea = null;
	private Button answerBack = null;
	private TextView questionAnswer = null;


	//人脸识别
	// Camera nv21格式预览帧的尺寸，默认设置640*480
	private int PREVIEW_WIDTH = 320;
	private int PREVIEW_HEIGHT = 240;
	// 预览帧数据存储数组和缓存数组
	private byte[] nv21;
	private byte[] buffer;
	//摄像头
	private Camera mCamera;
	private int mCameraId = CameraInfo.CAMERA_FACING_FRONT;
	private boolean mStopTrack;

	// 加速度感应器，用于获取手机的朝向
	private Accelerometer mAcc;
	// FaceDetector对象，集成了离线人脸识别：人脸检测、视频流检测功能
	private FaceDetector mFaceDetector;
	//使用两个SurfaceView绘制摄像头捕获的影像和识别到的人脸矩阵
	private SurfaceView mPreviewSurface;
	private SurfaceView mFaceSurface;
	// 缩放矩阵
	private Matrix mScaleMatrix = new Matrix();


	//Bmob操作对象
	public QuestionBmobTools questionBmobTools = null;

	//待机图片
	//private ImageView waitImg = null;
	private TypegifView gifView = null;
	private LinearLayout mainLayout = null;
	private long doNotAnyActionTime = 0;//没有进行任何操作自动进入待机界面
	
	//播放音效
	private SoundPool soundPool;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 

		//语音合成初始化
		speakTools.initSpeakParams(MainActivity.this);
		//语音录入初始化
		recordTools.initRecordParams(MainActivity.this);
		//语义理解初始化
		understandTools.initUnderstandParams(MainActivity.this);

		btnAdapter = new SearchButtonAdapter(this);
		textListener = new SearchEditTextListener(this);


		//人脸识别 
		nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		buffer = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
		mAcc = new Accelerometer(MainActivity.this);

		mFaceDetector = FaceDetector.createDetector(MainActivity.this, null);

		//控件初始化
		initUI();
		initData();

		SessionUtil.reSetSession();

		Setting.setShowLog(false);

	}
	//初始化数据库
	private void initData() {

		questionBmobTools = new QuestionBmobTools();
		//Bmob数据初始化
		questionBmobTools.BmobInitialize(this);


		/*//questionDb = this.openOrCreateDatabase(DBHelper.DBName, Context.MODE_PRIVATE, null);
		//使用dbHelper创建数据库 和 表
		dbHelper = new DBHelper(this);

		Log.d(TAG, "dbHelper --> getWritableDatabase()");
		//获取只读数据库
		questionDb = dbHelper.getReadableDatabase();*/


	}

	@SuppressLint("ShowToast")
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
				AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
				
				float volumnRatio = volumnCurrent / audioMaxVolumn;
				
				soundPool.play(1,volumnRatio, volumnRatio, 0, 0, 1);
				reSetTimeOut();//重置超时
				
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
				
				reSetTimeOut();//重置超时
			}
		});

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		//设置自定义按键监听
		//searchBtn.setOnClickListener(btnAdapter);

		//设置文本框监听
		searchEditText.addTextChangedListener(textListener);

		//设置ImageButton监听
		imgVoiceBtn.setOnClickListener(btnAdapter);



		//人脸识别
		/*mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
		mFaceSurface = (SurfaceView) findViewById(R.id.sfv_face);

		mPreviewSurface.getHolder().addCallback(mPreviewCallback);

		mFaceSurface.setZOrderOnTop(true);
		mFaceSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		//设置显示界面的大小
		setSurfaceSize();*/
	}

	//打开摄像头，捕获头像
	private void openCamera(){
		if(mCamera != null){
			return ;
		}

		if(!checkCameraPermission()){
			mStopTrack = true;
			return;
		}
		mStopTrack = false;

		// 只有一个摄相头，打开后置
		if (Camera.getNumberOfCameras() == 1) {
			mCameraId = CameraInfo.CAMERA_FACING_BACK;
		}

		try{
			mCamera = Camera.open(mCameraId);

		}
		catch(Exception e){
			closeCamera();
			return;
		}



		//如果是前置摄像头
		if (CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
		} else {
		}


		//初始化摄像头参数
		Parameters params = mCamera.getParameters();
		params.setPreviewFormat(ImageFormat.NV21);
		params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
		mCamera.setParameters(params);

		//mCamera.setDisplayOrientation(90);


		// 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
		int direction = Accelerometer.getDirection();

		switch(direction){

		case 0:
			// 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
			mCamera.setDisplayOrientation(0);
			break;
		case 1:
			// 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
			mCamera.setDisplayOrientation(90);
			break;
		case 2:
			// 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
			mCamera.setDisplayOrientation(90);
			mCamera.setDisplayOrientation(180);
			break;
		case 3:
			// 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
			mCamera.setDisplayOrientation(270);
			break;
		}

		//时事截获相机捕获的画面帧数据
		mCamera.setPreviewCallback(new PreviewCallback() {

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				System.arraycopy(data, 0, nv21, 0, data.length);

			}
		});
		//最后把数据显示到mPreviewSurface里面
		try {
			mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	//关闭摄像头
	private void closeCamera(){
		if (null != mCamera) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;

			mStopTrack = true;
		}
	}

	//检验摄像头的使用权限
	private boolean checkCameraPermission() {
		int status = checkPermission(permission.CAMERA, Process.myPid(), Process.myUid());
		if (PackageManager.PERMISSION_GRANTED == status) {
			return true;
		}
		return false;
	}

	//SurfaceView回调
	private Callback mPreviewCallback = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			closeCamera();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			openCamera();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			closeCamera();
			mScaleMatrix.setScale(width/(float)PREVIEW_HEIGHT, height/(float)PREVIEW_WIDTH);
			openCamera();
		}
	};

	private void setSurfaceSize() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PREVIEW_HEIGHT, (int) (PREVIEW_HEIGHT * PREVIEW_HEIGHT / (float)PREVIEW_WIDTH));

		mPreviewSurface.setLayoutParams(params);
		mFaceSurface.setLayoutParams(params);
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
			else if(bundleTmp.containsKey("showWaitImg")){
				waitImgBack();
			}
			else if(bundleTmp.containsKey("questionAnswer")){
				questionAnswer.setText(bundleTmp.getString("questionAnswer"));
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

	public void bindListAdapter(SimpleAdapter arrayAdapter,final List<Map<String,String>> resultList ){

		questionResult.setAdapter(arrayAdapter);

		questionResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				stopSpeakAndRecord();

				Log.d(TAG, "ItemClickId -->" + String.valueOf(id));
				Log.d(TAG, "ItemClickPosition -->" + String.valueOf(position));

				String answer = resultList.get(position).get(QuestionBmob.QUESTION_ANSWER);

				//有答案
				if(answer !=null && !answer.equals("")){

					showAndSpeakAnswer(answer);
				}
				else{
					newThreadSpeak("未找到该问题的答案。");
				}
				
				//questionDb.execSQL("select " + QuestionColumn.QuestionAnswer + " from Question where " + QuestionColumn.ID + "=" + id);

				/*String args[] = {String.valueOf(id)};
				Cursor cursor =  questionDb.rawQuery("select " + QuestionColumn.QuestionAnswer 
						+ " from Question where " 
						+ QuestionColumn.ID + "= ?", args);

				//有答案
				if(cursor.moveToNext()){
					String questionAnswerStr = cursor.getString(0);

					questionAnswer.setText(questionAnswerStr);

					//说出答案
					newThreadSpeak(questionAnswerStr);
					//显示答案
					showAnswer();
				}
				else{
					newThreadSpeak("未找到该问题的答案。");
				}*/
			}
		});
	}

	/*public SQLiteDatabase getSQLiteDatabase(){
		if(questionDb==null){
			initData();
		}
		return questionDb;
	}*/

	public QuestionBmobTools getBmobTools(){
		if(questionBmobTools==null){
			initData();
		}

		return questionBmobTools;
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
	}
	public void waitImgBack(){
		mainLayout.setVisibility(View.GONE);
		gifView.setVisibility(View.VISIBLE);
	}
	
	public void stopSpeakAndRecord(){
		speakTools.stopSpeaking();
		recordTools.stopRecording();
	}
	
	public void reSetTimeOut(){
		doNotAnyActionTime =  System.currentTimeMillis();
	}
	
	public void showAndSpeakAnswer(String answer){
		
		handlerSendMessage("questionAnswer",answer);
		//说出答案
		newThreadSpeak(answer);
		//显示答案
		showAnswer();
		
		reSetTimeOut();
	}

	public void newThreadSpeak(final String speakStr){

		new Thread(new Runnable() {

			@Override
			public void run() {
				speakTools.speakSomething(speakStr);

			}
		}).start();
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
		reSetTimeOut();
		
		return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (null != mAcc) {
			mAcc.start();
		}

		mStopTrack = false;

		//实时捕获数据

		/*new Thread(new Runnable() {
			@Override
			public void run() {
				while(!mStopTrack){

					//如果相机截获的帧数据为null 则跳到下一个循环
					if(nv21 == null){
						Log.d(TAG, "没有获取到帧数据");
						continue;
					}

					//nv21的数据临时存到buffer中
					synchronized (nv21) {
						System.arraycopy(nv21, 0, buffer, 0, nv21.length);
					}

					// 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
					int direction = Accelerometer.getDirection();

					//Log.d("direction","direction-->" + String.valueOf(direction));

					//判断是否是前置摄像头
					boolean frontCamera = (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId);
					// 前置摄像头预览显示的是镜像，需要将手机朝向换算成摄相头视角下的朝向。
					// 转换公式：a' = (360 - a)%360，a为人眼视角下的朝向（单位：角度）
					if (frontCamera) {
						// SDK中使用0,1,2,3,4分别表示0,90,180,270和360度
						direction = (4 - direction)%4;
					}

					if(mFaceDetector == null) {
		 *//**
		 * 离线视频流检测功能需要单独下载支持离线人脸的SDK
		 * 请开发者前往语音云官网下载对应SDK
		 *//*
						break;
					}
					//第一个参数 当期的帧数据
					//倒数第二个参数 设置脸部识别是聚焦还是检测 
					//返回脸部识别的结果
					String result = mFaceDetector.trackNV21(buffer, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1, direction);
					//将结果转换为脸型矩阵数组（可以同时识别多个脸）
					FaceRect[] faces = ParseResult.parseResult(result);

					//Log.d("face","faces-->" + String.valueOf(faces.length));
					//将当前的SurfaceView
					Canvas canvas = mFaceSurface.getHolder().lockCanvas();
					if (null == canvas) {
						continue;
					}

					canvas.drawColor(0, PorterDuff.Mode.CLEAR);
					canvas.setMatrix(mScaleMatrix);

					if(faces!=null){
						Log.d(TAG, "检测到人脸数量："+ faces.length);
					}


					if (null != faces && faces.length >0 && frontCamera == (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId)) {
						for (FaceRect face: faces) {
							face.bound = FaceUtil.RotateDeg90(face.bound, PREVIEW_WIDTH, PREVIEW_HEIGHT);
							if (face.point != null) {
								for (int i = 0; i < face.point.length; i++) {
									face.point[i] = FaceUtil.RotateDeg90(face.point[i], PREVIEW_WIDTH, PREVIEW_HEIGHT);
								}
							}
							//FaceUtil.drawFaceRect(canvas, face, PREVIEW_WIDTH, PREVIEW_HEIGHT, frontCamera, false);
						}


						//输出人脸的数量
						//handlerSendMessage("textViewText",String.valueOf(faces.length) + "   屏幕方向:" + direction);

						//已经检测到人脸 并记录下时间
						SessionUtil.findFaceTime = System.currentTimeMillis();

						if(!SessionUtil.isSayHello){

							newThreadSpeak(SessionUtil.speakStr);

							SessionUtil.isSayHello = true;

						}
					}
					else{ //没有检测到人脸

						//如果机器人没在录音或者机器人没在说话 则到了静默时间之后就会说再见语句
						if(SessionUtil.findFaceTime!=0&&(System.currentTimeMillis() - SessionUtil.findFaceTime)>=15000&&SessionUtil.isSayHello&&!SessionUtil.isSayBye&&!speakTools.isPlay&&!recordTools.isRcord){
							speakTools.speakSomething("再见，欢迎下次光临!");
							//重新设置session会话工具
							SessionUtil.reSetSession();
						}
						//如果机器人正在录音或者正在说话 则仍算会话时间 
						if(speakTools.isPlay||recordTools.isRcord){
							SessionUtil.findFaceTime = System.currentTimeMillis();
						}

					}
					//最后将绘制好的canvas显示到SurfaceView中
					//mFaceSurface.getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}).start();*/
		
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
							long timeOut = System.currentTimeMillis() - doNotAnyActionTime;
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
		closeCamera();
		if (null != mAcc) {
			mAcc.stop();
		}
		mStopTrack = true;

		// 停止说话和录音
		speakTools.stopSpeaking();
		recordTools.stopRecording();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 销毁对象
		mFaceDetector.destroy();
		mStopTrack = true;
		closeCamera();

		// 停止说话和录音
		speakTools.stopSpeaking();
		recordTools.stopRecording();
		//销毁语音语义对象
		speakTools.destory();
		recordTools.destory();
		understandTools.destory();
	}
	
	
}
