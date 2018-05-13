package com.example.feco.lapitchat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Készítette: feco
 * 2018.05.11.
 */
public class Messageadapter extends RecyclerView.Adapter<Messageadapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private Context ctx;

    public Messageadapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ctx = parent.getContext();
        View v = LayoutInflater.from(ctx).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String currentUser = mAuth.getCurrentUser().getUid();
        final String fromUser = mMessageList.get(position).getFrom();


        //képek betöltése
        DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentUserImage = dataSnapshot.child(currentUser).child("image_thumb").getValue().toString();
                String fromUserImage = dataSnapshot.child(fromUser).child("image_thumb").getValue().toString();
                if (fromUser.equals(currentUser)) {
                    holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                    holder.messageText.setTextColor(Color.WHITE);
                    Glide.with(ctx).load(currentUserImage).into(holder.profileImage);
                } else {
                    holder.messageText.setBackgroundColor(Color.WHITE);
                    holder.messageText.setTextColor(Color.BLACK);
                    Glide.with(ctx).load(fromUserImage).into(holder.profileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.messageText.setText(mMessageList.get(position).getMessage());
        String dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(mMessageList.get(position).getTime()));
        holder.timeText.setText(dateString);
        //holder.setProfileImage(ctx, mMessageList.get(position).getFrom());


        // idő feature
        // ha rá kattintunk az üzenetre akkor jelenik meg
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.timeText.getVisibility() == View.GONE) {
                    holder.timeText.setVisibility(View.VISIBLE);
                    //animáció
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_show_time);
                    holder.timeText.startAnimation(animation);

                } else {
                    Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_hide_time);
                    holder.timeText.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            holder.timeText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView messageText;
        private TextView timeText;
        private CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            messageText = (TextView) mView.findViewById(R.id.message_single_text);
            timeText = (TextView) mView.findViewById(R.id.message_single_time);
            profileImage = (CircleImageView) mView.findViewById(R.id.message_single_profileimage);
        }

        public void setProfileImage(Context ctx, String url) {
            Glide.with(ctx).load(url).into(profileImage);
        }
    }

}
