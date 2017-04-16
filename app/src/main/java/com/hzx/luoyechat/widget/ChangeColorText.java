package com.hzx.luoyechat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.hzx.luoyechat.R;


public class ChangeColorText extends View {

	int mColor = 0xFF45C01A;
	String mText = "落叶";
	int mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
			12, getResources().getDisplayMetrics());

	Canvas mCanvas;
	Bitmap mBitmap;
	Paint mPaint;
	Bitmap mIconBitma;

	float mAlpha;
	Rect mIconRect;
	Rect mTextBound;

	Paint mTextPaint;

	public ChangeColorText(Context context) {
		super(context);
	}

	/**获取自定义属性的值*/
	public ChangeColorText(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ChangeColorText);

		// 获取各个自定义属性的值
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.ChangeColorText_icon:
				BitmapDrawable drawable = (BitmapDrawable) a.getDrawable(attr);
				mIconBitma = drawable.getBitmap();
				break;
			case R.styleable.ChangeColorText_color:
				mColor = a.getColor(attr, 0xFF45C01A);
				break;
			case R.styleable.ChangeColorText_text:
				mText = a.getString(attr);
				break;

			case R.styleable.ChangeColorText_textSize:
				mTextSize = (int) a.getDimension(attr, TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
								getResources().getDisplayMetrics()));
				break;
			default:
				break;
			}
		}

		a.recycle(); // 这里一定要回收掉

		// 初始化一些变量
		mTextBound = new Rect();
		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(0xff555555);

		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 测量图标的宽高 
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int iconWidth = Math.min(getMeasuredWidth() - getPaddingLeft()
				- getPaddingRight(), getMeasuredHeight() - getPaddingTop()
				- getPaddingBottom() - mTextBound.height());

		int left = getMeasuredWidth() / 2 - iconWidth / 2;
		int top = (getMeasuredHeight() - mTextBound.height()) / 2 - iconWidth
				/ 2;
		mIconRect = new Rect(left, top, left + iconWidth, top + iconWidth);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 绘制控件
		canvas.drawBitmap(mIconBitma, null, mIconRect,null);
		
		int alpha = (int) Math.ceil(255 * mAlpha);
		
		// 内存去准备mBitmap , setAlpha , 纯色 ，xfermode ， 图标
		 setUpTargetBitmap(alpha);
		 
		 // 1、绘制原文本
		 drawSourceText(canvas,alpha);
		 // 2、绘制变色的文本
		 drawTargetText(canvas,alpha);
		 
		 
		 canvas.drawBitmap(mBitmap, 0, 0,null);
	}
	/**  绘制变色的文本*/
	private void drawTargetText(Canvas canvas, int alpha) {
		mTextPaint.setColor(mColor);
		mTextPaint.setAlpha(alpha);
		
		int x = getMeasuredWidth()/2 - mTextBound.width()/2;
		int y = mIconRect.bottom + mTextBound.height();
		canvas.drawText(mText,x , y, mTextPaint);
	}

	/**绘制原文本*/
	private void drawSourceText(Canvas canvas, int alpha) {
		mTextPaint.setColor(0xff333333);
		mTextPaint.setAlpha(255);
		int x = getMeasuredWidth()/2 - mTextBound.width()/2;
		int y = mIconRect.bottom + mTextBound.height();
		canvas.drawText(mText,x , y, mTextPaint);
	}
	PorterDuff.Mode mMode = PorterDuff.Mode.DST_IN;

	/** 在内存中绘制可变色的Icon */
	private void setUpTargetBitmap(int alpha) {
		mBitmap = Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(),Config.ARGB_8888);
		
		mCanvas = new Canvas(mBitmap);
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setAlpha(alpha);
		mCanvas.drawRect(mIconRect, mPaint);
		
		mPaint.setXfermode(new PorterDuffXfermode(mMode));
		mPaint.setAlpha(255);
		mCanvas.drawBitmap(mIconBitma, null, mIconRect,mPaint);
	}
	
	private static final String INSTANCE_STATUS = "instance_status";
	private static final String STATUS_ALPHA = "status_alpha";
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
		bundle.putFloat(STATUS_ALPHA, mAlpha);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mAlpha = bundle.getFloat(STATUS_ALPHA);
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
			return;
		}
		super.onRestoreInstanceState(state);
	}
	
	public void setIconAlpha(float alpha){
		this.mAlpha = alpha;
		invalidateView();
	}

	/*public void setText(String text){
		this.mText = text;
	}*/

	/** 重绘 */
	private void invalidateView() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			invalidate();
		}else {
			postInvalidate();
		}
	}
}
