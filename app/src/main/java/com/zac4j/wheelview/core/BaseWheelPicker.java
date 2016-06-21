package com.zac4j.wheelview.core;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.zac4j.wheelview.R;

import java.util.Arrays;
import java.util.List;

/**
 * @author zac 2016-05-16
 */
public abstract class BaseWheelPicker extends View implements IWheelPicker {

  protected static class Default {
    protected static final int TOUCH_DISTANCE_MINIMUM = 8;
    protected static final int VELOCITY_TRACKER_UNITS = 150;

    protected static final int WHEEL_START_INDEX = 0;
    protected static final int WHEEL_ITEM_COUNT = 8;

    protected static final int WHEEl_ITEM_PADDING = 8;
    protected static final int WHEEl_TEXT_SIZE = 20;

    protected static final int WHEEL_TEXT_DATA = R.array.ChineseProvince;
    protected static final int WHEEl_TEXT_COLOR = R.color.primaryTextLight;
    protected static final int WHEEl_SELECT_TEXT_COLOR = R.color.primaryTextDark;
  }

  protected VelocityTracker mTracker;
  protected WheelScroller mScroller;
  protected TextPaint mTextPaint;
  protected Paint mPaint;
  protected Rect mTextBound;
  protected Rect mDrawBound;
  protected Handler mHandler;
  protected OnWheelChangeListener mListener;
  protected BaseWheelDecor mWheelDecor;

  protected List<String> data;
  protected String curData;

  protected int state = SCROLL_STATE_IDLE;
  protected int itemCount;
  protected int startIndex;
  protected int itemSpace;
  protected int textSize;
  protected int textColor;
  protected int selectTextColor;
  protected int maxTextWidth, maxTextHeight;
  protected int wheelContentWidth, wheelContentHeight;
  protected int wheelCenterX, wheelCenterY, wheelCenterTextY;

  protected int lastX, lastY;
  protected int diSingleMoveX, diSingleMoveY;
  protected int disTotalMoveX, disTotalMoveY;

  protected boolean ignorePadding;
  protected boolean hasSameSize;

  public static final int SCROLL_STATE_IDLE = 0;
  public static final int SCROLL_STATE_DRAGGING = 1;
  public static final int SCROLL_STATE_SCROLLING = 2;

  public interface OnWheelChangeListener {
    void onWheelScrolling(float deltaX, float deltaY);

    void onWheelSelected(int index, String data);

    void onWheelScrollStateChanged(int state);

    void onWheelSelected(BaseWheelPicker wheelPicker, int index, String data);
  }

  public static class SimpleWheelChangeListener implements OnWheelChangeListener {

    @Override public void onWheelScrolling(float deltaX, float deltaY) {

    }

    @Override public void onWheelSelected(int index, String data) {

    }

    @Override public void onWheelScrollStateChanged(int state) {

    }

    @Override public void onWheelSelected(BaseWheelPicker wheelPicker, int index, String data) {

    }
  }

  public BaseWheelPicker(Context context) {
    this(context, null);
  }

  public BaseWheelPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    obtainAttrs(context, attrs);
    instantiation();
    assignment();
    computeWheelSizes();
  }

  protected void obtainAttrs(Context context, AttributeSet attrs) {
    if (null != attrs) {
      TypedArray attributes = null;
      try {
        attributes = getContext().obtainStyledAttributes(attrs, R.styleable.BaseWheelPicker);
        int dataResId = attributes.getResourceId(R.styleable.BaseWheelPicker_wheelData,
            Default.WHEEL_TEXT_DATA);
        if (dataResId == 0) dataResId = Default.WHEEL_TEXT_DATA;
        data = Arrays.asList(context.getResources().getStringArray(dataResId));

        startIndex = attributes.getInt(R.styleable.BaseWheelPicker_wheelStartIndex,
            Default.WHEEL_START_INDEX);
        itemCount =
            attributes.getInt(R.styleable.BaseWheelPicker_wheelItemCount, Default.WHEEL_ITEM_COUNT);
        itemSpace = attributes.getDimensionPixelSize(R.styleable.BaseWheelPicker_wheelItemSpace,
            (int) dp2px(context, Default.WHEEl_ITEM_PADDING));

        textSize = attributes.getDimensionPixelSize(R.styleable.BaseWheelPicker_android_textSize,
            (int) dp2px(context, Default.WHEEl_TEXT_SIZE));

        textColor = attributes.getColor(R.styleable.BaseWheelPicker_android_textColor,
            ContextCompat.getColor(context, Default.WHEEl_TEXT_COLOR));

        selectTextColor = attributes.getColor(R.styleable.BaseWheelPicker_wheelTextColorSelect,
            ContextCompat.getColor(context, Default.WHEEl_SELECT_TEXT_COLOR));
        hasSameSize = attributes.getBoolean(R.styleable.BaseWheelPicker_wheelItemSameSize, false);
      } finally {
        assert attributes != null;
        attributes.recycle();
      }
    } else {
      data = Arrays.asList(getContext().getResources().getStringArray(Default.WHEEL_TEXT_DATA));

      startIndex = Default.WHEEL_START_INDEX;
      itemCount = Default.WHEEL_ITEM_COUNT;

      itemSpace = (int) dp2px(context, Default.WHEEl_ITEM_PADDING);
      textSize = (int) dp2px(context, Default.WHEEl_TEXT_SIZE);

      selectTextColor = Default.WHEEl_SELECT_TEXT_COLOR;
    }
  }

  protected void instantiation() {
    mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    mTextPaint.setTextSize(textSize);
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    mTextBound = new Rect();
    mDrawBound = new Rect();

    mHandler = new Handler();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      mScroller = new OverScrollerCompat(getContext(), new DecelerateInterpolator());
    } else {
      mScroller = new ScrollerCompat(getContext(), new DecelerateInterpolator());
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      mScroller.setFriction(ViewConfiguration.getScrollFriction() / 25);
    }
  }

  protected void assignment() {
    curData = "";
  }

  protected void computeWheelSizes() {
    disTotalMoveX = 0;
    disTotalMoveY = 0;
    maxTextWidth = 0;
    maxTextHeight = 0;
    if (hasSameSize) {
      String text = data.get(0);
      mTextPaint.getTextBounds(text, 0, text.length(), mTextBound);
      maxTextWidth = Math.max(maxTextWidth, mTextBound.width());
      maxTextHeight = Math.max(maxTextHeight, mTextBound.height());
    } else {
      for (String text : data) {
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBound);
        maxTextWidth = Math.max(maxTextWidth, mTextBound.width());
        maxTextHeight = Math.max(maxTextHeight, mTextBound.height());
      }
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
    int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

    int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
    int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

    int resultWidth = wheelContentWidth, resultHeight = wheelContentHeight;
    resultWidth += (getPaddingLeft() + getPaddingRight());
    resultHeight += (getPaddingTop() + getPaddingBottom());

    resultWidth = measureSize(modeWidth, sizeWidth, resultWidth);
    resultHeight = measureSize(modeHeight, sizeHeight, resultHeight);

    setMeasuredDimension(resultWidth, resultHeight);
  }

  private int measureSize(int mode, int sizeExpect, int sizeActual) {
    int realSize;
    if (mode == MeasureSpec.EXACTLY) {
      realSize = sizeExpect;
    } else {
      realSize = sizeActual;
      if (mode == MeasureSpec.AT_MOST) {
        realSize = Math.min(realSize, sizeExpect);
      }
    }
    return realSize;
  }

  @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    onWheelSelected(startIndex, data.get(startIndex));
    onWheelSelected(this, startIndex, data.get(startIndex));

    mDrawBound.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(),
        h - getPaddingBottom());

    wheelCenterX = mDrawBound.centerX();
    wheelCenterY = mDrawBound.centerY();
    wheelCenterTextY = (int) (wheelCenterY - (mTextPaint.ascent() + mTextPaint.descent()) / 2);
  }

  @Override protected void onDraw(Canvas canvas) {
    drawBackground(canvas);

    canvas.save();
    canvas.clipRect(mDrawBound);
    drawItems(canvas);
    canvas.restore();

    drawForeground(canvas);
  }

  protected abstract void drawBackground(Canvas canvas);

  protected abstract void drawItems(Canvas canvas);

  protected abstract void drawForeground(Canvas canvas);

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (null == mTracker) {
      mTracker = VelocityTracker.obtain();
    }
    mTracker.addMovement(event);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        getParent().requestDisallowInterceptTouchEvent(true);
        if (!mScroller.isFinished()) mScroller.abortAnimation();
        lastX = (int) event.getX();
        lastY = (int) event.getY();
        onTouchDown(event);
        break;
      case MotionEvent.ACTION_MOVE:
        diSingleMoveX += (event.getX() - lastX);
        diSingleMoveY += (event.getY() - lastY);
        lastX = (int) event.getX();
        lastY = (int) event.getY();
        onTouchMove(event);
        break;
      case MotionEvent.ACTION_UP:
        disTotalMoveX += diSingleMoveX;
        disTotalMoveY += diSingleMoveY;
        diSingleMoveX = 0;
        diSingleMoveY = 0;
        mTracker.computeCurrentVelocity(Default.VELOCITY_TRACKER_UNITS);
        onTouchUp(event);
        getParent().requestDisallowInterceptTouchEvent(false);
        mTracker.recycle();
        mTracker = null;
        break;
      case MotionEvent.ACTION_CANCEL:
        getParent().requestDisallowInterceptTouchEvent(false);
        mScroller.abortAnimation();
        mTracker.recycle();
        mTracker = null;
        break;
    }
    return true;
  }

  protected abstract void onTouchDown(MotionEvent event);

  protected abstract void onTouchMove(MotionEvent event);

  protected abstract void onTouchUp(MotionEvent event);

  protected boolean isEventValid() {
    return isEventValidVer() || isEventValidHor();
  }

  protected boolean isEventValidHor() {
    return Math.abs(diSingleMoveX) > Default.TOUCH_DISTANCE_MINIMUM;
  }

  protected boolean isEventValidVer() {
    return Math.abs(diSingleMoveY) > Default.TOUCH_DISTANCE_MINIMUM;
  }

  protected void onWheelScrolling(float deltaX, float deltaY) {
    if (null != mListener) {
      mListener.onWheelScrolling(deltaX, deltaY);
    }
  }

  protected void onWheelSelected(int index, String data) {
    if (null != mListener) {
      mListener.onWheelSelected(index, data);
    }
  }

  protected void onWheelSelected(BaseWheelPicker wheelPicker, int index, String data) {
    if (mListener != null) {
      mListener.onWheelSelected(wheelPicker, index, data);
    }
  }

  protected void onWheelScrollStateChanged(int state) {
    if (this.state != state) {
      this.state = state;
      if (null != mListener) mListener.onWheelScrollStateChanged(state);
    }
  }

  @Override public void setData(List<String> data) {
    this.data = data;
    computeWheelSizes();
    requestLayout();
  }

  @Override public void setOnWheelChangeListener(OnWheelChangeListener listener) {
    this.mListener = listener;
  }

  @Override public void setStartIndex(int index) {
    startIndex = index;
    computeWheelSizes();
    requestLayout();
  }

  @Override public void setItemSpace(int space) {
    itemSpace = space;
    computeWheelSizes();
    requestLayout();
  }

  @Override public void setItemCount(int count) {
    itemCount = count;
    computeWheelSizes();
    requestLayout();
  }

  @Override public void setTextColor(int color) {
    textColor = color;
    invalidate();
  }

  @Override public void setTextSize(int size) {
    textSize = size;
    mTextPaint.setTextSize(size);
    computeWheelSizes();
    requestLayout();
  }

  @Override public void setSelectTextColor(int color) {
    selectTextColor = color;
    // May be you don't need to invalidate all of view area.
  }

  @Override public void setWheelDecor(boolean ignorePadding, BaseWheelDecor decor) {
    this.ignorePadding = ignorePadding;
    mWheelDecor = decor;
    // May be you don't need to invalidate all of view area.
  }

  private float dp2px(Context context, float dp) {
    Resources resources = context.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    return dp * (metrics.densityDpi / 160f);
  }
}