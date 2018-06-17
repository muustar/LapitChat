package com.muustar.feco.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;

public class SplashSreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashSreenActivity";
    private TextView mContentText, mSzlogen;
    private SharedPreferences mSharedPref;
    private FrameLayout mFrame;
    Constant constant;
    private int mVisibleTime;

    Boolean elso = false;
    Boolean masodik = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPref = getSharedPreferences("Plinng", MODE_PRIVATE);
        constant.mAppTheme = mSharedPref.getInt("theme", constant.theme);
        constant.mColorValue = mSharedPref.getInt("color", constant.color);
        constant.mColorPosition = mSharedPref.getInt("position", 0);
        mVisibleTime = mSharedPref.getInt("splash_time", 2000);
        setTheme(constant.mAppTheme);
        Constant.mVisibleTime = mVisibleTime;

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
        mContentText.animate().scaleXBy(1.0f).setDuration(mVisibleTime);

        mSzlogen = findViewById(R.id.szlogen);
        mSzlogen.setScaleX(0.2f);
        mSzlogen.animate().scaleXBy(1.0f).setDuration(mVisibleTime);

        // splash();

        elso = isReadStoragePermissionGranted();
        masodik = isWriteStoragePermissionGranted();

        if (elso && masodik) {
            splash();
        }
        Log.d(TAG, "permissions: " + isReadStoragePermissionGranted() + "  " +
                isWriteStoragePermissionGranted());
    }

    private void splash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashSreenActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, mVisibleTime);
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                        .READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Write Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Write Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                        .WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Write Permission is granted automatically");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: launch");
        switch (requestCode) {
            case 2:

                Log.d(TAG, "Write External storage");
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager
                            .PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    }
                    elso = true;
                }
                break;

            case 3:

                Log.d(TAG, "Read External storage1");

                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager
                            .PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                        //resume tasks needing this permission

                    }
                    masodik = true;
                }
                break;
        }
        if (elso || masodik) {
            splash();
        }
    }
}
