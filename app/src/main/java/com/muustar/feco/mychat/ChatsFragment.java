package com.muustar.feco.mychat;


import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Typeface.DEFAULT;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvView;

    private Context ctx;
    private DatabaseReference convRef;
    private FirebaseRecyclerAdapter<Conv, ConvViewHolder> adapter;
    private String mCurrentUserId;
    private DatabaseReference usersRef;
    private DatabaseReference messageRef;
    private LinearLayoutManager mLayoutManager;
    private boolean torolheto = false;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = container.getContext();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chats, container, false);
        mConvView = (RecyclerView) v.findViewById(R.id.chat_recycler);
        mConvView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(ctx);
        //mLayoutManager.setReverseLayout(true);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mConvView.setLayoutManager(mLayoutManager);


        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        convRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        convRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        messageRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
        messageRef.keepSynced(true);

        final Query query = convRef.orderByChild("timestamp");


        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(query, Conv.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Conv, ChatsFragment.ConvViewHolder>(options) {

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conv_single_layout, parent, false);
                return new ChatsFragment.ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {

                final String list_user_id = getRef(position).getKey();

                //az utolsó üzenet lekérése
                Query lastMessageQuery = messageRef.child(list_user_id).orderByKey().limitToLast(1);
                ChildEventListener lastMessageQueryEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages message = dataSnapshot.getValue(Messages.class);

                        holder.setLastMessage(message.getMessage(), message.getSeen(), message.getType());
                        long timestampLong = message.getTime();
                        String timestampString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(timestampLong));
                        holder.setmEmail(timestampString);

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
                lastMessageQuery.addChildEventListener(lastMessageQueryEventListener);


                // profil adatok betöltése, név, kép
                usersRef.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User u;
                        if (!dataSnapshot.exists()) {
                            //private String name, status, image, image_thumb, email, uid;
                            u = new User("Törölt profile", "...", "default", "default", "törölt", "null", false);
                        } else {
                            u = dataSnapshot.getValue(User.class);
                        }
                        final String userName = u.getName();
                        final String chatUserImg = u.getImage_thumb();
                        holder.setmSingleDisplayname(userName);
                        try {
                            holder.setmSingleImage(ctx, chatUserImg);
                        } catch (Exception e) {
                            Log.d("ERROR", e.getMessage());
                        }

                        //online ststus ellenőrzés

                        if (dataSnapshot.child("online").getValue() != null) {
                            String online = dataSnapshot.child("online").getValue().toString();
                            holder.setOnlineDot(online);
                        }


                        //kattintás feature
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(ctx, ChatActivity.class);
                                chatIntent.putExtra("uid", list_user_id);
                                chatIntent.putExtra("name", userName);
                                chatIntent.putExtra("img", chatUserImg);

                                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    // Do something for lollipop and above versions
                                    startActivity(chatIntent);
                                } else {
                                    // do something for phones running an SDK before lollipop
                                    Pair[] pairs = new Pair[1];
                                    pairs[0] = new Pair<View, String>(holder.mSingleStatus, "statusTrans");
                                    //pairs[1] = new Pair<View, String>(holder.mSingleImage, "imageTrans");
                                    ActivityOptions options = ActivityOptions
                                            .makeSceneTransitionAnimation(getActivity(), pairs);

                                    startActivity(chatIntent, options.toBundle());
                                }


                            }
                        });

                        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                hosszukattintasKezelese();

                                return true;
                            }

                            private void hosszukattintasKezelese() {
                                //beszélgetés törlése feature
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
                                alertDialog.setTitle("Delete Chat");
                                alertDialog.setIcon(android.R.drawable.ic_delete);
                                alertDialog.setPositiveButton("Delete",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                torolheto = true;


                                                // Chat és a messages -ből kell törölni
                                                DatabaseReference chatRefCuUser = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId).child(list_user_id);
                                                chatRefCuUser.removeValue();

                                                // az üzenetek törlése esetén meg kell vizsgálni, hogy kép üzenetről van e szó,
                                                // ha kép üzenetet találunk akkor törölni kell a tárolt képek közül is.


                                                // a képek vizsgálata és törlése
                                                Query queryKepek = messageRef.child(list_user_id).orderByChild("type").equalTo("image");
                                                queryKepek.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> ds = dataSnapshot.getChildren();
                                                        for (DataSnapshot d : ds) {
                                                            String filenev = d.getKey() + ".jpg";
                                                            Log.d("FECO", filenev);
                                                            StorageReference torlendoFajl = FirebaseStorage.getInstance().getReference().child("message_images").child(filenev);
                                                            torlendoFajl.delete().addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("ERROR", e.getMessage());
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                                //Log.d("FECO", torlendok.toString());
                                                // a cset üzenetek törlése
                                                DatabaseReference messRefCuUser = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId).child(list_user_id);
                                                messRefCuUser.removeValue();


                                            }

                                        });
                                alertDialog.setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener()

                                        {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                alertDialog.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };

        adapter.notifyDataSetChanged();

        // a megfelelő pozícióra ugrik - (többnyire :( )
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()

        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //mConvView.smoothScrollToPosition(adapter.getItemCount()-1);
                //mConvView.smoothScrollToPosition(itemCount);
                mConvView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        mConvView.setAdapter(adapter);
        mConvView.refreshDrawableState();
        adapter.startListening();
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter.startListening();
    }


    @Override
    public void onPause() {
        super.onPause();
        adapter.stopListening();
    }

    private class ConvViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private CircleImageView mSingleImage;
        private TextView mSingleDisplayname, mSingleStatus, mEmail;
        private ImageView mOnlineDot;

        public void setOnlineDot(String b) {
            mOnlineDot = (ImageView) mView.findViewById(R.id.conv_single_dot);
            if (b.equals("true")) {
                mOnlineDot.setImageResource(R.mipmap.online_dot);
                //animáció
                Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_online);
                mOnlineDot.startAnimation(animation);
            } else {
                mOnlineDot.setImageResource(R.mipmap.offline_dot);
                //animáció
                Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.anim_offline);
                mOnlineDot.startAnimation(animation);
            }


        }

        public void setmEmail(String mail) {
            mEmail = mView.findViewById(R.id.conv_single_email);
            mEmail.setText(mail);
        }

        public void setmSingleImage(Context ctx, String imgurl) {
            mSingleImage = mView.findViewById(R.id.conv_single_image);
            RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL); // ezzel lehet a képeket a lemezen synkronban tartani
            GlideApp
                    .with(ctx)
                    .load(imgurl)
                    .placeholder(R.mipmap.ic_placeholder_face)
                    .error(R.mipmap.ic_placeholder_face)
                    .into(mSingleImage);
        }

        public void setmSingleDisplayname(String name) {
            mSingleDisplayname = mView.findViewById(R.id.conv_single_displayname);
            mSingleDisplayname.setText(name);
        }

        public void setLastMessage(String message, Boolean seen, String type) {
            mSingleStatus = mView.findViewById(R.id.conv_last_message);
            if (type.equals("image")) {
                message = "image";
            }
            if (seen) {
                mSingleStatus.setText(message);
                mSingleStatus.setTypeface(Typeface.DEFAULT);
            } else {
                //mSingleStatus.setTextColor(Color.BLACK);
                mSingleStatus.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                mSingleStatus.setText(message);
            }
        }

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }
}
