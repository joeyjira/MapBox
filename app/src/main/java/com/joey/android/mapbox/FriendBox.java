package com.joey.android.mapbox;

public class FriendBox {
    private static final String TAG = "FriendBox";

    private String mFirstName;
    private String mLastName;
    private String mNickName;

    public FriendBox(String firstName, String lastName) {
        mFirstName = firstName;
        mLastName = lastName;
    }

    public String getName() {
        return mFirstName + " " + mLastName;
    }
}
