package com.mo.eat;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by motingwei on 2017/8/31.
 */
public class EatLoading extends View {

    private Paint mPaint;
    private Paint mFoodPaint;

    //起始角度和扫过的角度大小
    private float startAngle;
    private float sweepAngle;

    private final float START_ANGLE_DEFAULT = 20;
    private final float SWEEP_ANGLE_DEFAULT = 320;

    /**
     * 圆的半径
     */
    private float mRadius;

    /**
     * 圆心 X 坐标
     */
    private float circleX;

    /**
     * 圆心 Y 坐标
     */
    private float circleY;

    /**
     * 食物的数量
     */
    private int foodCount;

    private int mWidth;
    private int mHeight;

    private RectF mRectF;

    //平移和吃东西动画
    private ValueAnimator animEat;
    private ValueAnimator animTrans;
    private AnimatorSet animatorSet;
    private boolean mPlay;

    public EatLoading(Context context) {
        this(context, null);
    }

    public EatLoading(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EatLoading(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
//        mPaint.setColor(getResources().getColor(R.color.colorDefault));

//        mFoodPaint.setColor(getResources().getColor(R.color.colorDefault));

        //嘴巴开闭的动画
        animEat = ValueAnimator.ofFloat(0, 19.5f, 0);
        animEat.setDuration(500);
        animEat.setInterpolator(new LinearInterpolator());
        animEat.setRepeatCount(ValueAnimator.INFINITE);
        animEat.setRepeatMode(ValueAnimator.RESTART);

        animEat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                startAngle = START_ANGLE_DEFAULT - value;
                sweepAngle = SWEEP_ANGLE_DEFAULT + value * 2;

                invalidate();
            }
        });

        animatorSet = new AnimatorSet();
    }


    private void init(){
        startAngle = START_ANGLE_DEFAULT;
        sweepAngle = SWEEP_ANGLE_DEFAULT;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFoodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mRectF = new RectF(10, 10, 0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        float halfH = h / 2f;

        mRadius = halfH * 0.9f;

        //圆心坐标
        circleX = halfH;
        circleY = halfH;

        mRectF.right = mRectF.left + mRadius * 2;
        mRectF.bottom = mRectF.top + mRadius * 2;

        //计算食物数量
        foodCount = (int) ((w - h) / halfH);
        foodCountTemp = foodCount;

        //平移动画
        animTrans = ValueAnimator.ofFloat(mRectF.left, mWidth - mRadius *2);
        animTrans.setDuration(5000);
        animTrans.setInterpolator(new LinearInterpolator());

        animTrans.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mRectF.left = value;
                mRectF.right = mRectF.left + mRadius * 2;
                circleX = mRectF.left + mRadius;

                //根据移动的距离来减少食物的数量
                int count = (int) (circleX / (mHeight / 2));
                foodCount = foodCountTemp - count;
                Log.d("debug", "count " + foodCount);
//                invalidate();
            }
        });

    }

    private int foodCountTemp;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int resultWidth;
        int resultHeight;

        //测量宽高
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        //确定高
        if (heightMode == MeasureSpec.EXACTLY){
            resultHeight = height;
        }else {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            resultHeight = (int) px;

            if (widthMode == MeasureSpec.AT_MOST){
                resultHeight = Math.min(resultHeight, height);
            }
        }

        //确定宽
        if (widthMode == MeasureSpec.EXACTLY){
            resultWidth = width;
        }else {
            resultWidth = resultHeight * 4;

            if (widthMode == MeasureSpec.AT_MOST){
                resultWidth = Math.min(resultWidth, width);
            }
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(mRectF, startAngle, sweepAngle, true, mPaint);
//        canvas.drawArc(mRectF, startAngle, sweepAngle, true, mFoodPaint);

        //画食物
        drawFood(canvas);

        // 画眼睛
        canvas.drawCircle(circleX + mRadius / 3, circleY - mRadius / 2f, mRadius / 7, mPaint);



        if (!mPlay){
            mPlay = true;
            startAnim();
        }
    }

    private void startAnim() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animatorSet.play(animEat).with(animTrans);
                animatorSet.start();
            }
        }, 700);
    }

    private void drawFood(Canvas canvas) {
        canvas.save();

        //从坐边画到右边
        canvas.translate(mHeight, mHeight / 2);

        int offset = foodCountTemp - foodCount;
        canvas.translate(mHeight / 2 * offset, 0);

        for (int i = 0; i < foodCount + 1; i++) {
            canvas.drawCircle(0, 0, mRadius / 5, mFoodPaint);
            canvas.translate(mHeight / 2, 0);
        }

        canvas.restore();

    }


}
