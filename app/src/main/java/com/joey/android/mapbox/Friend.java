package com.joey.android.mapbox;

import java.util.UUID;

public class Friend {
    private static final String TAG = "Friend";

    private UUID mId;
    private String mFirstName;
    private String mLastName;
    private String mNickName;

    public Friend(String firstName, String lastName) {
        mId = UUID.randomUUID();
        mFirstName = firstName;
        mLastName = lastName;
    }

    public String getName() {
        return mFirstName + " " + mLastName;
    }

    public UUID getId() {
        return mId;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }
}
