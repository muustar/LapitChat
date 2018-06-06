package com.muustar.feco.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SplashSreenActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private TextView mContentText, mSzlogen;
    private SharedPreferences mSharedPref;
    private FrameLayout mFrame;
    Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPref = getSharedPreferences("colorInfo", MODE_PRIVATE);
        constant.mAppTheme = mSharedPref.getInt("theme", constant.theme);
        constant.mColorValue = mSharedPref.getInt("color", constant.color);
        constant.mColorPosition = mSharedPref.getInt("position", 0);
        Log.d(TAG, "onCreate: mAppTheme" + constant.mAppTheme);


        setTheme(constant.mAppTheme);

        setContentView(R.layout.activity_splash_sreen);
        mFrame = findViewById(R.id.frame);
        mFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        mContentText = findViewById(R.id.fullscreen_content);
        mContentText.setScaleX(0.2f);
        mContentText.animate().scaleXBy(1.0f).setDuration(3000);

        mSzlogen = findViewById(R.id.szlogen);
        mSzlogen.setScaleX(0.2f);
        mSzlogen.animate().scaleXBy(1.0f).setDuration(3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashSreenActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 2000);
    }
}
