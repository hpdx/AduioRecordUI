package com.example.administrator.aduiorecordui.playaudio;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.example.administrator.aduiorecordui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: BasePlayAudioView
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-14  15:08
 */
public abstract class BasePlayAudioView extends View {

    private static final String TAG = "BasePlayAudioView";

    boolean canTouchScroll = false;

    /**
     * 圆点距离上边距
     */
    int circleMarginTop = 100;

    /**
     * 矩形距离圆点间距
     */
    int rectMarginTop = 150;

    /**
     * 滚动的距离
     */
    public int scrollDx = 2;

    /**
     * 声音数据采集的频率 每秒钟采集个数
     */
    int audioSourceFrequency = 10;

    /**
     * 中心垂直线的宽
     */
    int centerLineWidth = 4;

    /**
     * 圆形半径
     */
    int circleRadius = 10;

    /**
     * 扫描过的矩形颜色
     */
    @ColorInt
    int swipedColor;

    /**
     * 没有扫描的矩形颜色
     */
    @ColorInt
    int unSwipeColor;

    /**
     * 线宽
     */
    int lineWidth = 10;
    /**
     * 间距
     */
    int rectGap = 4;

    /**
     * 最小可滑动值
     */
    protected int minScrollX = 0;
    /**
     * 最大可滑动值
     */
    protected int maxScrollX = 0;
    /**
     * 速度获取
     */
    protected VelocityTracker velocityTracker;
    /**
     * 控制滑动
     */
    protected OverScroller overScroller;
    /**
     * 惯性最大速度
     */
    protected int maxVelocity;
    /**
     * 惯性最小速度
     */
    protected int minVelocity;

    protected float mLastX = 0;

    List<SampleLine> sampleLineList = new ArrayList<>();

    float lineLocationX;
    private long delayMillis;

    float centerLineX;

    public BasePlayAudioView(Context context) {
        super(context);
        init(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    public BasePlayAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayAudio, 0, 0);
        audioSourceFrequency = typedArray.getInt(R.styleable.PlayAudio_p_audioSourceFrequency, audioSourceFrequency);
        circleMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleMarginTop, circleMarginTop);
        rectMarginTop = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectMarginTop, rectMarginTop);
        centerLineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_centerLineWidth, centerLineWidth);
        circleRadius = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_circleRadius, circleRadius);
        swipedColor = ContextCompat.getColor(context, android.R.color.holo_red_light);
        unSwipeColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectWidth, lineWidth);
        rectGap = typedArray.getDimensionPixelSize(R.styleable.PlayAudio_p_rectGap, rectGap);

        typedArray.recycle();
    }

    private void init(Context context) {

        delayMillis = (long) (1000 / (audioSourceFrequency * (lineWidth + rectGap) * scrollDx));

        overScroller = new OverScroller(context);
        velocityTracker = VelocityTracker.obtain();
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        minVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        checkAPILevel();
    }

    private void checkAPILevel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canTouchScroll) {
            return false;
        }
        float currentX = event.getX();
        //开始速度检测
        startVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                mLastX = currentX;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastX - currentX;
                mLastX = currentX;
                scrollBy((int) (moveX), 0);
                break;
            case MotionEvent.ACTION_UP:
                //手指离开屏幕，开始处理惯性滑动Fling
                velocityTracker.computeCurrentVelocity(500, maxVelocity);
                float velocityX = velocityTracker.getXVelocity();
                if (Math.abs(velocityX) > minVelocity) {
                    fling(-velocityX);
                }
                finishVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }
                finishVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }

    private void startVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    private void finishVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void fling(float velocity) {
        overScroller.fling(getScrollX(), 0, (int) velocity, 0, minScrollX, maxScrollX, 0, 0);
    }

    @Override
    public void computeScroll() {
        //滑动处理
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < minScrollX) {
            x = minScrollX;
        } else if (x > maxScrollX) {
            x = maxScrollX;
        }
        super.scrollTo(x, y);
    }

    boolean isPlaying;
    boolean isAutoScroll;
    Handler playHandler = new Handler();
    Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "lll run: centerLineX = " + centerLineX + ", getMeasuredWidth() / 2 = " + getMeasuredWidth() / 2);
            int middle = getMeasuredWidth() / 2;
            if (centerLineX < middle) {
                startCenterLineAnimation(delayMillis);
                isAutoScroll = false;
            } else {
                isAutoScroll = true;
                scrollBy(scrollDx, 0);
            }
            playHandler.postDelayed(playRunnable, delayMillis);
            if (getScrollX() >= maxScrollX) {
                stopPlay();
                reset();
            }
        }
    };

    public void startPlay() {
        if (!isPlaying) {
            playHandler.post(playRunnable);
            isPlaying = true;
        }
    }

    /**
     * 暂停播放录音
     */
    public void stopPlay() {
        if (isPlaying) {
            playHandler.removeCallbacks(playRunnable);
            isPlaying = false;
            isAutoScroll = false;
        }
    }

    float fromX;

    private void startCenterLineAnimation(long delayMillis) {
        Log.d(TAG, "lll startCenterLineAnimation: delayMillis = " + delayMillis);

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "centerLineX", fromX, fromX + scrollDx);
        animator.setDuration(delayMillis);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        fromX = fromX + scrollDx;
    }

    public float getCenterLineX() {
        return centerLineX;
    }

    public void setCenterLineX(float centerLineX) {
        this.centerLineX = centerLineX;
        invalidate();
    }

    public void reset() {
        stopPlay();
        fromX = 0;
        centerLineX = 0;
        isAutoScroll = false;
        scrollTo(minScrollX, 0);
        invalidate();
    }

}
