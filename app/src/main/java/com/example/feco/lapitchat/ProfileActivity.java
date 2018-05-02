package com.example.feco.lapitchat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private static final String REQUEST_TYPE = "request_type";
    private static final String RECEIVED = "received";
    private static final String SENT = "sent";
    private static final String NOT_FRIEND = "not_friend";
    private static final String REQ_SENT = "req_sent";
    private static final String REQ_RECEIVED = "req_received";
    private static final String FRIEND = "friend";
    private TextView mProfileDisplayName, mProfileStatus, mProfileTotalFriends;
    private ImageView mProfileImage;
    private Button mProfileSendFrndsReq, mProfileDeclineBtn;
    private String user_id;
    private FirebaseUser mCurrent_user;

    private String mCurrent_state;


    private DatabaseReference mUserDatabase, mFriendReqDatabase, mFriendDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("uid");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();


        mProfileDisplayName = findViewById(R.id.profile_displayname);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileTotalFriends = findViewById(R.id.profile_totalFriends);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendFrndsReq = findViewById(R.id.profile_sendReqBtn);
        mProfileDeclineBtn = findViewById(R.id.profile_declineBtn);


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = new User();
                u = dataSnapshot.getValue(User.class);
                mProfileDisplayName.setText(u.getName());
                mProfileStatus.setText(u.getStatus());

                Glide.with(getApplicationContext()).load(u.getImage()).into(mProfileImage);

                // total friends
                mFriendDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long total_friends = dataSnapshot.getChildrenCount();
                        mProfileTotalFriends.setText("have "+ total_friends + " friends");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ------------------------- FRIEND REQUEST FEATURES -------------------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child(REQUEST_TYPE).getValue().toString();

                            // ha van friend request akkor el kell dönteni, hogy kapom, vagy küldöm
                            if (req_type.equals(RECEIVED)) {

                                mProfileSendFrndsReq.setEnabled(true);
                                mCurrent_state = REQ_RECEIVED;
                                mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq.getContext().getResources().getColor(R.color.myGreen));
                                mProfileSendFrndsReq.setText("Accept Friend Request");

                            } else if (req_type.equals(SENT)) {

                                mCurrent_state = REQ_SENT;
                                mProfileSendFrndsReq.setText("Cancel Friend Request");
                            }
                        } else {
                            // ha nincs friend request, az lehet azért mert még nem volt kérés, vagy mert már barátok vagyunk
                            // nézzük, meg h barátok vagyunk -e

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {
                                        mProfileSendFrndsReq.setEnabled(true);
                                        mProfileSendFrndsReq.setBackgroundColor(Color.GRAY);
                                        mCurrent_state = FRIEND;
                                        mProfileSendFrndsReq.setText("UnFriend");
                                    } else {
                                        mProfileSendFrndsReq.setEnabled(true);
                                        mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq.getContext().getResources().getColor(R.color.colorAccent));
                                        mCurrent_state = NOT_FRIEND;
                                        mProfileSendFrndsReq.setText("Send Friend Request");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendFrndsReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendFrndsReq.setEnabled(false);
                // ------------------------- NOT FRIENDS STATE -------------------
                if (mCurrent_state.equals(NOT_FRIEND)) {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child(REQUEST_TYPE)
                            .setValue(SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child(REQUEST_TYPE)
                                        .setValue(RECEIVED).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProfileSendFrndsReq.setEnabled(true);
                                        mCurrent_state = REQ_SENT;
                                        mProfileSendFrndsReq.setText("Cancel Friend Request");
                                        //Toast.makeText(ProfileActivity.this, "success", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


                // ------------------------- CANCEL REQUEST STATE -------------------
                if (mCurrent_state.equals(REQ_SENT)) {

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendFrndsReq.setEnabled(true);
                                    mCurrent_state = NOT_FRIEND;
                                    mProfileSendFrndsReq.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                // ------------------------- REQ RECEIVED STATE -------------------
                // as Accept Friend Request

                if (mCurrent_state.equals(REQ_RECEIVED)) {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mProfileSendFrndsReq.setEnabled(true);
                                                                    mProfileSendFrndsReq.setBackgroundColor(Color.GRAY);
                                                                    mCurrent_state = FRIEND;
                                                                    mProfileSendFrndsReq.setText("UnFriend");
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }

                // ------------------------- FRIEND STATE -------------------
                // amikor már barátok vagyunk és az UnFriend gombra kattint.
                if (mCurrent_state.equals(FRIEND)) {
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendFrndsReq.setEnabled(true);
                                    mCurrent_state = NOT_FRIEND;
                                    mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq.getContext().getResources().getColor(R.color.colorAccent));
                                    mProfileSendFrndsReq.setText("Send Friend Request");
                                }
                            });
                        }
                    });

                }
            }
        });


    }
}
