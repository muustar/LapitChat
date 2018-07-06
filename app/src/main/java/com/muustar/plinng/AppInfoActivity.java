package com.muustar.plinng;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class AppInfoActivity extends AppCompatActivity {
    private TextView mAktualVersion, mNewVersion;
    private Button mUpdateBtn;
    private Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_app_info);

        setSupportActionBar((Toolbar)findViewById(R.id.appinfo_appbar));
        getSupportActionBar().setTitle("Infó");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAktualVersion = findViewById(R.id.appinfo_aktualVer);
        mNewVersion = findViewById(R.id.appinfo_elerhetoVer);
        mUpdateBtn = findViewById(R.id.appinfo_updateBtn);

        if (Constant.VERSION != Constant.AVAIABLE_VERSION){
            mUpdateBtn.setVisibility(View.VISIBLE);
        }

        mAktualVersion.setText(String.valueOf(Constant.VERSION));
        mNewVersion.setText(String.valueOf(Constant.AVAIABLE_VERSION));

    mUpdateBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // ugrunk az updatre
            Intent updateIntent = new Intent(AppInfoActivity.this, UpdateActivity.class);
            updateIntent.putExtra("text", constant.updateTXT);
            startActivity(updateIntent);
        }
    });
    }

}
