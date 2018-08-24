package com.example.ckassatestapplication;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DictionaryEntryView extends ViewGroup {

    private final static int HEIGHT = 40; //dp
    final static int PADDING = 5; //dp

    TextView mLeft;
    TextView mRight;

    public DictionaryEntryView(Context context) {
        super(context);

        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, getResources().getDisplayMetrics()));
        mLeft = new TextView(context);
        mLeft.setSingleLine(true);
        mLeft.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mLeft.setPadding(padding,padding,padding,padding);
        mRight = new TextView(context);
        mRight.setSingleLine(true);
        mRight.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        mRight.setPadding(padding,padding,padding,padding);
        addView(mLeft);
        addView(mRight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLeft.layout(0,0, (r-l)/2, b-t);
        mRight.layout((r-l)/2+1,0, r-l,b-t);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width_mode = MeasureSpec.getMode(widthMeasureSpec);
        int width_size = MeasureSpec.getSize(widthMeasureSpec);
        int height_mode = MeasureSpec.getMode(heightMeasureSpec);
        int height_size = MeasureSpec.getSize(heightMeasureSpec);

        int height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT, getResources().getDisplayMetrics()));

        if (height_mode == MeasureSpec.AT_MOST){
            height_size = (height > height_size) ? height_size : height;
        }else if (height_mode == MeasureSpec.UNSPECIFIED) {
            height_size = height;
        }

        mLeft.measure(View.MeasureSpec.makeMeasureSpec(width_size/2, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height_size, View.MeasureSpec.EXACTLY));
        mRight.measure(View.MeasureSpec.makeMeasureSpec(width_size - width_size/2, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height_size, View.MeasureSpec.EXACTLY));

        setMeasuredDimension(width_size, height_size);
    }

    public void setLeftText(String leftText){
        mLeft.setText(leftText);
    }

    public void setRightText(String rightText){
        mRight.setText(rightText);
    }

}
