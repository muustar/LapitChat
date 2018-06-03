package com.muustar.feco.mychat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
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
    private Context ctx;

    public MessageAdapter(List<Messages> mMessageList, int color) {
        this.mMessageList = mMessageList;
        this.color = color;
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
                View viewChatMine = layoutInflater.inflate(R.layout.message_single_layout_me, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.message_single_layout_other,
                        parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        Fresco.initialize(ctx);
        return viewHolder;
    }


    private int getBgDependsColor(int color) {
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
                return R.layout.message_single_layout_me;
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

    private void configureMyChatViewHolder(final MyChatViewHolder myChatViewHolder, int position) {
        final String fromUser = mMessageList.get(position).getFrom();
        if (mMessageList.get(position).getType().equals("image")) {
            myChatViewHolder.imageMessage.setVisibility(View.VISIBLE);
            myChatViewHolder.messageText.setVisibility(View.GONE);
            myChatViewHolder.imageMessage.setBackgroundResource(getBgDependsColor(color));

            final String imgUrl = mMessageList.get(position).getMessage();
            myChatViewHolder.setImageMessage(ctx, imgUrl);

            // üzenetben küldött kép kinagyítása
            myChatViewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //https://github.com/stfalcon-studio/FrescoImageViewer/blob/master/README.md
                    new ImageViewer.Builder(ctx, Collections.singletonList(imgUrl))
                            .setStartPosition(0)
                            .show();
                }
            });
        } else {
            myChatViewHolder.imageMessage.setVisibility(View.GONE);
            myChatViewHolder.messageText.setVisibility(View.VISIBLE);
            myChatViewHolder.messageText.setBackgroundResource(getBgDependsColor(color));
            myChatViewHolder.messageText.setText(mMessageList.get(position).getMessage());
        }

        Boolean lattam = mMessageList.get(position).getSeen();
        myChatViewHolder.setSeenText(lattam);
        String lattamText = "";
        if (lattam) {
            lattamText = ctx.getString(R.string.seen);
        } else {
            lattamText = ctx.getString(R.string.sent);
        }
        //üzenet idejánek beállítása
        String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(mMessageList
                .get(position).getTime()));
        myChatViewHolder.timeText.setText(dateString + "\n" + lattamText);
        // ha rá kattintunk az üzenetre akkor jelenik meg
        myChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
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
    }

    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder, int
            position) {
        final String fromUser = mMessageList.get(position).getFrom();

        //profil képek betöltése
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u;
                if (!dataSnapshot.child(mCurrenUserId).exists()) {
                    //private String name, status, image, image_thumb, email, uid;
                    u = new User("Törölt profile", "...", "default", "default", "törölt", "null",
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
                                    .setFailureImage(R.mipmap.placeholder_sad))
                            .show();
                }
            });
        } else {
            otherChatViewHolder.imageMessage.setVisibility(View.GONE);
            otherChatViewHolder.messageText.setVisibility(View.VISIBLE);
            otherChatViewHolder.messageText.setText(mMessageList.get(position).getMessage());
        }

        //üzenet idejánek beállítása
        String lattam = String.valueOf(mMessageList.get(position).getSeen());
        String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(mMessageList
                .get(position).getTime()));
        otherChatViewHolder.timeText.setText(dateString);

        // ha rá kattintunk az üzenetre akkor jelenik meg
        otherChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otherChatViewHolder.timeText.getVisibility() == View.GONE) {
                    otherChatViewHolder.timeText.setVisibility(View.VISIBLE);
                    //animáció
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_show_time);
                    otherChatViewHolder.timeText.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_hide_time);
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

    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText, seenText;
        private CircleImageView profileImage;
        private ImageView imageMessage;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_single_text_me);
            timeText = (TextView) itemView.findViewById(R.id.message_single_time_me);
            profileImage = (CircleImageView) itemView.findViewById(R.id
                    .message_single_profileimage_me);
            imageMessage = (ImageView) itemView.findViewById(R.id.message_image_layout_me);
            seenText = (TextView) itemView.findViewById(R.id.message_single_seen_me);
        }

        public void setSeenText(Boolean b) {
            if (b) {
                seenText.setVisibility(View.GONE);
                seenText.setText(R.string.seen);
            } else {
                seenText.setVisibility(View.VISIBLE);
                seenText.setText(R.string.sent);
            }
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
    }

    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timeText;
        private CircleImageView profileImage;
        private ImageView imageMessage;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_single_text);
            timeText = (TextView) itemView.findViewById(R.id.message_single_time);
            profileImage = (CircleImageView) itemView.findViewById(R.id
                    .message_single_profileimage);
            imageMessage = (ImageView) itemView.findViewById(R.id.message_image_layout);
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
    }
}