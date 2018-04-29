package com.example.feco.lapitchat;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private TextInputLayout mDisplayname, mEmail, mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebase section
        mAuth = FirebaseAuth.getInstance();


        // displeay elements imitializing
        mProgressBar = (ProgressBar)findViewById(R.id.reg_progressbar);
        mToolbar = (Toolbar)findViewById(R.id.reg_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.create_account);
        // setDisplayHomeAsUpEnabled(true) engedélyezi, hogy az appbaron ott legyen bal oldalt a kicsi nyíl
        // a manifestben meg kell határozni, ki a parent, oda mutat a visszanyíl
        //  <activity android:name=".RegisterActivity"
        //      android:parentActivityName=".StartActivity"></activity>
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDisplayname = (TextInputLayout) findViewById(R.id.reg_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_cerate_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayname.getEditText().getText().toString().trim();
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    register_user(display_name,email,password);
                }
            }
        });

    }

    private void register_user(String display_name, String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            Toast.makeText(RegisterActivity.this, "sikeres regisztráció ",Toast.LENGTH_SHORT).show();
            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}
