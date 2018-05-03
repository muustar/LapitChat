package com.example.feco.lapitchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private FirebaseRecyclerAdapter<User, UsersViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));



        //a firebase UI recycrerview kezelőjét használjuk
        //User osztály:  private String name, status, image, image_thumb;

        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .orderByChild("name");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new UsersViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull final User model) {
                holder.setmSingleImage(getApplicationContext(), model.getImage_thumb());
                holder.setmSingleDisplayname(model.getName());
                holder.setmSingleStatus(model.getStatus());
                holder.setmEmail(model.getEmail());

                // ha nem tárolnám a User osztályban a UID-t, akkor ezzel tudjuk lekérni.
                // String userID = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("uid", model.getUid());
                        startActivity(profileIntent);
                    }
                });
            }


        };
        mUsersList.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView mSingleImage;
        private TextView mSingleDisplayname, mSingleStatus, mEmail;

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

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


    }
}
