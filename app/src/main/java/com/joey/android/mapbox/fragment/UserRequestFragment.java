package com.joey.android.mapbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.activity.SignInActivity;

public class UserRequestFragment extends Fragment {
    private static final String TAG = "UserRequestFragment";

    private Button mSignOutButton;

    public static UserRequestFragment newInstance() {
        return new UserRequestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);

        mSignOutButton = view.findViewById(R.id.button_sign_out);
        mSignOutButton.setOnClickListener(signOutOnClickListener);

        return view;
    }

    private View.OnClickListener signOutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = SignInActivity.newIntent(getActivity());
            startActivity(intent);
        }
    };
}
