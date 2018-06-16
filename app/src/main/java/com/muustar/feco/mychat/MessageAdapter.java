package com.muustar.feco.mychat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Készítette: feco
 * 2018.05.21.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final String TAG = "FECO";

    private List<Messages> mMessageList;
    private int color; //a szín pozíciója
    private String mCurrenUserId;
    private String mChatuser;
    private Context ctx;
    private int lastPosition = -1;



    public MessageAdapter(List<Messages> mMessageList, int color, String mChatuser) {
        this.mMessageList = mMessageList;
        this.color = color;
        this.mChatuser = mChatuser;
    }

    public void add(Messages messages) {
        mMessageList.add(messages);
        notifyItemInserted(mMessageList.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.message_single_layout_me_uj,
                        parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.message_single_layout_other_uj,
                        parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }

        return viewHolder;
    }

    private int getBgDependsColor(int color) {
        // erre a API 19 miatt van szükség
        switch (color) {
            case 0:
                return R.drawable.message_text_background_color1;
            case 1:
                return R.drawable.message_text_background_color2;
            case 2:
                return R.drawable.message_text_background_color3;
            case 3:
                return R.drawable.message_text_background_color4;
            case 4:
                return R.drawable.message_text_background_color5;
            case 5:
                return R.drawable.message_text_background_color6;
            case 6:
                return R.drawable.message_text_background_color7;
            case 7:
                return R.drawable.message_text_background_color8;
            case 8:
                return R.drawable.message_text_background_color9;
            case 9:
                return R.drawable.message_text_background_color10;
            case 10:
                return R.drawable.message_text_background_color11;
            case 11:
                return R.drawable.message_text_background_color12;
            case 12:
                return R.drawable.message_text_background_color13;
            case 13:
                return R.drawable.message_text_background_color14;
            case 14:
                return R.drawable.message_text_background_color15;

            default:
                return R.drawable.message_text_background;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrenUserId = mAuth.getCurrentUser().getUid();

        if (TextUtils.equals(mMessageList.get(position).getFrom(),
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            configureMyChatViewHolder((MyChatViewHolder) holder, position);
        } else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
        }
    }

    private void setAnimation(View viewToAnimate, int position, int viewType) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            if (viewType == VIEW_TYPE_ME) {
                Animation animation = AnimationUtils.loadAnimation(ctx, android.R.anim
                        .slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            } else {
                Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_right);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }
    }

    //=============================================================================
    //  MY CHATVIEW HOLDER
    //=============================================================================
    private void configureMyChatViewHolder(final MyChatViewHolder myChatViewHolder, final int
            position) {

        //profilkép beállítása
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mCurrenUserId);
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String imageUrl = user.getImage_thumb();
                myChatViewHolder.setProfileImage(ctx, imageUrl);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //üzenet test betöltése
        String key = mMessageList.get(position).getNodeKey();
        if (mMessageList.get(position).getType().equals("image")) {
            myChatViewHolder.imageMessage.setVisibility(View.VISIBLE);
            myChatViewHolder.messageText.setVisibility(View.GONE);

            // erre a API 19 miatt van szükség
            myChatViewHolder.szoveghatterLinearLayout.setBackgroundResource(getBgDependsColor
                    (color));

            final String imgUrl = mMessageList.get(position).getMessage();
            myChatViewHolder.setImageMessage(ctx, imgUrl);

            // üzenetben küldött kép kinagyítása
            myChatViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //https://github.com/stfalcon-studio/FrescoImageViewer/blob/master/README.md
                    new ImageViewer.Builder(ctx, Collections.singletonList(imgUrl))
                            .setStartPosition(0)
                            .setCustomDraweeHierarchyBuilder(new GenericDraweeHierarchyBuilder
                                    (ctx.getResources())
                                    .setFailureImage(R.mipmap.placeholder_sad)
                                    .setPlaceholderImage(R.drawable.tenor))
                            .show();
                }
            });
        } else {

            myChatViewHolder.imageMessage.setVisibility(View.GONE);
            myChatViewHolder.messageText.setVisibility(View.VISIBLE);
            myChatViewHolder.messageText.setText(mMessageList.get(position).getMessage());

            // erre a API 19 miatt van szükség
            myChatViewHolder.szoveghatterLinearLayout.setBackgroundResource(getBgDependsColor
                    (color));
        }

        // seen status
        final DatabaseReference mMessageRef = FirebaseDatabase.getInstance().getReference().child
                ("messages").child(mChatuser).child(mCurrenUserId).child(key);

        mMessageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("seen")) {
                    String seenString = dataSnapshot.child("seen").getValue().toString();
                    Boolean seen = Boolean.parseBoolean(seenString);
                    myChatViewHolder.setSeen(seen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //üzenet idejánek beállítása
        String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date
                (mMessageList
                        .get(position).getTime()));
        myChatViewHolder.timeText.setText(dateString);
        String shortTime = new SimpleDateFormat("HH:mm").format(new Date
                (mMessageList
                        .get(position).getTime()));
        myChatViewHolder.setShortTime(shortTime);

        myChatViewHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ctx, ProfileActivity.class);
                profileIntent.putExtra("uid", mCurrenUserId);

                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    ctx.startActivity(profileIntent);
                } else {
                    // do something for phones running an SDK before lollipop
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View, String>(myChatViewHolder.profileImage,
                            "imageTrans");
                    ActivityOptions options;
                    options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) ctx, pairs);

                    ctx.startActivity(profileIntent, options.toBundle());
                }
            }
        });

        // ha rá kattintunk az üzenetre akkor jelenik meg
        myChatViewHolder.itemView.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (myChatViewHolder.timeText.getVisibility() == View.GONE) {
                    myChatViewHolder.timeText.setVisibility(View.VISIBLE);
                    //animáció
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_show_time);
                    myChatViewHolder.timeText.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_hide_time);
                    myChatViewHolder.timeText.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            myChatViewHolder.timeText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        //hosszan kattintunk
        myChatViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (myChatViewHolder.mDelete.getVisibility() == View.VISIBLE) {
                    myChatViewHolder.mDelete.setVisibility(View.GONE);
                    myChatViewHolder.mEdit.setVisibility(View.INVISIBLE);
                    myChatViewHolder.mEdited.setVisibility(View.INVISIBLE);
                } else {
                    myChatViewHolder.mDelete.setVisibility(View.VISIBLE);
                    myChatViewHolder.mEdit.setVisibility(View.VISIBLE);
                    myChatViewHolder.mEdited.setVisibility(View.GONE);
                }
                return true;
            }
        });

        // üzenet szerkesztése feature
        myChatViewHolder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: EDIT");

                // a fleugró ablak
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
                alertDialog.setTitle("Üzenet módosítása");
                //alertDialog.setMessage("termék neve");
                final EditText input = new EditText(ctx);
                input.setText(mMessageList.get(position).getMessage());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                alertDialog.setView(input);
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setPositiveButton("Módosítás",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // adatbázisba írás
                                // TODO
                                String newMessage = input.getText().toString().trim();
                                DatabaseReference messageref = FirebaseDatabase.getInstance().getReference().child
                                        ("messages");
                                // Mcurrentuser oldala
                                messageref.child(mCurrenUserId)
                                        .child(mChatuser)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("message")
                                        .setValue(newMessage);
                                messageref.child(mCurrenUserId)
                                        .child(mChatuser)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("edited_status")
                                        .setValue("edited");

                                //mChatUser oldala
                                messageref.child(mChatuser)
                                        .child(mCurrenUserId)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("message")
                                        .setValue(newMessage);
                                messageref.child(mChatuser)
                                        .child(mCurrenUserId)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("edited_status")
                                        .setValue("edited");

                                //billentyűzet elrejtése
                                InputMethodManager imm = (InputMethodManager) ctx
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        });
                alertDialog.setNegativeButton("Mégsem",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // hide keyboard
                                InputMethodManager imm = (InputMethodManager) ctx
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });
        // üzenet törlése feature
        myChatViewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO ----
                Log.d(TAG, "onClick: DELETE");
                // a fleugró ablak
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
                alertDialog.setTitle("Üzenet törlése");
                alertDialog.setIcon(android.R.drawable.ic_delete);
                alertDialog.setPositiveButton("Üzenet törlése",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // adatbázisba írás
                                // TODO
                                String newMessage = ". . . ";
                                DatabaseReference messageref = FirebaseDatabase.getInstance().getReference().child
                                        ("messages");
                                // Mcurrentuser oldala
                                messageref.child(mCurrenUserId)
                                        .child(mChatuser)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("message")
                                        .setValue(newMessage);
                                messageref.child(mCurrenUserId)
                                        .child(mChatuser)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("edited_status")
                                        .setValue("deleted");

                                //mChatUser oldala
                                messageref.child(mChatuser)
                                        .child(mCurrenUserId)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("message")
                                        .setValue(newMessage);
                                messageref.child(mChatuser)
                                        .child(mCurrenUserId)
                                        .child(mMessageList.get(position).getNodeKey())
                                        .child("edited_status")
                                        .setValue("deleted");

                            }
                        });
                alertDialog.setNegativeButton("Mégsem",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });

        // a szerkesztett jelzés megjelenítése
        myChatViewHolder.setEdited(mMessageList.get(position).getEdited_status());
        Log.d(TAG, "configureMyChatViewHolder: edited_status " + mMessageList.get(position)
                .getEdited_status());

        setAnimation(myChatViewHolder.itemView, position, VIEW_TYPE_ME);
    }

    //=============================================================================
    //  OTHER CHATVIEW HOLDER
    //=============================================================================
    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder,
                                              int
                                                      position) {
        final String fromUser = mMessageList.get(position).getFrom();

        //profil képek betöltése
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child
                ("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u;
                if (!dataSnapshot.child(mCurrenUserId).exists()) {
                    //private String name, status, image, image_thumb, email, uid;
                    u = new User("Törölt profile", "...", "default", "default", "törölt",
                            "null",
                            false);
                } else {
                    u = dataSnapshot.child(mCurrenUserId).getValue(User.class);
                }

                String fromUserImage;
                if (!dataSnapshot.child(fromUser).exists()) {
                    fromUserImage = null;
                } else {
                    fromUserImage = dataSnapshot.child(fromUser).child("image_thumb").getValue()
                            .toString();
                }
                otherChatViewHolder.setProfileImage(ctx, fromUserImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mMessageList.get(position).getType().equals("image")) {
            otherChatViewHolder.imageMessage.setVisibility(View.VISIBLE);
            otherChatViewHolder.messageText.setVisibility(View.GONE);

            final String imgUrl = mMessageList.get(position).getMessage();
            otherChatViewHolder.setImageMessage(ctx, imgUrl);

            // üzenetben küldött kép kinagyítása
            otherChatViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //https://github.com/stfalcon-studio/FrescoImageViewer/blob/master/README.md
                    new ImageViewer.Builder(ctx, Collections.singletonList(imgUrl))
                            .setStartPosition(0)
                            .setCustomDraweeHierarchyBuilder(new GenericDraweeHierarchyBuilder
                                    (ctx.getResources())
                                    .setFailureImage(R.mipmap.placeholder_sad)
                                    .setPlaceholderImage(R.drawable.tenor))
                            .show();
                }
            });
        } else {
            otherChatViewHolder.imageMessage.setVisibility(View.GONE);
            otherChatViewHolder.messageText.setVisibility(View.VISIBLE);
            otherChatViewHolder.messageText.setText(mMessageList.get(position).getMessage());
        }

        //üzenet idejánek beállítása
        String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date
                (mMessageList
                        .get(position).getTime()));
        otherChatViewHolder.timeText.setText(dateString);
        String shortTime = new SimpleDateFormat("HH:mm").format(new Date
                (mMessageList
                        .get(position).getTime()));
        otherChatViewHolder.setShortTime(shortTime);

        // ha rá kattintunk az üzenetre akkor jelenik meg
        otherChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otherChatViewHolder.timeText.getVisibility() == View.GONE) {
                    otherChatViewHolder.timeText.setVisibility(View.VISIBLE);
                    //animáció
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim
                            .anim_show_time);
                    otherChatViewHolder.timeText.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim
                            .anim_hide_time);
                    otherChatViewHolder.timeText.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            otherChatViewHolder.timeText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        // kattintás az üzenetben a profil képre
        otherChatViewHolder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(ctx, ProfileActivity.class);
                profileIntent.putExtra("uid", fromUser);

                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    ctx.startActivity(profileIntent);
                } else {
                    // do something for phones running an SDK before lollipop
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View, String>(otherChatViewHolder.profileImage,
                            "imageTrans");
                    ActivityOptions options;
                    options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity) ctx, pairs);

                    ctx.startActivity(profileIntent, options.toBundle());
                }
            }
        });


        // a szerkesztett jelzés megjelenítése
        otherChatViewHolder.setEdited(mMessageList.get(position).getEdited_status());
        Log.d(TAG, "configureMyChatViewHolder: edited_status " + mMessageList.get(position)
                .getEdited_status());
        setAnimation(otherChatViewHolder.itemView, position, VIEW_TYPE_OTHER);
    }

    @Override
    public int getItemCount() {
        if (mMessageList != null) {
            return mMessageList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(mMessageList.get(position).getFrom(),
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    //=============================================================================
    //  MyChatViewHolder
    //=============================================================================
    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText, seenTextView, shortTime;
        private CircleImageView profileImage;
        private ImageView imageMessage;
        private LinearLayout szoveghatterLinearLayout;
        private ImageView mDelete, mEdit, mEdited; // az edited marad az üzenet mellett ha
        // szerkesztve volt

        public MyChatViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_single_text_me);
            timeText = (TextView) itemView.findViewById(R.id.message_single_time_me);
            profileImage = (CircleImageView) itemView.findViewById(R.id
                    .message_single_profileimage_me);
            imageMessage = (ImageView) itemView.findViewById(R.id.message_image_layout_me);
            seenTextView = itemView.findViewById(R.id.message_single_seen_me);
            shortTime = itemView.findViewById(R.id.message_single_text_shorttime_me);
            szoveghatterLinearLayout = itemView.findViewById(R.id.message_single_text_ll);
            mEdit = itemView.findViewById(R.id.message_single_edit_me);
            mDelete = itemView.findViewById(R.id.message_single_delete_me);
            mEdited = itemView.findViewById(R.id.message_single_edited_me);
        }

        public void setProfileImage(Context ctx, String url) {
            GlideApp
                    .with(ctx)
                    .load(url)
                    .placeholder(R.mipmap.ic_placeholder_face)
                    .error(R.mipmap.placeholder_sad)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profileImage);
        }

        public void setShortTime(String time) {
            shortTime.setText(time);
        }

        public void setImageMessage(Context ctx, String url) {
            GlideApp
                    .with(ctx)
                    .load(url)
                    .placeholder(R.mipmap.placeholder_kicsi)
                    .error(R.mipmap.placeholder_sad)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageMessage);
        }

        public void setSeen(Boolean seen) {
            if (seen) {
                seenTextView.setText(R.string.seen);
                seenTextView.setVisibility(View.GONE);
            } else {
                seenTextView.setText(R.string.sent);
                seenTextView.setVisibility(View.VISIBLE);
            }
        }

        public void setEdited(String edited) {
            switch (edited) {
                case "original":
                    mEdited.setVisibility(View.INVISIBLE);
                    break;
                case "edited":
                    mEdited.setVisibility(View.VISIBLE);
                    break;
                case "deleted":
                    mEdited.setVisibility(View.VISIBLE);
                    mEdited.setImageResource(R.mipmap.round_delete_forever_black_24dp);
                    break;
            }
        }

    }

    //=============================================================================
    //  OtherChatViewHolder
    //=============================================================================
    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private TextView shortTime;
        private TextView messageText;
        private TextView timeText;
        private CircleImageView profileImage;
        private ImageView imageMessage, mEdited;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_single_text);
            timeText = (TextView) itemView.findViewById(R.id.message_single_time);
            profileImage = (CircleImageView) itemView.findViewById(R.id
                    .message_single_profileimage);
            imageMessage = (ImageView) itemView.findViewById(R.id.message_image_layout);
            shortTime = itemView.findViewById(R.id.message_single_text_shorttime);
            mEdited = itemView.findViewById(R.id.message_single_edited);
        }

        public void setShortTime(String time) {
            shortTime.setText(time);
        }

        public void setImageMessage(Context ctx, String url) {
            GlideApp
                    .with(ctx)
                    .load(url)
                    .placeholder(R.mipmap.placeholder_kicsi)
                    .error(R.mipmap.placeholder_sad)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageMessage);
        }

        public void setProfileImage(Context ctx, String url) {
            GlideApp
                    .with(ctx)
                    .load(url)
                    .placeholder(R.mipmap.ic_placeholder_face)
                    .error(R.mipmap.ic_placeholder_face)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profileImage);
        }

        public void setEdited(String edited) {
            switch (edited) {
                case "original":
                    mEdited.setVisibility(View.INVISIBLE);
                    break;
                case "edited":
                    mEdited.setVisibility(View.VISIBLE);
                    break;
                case "deleted":
                    mEdited.setVisibility(View.VISIBLE);
                    mEdited.setImageResource(R.mipmap.round_delete_forever_black_24dp);
                    break;
            }
        }
    }
}