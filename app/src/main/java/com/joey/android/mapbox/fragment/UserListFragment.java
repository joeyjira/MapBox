package com.joey.android.mapbox.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.activity.FriendProfileActivity;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.FriendsChild;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.model.FriendList;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.model.UserInfo;
import com.joey.android.mapbox.util.ProfileImageUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListFragment extends FirebaseFragment {
    private static final String TAG = "UserListFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final String[] STORAGE_PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_IMAGE_CODE = 2;

    private RecyclerView mFriendListRecyclerView;
    private TextView mEmptyListTextView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FriendListAdapter mAdapter;
    private Location mLocation;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mFriendListRecyclerView = view.findViewById(R.id.recycler_view_map_box);
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFriendListRecyclerView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Layout is complete, remove window background
                        getActivity().setTheme(R.style.MapBoxTheme);

                        mFriendListRecyclerView.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });

        mEmptyListTextView = view.findViewById(R.id.fragment_recycler_view_empty_list);
        mEmptyListTextView.setText("Try adding someone!");

        updateUI();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CODE) {
            // TODO: Revoke uri write permission

        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        mFriendsReference.child(getUid())
//                .orderByChild(FriendsChild.LAST_UDPATED)
//                .addValueEventListener(friendListListener);
        Log.i(TAG, "Fragment.onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();

        startLocationUpdates();
        Log.i(TAG, "Fragment.onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();
        Log.i(TAG, "Fragment.onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i(TAG, "Fragment.onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Fragment.onDestroy() called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAdapter.clearListener();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.i(TAG, "Fragment.onAttach() called");
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    // Location permission check is done in hasLocationPermission()
    @SuppressWarnings({"MissingPermission"})
    private void startLocationUpdates() {
        if (hasLocationPermission()) {
            mFusedLocationProviderClient.requestLocationUpdates(createLocationRequest(),
                    locationCallback, null);
        } else {
            // Toast ask user to accept location permission
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasReadExternalStoragePermission() {
        int result = ContextCompat
                .checkSelfPermission(getActivity(), STORAGE_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void updateUI() {
        if (mAdapter == null) {
            mAdapter = new FriendListAdapter();
            mFriendListRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.attachListener();
        }
    }

    private class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder> {
        private DatabaseReference mFriendsReference;
        private DatabaseReference mUsersReference;
        private ChildEventListener mChildEventListener;


        private List<User> mUsers = new ArrayList<>();
        private List<String> mUsersId = new ArrayList<>();
        private Map<User, Bitmap> mProfileImageMap = new HashMap<>();

        public FriendListAdapter() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            mFriendsReference = reference.child(Reference.FRIENDS);
            mUsersReference = reference.child(Reference.USERS);

            mEmptyListTextView.setVisibility(View.VISIBLE);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    final UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(dataSnapshot.getKey());

                    mUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String uid = userInfo.getUid();
                            User user = dataSnapshot.child(uid)
                                    .getValue(User.class);

                            // Download Profile Image from Google
                            new DownloadProfileImageTask().execute(user);

                            user.additionalInfo(userInfo);

                            // Update RecyclerView
                            mUsers.add(user);
                            mUsersId.add(uid);
                            mEmptyListTextView.setVisibility(View.GONE);
                            notifyItemInserted(mUsers.size() - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String uid = dataSnapshot.getKey();
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(uid);
                    int userIndex = mUsersId.indexOf(uid);

                    if (userIndex > -1) {
                        // Replace with new user data
                        User user = mUsers.get(userIndex);
                        user.additionalInfo(userInfo);

                        // Update RecyclerView
                        notifyItemChanged(userIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + uid);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String uid = dataSnapshot.getKey();
                    int userIndex = mUsersId.indexOf(uid);
                    if (userIndex > -1) {
                        mUsers.remove(userIndex);
                        mUsersId.remove(userIndex);

                        // Update RecyclerView
                        notifyItemRemoved(userIndex);
                        if (mUsers.isEmpty()) {
                            mEmptyListTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.i(TAG, "onChildMoved:" + dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled");
                }
            };



            mFriendsReference.child(getUid())
                    .orderByChild(FriendsChild.LAST_UDPATED)
                    .addChildEventListener(childEventListener);

            // Store childListener so it can be removed later
            mChildEventListener = childEventListener;
        }

        @NonNull
        @Override
        public FriendHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.viewholder_friend, viewGroup, false);
            return new FriendHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendHolder friendHolder, int position) {
            User user = mUsers.get(position);
            friendHolder.bind(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public void attachListener() {
            mFriendsReference.child(getUid()).addChildEventListener(mChildEventListener);
        }

        public void clearListener() {
            if (mChildEventListener != null) {
                mFriendsReference.child(getUid()).removeEventListener(mChildEventListener);
            }
        }

        public class DownloadProfileImageTask extends AsyncTask<User, Void, Bitmap> {
            User mUser;

            @Override
            protected Bitmap doInBackground(User... users) {
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
                mProfileImageMap.put(mUser, bitmap);
                getActivity().setTheme(R.style.MapBoxTheme);
            }
        }

        private class FriendHolder extends RecyclerView.ViewHolder
                implements OnMapReadyCallback {
            private User mUser;
            private Bitmap mProfileImage;
            private GoogleMap mGoogleMap;

            private LinearLayout mFriendTab;
            private TextView mFriendNameTextView;
            private Button mRequestLocationButton;
            private Button mSendLocationButton;
            private MapView mMapView;
            private CircleImageView mFriendImageView;

            public FriendHolder(View view) {
                super(view);

                mFriendTab = view.findViewById(R.id.viewholder_friend_tab);
                mFriendNameTextView = view.findViewById(R.id.viewholder_friend_list_name);
                mRequestLocationButton = view.findViewById(R.id.viewholder_friend_list_get_location);
                mSendLocationButton = view.findViewById(R.id.viewholder_friend_list_send_location);
                mFriendImageView = view.findViewById(R.id.viewholder_friend_list_image);
                mMapView = view.findViewById(R.id.viewholder_friend_list_map);

                // Scale button when pressed
                mSendLocationButton.setOnTouchListener(sendLocationListener);

                mRequestLocationButton.setOnClickListener(requestLocationOnClickListener);
                mSendLocationButton.setOnClickListener(sendLocationOnClickListener);

                if (mMapView != null) {
                    // Initialise the MapView
                    mMapView.onCreate(null);
                    // Set the map ready callback to receive the GoogleMap object
                    mMapView.getMapAsync(this);
                }
            }

            public void bind(User user) {
                mUser = user;
                LatLng latLng = mUser.getLatLng();
                long lastUpdated = mUser.getLastUpdated();
                long timeElapsed = new Date().getTime() - lastUpdated;

                setLastUpdated(timeElapsed);

                mFriendNameTextView.setText(mUser.getName());

                if (mProfileImageMap != null) {
                    mProfileImage = mProfileImageMap.get(user);
                    mFriendImageView.setImageBitmap(mProfileImage);
                }

                if (mLocation == null) {
                    mSendLocationButton.setEnabled(false);
                } else {
                    mSendLocationButton.setEnabled(true);
                }

                if (user.isRequesting()) {
                    mSendLocationButton.setSelected(true);
                } else {
                    mSendLocationButton.setSelected(false);
                }

                if (latLng != null && mGoogleMap != null) {
                    setMapLocation(latLng);
                }

                GoogleMap map = mGoogleMap;
                if (map != null) {

                }

                mFriendTab.setOnClickListener(friendProfileListener);
            }

            public void setLastUpdated(long timeElapsed) {
                Resources res = getResources();
                Long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
                Long hours = TimeUnit.MILLISECONDS.toHours(timeElapsed);
                Long days = TimeUnit.MILLISECONDS.toDays(timeElapsed);

                String buttonText = res.getString(R.string.request_location);
                if (minutes == 0) {
                    mRequestLocationButton.setText(buttonText + res.getString(R.string.updated_moments_ago));
                } else if (minutes < 61) {
                    mRequestLocationButton.setText(buttonText + res.getQuantityString(R.plurals.minutesPassed, minutes.intValue(), minutes.intValue()));
                } else if (hours < 25) {
                    mRequestLocationButton.setText(buttonText + res.getQuantityString(R.plurals.hoursPassed, hours.intValue(), hours.intValue()));
                } else {
                    mRequestLocationButton.setText(buttonText + res.getQuantityString(R.plurals.daysPassed, days.intValue(), days.intValue()));
                }
            }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                LatLng userLatLng = mUser.getLatLng();
                MapsInitializer.initialize(getActivity());
                setMapLocation(userLatLng);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            private void setMapLocation(LatLng userLatLng) {
                if (userLatLng != null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                    mGoogleMap.addMarker(new MarkerOptions().position(userLatLng));
                }
            }

            private View.OnClickListener requestLocationOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendsReference.child(mUser.getUid())
                            .child(getUid())
                            .child(FriendsChild.REQUEST_LOCATION)
                            .setValue(true);
                }
            };

            View.OnTouchListener sendLocationListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = (float) 1.25;
                        float y = (float) 1.25;

                        mSendLocationButton.setScaleX(x);
                        mSendLocationButton.setScaleY(y);
                    } else if (event.getAction() == MotionEvent.ACTION_UP
                            || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        float x = 1;
                        float y = 1;

                        mSendLocationButton.setScaleX(x);
                        mSendLocationButton.setScaleY(y);
                    }
                    return false;
                }
            };

            private View.OnClickListener sendLocationOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference friendRef =  mFriendsReference.child(mUser.getUid())
                            .child(getUid());
                    DatabaseReference userRef = mFriendsReference.child(getUid())
                            .child(mUser.getUid());

                    // Inverted timestamp so Firebase returns most recent first
                    long currentTime = -1 * (new Date().getTime());

                    if (mLocation != null) {
                        friendRef.child(FriendsChild.LATITUDE)
                                .setValue(mLocation.getLatitude());

                        friendRef.child(FriendsChild.LONGITUDE)
                                .setValue(mLocation.getLongitude());

                        friendRef.child(FriendsChild.LAST_UDPATED)
                                .setValue(currentTime);

                        userRef.child(FriendsChild.REQUEST_LOCATION)
                                .setValue(false);
                    }
                }
            };

            View.OnClickListener friendProfileListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = FriendProfileActivity.newIntent(getActivity(), mUser, mProfileImage);
                    startActivity(intent);
                }
            };
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                mLocation = location;
            }

            mAdapter.notifyDataSetChanged();
        }
    };
}
