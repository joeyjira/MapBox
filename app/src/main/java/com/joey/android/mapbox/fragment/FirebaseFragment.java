package com.joey.android.mapbox.fragment;

import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public abstract class FirebaseFragment extends Fragment {
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
