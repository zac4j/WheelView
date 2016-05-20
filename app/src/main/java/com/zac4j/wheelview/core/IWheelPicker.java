package com.zac4j.wheelview.core;

import java.util.List;

/**
 * WheelPicker interface
 */
public interface IWheelPicker {
  /**
   * Set up WheelView data set
   *
   * @param data data that will inflate the WheelView
   */
  void setData(List<String> data);

  /**
   * Set WheelView wheel state change listener
   *
   * @param listener listener that will monitor WheelView state changed.
   */
  void setOnWheelChangeListener(BaseWheelPicker.OnWheelChangeListener listener);

  /**
   * Set start wheel item index.
   *
   * @param index index that indicate the initial wheel data index.
   */
  void setStartIndex(int index);

  /**
   * Set WheelView item space.
   *
   * @param space the space between WheelView data item.
   */
  void setItemSpace(int space);

  /**
   * Set WheelView item count
   *
   * @param count count indicate the total number of  WheelView items.
   */
  void setItemCount(int count);

  /**
   * Set WheelView data text color.
   *
   * @param color the data of text color.
   */
  void setTextColor(int color);

  /**
   * Set WheelView data text size.
   *
   * @param size the size of data text.
   */
  void setTextSize(int size);

  /**
   * Set selected data text color.
   *
   * @param color the color of selected data text.
   */
  void setSelectTextColor(int color);

  /**
   * Set WheelView Wheel decorator
   *
   * @param ignorePadding true if ignore the space between item and border.
   * @param decor the WheelView display decorator.
   */
  void setWheelDecor(boolean ignorePadding, BaseWheelDecor decor);
}