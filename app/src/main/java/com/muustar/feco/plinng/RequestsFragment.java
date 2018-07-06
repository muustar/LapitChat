package com.muustar.feco.plinng;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestRecycler;
    private Context ctx;
    private String mCurrentUserId;
    private DatabaseReference notificationsRef;
    private FirebaseRecyclerAdapter<NotificationType, RequestsFragment.NotifyViewHolder> adapter;
    private DatabaseReference usersRef;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ctx = container.getContext();
        View v = inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestRecycler = (RecyclerView) v.findViewById(R.id.request_recycler);
        mRequestRecycler.setHasFixedSize(true);
        mRequestRecycler.setLayoutManager(new LinearLayoutManager(ctx));

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(mCurrentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Query query = notificationsRef;

        FirebaseRecyclerOptions<NotificationType> options =
                new FirebaseRecyclerOptions.Builder<NotificationType>()
                        .setQuery(query, NotificationType.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<NotificationType, RequestsFragment.NotifyViewHolder>(options) {

            @NonNull
            @Override
            public NotifyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.request_single_layout, parent, false);
                return new RequestsFragment.NotifyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final NotifyViewHolder holder, final int position, @NonNull final NotificationType model) {

                final User[] u = new User[1];
                // Display Name betöltése
                usersRef.child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try {
                            if (!dataSnapshot.exists()) {
                                //private String name, status, image, image_thumb, email, uid;
                                u[0] = new User("Törölt profile", "...", "default", "default", "törölt", "null", false);
                            } else {
                                u[0] = dataSnapshot.getValue(User.class);
                            }
                        }catch (Exception e){
                            Log.d("ERROR", e.getMessage());
                        }
                        holder.mRequestDisplayname.setText(u[0].getName());
                        holder.setImage(ctx, u[0].getImage_thumb());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.setRequestType(model.getType(), model.getSeen());
                String time = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(model.getTimestamp()));
                holder.setTime(time);


                // esemény kezelése
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (model.getType()) {
                            case "friend_request":
                                // megjelöljük láttotnak és át lépünk a profilra
                                notificationsRef.child(getRef(position).getKey()).child("seen").setValue(true);
                                Intent openProfile = new Intent(ctx, ProfileActivity.class);
                                openProfile.putExtra("uid", model.getFrom());
                                startActivity(openProfile);
                                break;
                            case "new_message":
                                //töröljük és ugrunk a csetre
                                notificationsRef.child(getRef(position).getKey()).removeValue();
                                Intent openChat = new Intent(ctx, ChatActivity.class);
                                openChat.putExtra("uid", model.getFrom());
                                openChat.putExtra("name", u[0].getName());
                                openChat.putExtra("img", u[0].getImage_thumb());
                                startActivity(openChat);
                                break;
                            case "new_image":
                                //töröljük és ugrunk a csetre
                                notificationsRef.child(getRef(position).getKey()).removeValue();
                                Intent openChatImage = new Intent(ctx, ChatActivity.class);
                                openChatImage.putExtra("uid", model.getFrom());
                                openChatImage.putExtra("name", u[0].getName());
                                openChatImage.putExtra("img", u[0].getImage_thumb());
                                startActivity(openChatImage);
                                break;

                            case "update":
                                // ugrunk az updatre
                                Intent updateIntent = new Intent(ctx, UpdateActivity.class);
                                updateIntent.putExtra("text", model.getText());
                                startActivity(updateIntent);
                                break;

                        }
                    }
                });

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        notificationsRef.child(getRef(position).getKey()).removeValue();
                        return true;
                    }
                });

            }
        };
        mRequestRecycler.setAdapter(adapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class NotifyViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mRequestDisplayname, mRequestType, mRequestTime;
        private CircleImageView mRequestImage;

        public NotifyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mRequestDisplayname = (TextView) mView.findViewById(R.id.request_displayname);
            mRequestType = (TextView) mView.findViewById(R.id.request_type);
            mRequestTime = (TextView) mView.findViewById(R.id.request_time);
            mRequestImage = (CircleImageView) mView.findViewById(R.id.request_profileimage);

        }

        public void setTime(String time) {
            mRequestTime.setText(time);
        }

        public void setImage(Context ctx, String url) {
            try {
                GlideApp
                        .with(ctx)
                        .load(url)
                        .placeholder(R.mipmap.ic_placeholder_face)
                        .error(R.mipmap.ic_placeholder_face)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mRequestImage);
            }catch (Exception e){
                Log.d("ERROR", e.getMessage());
            }
        }

        public void setRequestType(String type, Boolean seen) {
            if (!seen) {
                mRequestType.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                mRequestDisplayname.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
            } else {
                mRequestType.setTypeface(Typeface.DEFAULT);
                mRequestDisplayname.setTypeface(Typeface.DEFAULT);
            }
            if (type.equals("friend_request")) {
                mRequestType.setText(R.string.wanna_be_your_friend);
            } else if (type.equals("new_message")) {
                mRequestType.setText(R.string.sent_a_new_message);
            } else if (type.equals("new_image")){
                mRequestType.setText(R.string.new_image);
            }else if (type.equals("update")){
                mRequestType.setText(R.string.update);
            }
        }
    }
}
