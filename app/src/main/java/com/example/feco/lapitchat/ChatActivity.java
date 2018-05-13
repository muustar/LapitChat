package com.example.feco.lapitchat;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
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
    private final List<Messages> tempList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private Messageadapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 5;
    private int mCurrentPage = 1;
    private SwipeRefreshLayout mRefreshLayout;
    private int itemPosition = 0;
    private String mLastKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
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
                startActivity(profileIntent);
            }
        });

        // --- menti a last seen időpontját
        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                //itemPosition = 0;
                //Log.d("FECO", "mLastKey: " + mLastKey);
                loadMoreMessages();
                Log.d("FECO", "loadmore után tempsize: " + tempList.size());
                Log.d("FECO", "itemposition loadmore után " + itemPosition);
                if (tempList.size() > 0 && tempList.size() < TOTAL_ITEMS_TO_LOAD) {
                    mLastKey = tempList.get(0).getNodeKey();
                    tempList.remove(tempList.size() - 1);
                    messagesList.addAll(0, tempList);
                    tempList.clear();

                    mAdapter.notifyDataSetChanged();
                    //mMessageList.smoothScrollToPosition(itemPosition);
                    mLinearLayout.scrollToPositionWithOffset(TOTAL_ITEMS_TO_LOAD,0);
                    mRefreshLayout.setRefreshing(false);
                    tempList.clear();
                }
                itemPosition = 0;

            }
        });
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                message.setNodeKey(dataSnapshot.getKey());

                // ezt még érdemes nézegetni
                // https://stackoverflow.com/questions/37711220/firebase-android-pagination

                tempList.add(message);
                Log.d("FECO", "tempsize: " + tempList.size());
                if (tempList.size() == TOTAL_ITEMS_TO_LOAD) {
                    mLastKey = tempList.get(0).getNodeKey();
                    Log.d("FECO", "mLastKey: " + mLastKey);
                    //Collections.reverse(tempList);
                    tempList.remove(tempList.size() - 1);
                    /*for (Messages m : tempList) {
                        messagesList.add(0, m);
                        mAdapter.notifyDataSetChanged();
                    }*/

                    messagesList.addAll(0, tempList);

                    tempList.clear();
                }

                // messagesList.add(itemPosition,message);


                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(itemPosition);
                mRefreshLayout.setRefreshing(false);
                itemPosition++;


                /*mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(itemPosition);
                mRefreshLayout.setRefreshing(false);
                */

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
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                if (itemPosition == 0) {
                    mLastKey = dataSnapshot.getKey();
                    Log.d("FECO", "mLastKey: " + mLastKey);
                }
                itemPosition++;

                messagesList.add(message);

                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size() - 1);
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
        });
    }

    private void sendMessage() {

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

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("ERROR", databaseError.getMessage().toString());
                    } else {
                        mChatSendBtn.setEnabled(true);
                        mChatMessageEdT.setText("");
                    }
                }
            });
        }
    }


}
