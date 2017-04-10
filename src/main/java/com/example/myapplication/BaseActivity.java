package com.example.myapplication;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

public class BaseActivity extends AppCompatActivity {
//    private final String TAG = this.getClass().getSimpleName();
    private final String TAG = "BaseActivity";
    private WindowManager.LayoutParams wmParams;
    private  WindowManager mWindowManager;
    private View mWindowView;
    FloatView mFloatView ;

    private  Context mContext;
    private RelativeLayout mrlContent ;

    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //unusable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
//        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED); //unusable
//        getWindow().addPrivateFlags(WindowManager.LayoutParams.PRIVATE_FLAG_HOMEKEY_DISPATCHED); //屏蔽home按键
        setContentView(R.layout.activity_base);
        mContext = this;
        initBlackWindowParams();//设置黑屏

        addWindowBlackView();

        //悬浮按钮
        mFloatView = new FloatView(this);
        mFloatView.showFloatView();
        mFloatView.getBtnRaise().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeWindowBlackView();
//                if (isAppAtBackground(mContext)) {
//                    Intent intent = new Intent(mContext, FullscreenActivity.class);
////                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
////                    finish();
//                }
            }
        });


        //监听home 键
        registerReceiver(mHomeKeyEventReceiver,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));


    }

    /**
     * 设置黑屏肃静
     */
    public void initBlackWindowParams() {
        //初始化需要添加的view
        mWindowView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_base_window, null);

        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        // 更多type：https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#TYPE_PHONE
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT ;// phone类型没有权限 ,6.0以上需要动态申请权限 &token 重要必须设置
//        wmParams.format = 1;//PixelFormat.TRANSLUCENT;
//        // 更多falgs:https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html#FLAG_NOT_FOCUSABLE
////        wmParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
//        wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
//        wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制
//
//
//        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
//        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //设置为系统UI,并且全屏显示
        mWindowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE //状态栏显示处于低能显示状态
                |View.SYSTEM_UI_FLAG_FULLSCREEN   //全屏显示并隐藏状态栏
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //view全屏显示，但是状态栏不隐藏
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY //api 19以上 沉浸式，显示状态栏
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //隐藏虚拟导航栏 状态栏不隐藏
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //隐藏虚拟导航栏
        );

    }


    public void addWindowBlackView() {
        mWindowManager.addView(mWindowView, wmParams);
    }

    public void removeWindowBlackView(){
        if (mWindowView != null) {
            //移除在window中添加的view
            Log.i(TAG, "removeView");
            mWindowManager.removeView(mWindowView);
            mWindowView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeWindowBlackView();
        mFloatView.removeFloatView();
        Log.i(TAG, "onDestroy");
        unregisterReceiver(mHomeKeyEventReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "ljonKeyDown: keyCode= "+keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            Toast.makeText(this, "Black", Toast.LENGTH_SHORT).show();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            return true;
        }
        return true;//super.onKeyDown(keyCode, event)
    }

    @Override
    public void onAttachedToWindow(){
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        Toast.makeText(this, "onAttachedToWindow", Toast.LENGTH_SHORT).show();
        super.onAttachedToWindow();
    }


//    @Override //dispatchKeyEvent
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        Log.d(TAG, "ljdispatchKeyEvent: keyCode= "+event.getKeyCode());
//        if(event.getKeyCode()== KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
//            Toast.makeText(this, "Black", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if(event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
//            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if(event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
//            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
//            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//        return super.dispatchKeyEvent(event);
//    }

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




    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "ljmHomeKeyEventReceiver,action= "+action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) { // 监听home键
                String reason = intent.getStringExtra(SYSTEM_REASON);
                // 表示按了home键,程序到了后台

            }
        }
    };




}
