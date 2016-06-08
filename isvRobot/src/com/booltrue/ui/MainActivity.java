package com.booltrue.ui;

import java.io.IOException;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
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
import com.booltrue.utils.FaceRect;
import com.booltrue.utils.FaceUtil;
import com.booltrue.utils.ParseResult;
import com.booltrue.utils.SessionUtil;
import com.iflytek.cloud.FaceDetector;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.util.Accelerometer;


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


	//人脸识别
	// Camera nv21格式预览帧的尺寸，默认设置640*480
	private int PREVIEW_WIDTH = 640;
	private int PREVIEW_HEIGHT = 480;
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

		//人脸识别
		mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
		mFaceSurface = (SurfaceView) findViewById(R.id.sfv_face);

		mPreviewSurface.getHolder().addCallback(mPreviewCallback);

		mFaceSurface.setZOrderOnTop(true);
		mFaceSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		//设置显示界面的大小
		setSurfaceSize();
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

				stopSpeakAndRecord();

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

					//说出答案
					newThreadSpeak(questionAnswerStr);
					//显示答案
					showAnswer();
				}
				else{
					newThreadSpeak("未找到该问题的答案。");
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (null != mAcc) {
			mAcc.start();
		}

		mStopTrack = false;

		//实时捕获数据

		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!mStopTrack){

					//如果相机截获的帧数据为null 则跳到下一个循环
					if(nv21 == null){
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
						/**
						 * 离线视频流检测功能需要单独下载支持离线人脸的SDK
						 * 请开发者前往语音云官网下载对应SDK
						 */
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
							
							newThreadSpeak("你好我是小K，请 点我 问问题吧");
							
							SessionUtil.isSayHello = true;

						}
					}
					else{ //没有检测到人脸

						//如果机器人没在录音或者机器人没在说话 则到了静默时间之后就会说再见语句
						if(SessionUtil.findFaceTime!=0&&(System.currentTimeMillis() - SessionUtil.findFaceTime)>=15000&&SessionUtil.isSayHello/*&&!SessionUtil.isSayBye*/&&!speakTools.isPlay&&!recordTools.isRcord){
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
					mFaceSurface.getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}).start();

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
