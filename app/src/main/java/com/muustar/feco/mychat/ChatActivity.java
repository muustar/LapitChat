package com.muustar.feco.mychat;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int GALLERY_PICK_REQ = 4;
    private Toolbar mChatToolbar;
    private TextView mTitle;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageView mChatAddBtn;
    private EditText mChatMessageEdT;
    private ImageView mChatSendBtn;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID; // a programot futtató IDja
    private String mChatUser; // akivel a beszélgetés folyik
    private String mChatUserName;
    private String mChatUserImg;

    private RecyclerView mMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 5;
    private SwipeRefreshLayout mRefreshLayout;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private StorageReference mImageStorage;
    private Query messageQuery;
    private ChildEventListener loadMessageChildEvent;
    private ChildEventListener requestTorloEventListener;
    private DatabaseReference mUsersRef;
    private DatabaseReference mNotifyRef;
    private DatabaseReference mMessagesRef;
    private int colorMyChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        SharedPreferences sharedPref = getSharedPreferences("colorInfo", Context.MODE_PRIVATE);
        int mAppTheme = sharedPref.getInt("theme", -1);
        int mColorValue = sharedPref.getInt("color",0);
        int colorPosition = sharedPref.getInt("position",0);

        if (mAppTheme == -1) {
            setTheme(Constant.theme);
        } else {
            setTheme(mAppTheme);
        }
        */
        int colorPosition = Constant.mColorPosition;
        int mColorValue = Constant.mColorValue;
        setTheme(Constant.mAppTheme);

        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mUsersRef = mRootRef.child("Users");
        mNotifyRef = mRootRef.child("Notifications");
        mMessagesRef = mRootRef.child("messages");
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("uid");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImg = getIntent().getStringExtra("img");

        mChatAddBtn = findViewById(R.id.chat_add);
        mChatMessageEdT = findViewById(R.id.chat_message);
        mChatSendBtn = findViewById(R.id.chat_send);
        mMessageList = findViewById(R.id.message_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);


        mAdapter = new MessageAdapter(messagesList, colorPosition);
        mMessageList.setAdapter(mAdapter);

        loadMessages();
        loadSeenStatus();

        //Action bar kialakítása egyedire
        mChatToolbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setTitle(mChatUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View action_bar_view = inflater.inflate(R.layout
                .chat_custom_bar, null);

        mTitle = action_bar_view.findViewById(R.id.custom_bar_title);
        mTitle.setText(mChatUserName);
        mLastSeen = action_bar_view.findViewById(R.id.custom_bar_seen);
        mProfileImage = action_bar_view.findViewById(R.id.custom_bar_image);
        GlideApp
                .with(this)
                .load(mChatUserImg)
                .placeholder(R.mipmap.ic_placeholder_face)
                .error(R.mipmap.ic_placeholder_face)
                .into(mProfileImage);

        actionBar.setCustomView(action_bar_view);

        // ---  beállítja az online statust,  last seen-t
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("online")) {
                    String seen = dataSnapshot.child("online").getValue().toString();
                    if (seen.equals("true")) {
                        mLastSeen.setText(R.string.online);
                    } else {

                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        long onlineTime = Long.parseLong(seen);
                        String lastSeen = getTimeAgo.getTimeAgo(onlineTime, ChatActivity.this);
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
                    ActivityOptions options;
                    options = ActivityOptions
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

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),
                        GALLERY_PICK_REQ);
            }
        });


    }

    private void loadSeenStatus() {
        Query querySeen = mMessagesRef.child(mCurrentUserID).child(mChatUser).orderByKey()
                .limitToLast(1);
        querySeen.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.d("FECO", "seen: " + dataSnapshot.child("seen").toString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_REQ && resultCode == RESULT_OK) {
            chatOpening();
            chatNotification();
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + "" +
                    ".jpg");

            // képet küldünk üzenetben
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask
                    .TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);

                        Map<String, Object> messageUserMap = new HashMap<String, Object>();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference
                                .CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference
                                    databaseReference) {
                                if (databaseError != null) {
                                    Log.d("ERROR", databaseError.getMessage());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child
                (mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast
                (TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                message.setNodeKey(messageKey);

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);
                } else {

                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {

                    mLastKey = messageKey;
                }

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

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child
                (mChatUser);
        messageQuery = messageRef.limitToLast(30);

        final DatabaseReference seenef = mRootRef.child("messages").child(mChatUser).child
                (mCurrentUserID);

        loadMessageChildEvent = new ChildEventListener() {
            public Messages message;
            public Boolean b;

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                message.setNodeKey(messageKey);

                // a megnyitott üzenetnél oda tesszük h olvasott
                //String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
                String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
                String push_id = dataSnapshot.getKey();
                mRootRef.child(current_user_ref).child(push_id).child("seen").setValue(true);

                // a megnyitott üzenetnél oda tesszük h olvasott <---

                itemPos++;

                if (itemPos == 1) {

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                loadSeenStatus();
                if (!mCurrentUserID.equals(message.getFrom())) {

                    // vibrálás ha érkezik uj üzenet
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect
                                .DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(200);
                    }
                }

                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size() - 1);
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

        // ha betöltjük az üzeneteket, akkor a hozzá tartozó értesítéseket töröljük
        requestTorles();

        //messageQuery.addChildEventListener(loadMessageChildEvent);

    }

    private void requestTorles() {
        // ha betöltjük az üzeneteket, akkor a hozzá tartozó értesítéseket töröljük
        mNotifyRef.child(mCurrentUserID);
        requestTorloEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();

                NotificationType n = dataSnapshot.getValue(NotificationType.class);
                if (n.getFrom().equals(mChatUser)) {
                    if (n.getType().equals("new_message")) {
                        mNotifyRef.child(mCurrentUserID).child(key).removeValue();
                    }
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
        };
    }

    private void chatOpening() {
        // ez hozza létre az adatbátisban a "Chat" táblát ami leírja milyen csetek vannak nyitva
        // , az üzenetek et a messges táblába tároljuk
        Map<String, Object> chatAddMap = new HashMap<String, Object>();
        chatAddMap.put("seen", false);
        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

        Map<String, Object> chatUserMap = new HashMap<>();
        chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, chatAddMap);
        chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID, chatAddMap);

        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    Log.d("ERROR", databaseError.getMessage());
                }
            }
        });
    }

    private void sendMessage() {
        chatOpening();

        // csak akkor futtassuk le ha a másik user nem "online", vagyis ha meg van éppen nyitva a
        // cset ablak akkor ne futtassunk értesítést.
        mUsersRef.child(mChatUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("chat_window_open").exists()) {
                    // Log.d("FECO", "chat_windows_open: " + dataSnapshot.child
                    // ("chat_window_open").getValue() + " mCurrent: " + mCurrentUserID);
                    if (!dataSnapshot.child("chat_window_open").getValue().equals(mCurrentUserID)) {
                        chatNotification();
                    }
                } else {
                    chatNotification();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //chatNotification();

        String message = mChatMessageEdT.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            mChatSendBtn.setEnabled(false);
            String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            Map<String, Object> messageMap = new HashMap<String, Object>();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            Map<String, Object> messageUserMap = new HashMap<String, Object>();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageEdT.setText("");
            mChatSendBtn.setEnabled(true);
            mMessageList.scrollToPosition(messagesList.size() + 1);
            mLinearLayout.scrollToPositionWithOffset(messagesList.size() + 1, 2);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference
                        databaseReference) {
                    if (databaseError != null) {
                        Log.d("ERROR", databaseError.getMessage());
                    }
                }
            });
        }
    }

    private void chatNotification() {

        // notifications adatbázisba bejegyzés
        // ez az új üzenet írásakor fut le

        DatabaseReference newNotificationRef = mNotifyRef.child(mChatUser).push();
        String newNotificationId = newNotificationRef.getKey();
        Map<String, Object> notificationDataMap = new HashMap<String, Object>();
        notificationDataMap.put("from", mCurrentUserID);
        notificationDataMap.put("type", "new_message");
        notificationDataMap.put("seen", false);
        notificationDataMap.put("timestamp", ServerValue.TIMESTAMP);

        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("Notifications/" + mChatUser + "/" + newNotificationId, notificationDataMap);

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(ChatActivity.this, "There was some error.", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesList.clear();
        messageQuery.addChildEventListener(loadMessageChildEvent);
        mNotifyRef.child(mCurrentUserID).addChildEventListener(requestTorloEventListener);

        // chehck the user logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child
                    ("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.child("online").setValue("true");
            // a chat üzenet nyitva ablakban nem elég azt jelezni, hogy nyitva van az ablak, azt
            // is jelezni kell, hogy éppen kinek az ablka van nyitva.
            mUserDatabase.child("chat_window_open").setValue(mChatUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager nMgr = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // chehck the user logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child
                    ("Users").child(currentUser.getUid());
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            mUserDatabase.child("chat_window_open").setValue("false");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageQuery.removeEventListener(loadMessageChildEvent);
        mNotifyRef.child(mCurrentUserID).removeEventListener(requestTorloEventListener);
    }


}
