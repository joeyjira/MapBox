package com.joey.android.mapbox.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class User {
    private static final String TAG = "User";

    private String mUid;
    private String mName;
    private String mPhotoUri;
    private String mEmail;
    private boolean mRequesting;
    private LatLng mLatLng;
    private Date mLastUpdated;

    public User() {
    }

    public User(String uid, String name) {
        mUid = uid;
        mName = name;
    }

    public void additionalInfo(UserInfo info) {
        if (info.getIsRequesting() != null) {
            setRequesting(info.getIsRequesting());
        }

        if (info.getLatitude() != null && info.getLongitude() != null) {
            setLatLng(new LatLng(info.getLatitude(), info.getLongitude()));
        }

        if (info.getLastUpdated() != null) {
            // Revert the inverted timestamp back to correct date
            setLastUpdated(new Date(-1 * (info.getLastUpdated())));

        }
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

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public String toString() {
        String user = "UID: " + getUid() + ", Name: " + getName() + ", Email: " + getEmail();
        return user;
    }

    public boolean isRequesting() {
        return mRequesting;
    }

    public void setRequesting(boolean requesting) {
        mRequesting = requesting;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        mLastUpdated = lastUpdated;
    }
}
