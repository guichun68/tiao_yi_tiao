package com.wechat.tiaotiao;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

/**
 * Created by caorb1 on 2017/12/30.
 * Desc:
 */

public class MyDrawView extends SurfaceView{
    private final String TAG = MyDrawView.class.getSimpleName();
    // SurfaceHolder实例
    private SurfaceHolder mSurfaceHolder;
    // Canvas对象
    private Canvas mCanvas;
    // 控制子线程是否运行
    private boolean startDraw;
    // Path实例
    private Path mPath = new Path();
    // Paint实例
    private Paint mpaint = new Paint();

    private float startX,startY;
    private float endX,endY;

    public MyDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(); // 初始化
    }

    private double distance;

    private void initView() {
        mSurfaceHolder = getHolder();
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//设置背景透明

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        // 设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        this.setKeepScreenOn(true);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() > 1) {
                    return false;
                }

                //获取手指的操作--》按下、移动、松开
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN://按下
                        startX = event.getX();
                        startY = event.getY();
                        endX = startX;
                        endY=startY;
                        Log.e(TAG, "ACTION_DOWN,坐标："+startX+","+startY);
                        break;

                    case MotionEvent.ACTION_MOVE://移动
                        Log.i(TAG, "ACTION_MOVE");
                        draw(startX,startY,event.getX(),event.getY());
                        break;
                    case MotionEvent.ACTION_UP://松开
                       /* System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                        if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {
                            Log.e(TAG,"ACTION_UP,拦截");
                            return true;
                        }*/

                        endX = event.getX();
                        endY = event.getY();
                        Log.i(TAG, "ACTION_UP,坐标："+endX+","+endY);
                        distance = calcDistance(startX, startY, endX, endY);
                        Log.e(TAG,"ACTION_UP,跳跃起始终止点坐标（"+startX+"，"+startY+"; "+endX+"，"+endY+")->距离："+distance);
                        draw(startX,startY,endX,endY);
                        /*if(isCMDJump){
                            Log.i(TAG,"ACTION_UP isCMDJump is true!");
                            jump(dis);
                        }else{
                            Log.i(TAG,"ACTION_UP isCMDJump is false!");
                            jump(dis);
                        }*/
//                        jump(dis);
                        break;
                }
                return true;
            }
        });

    }

    public double getDistance() {
        return distance;
    }

    private double calcDistance(double startX, double startY, double endX, double endY){
        double x_= Math.pow((startX - endX),2);
        double y_ = Math.pow((startY-endY),2);
        return Math.sqrt(x_+y_);
    }

/*    @Override
    public void run() {
        // 如果不停止就一直绘制
        while (startDraw) {
            // 绘制
            draw(0,0,0,0);
        }
    }*/

    /*
     * 创建
     */
/*    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDraw = true;
//        new Thread(this).start();
    }*/

    /*
     * 改变
     */
 /*   @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
*/
    /*
     * 销毁
     */
/*    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        startDraw = false;
    }*/



    private void draw(float startX,float startY,float endX,float endY) {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
//            mCanvas.drawColor(Color.argb(100,204,204,204));
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mpaint.setStyle(Paint.Style.STROKE);

            mpaint.setStrokeWidth(DensityUtil.px2dip(getContext(), 30));
            mpaint.setColor(Color.BLACK);
//            mCanvas.drawPath(mPath, mpaint);
            mCanvas.drawLine(startX,startY,endX,endY,mpaint);
            if(mListener != null){
                mListener.onFinish(startX,startY,endX,endY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 对画布内容进行提交
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }


    // 重置画布
    public void reset() {
        mPath.reset();
    }

    private OnDrawLineFinish mListener;
    public void setOnDrawFinishListener(OnDrawLineFinish listener){
        this.mListener = listener;
    }


    private void jump(double distance) {
        if(distance<=20){
            Toast.makeText(getContext(), "距离太短,请重新划定", Toast.LENGTH_SHORT).show();
            return;
        }
        FloatWindowService.mJumpListener.onJumpClicked(distance);
    }

    interface OnDrawLineFinish{
        void onFinish(float startX,float startY,float endX,float endY);
    }
}
