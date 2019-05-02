package com.joey.android.mapbox.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.joey.android.mapbox.firebase.MapBoxFBSchema.FriendsChild;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.model.FriendList;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.model.UserInfo;

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
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
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

//        mFriendsReference.child(getUid()).removeEventListener(friendListListener);
        mAdapter.clearListener();
        Log.i(TAG, "Fragment.onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Fragment.onDestroy() called");
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
        }
    }

    private class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder> {
        private DatabaseReference mFriendsReference;
        private DatabaseReference mUsersReference;
        private ChildEventListener mChildEventListener;


        private List<User> mUsers = new ArrayList<>();
        private List<String> mUsersId = new ArrayList<>();
        private Map<User, Bitmap> mProfileImageMap;

        public FriendListAdapter() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            mFriendsReference = reference.child(Reference.FRIENDS);
            mUsersReference = reference.child(Reference.USERS);

            mEmptyListTextView.setVisibility(View.VISIBLE);

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.i(TAG, "onChildAdded:" + dataSnapshot);

                    final UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(dataSnapshot.getKey());

                    mUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String uid = userInfo.getUid();
                            User user = dataSnapshot.child(uid)
                                    .getValue(User.class);

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
                    Log.i(TAG, "onChildChanged:" + dataSnapshot);

                    String uid = dataSnapshot.getKey();
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(uid);

                    int userIndex = mUsersId.indexOf(uid);
                    if (userIndex > -1) {
                        // Replace with new user data
                        mUsers.get(userIndex).additionalInfo(userInfo);

                        // Update RecyclerView
                        notifyItemChanged(userIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + uid);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onChildRemoved:" + dataSnapshot);

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

        public void clearListener() {
            if (mChildEventListener != null) {
                mFriendsReference.child(getUid()).removeEventListener(mChildEventListener);
            }
        }

        public class DownloadProfileImageTask extends AsyncTask<User, Void, Map<User, Bitmap>> {
            @Override
            protected Map<User, Bitmap> doInBackground(User... users) {
                Log.i(TAG, "Async Task called to download image");
                User user = users[0];
                Map<User, Bitmap> map = new HashMap<>();

                String photoUri = user.getPhotoUri();
                Bitmap photoBitmap;

                try {
                    byte[] imageBytes = getUrlBytes(photoUri);
                    photoBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    map.put(user, photoBitmap);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to fetch bitmap from url");
                }

                return map;
            }

            @Override
            protected void onPostExecute(Map<User, Bitmap> map) {
                mProfileImageMap = map;
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

        private class FriendHolder extends RecyclerView.ViewHolder
                implements OnMapReadyCallback {
            private User mUser;
            private File mPhotoFile;
            private GoogleMap mGoogleMap;

            private TextView mFriendNameTextView;
            private TextView mLastUpdatedTextView;
            private Button mRequestLocationButton;
            private Button mSendLocationButton;
            private MapView mMapView;
            private CircleImageView mFriendImageView;

            public FriendHolder(View view) {
                super(view);

                mFriendNameTextView = view.findViewById(R.id.viewholder_friend_list_name);
                mLastUpdatedTextView = view.findViewById(R.id.viewholder_friend_list_time_updated);
                mRequestLocationButton = view.findViewById(R.id.viewholder_friend_list_get_location);
                mSendLocationButton = view.findViewById(R.id.viewholder_friend_list_send_location);
                mFriendImageView = view.findViewById(R.id.viewholder_friend_list_image);
                mMapView = view.findViewById(R.id.viewholder_friend_list_map);

                mRequestLocationButton.setOnClickListener(requestLocationOnClickListener);
                mSendLocationButton.setOnClickListener(sendLocationOnClickListener);
//                mFriendImageView.setOnClickListener(friendImageOnClickListener);

                if (mMapView != null) {
                    // Initialise the MapView
                    mMapView.onCreate(null);
                    // Set the map ready callback to receive the GoogleMap object
                    mMapView.getMapAsync(this);
                }
            }

            public void bind(User user) {
                mUser = user;

                new DownloadProfileImageTask().execute(user);

                LatLng latLng = mUser.getLatLng();
                Date currentTime = mUser.getLastUpdated();

                mFriendNameTextView.setText(mUser.getName());

                mPhotoFile = FriendList.get(getActivity()).getPhotoFile(mUser);

                if (mProfileImageMap != null) {
                    Log.i(TAG, "Setting Profile Image");
                    Bitmap image = mProfileImageMap.get(user);
                    mFriendImageView.setImageBitmap(image);
                }

                if (currentTime != null) {
                    mLastUpdatedTextView.setText(currentTime.toString());
                }

                if (mLocation == null) {
                    mSendLocationButton.setEnabled(false);
                } else {
                    mSendLocationButton.setEnabled(true);
                }

                if (user.isRequesting()) {
//                mSendLocationButton.setBackgroundColor(getResources().getColor(R.color.colorBorder));
                    mSendLocationButton.setSelected(true);
                } else {
//                mSendLocationButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    mSendLocationButton.setSelected(false);
                }

//                if (mPhotoFile.exists()) {
//                    mFriendImageView.setImageBitmap(BitmapFactory.decodeFile(mPhotoFile.getPath()));
//                } else {
//                    mFriendImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_pic));
//                }

                if (latLng != null && mGoogleMap != null) {
                    setMapLocation(latLng);
                }

                GoogleMap map = mGoogleMap;
                if (map != null) {

                }
            }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                mLastUpdatedTextView.bringToFront();
                LatLng userLatLng = mUser.getLatLng();
                MapsInitializer.initialize(UserListFragment.this.getActivity());
                mGoogleMap = googleMap;
                setMapLocation(userLatLng);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            private void setMapLocation(LatLng userLatLng) {
                if (userLatLng != null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                    mGoogleMap.addMarker(new MarkerOptions().position(userLatLng));
                } else {
//                mGoogleMap.moveCamera(CameraUpdateFactory
//                        .newLatLngZoom(new LatLng(mLocation
//                                .getLatitude(), mLocation.getLongitude()), 13f));
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

            private View.OnClickListener sendLocationOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference friendRef =  mFriendsReference.child(mUser.getUid())
                            .child(getUid());
                    DatabaseReference userRef = mFriendsReference.child(getUid())
                            .child(mUser.getUid());

                    // Inverted timestamp so firebase returns most recent first
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

//            private View.OnClickListener friendImageOnClickListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (hasReadExternalStoragePermission()) {
//                        Uri uri = FileProvider.getUriForFile(getActivity(),
//                                "com.joey.android.mapbox.fileprovider",
//                                mPhotoFile);
//                        Log.i(TAG, "" + uri);
//                        // Create an Intent with action as ACTION_PICk
//                        Intent intent = new Intent(Intent.ACTION_PICK);
//                        // Sets the type as image/*. This ensures only components of type image are selected
//                        intent.setType("image/*");
//                        // Put extra for gallery to return a cropped image
//                        intent.putExtra("crop", true);
//                        intent.putExtra("outputX", 200);
//                        intent.putExtra("outputY", 200);
//                        intent.putExtra("aspectX", 1);
//                        intent.putExtra("aspectY", 1);
//                        intent.putExtra("scale", true);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//                        // We pass an extra array with the accepted mime types.
//                        // This will ensure only components with these MIME types as targets.
//                        String[] mimeTypes = {"image/jpeg", "image/png"};
//                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//
//                        List<ResolveInfo> galleryActivities = getActivity()
//                                .getPackageManager().queryIntentActivities(intent,
//                                        PackageManager.MATCH_DEFAULT_ONLY);
//
//                        for (ResolveInfo activity : galleryActivities) {
//                            getActivity().grantUriPermission(activity.activityInfo.packageName,
//                                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                        }
//
//                        // Launching the Intent with chooser
//                        Intent chooser = Intent.createChooser(intent, "Choose Image");
//                        startActivityForResult(chooser, REQUEST_IMAGE_CODE);
//                    } else {
//                        requestPermissions(STORAGE_PERMISSIONS,
//                                REQUEST_STORAGE_PERMISSION) ;
//                    }
//                }
//            };
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
