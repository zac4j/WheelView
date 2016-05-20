package com.zac4j.wheelview.core;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class BaseWheelDecor {
  public abstract void drawDecor(Canvas canvas, Rect rectLast, Rect rectNext, Paint paint);
}