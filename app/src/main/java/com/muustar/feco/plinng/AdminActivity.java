package com.muustar.feco.plinng;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mAdminMsg, mAdminVersion;
    private Button mAdminSend;
    private DatabaseReference mNotificationsRef, mUsersRef;
    private String mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_admin);

        mToolbar = findViewById(R.id.admin_appbar);
        mAdminMsg = findViewById(R.id.admin_message);
        mAdminSend = findViewById(R.id.admin_send_btn);
        mAdminVersion = findViewById(R.id.admin_version);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.admin);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        mAdminVersion.setText(String.valueOf(Constant.VERSION));

        mAdminSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mAdminMsg.getText().toString().trim();
                int newVersion = Integer.parseInt(mAdminVersion.getText().toString().trim());
                if (!TextUtils.isEmpty(message)) {
                    mAdminMsg.setText("");

                    Map versionMap = new HashMap<>();
                    versionMap.put("version", newVersion);
                    versionMap.put("release_date", ServerValue.TIMESTAMP);
                    versionMap.put("text", message);

                    DatabaseReference versionRef = FirebaseDatabase.getInstance().getReference()
                            .child("Ver");
                    versionRef.push().setValue(versionMap);

                    final Map notificationDataMap = new HashMap<>();
                    notificationDataMap.put("from", mCurrentUser);
                    notificationDataMap.put("type", "update");
                    notificationDataMap.put("seen", false);
                    notificationDataMap.put("text", message);
                    notificationDataMap.put("timestamp", ServerValue.TIMESTAMP);

                    mUsersRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            String user_id = dataSnapshot.getKey().toString();

                            /*
                            if (user_id.equals("USNwh8xQEFd9D01zaPBUgniK0Xv2") || user_id.equals
                                    ("lc8r6Zw0tue3fPLKUbNDmbpqSrf2")) {
                                mNotificationsRef.child(user_id).push().setValue
                                        (notificationDataMap);
                            }

                            */

                            mNotificationsRef.child(user_id).push().setValue
                                    (notificationDataMap);
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
}
