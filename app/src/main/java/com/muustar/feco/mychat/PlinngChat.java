package com.muustar.feco.mychat;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Készítette: feco
 * 2018.05.03.
 */
public class PlinngChat extends Application {
    private DatabaseReference mUserDatabase;
    protected FirebaseAuth mAuth;

    public void showKeyboard(View v){
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard(View v){
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);  // helyben szinkronhoz
        // manifestben: android:name=".PlinngChat">

        // Glide sync
        // http://bumptech.github.io/glide/doc/migrating.html#generated-api

        //animáció
        //Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_offline);
        //mOnlineDot.startAnimation(animation);

        //https://github.com/akshayejh/Lapit---Android-Firebase-Chat-App

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }


}
