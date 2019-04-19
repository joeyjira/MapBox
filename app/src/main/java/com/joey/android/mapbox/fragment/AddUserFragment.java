package com.joey.android.mapbox.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;

public class AddUserFragment extends FirebaseFragment {
    private static final String TAG = "AddUserFragment";

    private DatabaseReference mReference;
    private User mUser;

    private EditText mEmailEditText;
    private Button mSearchUserButton;
    private TextView mUserNameText;
    private TextView mUserEmail;
    private Button mResponseButton;

    public static AddUserFragment newInstance() {
        return new AddUserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReference = FirebaseDatabase.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

        mEmailEditText = view.findViewById(R.id.edit_email);

        mSearchUserButton = view.findViewById(R.id.fragment_add_friend_get_user);
        mSearchUserButton.setOnClickListener(getUserOnClickListener);

        mResponseButton = view.findViewById(R.id.fragment_add_friend_response);
        mResponseButton.setOnClickListener(addUserOnClickListener);

        mUserNameText = view.findViewById(R.id.fragment_add_friend_name);
        mUserEmail = view.findViewById(R.id.fragment_add_friend_email);

        return view;
    }

    private void searchUser(String email) {
        mReference.child(Reference.EMAILS)
                .child(encodeEmail(email))
                .addListenerForSingleValueEvent(getUserListener);

    }

    private void addUser() {
        mReference.child(Reference.FRIEND_REQUESTS)
                .child(mUser.getUid())
                .child(getUid())
                .setValue(true);
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    View.OnClickListener getUserOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = mEmailEditText.getText().toString().trim().toLowerCase();
            searchUser(email);
        }
    };

    View.OnClickListener addUserOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addUser();
            mResponseButton.setText(R.string.sent_request);
            mResponseButton.setEnabled(false);
        }
    };

    ValueEventListener getUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot userSnapshot) {
            if (userSnapshot.exists()) {
                mReference.child(Reference.USERS)
                        .child(userSnapshot.getValue().toString())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mUser = dataSnapshot.getValue(User.class);
                                mUserNameText.setText(mUser.getName());
                                mUserEmail.setText(mUser.getEmail());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                // Check if the email user searched refers to himself/herself
                if (!userSnapshot.getValue().toString().equals(getUid())) {
                    mReference.child(Reference.FRIEND_REQUESTS)
                            .child(userSnapshot.getValue().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot friendRequestSnapshot) {
                                    if (friendRequestSnapshot.exists()) {
                                        mResponseButton.setText(R.string.pending_request);
                                        mResponseButton.setEnabled(false);
                                        mResponseButton.setVisibility(View.VISIBLE);
                                        return;
                                    }

                                    mReference.child(Reference.FRIENDS)
                                            .child(userSnapshot.getValue().toString())
                                            .child(getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
                                                    if (friendsSnapshot.exists()) {
                                                        mResponseButton.setText(R.string.existing_friend);
                                                        mResponseButton.setEnabled(false);
                                                    } else {
                                                        mResponseButton.setText(R.string.add_friend);
                                                        mResponseButton.setEnabled(true);
                                                    }

                                                    mResponseButton.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                } else {
                    mResponseButton.setText("This is you!");
                    mResponseButton.setEnabled(false);
                    mResponseButton.setVisibility(View.VISIBLE);
                }
            } else {
                // Make a toast, user cannot be found
                mResponseButton.setEnabled(false);
                mResponseButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
