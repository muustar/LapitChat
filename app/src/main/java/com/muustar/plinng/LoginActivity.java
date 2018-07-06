package com.muustar.plinng;

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
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private TextInputLayout mEmail, mPassword;
    private Button mLogin;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private TextView mNewPasswd;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_login);


        //firebase section
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        // displeay elements imitializing
        mProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.login);
        // setDisplayHomeAsUpEnabled(true) engedélyezi, hogy az appbaron ott legyen bal oldalt a kicsi nyíl
        // a manifestben meg kell határozni, ki a parent, oda mutat a visszanyíl
        //  <activity android:name=".RegisterActivity"
        //      android:parentActivityName=".StartActivity"></activity>
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = (TextInputLayout) findViewById(R.id.login_email);
        mPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLogin = (Button) findViewById(R.id.login_loginBtn);
        mNewPasswd = findViewById(R.id.login_newpassword);

        mNewPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPasswordIntent = new Intent(LoginActivity.this,ReqNewPasswordActivity.class);
                String emailAddress = mEmail.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(emailAddress)){
                    newPasswordIntent.putExtra("email", emailAddress);
                }
                startActivity(newPasswordIntent);

            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    loginUser(email, password);
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null){

                                // ------------- device token ---------------------
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                mUsersDatabase.child(user.getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                });


                            }
                        } else {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
