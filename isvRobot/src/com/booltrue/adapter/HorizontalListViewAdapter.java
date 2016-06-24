package com.booltrue.adapter;


import java.util.ArrayList;
import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.booltrue.isvRobot.R;
import com.booltrue.utils.BitmapUtil;
import com.booltrue.utils.ToastUtil;

public class HorizontalListViewAdapter extends BaseAdapter{

	private Context mContext;  
	private LayoutInflater mInflater; 
	private int selectIndex = -1;

	//图片Url
	private List<String>  imageUrlList = null;

	public HorizontalListViewAdapter(Context context, List<String> imageUrlList){  
		this.mContext = context;  

		if(imageUrlList!=null){
			this.imageUrlList = imageUrlList; 
		}
		else{
			this.imageUrlList = new ArrayList<String>();
		}


		mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);  
	}

	@Override  
	public int getCount() {  
		//return mIconIDs.length;

		return imageUrlList.size();
	}

	@Override  
	public Object getItem(int position) {  
		return position;
	}

	@Override  
	public long getItemId(int position) {  
		return position;
	}

	@Override  
	public View getView(int position, View convertView, ViewGroup parent) {  

		final ViewHolder holder;  
		if(convertView==null){  
			holder = new ViewHolder();  
			convertView = mInflater.inflate(R.layout.horizontal_list_item, null);
			holder.mImage=(ImageView)convertView.findViewById(R.id.img_list_item);
			convertView.setTag(holder);
		}
		else{  
			holder=(ViewHolder)convertView.getTag();
		}

		//获取横向listView图片的宽高
		int w = mContext.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);
		int h = mContext.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);

		ImageOptions imageOptions = new ImageOptions.Builder()
		.setRadius(DensityUtil.dip2px(10))//ImageView圆角半径
		.setSize(w,h)
		.setCrop(true)
		.setImageScaleType(ScaleType.FIT_CENTER)
		.setLoadingDrawableId(R.drawable.img_loading)//加载中默认显示图片
		.setFailureDrawableId(R.drawable.img_fail)
		.build();

		//异步绑定图片
		x.image().bind(holder.mImage, imageUrlList.get(position),imageOptions);

		return convertView;
	}

	private static class ViewHolder {
		private ImageView mImage;
	}  
	public void setSelectIndex(int i){  
		selectIndex = i;  
	}  
}