package com.example.administrator.aduiorecordui.record;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: AudioRecord
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  20:00
 */
public class AudioRecord extends BaseAudioRecord {


    Paint ruleHorizontalLinePaint = new Paint();
    Paint middleLinePaint = new Paint();
    Paint smallScalePaint = new Paint();
    Paint bigScalePaint = new Paint();
    Paint ruleTextPaint = new Paint();
    Paint pointPaint = new Paint();
    Paint rectPaint = new Paint();
    Paint rectInvertedPaint = new Paint();
    Paint centerLine = new Paint();


    /**
     * 提前刻画量
     */
    protected int mDrawOffset;

    int rectLocationX;
    int rectWidth = 6;
    protected List<Rect> radioRectList = new ArrayList<>();


    private int circleRadius = 10;

    public AudioRecord(Context context) {
        super(context);
        init();
    }

    public AudioRecord(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioRecord(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        pointPaint.setAntiAlias(true);
        pointPaint.setStrokeWidth(10);
        pointPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        centerLine.setAntiAlias(true);
        centerLine.setStrokeWidth(5);
        centerLine.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        middleLinePaint.setAntiAlias(true);
        middleLinePaint.setStrokeWidth(2);
        middleLinePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));


        ruleHorizontalLinePaint.setAntiAlias(true);
        ruleHorizontalLinePaint.setStrokeWidth(ruleHorizontalLineStrokeWidth);
        ruleHorizontalLinePaint.setColor(ruleHorizontalLineColor);

        rectPaint.setAntiAlias(true);
        rectPaint.setStrokeWidth(2);
        rectPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));

        rectInvertedPaint.setAntiAlias(true);
        rectInvertedPaint.setStrokeWidth(2);
        rectInvertedPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        smallScalePaint = new Paint();
        smallScalePaint.setStrokeWidth(smallScaleStrokeWidth);
        smallScalePaint.setColor(ruleVerticalLineColor);
        smallScalePaint.setStrokeCap(Paint.Cap.ROUND);

        bigScalePaint = new Paint();
        bigScalePaint.setColor(ruleVerticalLineColor);
        bigScalePaint.setStrokeWidth(bigScaleStrokeWidth);
        bigScalePaint.setStrokeCap(Paint.Cap.ROUND);

        ruleTextPaint = new Paint();
        ruleTextPaint.setAntiAlias(true);
        ruleTextPaint.setColor(ruleTextColor);
        ruleTextPaint.setTextSize(ruleTextSize);
        ruleTextPaint.setTextAlign(Paint.Align.CENTER);


        mDrawOffset = scaleInterval;
    }

    @Override
    public void makeRect(int height) {
        int rectBottom = getMeasuredHeight() / 2;
        int rectTop = rectBottom - height;
        Rect rect = new Rect(rectLocationX, rectTop, rectLocationX + rectWidth, rectBottom);
        rectLocationX = rectLocationX + rect.width();
        radioRectList.add(rect);
        if (!isAutoScroll) {
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawRect(canvas);
        drawCenterVerticalLine(canvas);
    }

    private void drawCenterVerticalLine(Canvas canvas) {
        float circleX = getScrollX() + canvas.getWidth() / 2;
        float topCircleY = ruleHorizontalLineHeight - circleRadius;
        canvas.drawCircle(circleX, topCircleY, circleRadius, centerLine);
        float bottomCircleY = canvas.getHeight() / 2 + (canvas.getHeight() / 2 - ruleHorizontalLineHeight) + circleRadius;
        canvas.drawCircle(circleX, bottomCircleY, circleRadius, centerLine);
        canvas.drawLine(circleX, topCircleY, circleX, bottomCircleY, centerLine);
    }

    private void drawScale(Canvas canvas) {

        int firstPoint = (getScrollX() - mDrawOffset) / scaleInterval;
        int lastPoint = (getScrollX() + canvas.getWidth() + mDrawOffset) / (scaleInterval);
        for (int i = firstPoint; i < lastPoint; i++) {
            float locationX = i * scaleInterval;
            if (i % intervalCount == 0) {
                canvas.drawLine(locationX, ruleHorizontalLineHeight - bigScaleStrokeLength, locationX, ruleHorizontalLineHeight, bigScalePaint);
                int index = i / intervalCount;
                canvas.drawText(formatTime(index), locationX+bigScaleStrokeWidth/2+ruleTextSize*1.5f, ruleHorizontalLineHeight - bigScaleStrokeLength + ruleTextSize/1.5f, ruleTextPaint);
            } else {
                canvas.drawLine(locationX, ruleHorizontalLineHeight - smallScaleStrokeLength, locationX, ruleHorizontalLineHeight, smallScalePaint);
            }
        }


        //画轮廓线
        canvas.drawLine(getScrollX(), ruleHorizontalLineHeight, getScrollX() + canvas.getWidth(), ruleHorizontalLineHeight, ruleHorizontalLinePaint);

        //测试用的圆
        canvas.drawCircle(540, canvas.getHeight() / 2, 25, pointPaint);


    }

    private String formatTime(int index) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        Date date = new Date();
        date.setTime(TimeUnit.SECONDS.toMillis(index));
        return dateFormat.format(date);
    }

    private void drawRect(Canvas canvas) {
        List<Rect> drawRectList = getDrawRectList(canvas);
        if (drawRectList == null || drawRectList.size() == 0) {
            return;
        }
        //绘制矩形
        for (Rect rect : drawRectList) {
            canvas.drawRect(rect, rectPaint);
            Rect rectInverted = new Rect(rect);
            rectInverted.top = canvas.getHeight() / 2;
            rectInverted.bottom = rectInverted.top + rect.height();
            canvas.drawRect(rect, rectPaint);
            canvas.drawRect(rectInverted, rectInvertedPaint);
        }

        //测试用的基准线
        int middleLineY = canvas.getHeight() / 2;
        canvas.drawLine(getScrollX() + 50, middleLineY, getScrollX() + canvas.getWidth() - 50, middleLineY, middleLinePaint);

    }

    private List<Rect> getDrawRectList(Canvas canvas) {
        if (radioRectList.size() == 0) {
            return null;
        }
        List<Rect> rectList = new ArrayList<>();

        int recentlyRectIndex = getScrollX() / rectWidth;
        if (recentlyRectIndex < 0) {
            recentlyRectIndex = 0;
        } else if (recentlyRectIndex >= radioRectList.size()) {
            recentlyRectIndex = radioRectList.size() - 1;
        }

        int mixWidth = getScrollX() - rectWidth;
        int maxWidth = getScrollX() + canvas.getWidth() / 2 + rectWidth;
        for (int i = recentlyRectIndex; i < radioRectList.size(); i++) {
            Rect next = radioRectList.get(i);
            if (next.left >= mixWidth && next.right <= maxWidth) {
                rectList.add(next);
            }
            if (next.left > maxWidth) {
                break;
            }
        }

        return rectList;
    }
}
