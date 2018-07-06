package com.muustar.feco.plinng;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String REQUEST_TYPE = "request_type";
    private static final String RECEIVED = "received";
    private static final String SENT = "sent";
    private static final String NOT_FRIEND = "not_friend";
    private static final String REQ_SENT = "req_sent";
    private static final String REQ_RECEIVED = "req_received";
    private static final String FRIEND = "friend";
    private TextView mProfileDisplayName, mProfileStatus, mProfileTotalFriends, mProfileEmail;
    private ImageView mProfileImage;
    private Button mProfileSendFrndsReq, mProfileDeclineBtn;
    private String user_id;
    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    private DatabaseReference mUserDatabase, mFriendReqDatabase, mFriendDatabase,
            mNotificationDatabase;
    private DatabaseReference mRootRef;

    private long total_friends;
    private SharedPreferences mSharedPref;
    private Constant constant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPref = getSharedPreferences("Plinng", MODE_PRIVATE);
        constant.mAppTheme = mSharedPref.getInt("theme", constant.theme);
        constant.mColorValue = mSharedPref.getInt("color", constant.color);
        constant.mColorPosition = mSharedPref.getInt("position", 0);
        setTheme(constant.mAppTheme);
        setContentView(R.layout.activity_profile);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                ;
            }
        }

        user_id = getIntent().getStringExtra("uid");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendReqDatabase.keepSynced(false);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(false);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child
                ("Notifications");
        mNotificationDatabase.keepSynced(false);
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileDisplayName = findViewById(R.id.profile_displayname);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileTotalFriends = findViewById(R.id.profile_totalFriends);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileSendFrndsReq = findViewById(R.id.profile_sendReqBtn);
        mProfileDeclineBtn = findViewById(R.id.profile_declineBtn);
        mProfileEmail = findViewById(R.id.profile_email);

        mProfileDeclineBtn.setEnabled(false);
        mProfileDeclineBtn.setVisibility(View.INVISIBLE);

        //megvélő adatok betöltése ------------------------------------------------------
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u;
                if (!dataSnapshot.exists()) {
                    //private String name, status, image, image_thumb, email, uid;
                    u = new User("Törölt profile", "...", "default", "default", "törölt", "null",
                            false);
                } else {
                    u = dataSnapshot.getValue(User.class);
                }

                mProfileDisplayName.setText(u.getName());
                mProfileStatus.setText(u.getStatus());

                //mProfileEmail.setVisibility(View.GONE);
                if (u.getEmail_visible()){
                    mProfileEmail.setText(u.getEmail());
                    mProfileEmail.setVisibility(View.VISIBLE);
                }

                GlideApp
                        .with(getApplicationContext())
                        .load(u.getImage())
                        .error(R.mipmap.ic_placeholder_face)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mProfileImage);
                // total friends
                mFriendDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        total_friends = dataSnapshot.getChildrenCount();
                        mProfileTotalFriends.setText(getString(R.string.have) + " " +
                                total_friends + " " + getString(R.string.friends_have));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ------------------------- FRIEND REQUEST FEATURES -------------------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(user_id)) {
                                    String req_type = dataSnapshot.child(user_id).child
                                            (REQUEST_TYPE)
                                            .getValue().toString();

                                    // ha van friend request akkor el kell dönteni, hogy kapom,
                                    // vagy küldöm
                                    if (req_type.equals(RECEIVED)) {

                                        mProfileSendFrndsReq.setEnabled(true);
                                        mCurrent_state = REQ_RECEIVED;
                                        mProfileSendFrndsReq.setVisibility(View.VISIBLE);
                                        mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq
                                                .getContext().getResources().getColor(R.color
                                                        .myGreen));
                                        mProfileSendFrndsReq.setText(R.string
                                                .accept_friend_request);

                                        mProfileDeclineBtn.setEnabled(true);
                                        mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                    } else if (req_type.equals(SENT)) {

                                        mCurrent_state = REQ_SENT;
                                        mProfileSendFrndsReq.setVisibility(View.VISIBLE);
                                        mProfileSendFrndsReq.setText(R.string
                                                .cancel_friend_request);
                                    }
                                } else {
                                    // ha nincs friend request, az lehet azért mert még nem volt
                                    // kérés,
                                    // vagy mert már barátok vagyunk
                                    // nézzük, meg h barátok vagyunk -e

                                    mFriendDatabase.child(mCurrent_user.getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot
                                                                                 dataSnapshot) {

                                                    if (dataSnapshot.hasChild(user_id)) {
                                                        mProfileSendFrndsReq.setEnabled(true);
                                                        mProfileSendFrndsReq.setVisibility(View
                                                                .VISIBLE);
                                                        mProfileSendFrndsReq.setBackgroundColor
                                                                (Color.GRAY);
                                                        mCurrent_state = FRIEND;
                                                        mProfileSendFrndsReq.setText(R.string
                                                                .unfriend);
                                                    } else {
                                                        mProfileSendFrndsReq.setEnabled(true);
                                                        mProfileSendFrndsReq.setVisibility(View
                                                                .VISIBLE);
                                                        mProfileSendFrndsReq.setBackgroundColor
                                                                (mProfileSendFrndsReq.getContext
                                                                        ().getResources()
                                                                        .getColor(R.color
                                                                                .colorAccent));
                                                        mCurrent_state = NOT_FRIEND;
                                                        mProfileSendFrndsReq.setText(R.string
                                                                .send_friend_request);
                                                        // ------ saját profil ellenőrzés -------
                                                        if (TextUtils.equals(user_id,
                                                                mCurrent_user.getUid())) {
                                                            mProfileSendFrndsReq.setEnabled(false);
                                                            mProfileSendFrndsReq.setVisibility
                                                                    (View.INVISIBLE);

                                                            mProfileDeclineBtn.setEnabled(false);
                                                            mProfileDeclineBtn.setVisibility(View
                                                                    .INVISIBLE);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError
                                                                                databaseError) {

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

                    // notification
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child
                            (user_id).push();
                    String newNotificationId = newNotificationRef.getKey();
                    Map notificationDataMap = new HashMap<>();
                    notificationDataMap.put("from", mCurrent_user.getUid());
                    notificationDataMap.put("type", "friend_request");
                    notificationDataMap.put("seen", false);
                    notificationDataMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/" +
                            REQUEST_TYPE, SENT);
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/" +
                            REQUEST_TYPE, RECEIVED);
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId,
                            notificationDataMap);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference
                                databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There was some error.",
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                mProfileSendFrndsReq.setEnabled(true);
                                mCurrent_state = REQ_SENT;
                                mProfileSendFrndsReq.setText(R.string.cancel_friend_request);
                            }
                        }
                    });
                }

                // ------------------------- CANCEL REQUEST STATE -------------------
                // unfriend
                if (mCurrent_state.equals(REQ_SENT)) {
                    //Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();

                    Map cancelRequestMap = new HashMap();
                    cancelRequestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id,
                            null);
                    cancelRequestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(),
                            null);

                    mRootRef.updateChildren(cancelRequestMap, new DatabaseReference
                            .CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference
                                databaseReference) {
                            if (databaseError == null) {
                                mProfileSendFrndsReq.setEnabled(true);
                                mCurrent_state = NOT_FRIEND;
                                mProfileSendFrndsReq.setText(R.string.send_friend_request);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }

                // ------------------------- REQ RECEIVED STATE -------------------
                // as Accept Friend Request

                if (mCurrent_state.equals(REQ_RECEIVED)) {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date",
                            currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date",
                            currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    //barátság elfogadása után a notificationt automatikusan töröljük
                    final DatabaseReference notifyRef = FirebaseDatabase.getInstance()
                            .getReference().child("Notifications").child(mCurrent_user.getUid());
                    Query query = notifyRef.orderByChild("from").equalTo(user_id);
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.child("type").getValue().toString().equals
                                    ("friend_request")) {
                                String key = dataSnapshot.getKey();
                                notifyRef.child(key).removeValue();
                            }
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

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference
                                databaseReference) {
                            if (databaseError == null) {
                                mProfileSendFrndsReq.setEnabled(true);
                                mProfileSendFrndsReq.setBackgroundColor(Color.GRAY);
                                mCurrent_state = FRIEND;
                                mProfileTotalFriends.setText(getString(R.string.have) + " " +
                                        (++total_friends) + " " + getString(R.string.friends_have));
                                mProfileSendFrndsReq.setText(R.string.unfriend);
                                mProfileDeclineBtn.setEnabled(false);
                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }

                // ------------------------- FRIEND STATE -------------------
                // amikor már barátok vagyunk és az UnFriend gombra kattint.
                if (mCurrent_state.equals(FRIEND)) {

                    Map unfriendRequestMap = new HashMap();
                    unfriendRequestMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id,
                            null);
                    unfriendRequestMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(),
                            null);

                    mRootRef.updateChildren(unfriendRequestMap, new DatabaseReference
                            .CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference
                                databaseReference) {
                            if (databaseError == null) {
                                mProfileSendFrndsReq.setEnabled(true);
                                mCurrent_state = NOT_FRIEND;
                                mProfileTotalFriends.setText(getString(R.string.have) + " " +
                                        (--total_friends) + " " + getString(R.string.friends_have));
                                mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq
                                        .getContext().getResources().getColor(R.color.colorAccent));
                                mProfileSendFrndsReq.setText(R.string.send_friend_request);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            }
        });

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map cancelRequestMap = new HashMap();
                cancelRequestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                cancelRequestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                //barátság visszautasítása után a notificationt automatikusan töröljük
                final DatabaseReference notifyRef = FirebaseDatabase.getInstance().getReference()
                        .child("Notifications").child(mCurrent_user.getUid());
                Query query = notifyRef.orderByChild("from").equalTo(user_id);
                query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.child("type").getValue().toString().equals
                                ("friend_request")) {
                            String key = dataSnapshot.getKey();
                            notifyRef.child(key).removeValue();
                        }
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

                mRootRef.updateChildren(cancelRequestMap, new DatabaseReference
                        .CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference
                            databaseReference) {
                        if (databaseError == null) {
                            mProfileSendFrndsReq.setEnabled(true);
                            mCurrent_state = NOT_FRIEND;
                            mProfileSendFrndsReq.setBackgroundColor(mProfileSendFrndsReq
                                    .getContext().getResources().getColor(R.color.colorAccent));
                            mProfileSendFrndsReq.setText(R.string.send_friend_request);
                            mProfileDeclineBtn.setEnabled(false);
                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                        } else {
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid())
                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        });
            }
        });
    }
}
