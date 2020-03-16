package com.samsung.android.bling.setting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class BlingCanvas extends View {

    private static final String TAG = "Bling/BlingCanvas";

    Paint mPaint = new Paint();

    int memberCount = 7;
    Path mPath[];
    Path myPath = new Path();

    interface CanvasTouchListener {
        void onUserTouch(int action, int x, int y);
    }

    private CanvasTouchListener mListener = null;

    public void setOnCanvasTouchListener(CanvasTouchListener listener) {
        mListener = listener;
    }

    public BlingCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(8);

        mPath = new Path[memberCount];
        for (int i = 0; i < memberCount; i++) {
            mPath[i] = new Path();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        canvas.drawPath(myPath, mPaint);

        for (int i = 0; i < memberCount; i++) {
            canvas.drawPath(mPath[i], mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);

                myPath.moveTo(x, y); // 자취에 그리지 말고 위치만 이동해라

                mListener.onUserTouch(0, (int) x, (int) y);
                break;
            case MotionEvent.ACTION_MOVE:
                myPath.lineTo(x, y); // 자취에 선을 그려라

                mListener.onUserTouch(1, (int) x, (int) y);
                break;
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        invalidate(); // 화면을 다시그려라

        return true;
    }

    public void cleanCanvas() {
        myPath.reset();

        mListener.onUserTouch(2, 0, 0);

        invalidate();
    }

    public void drawStarLine(int drawingInfo, int x, int y, int member) {
        if (drawingInfo == 0) {
            mPath[member].moveTo(x, y);
        } else if (drawingInfo == 1) {
            mPath[member].lineTo(x, y);
        } else if (drawingInfo == 2) {
            mPath[member].reset();
        }

        invalidate();
    }
}


