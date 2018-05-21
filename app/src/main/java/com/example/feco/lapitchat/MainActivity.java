package com.example.feco.lapitchat;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.MenuRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mNotifyDatabase;
    private MenuItem mDynamicMenuItem;
    private boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        // Tabs
        mViewPager = findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isVisible = true;
                } else {
                    isVisible = false;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // chehck the user logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // chehck the user logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToStart();
        } else {
            getSupportActionBar().setTitle(currentUser.getEmail());
            mNotifyDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications").child(currentUser.getUid());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (Build.VERSION.SDK_INT > 11) {
            invalidateOptionsMenu();
            mDynamicMenuItem.setVisible(isVisible);
            //menu.findItem(R.id.main_logout_btn).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.request_menu, menu);
        mDynamicMenuItem = menu.findItem(R.id.request_clear);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn) {

            // kilogoláskor a devicetokent töröljük

            Map tokenMap = new HashMap();
            tokenMap.put("device_token", null);
            mUserDatabase.updateChildren(tokenMap);
            FirebaseAuth.getInstance().signOut();
            sendToStart();


            // if (mAuth.getCurrentUser() == null) {
            //   sendToStart();
            //}
            return true;
        }

        if (item.getItemId() == R.id.main_account_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if (item.getItemId() == R.id.main_all_users) {
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
            return true;
        }

        if (item.getItemId() == R.id.request_clear) {
            mNotifyDatabase.removeValue();
        }
        return false;
    }


}
