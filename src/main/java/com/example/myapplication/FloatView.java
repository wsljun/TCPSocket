package com.example.myapplication;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by 李均 on 2017/3/16.
 */

public class  FloatView extends LinearLayout {
    private final ImageButton btnRaise;
    private  WindowManager mWindowManager;
    private View mFloatView;
    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;

    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    private  Context mContext;

    private int screenWidth;
    private int screenHeight;


    public FloatView(final Context context) {
        this(context,null);
        this.mContext = context;

    }

    public FloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        mFloatView = LayoutInflater.from(context).inflate(R.layout.layout_raise, this); //
        mFloatView = View.inflate(mContext,R.layout.layout_raise,this);
        btnRaise = (ImageButton) mFloatView.findViewById(R.id.btnRaise);
        initWindowManager();
        initOnClick();
    }

    /**
     * 初始化窗口设置
     * 6.0之上需要动态获取权限
     */
    public void initWindowManager(){
        //获取WindowManager
        mWindowManager=(WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
//        mScreenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        //设置LayoutParams(全局变量）相关参数
        wmParams = new WindowManager.LayoutParams();
        wmParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // 系统提示类型,重要 | TYPE_PHONE
        wmParams.format=1;
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
//        wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
//        wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制
           // 设置Window flag
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.alpha = 1.0f;

        wmParams.gravity= Gravity.LEFT|Gravity.TOP;   //调整悬浮窗口至左上角
        //以屏幕左上角为原点，设置x、y初始值
        wmParams.x = screenWidth;
        wmParams.y = 0;


        //设置悬浮窗口宽高
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //显示myFloatView图像
//        mWindowManager.addView(this, wmParams);
    }

    public void showFloatView(){
        //显示myFloatView图像
        mWindowManager.addView(this, wmParams);
    }

    public void removeFloatView(){
        if(mWindowManager!=null){
            mWindowManager.removeView(this);
        }
    }

    public ImageButton getBtnRaise(){
        return btnRaise;
    }
    private void initOnClick() {
//        btnRaise.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isAppAtBackground(mContext)) {
//                    Intent intent = new Intent(mContext, MainActivity.class);
////            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(intent);
//                }
//            }
//        });

        btnRaise.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mEndX = (int) event.getRawX();
                        mEndY = (int) event.getRawY();
                        if (needIntercept()) {
                            //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                            wmParams.x = (int) event.getRawX() - getMeasuredWidth() / 2;
                            wmParams.y = (int) event.getRawY() - getMeasuredHeight() / 2-50;//mView.getMeasuredHeight()
                            mWindowManager.updateViewLayout(mFloatView, wmParams);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (needIntercept()) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mStartX = (int) event.getRawX();
//                mStartY = (int) event.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mEndX = (int) event.getRawX();
//                mEndY = (int) event.getRawY();
//                if (needIntercept()) {
//                    //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
//                    wmParams.x = (int) event.getRawX() - getMeasuredWidth() / 2;
//                    wmParams.y = (int) event.getRawY() - getMeasuredHeight() / 2;//mView.getMeasuredHeight()
//                    mWindowManager.updateViewLayout(mFloatView, wmParams);
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (needIntercept()) {
//                    return true;
//                }
//                break;
//            default:
//                break;
//        }
//        return true;
//    }


    /**
     * 是否拦截
     * @return true:拦截;false:不拦截.
     */
    private boolean needIntercept() {
        if (Math.abs(mStartX - mEndX) > 30 || Math.abs(mStartY - mEndY) > 30) {
            return true;
        }
        return false;
    }


    /**
     *判断当前应用程序处于前台还是后台
     */
    private boolean isAppAtBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

}
