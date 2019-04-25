package com.joey.android.mapbox.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class UserInfo {
    private static final String TAG = "UserInfo";

    private String mUid;
    private Boolean mIsRequesting;
    private Double mLatitude;
    private Double mLongitude;
    private Long mLastUpdated;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(UserInfo.class)
    }

    public Boolean getIsRequesting() {
        return mIsRequesting;
    }

    public void setIsRequesting(Boolean requesting) {
        mIsRequesting = requesting;
    }

    public Long getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        mLastUpdated = lastUpdated;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public String toString() {
        return "Uid:" + mUid
                + ", isRequesting:" + mIsRequesting
                + ", Latitude:" + mLatitude
                + ", Longitude:" + mLongitude
                + ", LastUpdated:" + mLastUpdated;
    }
}
