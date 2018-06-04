package com.muustar.feco.mychat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mInputStatus;
    private Button mSaveBtn;
    private ImageView mImageDone;

    private DatabaseReference databaseReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences("colorInfo", Context.MODE_PRIVATE);
        int mAppTheme = sharedPref.getInt("theme", -1);
        int mColorValue = sharedPref.getInt("color",0);
        int colorPosition = sharedPref.getInt("position",0);

        if (mAppTheme == -1) {
            setTheme(Constant.theme);
        } else {
            setTheme(mAppTheme);
        }
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mCurrentUserId = mCurrentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);

        mToolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.change_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInputStatus = findViewById(R.id.status_input);

        mSaveBtn = findViewById(R.id.status_saveBtn);
        mImageDone = findViewById(R.id.status_imageDone);

        final String currentStatus = getIntent().getStringExtra("status");
        mInputStatus.getEditText().setText(currentStatus);

        //szöveg kijelölése
        int textLength = mInputStatus.getEditText().getText().length();
        mInputStatus.getEditText().setSelection(0, textLength);

        //show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = mInputStatus.getEditText().getText().toString().trim();
                if (!TextUtils.equals(currentStatus, newStatus)) {
                    //ha volt változás akkor mentünk
                    databaseReference.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(StatusActivity.this, "Save OK", Toast.LENGTH_SHORT).show();
                                mImageDone.setVisibility(View.VISIBLE);
                                mImageDone.setAlpha(1f);
                                new CountDownTimer(2000, 20) {

                                    public void onTick(long millisUntilFinished) {
                                        Float a = mImageDone.getAlpha();
                                        mImageDone.setAlpha(a - 0.01f);
                                    }

                                    public void onFinish() {
                                        mImageDone.setAlpha(0f);
                                        mImageDone.setVisibility(View.GONE);

                                    }
                                }.start();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
