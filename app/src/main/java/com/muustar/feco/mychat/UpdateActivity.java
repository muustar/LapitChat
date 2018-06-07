package com.muustar.feco.mychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class UpdateActivity extends AppCompatActivity {
    private TextView mUpdatetext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_update);

        setSupportActionBar((Toolbar) findViewById(R.id.update_appbar));
        getSupportActionBar().setTitle("Update");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String text = getIntent().getStringExtra("text");
        mUpdatetext = findViewById(R.id.updateText);
        mUpdatetext.setText(text);


    }
}
