package com.example.feco.lapitchat;

import android.app.Application;

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
public class LapitChat extends Application {
    private DatabaseReference mUserDatabase;
    protected FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);  // helyben szinkronhoz
        // manifestben: android:name=".LapitChat">

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
