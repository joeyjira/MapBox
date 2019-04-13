package com.joey.android.mapbox.model;

public class User {
    private static final String TAG = "User";

    private String mUid;
    private String mName;
    private String mEmail;

    public User(String uid, String name, String email) {
        mUid = uid;
        mName = name;
        mEmail = email;
    }

    public String getPhotoFilename() {
        return "IMG_" + getUid() + ".jpg";
    }


    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }
}
