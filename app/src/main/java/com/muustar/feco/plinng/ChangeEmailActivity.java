package com.muustar.feco.plinng;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeEmailActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private FirebaseAuth mAuth;

    private TextInputLayout mChangeemailCurrentEmail, mChangeemailPassword, mChangeemailNewEmail;
    private Button mChangeBtn;
    private ProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_change_email);

        setSupportActionBar((Toolbar)findViewById(R.id.changeemail_appbar));
        getSupportActionBar().setTitle(R.string.change_email);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChangeemailCurrentEmail = findViewById(R.id.changeemail_email);
        mChangeemailPassword = findViewById(R.id.changeemail_password);
        mChangeemailNewEmail = findViewById(R.id.changeemail_new_email);
        mChangeBtn = findViewById(R.id.changeemail_button);
        mProgressbar = findViewById(R.id.changeemail_progressbar);

        mAuth = FirebaseAuth.getInstance();

        mChangeemailCurrentEmail.getEditText().setText(mAuth.getCurrentUser().getEmail());

        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentEmail = mChangeemailCurrentEmail.getEditText().getText().toString().trim();
                String password = mChangeemailPassword.getEditText().getText().toString().trim();
                final String newEmail = mChangeemailNewEmail.getEditText().getText().toString();

                if (!TextUtils.isEmpty(currentEmail) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(newEmail)){
                    mProgressbar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider
                            .getCredential(currentEmail, password);

                    // Prompt the user to re-provide their sign-in credentials
                    mAuth.getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        emailModositas();
                                    }
                                    else{
                                        Toast.makeText(ChangeEmailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                private void emailModositas() {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    user.updateEmail(newEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User email address updated.");
                                                        // frissíteni kell a Users táblát is
                                                        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                                                        mUserRef.child("email").setValue(newEmail);
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            });




                }
            }
        });
    }
}
