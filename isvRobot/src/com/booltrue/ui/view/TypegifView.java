package com.booltrue.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.booltrue.isvRobot.R;

public class TypegifView extends ImageView implements Runnable {
	gifOpenHelper gHelper;
	private boolean isStop = true;
	int delta;
	String title;

	Bitmap bmp;

	Context mContext = null;

	private int screenWidth ;
	private int screenHeight;

	// construct - refer for java
	public TypegifView(Context context) {
		this(context, null);

	}

	// construct - refer for xml
	public TypegifView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();


		//添加属性
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.gifView);
		int n = ta.getIndexCount();

		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);

			switch (attr) {
			case R.styleable.gifView_src:
				int id = ta.getResourceId(R.styleable.gifView_src, 0);
				setSrc(id);
				break;

			case R.styleable.gifView_delay:
				int idelta = ta.getInteger(R.styleable.gifView_delay, 1);
				setDelta(idelta);
				break;

			case R.styleable.gifView_stop:
				boolean sp = ta.getBoolean(R.styleable.gifView_stop, false);
				if (!sp) {
					setStop();
				}
				break;
			}

		}

		ta.recycle();
	}

	/**
	 * 设置停止
	 * 
	 * @param stop
	 */
	public void setStop() {
		isStop = false;
	}

	/**
	 * 设置启动
	 */
	public void setStart() {
		isStop = true;

		Thread updateTimer = new Thread(this);
		updateTimer.start();
	}

	/**
	 * 通过下票设置第几张图片显示
	 * @param id
	 */
	public void setSrc(int id) {

		gHelper = new gifOpenHelper();
		gHelper.read(TypegifView.this.getResources().openRawResource(id));
		bmp = gHelper.getImage();// 得到第一张图片
	}

	public void setDelta(int is) {
		delta = is;
	}

	// to meaure its Width & Height
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int height = View.MeasureSpec.getSize(heightMeasureSpec);    
		int width = View.MeasureSpec.getSize(widthMeasureSpec); 

		setMeasuredDimension(measureWidth(width),
				measureHeight(height));
	}

	private int measureWidth(int measureSpec) {
		return gHelper.getWidth();
	}

	private int measureHeight(int measureSpec) {
		return gHelper.getHeigh();
	}

	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		//canvas.drawBitmap(bmp, 0, 0, new Paint());

		//canvas.drawBitmap(bmp, getMatrix(), new Paint());

		RectF rectF = new RectF(0, 0, screenWidth, screenHeight);
		canvas.drawBitmap(bmp, null, rectF, null);

		bmp = gHelper.nextBitmap();

	}

	public void run() {
		// TODO Auto-generated method stub
		while (isStop) {
			try {
				this.postInvalidate();
				Thread.sleep(gHelper.nextDelay() / delta);
			} catch (Exception ex) {

			}
		}
	}

}
