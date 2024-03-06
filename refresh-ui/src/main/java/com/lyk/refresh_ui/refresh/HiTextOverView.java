package com.lyk.refresh_ui.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.lyk.log.HiLog;
import com.lyk.refresh_ui.R;


public class HiTextOverView extends HiOverView {
    private TextView mText;
    private View mRotateView;

    public HiTextOverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HiTextOverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HiTextOverView(Context context) {
        super(context);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.hi_refresh_overview, this, true);
        mText = findViewById(R.id.text);
        mRotateView = findViewById(R.id.iv_rotate);
    }

    public static String TAG = "HiTextOverView";

    @Override
    protected void onScroll(int scrollY, int pullRefreshHeight) {
        HiLog.it(TAG  , "onScroll");
        mText.setText("onScroll");
    }

    @Override
    public void onVisible() {
        HiLog.it(TAG  , "onVisible");
        mText.setText("onVisible");
    }

    @Override
    public void onOver() {
        HiLog.it(TAG  , "onOver");
        mText.setText("onOver");
    }

    @Override
    public void onRefresh() {
        HiLog.it(TAG  , "onRefresh");
        mText.setText("onRefresh...");
        Animation operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        mRotateView.startAnimation(operatingAnim);
    }

    @Override
    public void onFinish() {
        HiLog.it(TAG  , "onFinish");
        mText.setText("onFinish");
        mRotateView.clearAnimation();
    }


}