package com.muustar.plinng;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class FullImageActivity extends AppCompatActivity {
    private ImageView mImage;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_full_image);

        ctx = FullImageActivity.this;

        String url = getIntent().getStringExtra("url");

        mImage = findViewById(R.id.fullimage);
        GlideApp
                .with(ctx)
                .load(url)
                .placeholder(R.mipmap.placholder_sandclock)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImage);
    }
}
