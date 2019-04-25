package com.joey.android.mapbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.activity.SignInActivity;

public class UserProfileFragment extends FirebaseFragment {
    private static final String TAG = "UserProfileFragment";

    private Button mSignOutButton;

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    View.OnClickListener signOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mSignOutButton = view.findViewById(R.id.fragment_user_profile_sign_out);
        mSignOutButton.setOnClickListener(signOutListener);

        return view;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
