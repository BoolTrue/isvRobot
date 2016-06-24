package com.booltrue.ui;

import java.util.ArrayList;
import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.booltrue.adapter.HorizontalListViewAdapter;
import com.booltrue.base.BaseActivity;
import com.booltrue.isvRobot.R;
import com.booltrue.modle.AnswerImg;
import com.booltrue.tools.BmobTools;
import com.booltrue.utils.ToastUtil;

public class AnswerActivity extends BaseActivity implements OnTouchListener  {
	private final String TAG = "AnswerActivity";

	private ImageButton backMain = null;

	private ViewPager viewPager = null;
	//横向ListView
	//private HorizontalListView thumbnailView = null;

	private List<View> pagerViewList = new ArrayList<View>();

	private List<String> imgUrlList = new ArrayList<String>();
	//xUtils框架绑定网络图片之后返回的drawable
	private List<Drawable> imgDrawable = new ArrayList<Drawable>();

	private String objectId = "";

	private LayoutInflater mInflater;
	
	private LinearLayout thumbnailLinearLayout;
	
	private int listCount = 0 ;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		setContentView(R.layout.answer_activity);

		processExtraData();

		initData();
		initUI();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);

		initData();
		initUI();
	}



	@SuppressLint("HandlerLeak")
	private void initData() {

		//xUtils 初始化
		x.view().inject(this); 

		//Bmob数据初始化
		if(bmobTools==null){
			bmobTools = new BmobTools();
		}
		bmobTools.BmobInitialize(this);

		//根据问题ID查询答案配图
		if(objectId!=null&&!objectId.equals("")){

			QueryBmobObjects(new String[]{objectId},AnswerImg.QUESTION_ID);
		}

		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {

			}
		};

	}

	private void initUI() {
		backMain = (ImageButton)findViewById(R.id.back_main);

		backMain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AnswerActivity.this.finish();
			}
		});
		//缩略图
		//thumbnailView = (HorizontalListView)findViewById(R.id.thumbnailView);

		//图片切换
		viewPager = (ViewPager)findViewById(R.id.viewPager);
		
		thumbnailLinearLayout = (LinearLayout)findViewById(R.id.thumbnailLinearLayout);

	}

	private void processExtraData() {

		//语音合成初始化
		speakTools.initSpeakParams(AnswerActivity.this);
		//语音录入初始化
		recordTools.initRecordParams(AnswerActivity.this);

		mInflater = LayoutInflater.from(this);

		Intent intent = getIntent();

		objectId = intent.getStringExtra("objectId");
	}

	@Override
	public void QueryBmobObjects(String[] param, String columnKey) {
		BmobQuery<AnswerImg> mainQuery = bmobTools.getBmobQuery(AnswerImg.class);

		mainQuery.setLimit(50);

		mainQuery.addWhereEqualTo(columnKey, param[0]);

		mainQuery.order(AnswerImg.SEQUENCE);

		mainQuery.findObjects(AnswerActivity.this, new FindListener<AnswerImg>() {

			@Override
			public void onSuccess(List<AnswerImg> resultList) {

				if(resultList==null||resultList.size()==0){
					return;
				}

				Log.d(TAG, "resultList -->" + resultList.size());

				for(AnswerImg imgObj : resultList){

					View pageView = mInflater.inflate(R.layout.pager_view, null);

					TextView textView = (TextView)pageView.findViewById(R.id.pagerText);
					ImageView imgView = (ImageView)pageView.findViewById(R.id.pagerImg);
					
					textView.setText(imgObj.getAnswerText());

					String imgUrl = imgObj.getAnswerImg().getUrl();
					
					
					//绑定img控件
					ImageOptions imageOptions = new ImageOptions.Builder()
					.setRadius(DensityUtil.dip2px(5))//ImageView圆角半径
					.setCrop(true)// 如果ImageView的大小不是定义为wrap_content, 不要crop.
					.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
					.setLoadingDrawableId(R.drawable.img_loading)//加载中默认显示图片
					.setFailureDrawableId(R.drawable.img_fail)
					.build();
					
					//绑定img图片到ImageView里面
					x.image().bind(imgView, imgUrl.toString(),imageOptions,new CommonCallback<Drawable>() {
						
						@Override
						public void onSuccess(Drawable paramResultType) {
							imgDrawable.add(paramResultType);
						}
						
						@Override
						public void onFinished() {
							
						}
						
						@Override
						public void onError(Throwable paramThrowable, boolean paramBoolean) {
							
						}
						
						@Override
						public void onCancelled(CancelledException paramCancelledException) {
							
						}
					});
					
					
					pagerViewList.add(pageView);

					Log.d(TAG, imgUrl);
					imgUrlList.add(imgUrl);
				}
				//设定HorizontalListView 
				
				/*ListAdapter thumbnailViewAdapter = new HorizontalListViewAdapter(AnswerActivity.this,  imgUrlList);

				thumbnailView.setAdapter(thumbnailViewAdapter);

				thumbnailView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

						PagerAdapter pagerAdapter =  viewPager.getAdapter();

						int pagerCount = pagerAdapter.getCount();

						if(position > pagerCount||pagerCount==0){
							return;
						}

						viewPager.setCurrentItem(position);
						
					}
				});*/
				
				//加载缩略图
				
				listCount = 0;
				for(String imgUrl : imgUrlList){
					
					listCount++;
					
					if(imgUrl.equals("")){
						continue;
					}
					
					View view = mInflater.inflate(R.layout.horizontal_list_item,null);
					
					ImageView thumImg = (ImageView)view.findViewById(R.id.img_list_item);
					
					int w = getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
					int h = getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);
					
					//图片绑定设置
					
					ImageOptions imageOptions = new ImageOptions.Builder()
					.setRadius(DensityUtil.dip2px(10))//ImageView圆角半径
					.setSize(w,h)
					.setCrop(true)
					.setImageScaleType(ScaleType.FIT_CENTER)
					.setLoadingDrawableId(R.drawable.img_loading)//加载中默认显示图片
					.setFailureDrawableId(R.drawable.img_fail)
					.build();
					//异步加载图片
					x.image().bind(thumImg, imgUrl, imageOptions);
					
					//添加到布局中
					thumbnailLinearLayout.addView(view);
				}
				
				
				
				
				thumbnailLinearLayout.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						
						int width = thumbnailLinearLayout.getWidth();
						
						int itemCount = thumbnailLinearLayout.getChildCount();
						
						float itemWidth = width/itemCount*1.0f;
						float touchX = event.getX(0);
						
						int coverCount = (int) (touchX/itemWidth);
						
						viewPager.setCurrentItem(coverCount);
						
						for(int i=0;i<itemCount;i++){
							thumbnailLinearLayout.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
						}

						thumbnailLinearLayout.getChildAt(coverCount).setBackgroundColor(Color.argb(50, 0, 186, 255));
						
						Log.d(TAG, "RawX -->" + event.getX(0) + "    RawY -->" + event.getY(0));
						
						return false;
					}
				});

				
				//绑定adapter
				viewPager.setAdapter(new PagerAdapter() {

					@Override
					public boolean isViewFromObject(View arg0, Object arg1) {
						return arg0 == arg1;
					}

					@Override
					public int getCount() {

						return pagerViewList.size();
					}

					@Override
					public Object instantiateItem(ViewGroup container, int position) {

						((ViewPager) container).addView(pagerViewList.get(position));

						return pagerViewList.get(position);
					}

					@Override
					public void destroyItem(ViewGroup container, int position, Object object) {
						((ViewPager) container).removeView(pagerViewList.get(position));
					}
				});
				//绑定滑动监听
				viewPager.setOnPageChangeListener(new OnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {

					}

					@Override
					public void onPageScrolled(int position, float percent, int arg2) {

						if(percent == 0.0f){
							Log.d(TAG, String.valueOf(percent));
							stopSpeakAndRecord();

							if(pagerViewList.size()<position){
								return;
							}

							View view = pagerViewList.get(position);

							TextView text = (TextView)view.findViewById(R.id.pagerText);

							speakTools.speakSomething(text.getText().toString());
						}
					}

					@Override
					public void onPageScrollStateChanged(int state) {
						
					}
				});
				
				viewPager.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(thumbnailLinearLayout.getVisibility() == View.VISIBLE){
							thumbnailLinearLayout.setVisibility(View.GONE);
						}
						else{
							thumbnailLinearLayout.setVisibility(View.VISIBLE);
						}
						
					}
				});

			}

			@Override
			public void onError(int arg0, String msg) {
				ToastUtil.showTip(msg, AnswerActivity.this, Toast.LENGTH_SHORT);
				Log.d(TAG, msg);
			}
		});
	}
	
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		stopSpeakAndRecord();

		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

}
