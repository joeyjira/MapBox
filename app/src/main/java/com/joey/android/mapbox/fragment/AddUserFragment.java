package com.joey.android.mapbox.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.joey.android.mapbox.R;
import com.joey.android.mapbox.firebase.FirebaseHelper;
import com.joey.android.mapbox.model.User;

public class AddUserFragment extends Fragment
        implements FirebaseHelper.Callbacks {
    private static final String TAG = "AddUserFragment";

    private FirebaseHelper mFirebaseHelper;
    private String mUserUid;
    private String mName;
    private EditText mEmailText;
    private Button mGetUserButton;
    private TextView mUserNameText;
    private Button mAddUserButton;

    public static AddUserFragment newInstance() {
        return new AddUserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseHelper = FirebaseHelper.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        mEmailText = (EditText) view.findViewById(R.id.edit_email);

        mGetUserButton = (Button) view.findViewById(R.id.button_get_user);
        mGetUserButton.setOnClickListener(getUserOnClickListener);

        mAddUserButton = (Button) view.findViewById(R.id.button_add_user);
        mAddUserButton.setOnClickListener(addUserOnClickListener);

        mUserNameText = (TextView) view.findViewById(R.id.add_friend_name);

        return view;
    }

    @Override
    public void updateUid(String uid) {
        mUserUid = uid;
        mFirebaseHelper.getUserFromUid(uid, AddUserFragment.this);
    }

    @Override
    public void updateName(String name) {
        mName = name;
        mUserNameText.setText(mName);
    }

    View.OnClickListener getUserOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = mEmailText.getText().toString();
            mFirebaseHelper.getUidFromEmail(email, AddUserFragment.this);
        }
    };

    View.OnClickListener addUserOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFirebaseHelper.addUserRequest(mUserUid);
        }
    };
}
