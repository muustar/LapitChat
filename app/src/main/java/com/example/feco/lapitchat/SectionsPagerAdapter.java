package com.example.feco.lapitchat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;

class SectionsPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RequestsFragment();
            case 1:
                return new ChatsFragment();
            case 2:
                return new FriendsFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);
        switch (position){
            case 0: return mContext.getResources().getString(R.string.requests);
            case 1: return mContext.getResources().getString(R.string.chats);
            case 2: return mContext.getResources().getString(R.string.friends);
            default: return null;
        }
    }


}
