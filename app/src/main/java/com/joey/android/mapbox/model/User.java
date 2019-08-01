package com.joey.android.mapbox.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class User implements Parcelable {
    private static final String TAG = "User";

    private String mUid;
    private String mName;
    private String mPhotoUri;
    private String mEmail;
    private boolean mRequesting;
    private double mLatitude;
    private double mLongitude;
    private long mLastUpdated;

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUid);
        dest.writeString(mName);
        dest.writeString(mPhotoUri);
        dest.writeString(mEmail);
        dest.writeByte((byte)(mRequesting ? 1 : 0));
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeLong(mLastUpdated);
    }

    // Default constructor for Firebase
    public User() {
    }

    protected User(Parcel in) {
        mUid = in.readString();
        mName = in.readString();
        mPhotoUri = in.readString();
        mEmail = in.readString();
        mRequesting = in.readByte() != 0;
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mLastUpdated = in.readLong();
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
            setLatLng(info.getLatitude(), info.getLongitude());
        }

        if (info.getLastUpdated() != null) {
            // Revert the inverted timestamp back to correct date
            mLastUpdated = -1 * info.getLastUpdated();
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
        return new LatLng(mLatitude, mLongitude);
    }

    public void setLatLng(double lat, double lng) {
        mLatitude = lat;
        mLongitude =lng;
    }

    public long getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        mLastUpdated = lastUpdated;
    }
}
