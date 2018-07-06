package com.muustar.plinng;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;

import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.facebook.drawee.backends.pipeline.Fresco;
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


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "FECO";
    private static final int GALLERY_PICK_REQ = 4;
    private static final int REQ_CAMERA_CODE = 5;
    private static final int REQ_CAMERA_PERMISSION = 6;
    private static final int REQ_STUFF_PICK = 7;
    private Toolbar mChatToolbar;
    private TextView mTitle;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;
    private DotLoader mDotloader;

    private CircleImageView mChatAddBtn;
    private EditText mChatMessageEdT;
    private CircleImageView mChatSendBtn;

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

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
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
    private Constant constant;
    private String mNotificationTAG;
    private Boolean isVibrate; //a user beállítás szerint , ha igaz akkor vibrálhat, ha hamis
    private ChildEventListener vibrateChildEventListener;
    private ValueEventListener dotValueEventListener;
    private TextView mPostCount; // a címsávban jelzi, hány bejegyzés történt eddig
    private ValueEventListener counterEventListener;
    private DatabaseReference mPostCounter;
    private String mCurrentPhotoPath;
    // akkor nem

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //téma betöltése
        SharedPreferences mSharedPref = getSharedPreferences("Plinng", MODE_PRIVATE);
        constant.mAppTheme = mSharedPref.getInt("theme", constant.theme);
        constant.mColorValue = mSharedPref.getInt("color", constant.color);
        constant.mColorPosition = mSharedPref.getInt("position", 0);
        int colorPosition = Constant.mColorPosition;
        int mColorValue = Constant.mColorValue;
        setTheme(Constant.mAppTheme);
        setContentView(R.layout.activity_chat);

        // a képek miatt inicializáni kell a Frescot
        Fresco.initialize(this);

        // vibrációs beállítások
        SharedPreferences mSharedProfileSettingsPref = getSharedPreferences("Plinng",
                MODE_PRIVATE);
        isVibrate = mSharedProfileSettingsPref.getBoolean("vibrate", true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mUsersRef = mRootRef.child("Users");
        mUsersRef.keepSynced(true);
        mNotifyRef = mRootRef.child("Notifications");
        mNotifyRef.keepSynced(true);
        mMessagesRef = mRootRef.child("messages");
        mMessagesRef.keepSynced(true);
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
        mDotloader = findViewById(R.id.dot_loader);

        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        mAdapter = new MessageAdapter(messagesList, colorPosition, mChatUser);
        mMessageList.setAdapter(mAdapter);

        loadMessages();
        loadSeenStatus();

        //Action bar kialakítása egyedire
        mChatToolbar = findViewById(R.id.chat_appbar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View action_bar_view = inflater.inflate(R.layout
                .chat_custom_bar, null);

        mPostCount = action_bar_view.findViewById(R.id.custom_bar_counter);
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

        // forgó elküldés gomb
        final Boolean[] fordulhat = {true};
        mChatMessageEdT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.d(TAG, "sequence: " + s + " count: " + count);
                RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(500);
                rotate.setInterpolator(new LinearInterpolator());

                if (s.length() == 0) {
                    fordulhat[0] = true;
                    // gépelés befejeződött, mentük az adatbázisba
                    mUsersRef.child(mCurrentUserID).child("typing").setValue(false);
                }
                if (s.length() == 1) {
                    // gépelés megkezdődött, mentük az adatbázisba
                    mUsersRef.child(mCurrentUserID).child("typing").setValue(true);

                    // fordulás animáció
                    if (fordulhat[0]) {
                        mChatSendBtn.startAnimation(rotate);
                        fordulhat[0] = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mChatMessageEdT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.d(TAG, "onTouch: TOUCH" + event);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMessageList.scrollToPosition(messagesList.size() - 1);
                        mLinearLayout.scrollToPositionWithOffset(messagesList.size() - 1, 0);
                    }
                }, 200);
                return false;
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

        // kép küldése - az onActivityRequest folytatja a kezelést
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(ChatActivity.this, mChatAddBtn);
                popupMenu.inflate(R.menu.popup_menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.pup_camera) {

                            if (ContextCompat.checkSelfPermission(ChatActivity.this, android
                                    .Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_DENIED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new
                                                String[]{android.Manifest.permission.CAMERA},
                                        REQ_CAMERA_PERMISSION);
                                Toast.makeText(ChatActivity.this, "Kérek engedélyt a CAMERA-hoz. " +
                                        "(Alkalmazások/Plinng/Engedélyek)", Toast.LENGTH_SHORT)
                                        .show();
                            } else {

                                Intent takePictureIntent = new Intent(MediaStore
                                        .ACTION_IMAGE_CAPTURE);
                                // Ensure that there's a camera activity to handle the intent
                                if (takePictureIntent.resolveActivity(getPackageManager()) !=
                                        null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                    }
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                                "com.example.android.fileprovider",
                                                photoFile);
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                photoURI);
                                        startActivityForResult(takePictureIntent, REQ_CAMERA_CODE);
                                    }
                                }
                            }
                            return true;
                        } else if (item.getItemId() == R.id.pup_gallery) {

                            Intent galleryIntent = new Intent();
                            galleryIntent.setType("image/*");
                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(galleryIntent, "Select " +
                                            "Image"),
                                    GALLERY_PICK_REQ);
                            return true;
                        }

                        return false;
                    }
                });

                @SuppressLint("RestrictedApi") MenuPopupHelper menuHelper = new MenuPopupHelper
                        (ChatActivity.this, (MenuBuilder) popupMenu.getMenu(), mChatAddBtn);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();


                /*

                 */
            }
        });

        // bejegyzés számláló
        loadPostCounter();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadPostCounter() {
        mPostCounter = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);
        counterEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d(TAG, "onDataChange: count: "+dataSnapshot.getChildrenCount());
                long count = dataSnapshot.getChildrenCount();
                mPostCount.setText(String.valueOf(count) + " bejegyzés");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPostCounter.addValueEventListener(counterEventListener);
    }

    private void dotloaderInit() {

        dotValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("typing")) {
                    String window = dataSnapshot.child("chat_window_open").getValue().toString();
                    String typing = dataSnapshot.child("typing").getValue().toString();
                    //Log.d(TAG, "onDataChange: window: " + window + " typing: " + typing);
                    if (window.equals(mCurrentUserID) && typing.equals("true")) {
                        mDotloader.setVisibility(View.VISIBLE);
                    } else {
                        mDotloader.setVisibility(View.INVISIBLE);
                    }
                    if (typing.equals("false")) {
                        mDotloader.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUsersRef.child(mChatUser).addValueEventListener(dotValueEventListener);
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

                //Log.d("FECO", "seen: " + dataSnapshot.child("seen").toString());
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
            elkeszultKepKuldese(data);
        }

        if (requestCode == REQ_CAMERA_CODE && resultCode == RESULT_OK) {
            //elkeszultKepKuldese(data);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            elkeszultKepKuldese(mediaScanIntent);
        }
    }

    private void elkeszultKepKuldese(Intent data) {

        Uri imageUri = data.getData();

        String mimeType = getContentResolver().getType(imageUri);
        String typeExtension = "";
        if (mimeType == null) {
            String fileType = imageUri.getPath();
            typeExtension = fileType.substring(fileType.length() - 3, fileType.length());
        } else {
            typeExtension = mimeType.substring(mimeType.indexOf("/") + 1, mimeType.length());
        }

        chatOpening();

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatMode(Animation.RESTART);
        mChatAddBtn.startAnimation(rotateAnimation);
        mChatAddBtn.setEnabled(false);

        final String current_user_ref = "messages/" + mCurrentUserID + "/" + mChatUser;
        final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserID;

        DatabaseReference user_message_push = mRootRef.child("messages")
                .child(mCurrentUserID).child(mChatUser).push();
        final String push_id = user_message_push.getKey();

        StorageReference filepath = mImageStorage.child("message_images").child(push_id + "." +
                typeExtension);

        // képet küldünk üzenetben
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask
                .TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    String download_url = task.getResult().getDownloadUrl().toString();

                    // az értesítésben megküldjük a típusát és az urlt
                    String type = "image";
                    chatNotification(download_url, type);

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
                            } else {
                                mChatAddBtn.clearAnimation();
                                mChatAddBtn.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
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

        //a képernyő teteján megjelenített értéesítéseket törli
        NotificationManager nMgr = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child
                (mChatUser);
        messageQuery = messageRef.limitToLast(30);
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
                //loadSeenStatus();

                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size() - 1);
                mLinearLayout.scrollToPositionWithOffset(messagesList.size() - 1, 0);
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

        // ha betöltjük az üzeneteket, akkor a hozzá tartozó értesítéseket töröljük az adatbázisból
        requestTorles();
        // ha betöltjük az üzeneteket és van közöttük olyan amit még nem láttak akkor vibrálunk
        vibralasOlvasatlanEseten();
    }

    private void vibralasOlvasatlanEseten() {
        vibrateChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages m = dataSnapshot.getValue(Messages.class);
                if (!m.getSeen() && isVibrate && (m.getFrom() != mCurrentUserID)) {
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
        mMessagesRef.child(mCurrentUserID).child(mChatUser).addChildEventListener
                (vibrateChildEventListener);
    }

    private void requestTorles() {
        // ha betöltjük az üzeneteket, akkor a hozzá tartozó értesítéseket töröljük az adatbázisból
        requestTorloEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();

                NotificationType n = dataSnapshot.getValue(NotificationType.class);
                if (n.getFrom().equals(mChatUser)) {
                    if (n.getType().equals("new_message") || n.getType().equals("new_image")) {
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

        String message = mChatMessageEdT.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {

            //notifications kezelése
            String type = "message";
            ertesitesAzUzenetrol(message, type);

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

    private void ertesitesAzUzenetrol(final String message, final String type) {
        // csak akkor futtassuk le ha a másik user nem "online", vagyis ha meg van éppen nyitva a
        // cset ablak akkor ne futtassunk értesítést.
        mUsersRef.child(mChatUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("chat_window_open").exists()) {
                    // Log.d("FECO", "chat_windows_open: " + dataSnapshot.child
                    // ("chat_window_open").getValue() + " mCurrent: " + mCurrentUserID);
                    if (!dataSnapshot.child("chat_window_open").getValue().equals(mCurrentUserID)) {
                        chatNotification(message, type);
                    }
                } else {
                    chatNotification(message, type);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void chatNotification(String message, String type) {

        // notifications adatbázisba bejegyzés
        // ez az új üzenet írásakor fut le

        DatabaseReference newNotificationRef = mNotifyRef.child(mChatUser).push();
        String newNotificationId = newNotificationRef.getKey();
        Map<String, Object> notificationDataMap = new HashMap<String, Object>();
        notificationDataMap.put("from", mCurrentUserID);
        if (type.equals("message")) {
            notificationDataMap.put("type", "new_message");
        } else if (type.equals("image")) {
            notificationDataMap.put("type", "new_image");
        }

        notificationDataMap.put("seen", false);
        notificationDataMap.put("timestamp", ServerValue.TIMESTAMP);
        notificationDataMap.put("text", message);

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
        // géplést jelző pontok inicializálása
        dotloaderInit();
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
            mUserDatabase.child("typing").setValue("false");
        }
        mMessagesRef.child(mCurrentUserID).child(mChatUser).removeEventListener
                (vibrateChildEventListener);
        mUsersRef.child(mChatUser).removeEventListener(dotValueEventListener);
        mPostCounter.addValueEventListener(counterEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageQuery.removeEventListener(loadMessageChildEvent);
        mNotifyRef.child(mCurrentUserID).removeEventListener(requestTorloEventListener);
    }
}
