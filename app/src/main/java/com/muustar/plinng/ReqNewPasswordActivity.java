package com.muustar.plinng;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ReqNewPasswordActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private TextInputLayout mEmail;
    private Button mReqBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_new_password);

        setSupportActionBar((Toolbar) findViewById(R.id.reqnewpasswd_toolbar));
        getSupportActionBar().setTitle(R.string.new_password_request);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = findViewById(R.id.reqnewpasswd_email);
        mReqBtn = findViewById(R.id.reqnewpasswd_reqbutton);

        final String email = getIntent().getStringExtra("email");
        mEmail.getEditText().setText(email);

        mReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = mEmail.getEditText().getText().toString().trim();

                if (!TextUtils.isEmpty(emailAddress)) {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Email sent.");
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }
}
