package com.example.pranav.justchat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

class SectionsPagerAdepter extends FragmentPagerAdapter {
    public SectionsPagerAdepter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestsFragment requestFragment = new RequestsFragment();
                return requestFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return  chatFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Requests";
            case 1:
                return  "Chats";
            case 2:
                return "Friends";

                default:
                    return null;
        }

    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }
}
