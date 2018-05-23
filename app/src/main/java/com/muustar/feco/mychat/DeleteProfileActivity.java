package com.muustar.feco.mychat;

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
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class DeleteProfileActivity extends AppCompatActivity {
    private TextInputLayout mDelEmail, mDelPassword;
    private Toolbar mToolbar;
    private Button mDelButton;

    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private DatabaseReference mRootRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        mToolbar = (Toolbar) findViewById(R.id.deleteprofile_appbar);
        mDelEmail = (TextInputLayout) findViewById(R.id.deleteprofile_email);
        mDelPassword = (TextInputLayout) findViewById(R.id.deleteprofile_password);
        mDelButton = (Button) findViewById(R.id.deleteprofile_button);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.delete_profile));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // --- TODO törölni kell!
        mDelEmail.getEditText().setText(mAuth.getCurrentUser().getEmail());

        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mDelEmail.getEditText().getText().toString().trim();
                String passw = mDelPassword.getEditText().getText().toString().trim();
                //Toast.makeText(DeleteProfileActivity.this, email+passw, Toast.LENGTH_SHORT).show();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(passw)) {
                    if (TextUtils.equals(email, mAuth.getCurrentUser().getEmail())) {
                        Log.d("FECO",""+mAuth.getCurrentUser());
                        //GoogleAuthProvider.getCredential(googleIdToken,null);
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(email, passw);

                        // Prompt the user to re-provide their sign-in credentials
                        mAuth.getCurrentUser().reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            felhasznaloiProfilTorlese();
                                        }
                                        else{
                                            Toast.makeText(DeleteProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Log.d("ERROR", "hiba van");
                    }
                }
            }
        });
    }

    private void felhasznaloiProfilTorlese() {
        try {

            StorageReference profilkep_utvonal = mStorageRef.child("profile_images").child(mCurrentUserId + ".jpg");
            profilkep_utvonal.delete();
            StorageReference profilkepThumb_utvonal = mStorageRef.child("profile_images").child(mCurrentUserId + ".jpg");
            profilkepThumb_utvonal.delete();


            mRootRef.child("Users").child(mCurrentUserId).removeValue();
            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(DeleteProfileActivity.this, "Felhasználó törölve", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        goToLogin();
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(DeleteProfileActivity.this, "Hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void goToLogin() {
        Intent goToLogin = new Intent(DeleteProfileActivity.this, StartActivity.class);
        startActivity(goToLogin);
        finish();
    }
}
