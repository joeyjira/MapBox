package com.joey.android.mapbox.model;

import android.net.Uri;

import java.util.UUID;

public class Friend {
    private static final String TAG = "Friend";

    private UUID mId;
    private String mFirstName;
    private String mLastName;
    private String mNickName;

    public Friend(String firstName, String lastName) {
        this(UUID.randomUUID(), firstName, lastName);
    }

    public Friend(UUID id, String firstName, String lastName) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
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
