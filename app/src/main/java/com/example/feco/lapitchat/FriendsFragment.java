package com.example.feco.lapitchat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsView;
    private FirebaseRecyclerAdapter<Friend, FriendsFragment.FriendsViewHolder> adapter;
    private Context ctx;
    private String mCurrentUser;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx =  container.getContext();
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsView = (RecyclerView)v.findViewById(R.id.friends_recycler);
        mFriendsView.setHasFixedSize(true);
        mFriendsView.setLayoutManager(new LinearLayoutManager(ctx));

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);


        friendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);
        friendsRef.keepSynced(true);

        Query query = friendsRef;

        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(query, Friend.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Friend, FriendsFragment.FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friend model) {
                holder.setmSingleStatus(model.getDate());

                String list_user_id = getRef(position).getKey();

                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name= dataSnapshot.child("name").getValue().toString();
                        String imgUrl = dataSnapshot.child("image_thumb").getValue().toString();
                        Boolean online;
                        if (dataSnapshot.child("online").getValue() != null){
                            online = (Boolean) dataSnapshot.child("online").getValue();
                        }else{
                            online = false;
                        }

                        holder.setmSingleDisplayname(name);
                        holder.setmSingleImage(ctx, imgUrl);
                        holder.setOnlineDot(online);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ha nem tárolnám a User osztályban a UID-t, akkor ezzel tudjuk lekérni.
                // String userID = getRef(position).getKey();


            }


            @NonNull
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new FriendsFragment.FriendsViewHolder(view);

            }




        };
        mFriendsView.setAdapter(adapter);


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

    public class FriendsViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView mSingleImage;
        private TextView mSingleDisplayname, mSingleStatus, mEmail;
        private ImageView mOnlineDot;

        public void setOnlineDot(Boolean b){
            mOnlineDot = (ImageView)mView.findViewById(R.id.users_single_dot);
            if (b){
                mOnlineDot.setImageResource(R.mipmap.online_dot);
                //animáció
                Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_online);
                mOnlineDot.startAnimation(animation);
            }else{
                mOnlineDot.setImageResource(R.mipmap.offline_dot);
                //animáció
                Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_offline);
                mOnlineDot.startAnimation(animation);
            }


        }

        public void setmEmail(String mail){
            mEmail = mView.findViewById(R.id.users_single_email);
            mEmail.setText(mail);
        }

        public void setmSingleImage(Context ctx, String imgurl) {
            mSingleImage = mView.findViewById(R.id.users_single_image);
            RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL); // ezzel lehet a képeket a lemezen synkronban tartani
            Glide.with(ctx)
                    .load(imgurl)
                    .apply(options)
                    .into(mSingleImage);
        }

        public void setmSingleDisplayname(String name) {
            mSingleDisplayname = mView.findViewById(R.id.users_single_displayname);
            mSingleDisplayname.setText(name);
        }

        public void setmSingleStatus(String status) {
            mSingleStatus = mView.findViewById(R.id.users_single_status);
            mSingleStatus.setText(status);
        }

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


    }

}
