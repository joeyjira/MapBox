package com.joey.android.mapbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.activity.SignInActivity;
import com.joey.android.mapbox.firebase.MapBoxFBSchema;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;

import org.w3c.dom.Text;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRequestFragment extends FirebaseFragment {
    private static final String TAG = "UserRequestFragment";

    private RecyclerView mUserRequestRecyclerView;
    private TextView mEmptyListTextView;
    private UserRequestAdapter mAdapter;
    private DatabaseReference mReference;
    private List<User> mRequests;

    public static UserRequestFragment newInstance() {
        return new UserRequestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReference = FirebaseDatabase.getInstance()
                .getReference();
        mRequests = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mUserRequestRecyclerView = view.findViewById(R.id.recycler_view_map_box);
        mUserRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyListTextView = view.findViewById(R.id.fragment_recycler_view_empty_list);
        mEmptyListTextView.setText("There are currently no friend requests");

        updateUI();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mReference.child(Reference.FRIEND_REQUESTS)
                .child(getUid())
                .addValueEventListener(friendRequestListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        mReference.child(Reference.FRIEND_REQUESTS)
                .child(getUid())
                .removeEventListener(friendRequestListener);
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new UserRequestAdapter(mRequests);
            mUserRequestRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setUsers(mRequests);
            mAdapter.notifyDataSetChanged();
        }

        if (mRequests.size() == 0 || mRequests == null) {
            mEmptyListTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListTextView.setVisibility(View.GONE);
        }
    }

    private class UserRequestAdapter extends RecyclerView.Adapter<UserRequestHolder> {
        List<User> mUsers;

        public UserRequestAdapter(List<User> users) {
            mUsers = users;
        }

        @NonNull
        @Override
        public UserRequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.viewholder_friend_request, viewGroup, false);

            return new UserRequestHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserRequestHolder userRequestHolder, int position) {
            User user = mUsers.get(position);
            userRequestHolder.bind(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public void setUsers(List<User> users) {
            mUsers = users;
        }
    }

    private class UserRequestHolder extends RecyclerView.ViewHolder {
        private User mUser;
        private CircleImageView mImageViewProfile;
        private TextView mTextViewName;
        private Button mButtonAccept;
        private Button mButtonIgnore;

        public UserRequestHolder(View view) {
            super(view);

            mTextViewName = view.findViewById(R.id.viewholder_friend_request_name);
            mButtonAccept = view.findViewById(R.id.viewholder_friend_request_accept);
            mButtonIgnore = view.findViewById(R.id.viewholder_friend_request_ignore);
        }

        public void bind(User user) {
            mUser = user;

            mTextViewName.setText(user.getName());
            mButtonAccept.setOnClickListener(responseOnClickListener);
            mButtonIgnore.setOnClickListener(responseOnClickListener);
        }

        private void respondRequest(boolean doesAccept) {
            DatabaseReference friendRequestRef = mReference.child(Reference.FRIEND_REQUESTS);
            DatabaseReference friendRef = mReference.child(Reference.FRIENDS);

            if (doesAccept) {
                friendRef.child(getUid())
                        .child(mUser.getUid())
                        .child(MapBoxFBSchema.FriendsChild.REQUEST_LOCATION)
                        .setValue(false);

                friendRef.child(mUser.getUid())
                        .child(getUid())
                        .child(MapBoxFBSchema.FriendsChild.REQUEST_LOCATION)
                        .setValue(false);
            }

            friendRequestRef.child(getUid()).child(mUser.getUid()).removeValue();
        }

        private View.OnClickListener responseOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewId = v.getId();
                respondRequest(viewId == R.id.viewholder_friend_request_accept);
            }
        };
    }

    ValueEventListener friendRequestListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            DatabaseReference userReference = mReference.child(Reference.USERS);
            final List<String> uids = new ArrayList<>();

            for (DataSnapshot data : dataSnapshot.getChildren()) {
                uids.add(data.getKey());
            }

            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mRequests = new ArrayList<>();

                    for (String uid : uids) {
                        User user = dataSnapshot.child(uid).getValue(User.class);
                        mRequests.add(user);
                    }

                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
