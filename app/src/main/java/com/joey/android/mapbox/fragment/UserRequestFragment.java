package com.joey.android.mapbox.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    @Override
    public void onStop() {
        super.onStop();

        mAdapter.clearListener();
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

    private class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.UserRequestHolder> {
        List<User> mUsers;
        Map<User, Bitmap> mProfileImageMap = new HashMap<>();

        public UserRequestAdapter(List<User> users) {
            mUsers = users;

            mReference.child(Reference.FRIEND_REQUESTS)
                    .child(getUid())
                    .addValueEventListener(friendRequestListener);
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

        public void clearListener() {
            mReference.child(Reference.FRIEND_REQUESTS)
                    .child(getUid())
                    .removeEventListener(friendRequestListener);
        }

        public void setUsers(List<User> users) {
            mUsers = users;
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
                mImageViewProfile = view.findViewById(R.id.viewholder_friend_request_image);
            }

            public void bind(User user) {
                mUser = user;

                mTextViewName.setText(user.getName());
                mButtonAccept.setOnClickListener(responseOnClickListener);
                mButtonIgnore.setOnClickListener(responseOnClickListener);
                mImageViewProfile.setImageBitmap(mProfileImageMap.get(user));
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

        public class DownloadProfileImageTask extends AsyncTask<User, Void, Bitmap> {
            User mUser;

            @Override
            protected Bitmap doInBackground(User... users) {
                Log.i(TAG, "Async Task called to download image");
                mUser = users[0];

                String photoUri = mUser.getPhotoUri();
                Bitmap photoBitmap = null;

                try {
                    byte[] imageBytes = getUrlBytes(photoUri);
                    photoBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    Log.i(TAG, "PHOTO" + photoUri);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to fetch bitmap from url");
                }

                return photoBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                Log.i(TAG, "User:" + mUser + ", bitmap:" + bitmap);
                mProfileImageMap.put(mUser, bitmap);
            }

            private byte[] getUrlBytes(String urlSpec) throws IOException {
                URL url = new URL(urlSpec);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream in = connection.getInputStream();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new IOException(connection.getResponseMessage() +
                                ": with " +
                                urlSpec);
                    }

                    int bytesRead;
                    byte[] buffer = new byte[1024];

                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }

                    out.close();
                    return out.toByteArray();
                } finally {
                    connection.disconnect();
                }
            }
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
                            new DownloadProfileImageTask().execute(user);
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
}
