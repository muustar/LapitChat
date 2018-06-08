package com.muustar.feco.mychat;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class UpdateActivity extends AppCompatActivity {
    private TextView mUpdatetext;
    private Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //téma betöltése
        SharedPreferences mSharedPref = getSharedPreferences("colorInfo", MODE_PRIVATE);
        constant.mAppTheme = mSharedPref.getInt("theme", constant.theme);
        constant.mColorValue = mSharedPref.getInt("color", constant.color);
        constant.mColorPosition = mSharedPref.getInt("position", 0);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_update);

        setSupportActionBar((Toolbar) findViewById(R.id.update_appbar));
        getSupportActionBar().setTitle("Update");

        String text = getIntent().getStringExtra("text");
        mUpdatetext = findViewById(R.id.updateText);
        mUpdatetext.setText(text);


    }
}
