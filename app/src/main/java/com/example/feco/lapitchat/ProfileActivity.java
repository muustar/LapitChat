package com.example.feco.lapitchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private TextView mProfileDisplayName, mProfileStatus, mProfileTotalFriends;
    private ImageView mProfileImage;
    private Button mProfileSendFrndsReq;
    private String UID;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UID = getIntent().getStringExtra("uid");

        mProfileDisplayName = findViewById(R.id.profile_displayname);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileTotalFriends = findViewById(R.id.profile_totalFriends);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendFrndsReq = findViewById(R.id.profile_sendReqBtn);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = new User();
                u = dataSnapshot.getValue(User.class);
                mProfileDisplayName.setText(u.getName());
                mProfileStatus.setText(u.getStatus());
                mProfileTotalFriends.setText("sok");
                Glide.with(getApplicationContext()).load(u.getImage()).into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
