package com.zac4j.wheelview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author zac 2016-05-16
 */
public class WheelStraightPicker extends WheelCrossPicker {
  public WheelStraightPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public WheelStraightPicker(Context context) {
    super(context);
  }

  @Override public void computeWheelSizes() {
    super.computeWheelSizes();

    wheelContentWidth =
        mOrientation.getStraightWidth(itemCount, itemSpace, maxTextWidth, maxTextHeight);
    wheelContentHeight =
        mOrientation.getStraightHeight(itemCount, itemSpace, maxTextWidth, maxTextHeight);

    unit = mOrientation.getStraightUnit(itemSpace, maxTextWidth, maxTextHeight);

    int disDisplay = mOrientation.getDisplay(itemCount, itemSpace, maxTextWidth, maxTextHeight);

    unitDisplayMin = -disDisplay;
    unitDisplayMax = disDisplay;

    unitDeltaMin = -unit * (data.size() - startIndex - 1);
    unitDeltaMax = unit * startIndex;
  }

  @Override protected void drawItems(Canvas canvas) {
    for (int i = -startIndex; i < data.size() - startIndex; i++) {
      int curDis =
          mOrientation.computeStraightUnit(unit, i, disTotalMoveX, disTotalMoveY, diSingleMoveX,
              diSingleMoveY);
      if (curDis > unitDisplayMax || curDis < unitDisplayMin) {
        continue;
      }
      canvas.save();
      canvas.clipRect(rectCurItem, Region.Op.DIFFERENCE);
      mTextPaint.setColor(textColor);
      mTextPaint.setAlpha(255 - 255 * Math.abs(curDis) / unitDisplayMax);
      mOrientation.draw(canvas, mTextPaint, data.get(i + startIndex), curDis, wheelCenterX,
          wheelCenterTextY);
      canvas.restore();

      canvas.save();
      canvas.clipRect(rectCurItem);
      mTextPaint.setColor(selectTextColor);
      mOrientation.draw(canvas, mTextPaint, data.get(i + startIndex), curDis, wheelCenterX,
          wheelCenterTextY);
      canvas.restore();
    }
  }

  @Override protected void onTouchMove(MotionEvent event) {
    super.onTouchMove(event);
  }

  @Override protected void onTouchUp(MotionEvent event) {
    unitDeltaTotal = mOrientation.getUnitDeltaTotal(disTotalMoveX, disTotalMoveY);
    super.onTouchUp(event);
  }
}