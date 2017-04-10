package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by 李均 on 2017/3/17.
 * 动态显示和隐藏状态栏 测试
 */

public class SystemUIStateActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_ui_test);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide(); //隐藏ActionBar
        }
        //状态栏透明
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        //透明导航栏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn1:
                //显示状态栏，Activity不全屏显示(恢复到有状态的正常情况)
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                break;
            case R.id.btn2:
                //隐藏状态栏，同时Activity会伸展全屏显示
                linearLayout.setSystemUiVisibility(View.INVISIBLE);
                break;
            case R.id.btn3:
                //Activity全屏显示，且状态栏被隐藏覆盖掉。同上
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                break;
            case R.id.btn4:
                //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                break;

            case R.id.btn5:
                //同4
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                break;
            case R.id.btn6:
                //同4
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_LAYOUT_FLAGS);
                break;
            case R.id.btn7:
                //隐藏虚拟按键(导航栏)
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                break;
            case R.id.btn8:
                //状态栏显示处于低能显示状态(low profile模式)，状态栏上一些图标显示会被隐藏。
                linearLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                break;
        }

    }
}
