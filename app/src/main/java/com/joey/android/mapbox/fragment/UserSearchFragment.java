package com.joey.android.mapbox.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.util.ProfileImageUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchFragment extends FirebaseFragment {
    private static final String TAG = "UserSearchFragment";

    private DatabaseReference mReference;
    private User mUser;

    private SearchView mEmailSearchView;
    private LinearLayout mUserProfileLayout;
    private CircleImageView mProfileImage;
    private ImageView mBlurryImage;
    private TextView mUserNameText;
    private TextView mUserStatusText;
    private Button mResponseButton;

    public static UserSearchFragment newInstance() {
        return new UserSearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_user_search, container, false);

        mEmailSearchView = view.findViewById(R.id.edit_email);
        mEmailSearchView.setOnQueryTextListener(userQueryListener);

        mUserProfileLayout = view.findViewById(R.id.layout_user_profile);

        mProfileImage = view.findViewById(R.id.fragment_user_search_image);
        mBlurryImage = view.findViewById(R.id.fragment_user_search_blurry);

        mUserNameText = view.findViewById(R.id.fragment_user_search_name);
        mUserStatusText = view.findViewById(R.id.fragment_user_search_status);

        mResponseButton = view.findViewById(R.id.fragment_user_search_response);
        mResponseButton.setOnClickListener(addUserOnClickListener);

        return view;
    }

    private void searchUser(String email) {
        mReference.child(Reference.EMAILS)
                .child(encodeEmail(email))
                .addListenerForSingleValueEvent(checkUserListener);

        mUserProfileLayout.setVisibility(View.VISIBLE);
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

    View.OnClickListener addUserOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addUser();
            mResponseButton.setText(R.string.sent_request);
            mResponseButton.setEnabled(false);
        }
    };

    SearchView.OnQueryTextListener userQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            String email = mEmailSearchView.getQuery().toString().trim().toLowerCase();
            searchUser(email);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    ValueEventListener checkUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull final DataSnapshot userSnapshot) {
            // Check that user has signed up for app before
            if (userSnapshot.exists()) {
                // Retrieve user from corresponding email
                mReference.child(Reference.USERS)
                        .child(userSnapshot.getValue().toString())
                        .addListenerForSingleValueEvent(getUserListener);

                if (!userSnapshot.getValue().toString().equals(getUid())) {
                    mReference.child(Reference.FRIEND_REQUESTS)
                            .addListenerForSingleValueEvent(checkFriendRequestListener);
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

    ValueEventListener getUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            new DownloadProfileImageTask().execute(mUser);
            mUserNameText.setText(mUser.getName());

            // Check if the email user searched refers to own email
            if (mUser.getUid().equals(getUid())) {
                mUserStatusText.setText(R.string.own_email);
                mUserStatusText.setVisibility(View.VISIBLE);
                mResponseButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener checkFriendRequestListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot friendRequestSnapshot) {
            // Check if there is a pending request already
            String friendUid = mUser.getUid();
            if (friendRequestSnapshot.child(friendUid).exists()) {
                // Update status to request is pending
                mUserStatusText.setText("has not responded to your request yet");
                mUserStatusText.setVisibility(View.VISIBLE);
                mResponseButton.setEnabled(false);
                mResponseButton.setVisibility(View.GONE);
                return;
            }

            if (friendRequestSnapshot.child(getUid()).exists()) {
                // Update status to waiting for response
                mUserStatusText.setText(R.string.waiting_for_response);
                mUserStatusText.setVisibility(View.VISIBLE);
                mResponseButton.setVisibility(View.GONE);
                return;
            }

            // Check if users are friends already
            mReference.child(Reference.FRIENDS)
                    .child(friendUid)
                    .child(getUid())
                    .addListenerForSingleValueEvent(isFriendListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener isFriendListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot friendsSnapshot) {
            if (friendsSnapshot.exists()) {
                mUserStatusText.setText(R.string.existing_friend);
                mUserStatusText.setVisibility(View.VISIBLE);
                mResponseButton.setEnabled(false);
                mResponseButton.setVisibility(View.GONE);
            } else {
                mUserStatusText.setVisibility(View.GONE);
                mResponseButton.setText(R.string.add_friend);
                mResponseButton.setEnabled(true);
                mResponseButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public class DownloadProfileImageTask extends AsyncTask<User, Void, Bitmap> {
        User mUser;

        @Override
        protected Bitmap doInBackground(User... users) {
            Log.i(TAG, "Async Task called to download image");
            mUser = users[0];

            String photoUri = mUser.getPhotoUri();
            Bitmap photoBitmap = null;

            try {
                byte[] imageBytes = ProfileImageUtility.getUrlBytes(photoUri);
                photoBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch bitmap from url");
            }

            return photoBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProfileImage.setImageBitmap(bitmap);
            Bitmap blurImage = ProfileImageUtility.blur(bitmap, getActivity());
            mBlurryImage.setImageBitmap(blurImage);
        }
    }
}
