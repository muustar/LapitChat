package com.muustar.feco.mychat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class SectionsPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private DatabaseReference mNotifyDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    private long notifyCount;

    SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser().getUid();
        }
        mNotifyDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new FriendsFragment();
            case 2:
                return new RequestsFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public void updateTitleData(long notifyCount) {
        this.notifyCount = notifyCount;
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);


        switch (position) {

            case 0:
                return mContext.getResources().getString(R.string.chats);
            case 1:
                return mContext.getResources().getString(R.string.friends);
            case 2:
                String requestSzoveg = mContext.getResources().getString(R.string.requests);
                return requestSzoveg + " (" + notifyCount + ")";

            default:
                return null;
        }
    }

}
