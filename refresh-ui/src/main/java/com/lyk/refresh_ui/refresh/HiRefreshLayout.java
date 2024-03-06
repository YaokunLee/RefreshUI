package com.lyk.refresh_ui.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.lyk.log.HiLog;
import com.lyk.refresh_ui.refresh.HiOverView.HiRefreshState;
/**
 * 下拉刷新View
 */
public class HiRefreshLayout extends FrameLayout implements HiRefresh {
    private static final String TAG = HiRefreshLayout.class.getSimpleName();
    private HiRefreshState mState;
    private GestureDetector mGestureDetector;
    private AutoScroller mAutoScroller;
    private HiRefresh.HiRefreshListener mHiRefreshListener;
    protected HiOverView mHiOverView;
    private int mLastY;
    //刷新时是否禁止滚动
    private boolean disableRefreshScroll;

    public HiRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HiRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public HiRefreshLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(getContext(), hiGestureDetector);
        mAutoScroller = new AutoScroller();
    }

    @Override
    public void setDisableRefreshScroll(boolean disableRefreshScroll) {
        this.disableRefreshScroll = disableRefreshScroll;
    }

    private long refreshTime = 1000L;

    @Override
    public void setRefreshTime(Long time) {
        refreshTime = time;
    }

    private boolean enableRefresh = true;

    @Override
    public void setEnableRefresh(boolean enableRefresh) {
        this.enableRefresh = enableRefresh;
    }


    public void refreshFinished() {
        final View head = getChildAt(0);
        HiLog.i(this.getClass().getSimpleName(), "refreshFinished head-bottom:" + head.getBottom());
        mHiOverView.onFinish();
        mHiOverView.setState(HiRefreshState.STATE_INIT);
        final int bottom = head.getBottom();

        if (mHiRefreshListener != null) {
            mHiRefreshListener.onRefreshFinished();
        }

        if (bottom > 0) {
            //下over pull 200，height 100
             //  bottom  =100 ,height 100
            mAutoScroller.recover(bottom);
        }
        setRefreshState(HiRefreshState.STATE_INIT);
    }

    @Override
    public void setRefreshListener(HiRefresh.HiRefreshListener hiRefreshListener) {
        mHiRefreshListener = hiRefreshListener;
    }

    /**
     * 设置下拉刷新的视图
     *
     * @param hiOverView
     */
    @Override
    public void setRefreshOverView(HiOverView hiOverView) {
        if (this.mHiOverView != null) {
            removeView(mHiOverView);
        }
        this.mHiOverView = hiOverView;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mHiOverView, 0, params);
    }

    HiGestureDetector hiGestureDetector = new HiGestureDetector() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float disX, float disY) {
            if (Math.abs(disX) > Math.abs(disY) || mHiRefreshListener != null && !enableRefresh) {
                //横向滑动，或刷新被禁止则不处理
                return false;
            }
            if (disableRefreshScroll && mState == HiRefreshState.STATE_REFRESH) {//刷新时是否禁止滑动
                return true;
            }

            View head = getChildAt(0);
            View child = HiScrollUtil.findScrollableChild(HiRefreshLayout.this);
            if (HiScrollUtil.childScrolled(child)) {
                //如果列表发生了滚动则不处理
                return false;
            }
            //没有刷新或没有达到可以刷新的距离，且头部已经划出或下拉
            if ((mState == null || mState == HiRefreshState.STATE_INIT || mState == HiRefreshState.STATE_VISIBLE)
                    && (head.getBottom() > 0 || disY <= 0.0F)) {
                //还在滑动中
                int height;
                //阻尼计算
                if (child.getTop() < mHiOverView.mPullRefreshHeight) {
                    height = (int) (mLastY / mHiOverView.minDamp);
                } else {
                    height = (int) (mLastY / mHiOverView.maxDamp);
                }
                //如果是正在刷新状态，则不允许在滑动的时候改变状态
                boolean bool = moveDown(height);
                mLastY = (int) (-disY);
                return bool;
            } else {
                HiLog.it(TAG + " hiGestureDetector", "return false");
                return false;
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //事件分发处理
        if (!mAutoScroller.isFinished()) {
            return false;
        }

        View head = getChildAt(0);
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {//松开手
            // https://www.gcssloop.com/customview/multi-touch.html 多点触控教程
            // https://www.bilibili.com/video/BV1d54y1d7y7/?p=1&spm_id_from=pageDriver 事件分发机制，有助于你理解cancel事件
            HiLog.it(TAG + " dispatchTouchEvent", "release");
            if (head.getBottom() > 0) {
                HiLog.it(TAG + " dispatchTouchEvent", "bottom > 0");
                if (mState != HiRefreshState.STATE_REFRESH) {//非正在刷新
                    HiLog.it(TAG + " dispatchTouchEvent", "recover");
                    recover(head.getBottom());
                    return false;
                }
            }
            mLastY = 0;
        }
        boolean consumed = mGestureDetector.onTouchEvent(ev);
        HiLog.it(TAG + " dispatchTouchEvent", "gesture consumed：" + consumed);
        if ((consumed || (mState != HiRefreshState.STATE_INIT && mState != HiRefreshState.STATE_REFRESH)) && head.getBottom() != 0) {
            HiLog.it(TAG + " dispatchTouchEvent", "send cancel to parent");
            ev.setAction(MotionEvent.ACTION_CANCEL);//让父类接受不到真实的事件
            return super.dispatchTouchEvent(ev);
        }

        if (consumed) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //定义head和child的排列位置
        View head = getChildAt(0);
        View child = getChildAt(1);
        if (head != null && child != null) {
            HiLog.i(TAG, "onLayout head-height:" + head.getMeasuredHeight());
            int childTop = child.getTop();
            if (mState == HiRefreshState.STATE_REFRESH) {
                head.layout(0, mHiOverView.mPullRefreshHeight - head.getMeasuredHeight(), right, mHiOverView.mPullRefreshHeight);
                child.layout(0, mHiOverView.mPullRefreshHeight, right, mHiOverView.mPullRefreshHeight + child.getMeasuredHeight());
            } else {
                //left,top,right,bottom
                head.layout(0, childTop - head.getMeasuredHeight(), right, childTop);
                child.layout(0, childTop, right, childTop + child.getMeasuredHeight());
            }

            View other;
            //让HiRefreshLayout节点下两个以上的child能够不跟随手势移动以实现一些特殊效果，如悬浮的效果
            for (int i = 2; i < getChildCount(); ++i) {
                other = getChildAt(i);
                other.layout(0, top, right, bottom);
            }
            HiLog.i(TAG, "onLayout head-bottom:" + head.getBottom());
        }
    }

    private void recover(int dis) {//dis =200  200-100
        if (dis > mHiOverView.mPullRefreshHeight) {
            mAutoScroller.recover(dis - mHiOverView.mPullRefreshHeight);
            setRefreshState(HiRefreshState.STATE_OVER_RELEASE);
            // 恢复到刷新位置
//            getHandler().postAtTime(() -> refreshFinished(), System.currentTimeMillis() + refreshTime);
        } else {
            refreshFinished();
        }
    }

    public void setRefreshState(HiRefreshState state) {
        HiLog.it(TAG  + " setState", "setState from " + this.mState + " to "+ state);
        this.mState = state;
    }


    private void updateStateWhenDown(int childTop) {
        if (childTop <= mHiOverView.mPullRefreshHeight) {
            setRefreshState(HiRefreshState.STATE_VISIBLE);
            if (mHiOverView.getState() != HiRefreshState.STATE_VISIBLE) {
                mHiOverView.onVisible();
                mHiOverView.setState(HiRefreshState.STATE_VISIBLE);
            }
        } else {
            //超出刷新位置
            if (mHiOverView.getState() != HiRefreshState.STATE_OVER) {
                mHiOverView.onOver();
                mHiOverView.setState(HiRefreshState.STATE_OVER);
            }
        }
    }

    /**
     * 根据偏移量移动header与child
     *
     * @param offsetY 偏移量
     * @return
     */
    private boolean moveDown(int offsetY) {
        HiLog.it(TAG + " moveDown", "offsetY:" + offsetY);
        View head = getChildAt(0);
        View child = getChildAt(1);
        int childTop = child.getTop() + offsetY;

        HiLog.it(TAG + " moveDown", "current state: " + mState);
        HiLog.it(TAG + " moveDown", "mPullRefreshHeight: " + mHiOverView.mPullRefreshHeight);
        HiLog.it(TAG + " moveDown", "moveDown head-bottom: " + head.getBottom() + ", child.getTop():" + child.getTop() + ", offsetY:" + offsetY);

        if (mState == null) {
            setRefreshState(HiRefreshState.STATE_INIT);
        }

        switch (mState) {
            case STATE_INIT:
            case STATE_VISIBLE:
                //移动head与child的位置，到原始位置
                head.offsetTopAndBottom(offsetY);
                child.offsetTopAndBottom(offsetY);
                updateStateWhenDown(childTop);
                break;

            default:
                HiLog.it(TAG + " moveDown", "default");
                setRefreshState(HiRefreshState.STATE_INIT);
                break;
        }

        if (mHiOverView != null) {
            mHiOverView.onScroll(head.getBottom(), mHiOverView.mPullRefreshHeight);
        }

        return true;
    }


    /**
     * 根据偏移量移动header与child
     *
     * @param offsetY 偏移量
     * @return
     */
    private boolean moveUpWhenRelease(int offsetY) {
        HiLog.it(TAG + " moveUpWhenRelease", "offsetY:" + offsetY);
        View head = getChildAt(0);
        View child = getChildAt(1);
        int childTop = child.getTop() + offsetY;

        HiLog.it(TAG + " moveUpWhenRelease", "current state: " + mState);
        HiLog.it(TAG + " moveUpWhenRelease", "mPullRefreshHeight: " + mHiOverView.mPullRefreshHeight);
        HiLog.it(TAG + " moveUpWhenRelease", "moveDown head-bottom: " + head.getBottom() + ", child.getTop():" + child.getTop() + ", offsetY:" + offsetY);
        if (childTop <= 0) {//异常情况的补充
            HiLog.it(TAG  + " moveUpWhenRelease", "case 1");
            offsetY = -child.getTop();
            //移动head与child的位置，到原始位置
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
            if (mState != HiRefreshState.STATE_REFRESH) {
                setRefreshState(HiRefreshState.STATE_INIT);
            }
        } else if (childTop <= mHiOverView.mPullRefreshHeight) {//还没超出设定的刷新距离
            HiLog.it(TAG  + " moveUpWhenRelease", "case 3");
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
            if (childTop == mHiOverView.mPullRefreshHeight && mState == HiRefreshState.STATE_OVER_RELEASE) {
                HiLog.it(TAG + " moveUpWhenRelease", "refresh，childTop：" + childTop);
                refresh();
            }
        } else {
            HiLog.it(TAG  + " moveUpWhenRelease", "case 4");
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
        }
        if (mHiOverView != null) {
            mHiOverView.onScroll(head.getBottom(), mHiOverView.mPullRefreshHeight);
        }
        return true;
    }


    /**
     * 刷新
     */
    private void refresh() {
        setRefreshState(HiRefreshState.STATE_REFRESH);
        mHiOverView.onRefresh();
        mHiOverView.setState(HiRefreshState.STATE_REFRESH);

        if (mHiRefreshListener != null) {
            mHiRefreshListener.onRefreshStart();
        }

        postDelayed(()-> refreshFinished(), refreshTime);
    }


    /**
     * 借助Scroller实现视图的自动滚动
     * https://juejin.im/post/5c7f4f0351882562ed516ab6
     */
    private class AutoScroller implements Runnable {
        private Scroller mScroller;
        private int mLastY;
        private boolean mIsFinished;

        AutoScroller() {
            mScroller = new Scroller(getContext(), new LinearInterpolator());
            mIsFinished = true;
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {//还未滚动完成
                moveUpWhenRelease(mLastY - mScroller.getCurrY());
                mLastY = mScroller.getCurrY();
                post(this);
            } else {
                removeCallbacks(this);
                mIsFinished = true;
            }
        }

        void recover(int dis) {
            if (dis <= 0) {
                return;
            }
            removeCallbacks(this);
            mLastY = 0;
            mIsFinished = false;
            mScroller.startScroll(0, 0, 0, dis, 300);
            post(this);
        }

        boolean isFinished() {
            return mIsFinished;
        }

    }
}