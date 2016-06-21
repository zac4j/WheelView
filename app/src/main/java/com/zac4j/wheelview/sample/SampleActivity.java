package com.zac4j.wheelview.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.zac4j.wheelview.R;

/**
 * Sample Activity
 * Created by Zac on 2016/6/21.
 */
public class SampleActivity extends AppCompatActivity{

  private EditText mEditText;
  private Button mActionButton;
  private LinearLayout mBottomSheetLayout;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);

    mEditText = (TextInputEditText) findViewById(R.id.sample_et_address);
    mBottomSheetLayout = (LinearLayout) findViewById(R.id.bottom_sheet_layout);
    final BottomSheetBehavior behavior = BottomSheetBehavior.from(mBottomSheetLayout);

    findViewById(R.id.sample_btn_add_address).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
          // TODO update bottom sheet height
          behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
          behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
      }
    });
  }
}
