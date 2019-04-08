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

import com.joey.android.mapbox.R;
import com.joey.android.mapbox.model.Friend;
import com.joey.android.mapbox.model.FriendList;

public class AddFriendFragment extends Fragment {
    private static final String TAG = "AddFriendFragment";

    private EditText mFirstNameText;
    private EditText mLastNameText;
    private Button mAddFriendButton;

    public static AddFriendFragment newInstance() {
        return new AddFriendFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        mFirstNameText = (EditText) view.findViewById(R.id.edit_first_name);

        mLastNameText = (EditText) view.findViewById(R.id.edit_last_name);

        Button mAddFriendButton = (Button) view.findViewById(R.id.button_add_friend);
        mAddFriendButton.setOnClickListener(addFriendOnClickListener);

        return view;
    }

    View.OnClickListener addFriendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           FriendList friendList = FriendList.get(getActivity());
           String firstName = mFirstNameText.getText().toString();
           String lastName = mLastNameText.getText().toString();
           Friend friend = new Friend(firstName, lastName);
           Log.i(TAG, "" + friend.getFirstName());
           friendList.addFriend(friend);
           getActivity().finish();
        }
    };
}
