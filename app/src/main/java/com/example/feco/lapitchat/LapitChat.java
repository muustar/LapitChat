package com.example.feco.lapitchat;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Készítette: feco
 * 2018.05.03.
 */
public class LapitChat extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);  // helyben szinkronhoz
        // manifestben: android:name=".LapitChat">

        // Glide sync
        // http://bumptech.github.io/glide/doc/migrating.html#generated-api

    }
}
