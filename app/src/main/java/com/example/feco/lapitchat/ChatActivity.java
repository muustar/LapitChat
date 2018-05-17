package com.example.feco.lapitchat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private static final int GALLERY_PICK_REQ = 4;
    private String mChatUser;
    private Toolbar mChatToolbar;
    private TextView mTitle;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageView mChatAddBtn;
    private EditText mChatMessageEdT;
    private ImageView mChatSendBtn;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private String mChatUserName;
    private String mChatUserImg;

    private RecyclerView mMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private Messageadapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 5;
    private int mCurrentPage = 1;
    private SwipeRefreshLayout mRefreshLayout;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private StorageReference mImageStorage;
    private Query messageQuery;
    private ChildEventListener loadMessageChildEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("uid");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImg = getIntent().getStringExtra("img");


        mChatAddBtn = (ImageView) findViewById(R.id.chat_add);
        mChatMessageEdT = (EditText) findViewById(R.id.chat_message);
        mChatSendBtn = (ImageView) findViewById(R.id.chat_send);
        mMessageList = (RecyclerView) findViewById(R.id.message_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mAdapter = new Messageadapter(messagesList);
        mMessageList.setAdapter(mAdapter);
        loadMessages();

        //Action bar kialakítása egyedire
        mChatToolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setTitle(mChatUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        mTitle = (TextView) action_bar_view.findViewById(R.id.custom_bar_title);
        mTitle.setText(mChatUserName);
        mLastSeen = (TextView) action_bar_view.findViewById(R.id.custom_bar_seen);
        mProfileImage = action_bar_view.findViewById(R.id.custom_bar_image);
        Glide.with(this).load(mChatUserImg).into(mProfileImage);

        actionBar.setCustomView(action_bar_view);

        // ---  beállítja az online statust,  last seen-t
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("online")) {
                    String seen = dataSnapshot.child("online").getValue().toString();
                    if (seen.equals("true")) {
                        mLastSeen.setText("online");
                    } else {

                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        long onlineTime = Long.parseLong(seen);
                        String lastSeen = getTimeAgo.getTimeAgo(onlineTime);
                        mLastSeen.setText(lastSeen);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // kattintás a profilképen feature
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                profileIntent.putExtra("uid", mChatUser);


                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    startActivity(profileIntent);
                } else {
                    // do something for phones running an SDK before lollipop
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View, String>(mProfileImage, "imageTrans");
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ChatActivity.this, pairs);

                    startActivity(profileIntent, options.toBundle());
                }
            }
        });




        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;

                loadMoreMessages();

            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK_REQ);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_REQ && resultCode == RESULT_OK) {
            chatOpening();
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            // képet küldünk üzenetben
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);


                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.d("ERROR", databaseError.getMessage().toString());
                                } else {

                                }
                            }
                        });

                    }
                }
            });
        }
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                //Log.d("FECO", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(0, 0);
                //Toast.makeText(ChatActivity.this, "loadmore", Toast.LENGTH_SHORT).show();

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

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);

        messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        loadMessageChildEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                // a megnyitott üzenetnél oda tesszük h olvasott

                String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
                String push_id = dataSnapshot.getKey();
                mRootRef.child(current_user_ref).child(push_id).child("seen").setValue(true);

                // a megnyitott üzenetnél oda tesszük h olvasott <---

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                //mMessageList.scrollToPosition(messagesList.size()+1);
                mLinearLayout.scrollToPositionWithOffset(messagesList.size() - 1, 0);
                //Toast.makeText(ChatActivity.this, "load_sima", Toast.LENGTH_SHORT).show();

                mRefreshLayout.setRefreshing(false);

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
        };

        //messageQuery.addChildEventListener(loadMessageChildEvent);

    }

    private void chatOpening(){
        Map chatAddMap = new HashMap();
        chatAddMap.put("seen", false);
        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

        Map chatUserMap = new HashMap();
        chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, chatAddMap);
        chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID, chatAddMap);

        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("ERROR", databaseError.getMessage().toString());
                }
            }
        });
    }

    private void sendMessage() {
       chatOpening();
       chatNotification();

        String message = mChatMessageEdT.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            mChatSendBtn.setEnabled(false);
            String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageEdT.setText("");
            mChatSendBtn.setEnabled(true);
            mMessageList.scrollToPosition(messagesList.size() + 1);
            mLinearLayout.scrollToPositionWithOffset(messagesList.size() + 1, 2);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("ERROR", databaseError.getMessage().toString());
                    } else {

                    }
                }
            });


        }
    }

    private void chatNotification() {

        // notification
        DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(mChatUser).push();
        String newNotificationId = newNotificationRef.getKey();
        Map notificationDataMap = new HashMap<>();
        notificationDataMap.put("from", mCurrentUserID);
        notificationDataMap.put("type", "new_message");
        notificationDataMap.put("seen", false);
        notificationDataMap.put("timestamp", ServerValue.TIMESTAMP);

        Map requestMap = new HashMap();
        requestMap.put("Notifications/" + mChatUser + "/" + newNotificationId, notificationDataMap);

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(ChatActivity.this, "There was some error.", Toast.LENGTH_SHORT).show();
                } else {

                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesList.clear();
        messageQuery.addChildEventListener(loadMessageChildEvent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageQuery.removeEventListener(loadMessageChildEvent);

    }
}
