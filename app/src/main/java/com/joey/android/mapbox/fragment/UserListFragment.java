package com.joey.android.mapbox.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.firebase.MapBoxFBSchema;
import com.joey.android.mapbox.firebase.MapBoxFBSchema.Reference;
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.model.FriendList;
import com.joey.android.mapbox.R;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private DatabaseReference mFriendsReference;
    private DatabaseReference mUsersReference;

    private RecyclerView mFriendListRecyclerView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FriendListAdapter mAdapter;
    private Location mLocation;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mFriendsReference = reference.child(Reference.FRIENDS);
        mUsersReference = reference.child(Reference.USERS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mFriendListRecyclerView = view.findViewById(R.id.recycler_view_map_box);
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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

        Log.i(TAG, "requestCode:" + requestCode);


        if (requestCode == REQUEST_IMAGE_CODE) {
            // TODO: Revoke uri write permission

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "Fragment has been started");
    }

    @Override
    public void onResume() {
        super.onResume();

        startLocationUpdates();
        updateUI();
        Log.i(TAG, "Fragment has resumed");
    }

    @Override
    public void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Fragment has been destroeyd");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.i(TAG, "Fragment has been attached");
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
            mAdapter.notifyDataSetChanged();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private class FriendListAdapter extends RecyclerView.Adapter<FriendHolder> {
        private List<User> mUsers = new ArrayList<>();

        public FriendListAdapter() {
            mFriendsReference.child(getUid()).addValueEventListener(friendListListener);
        }

        @NonNull
        @Override
        public FriendHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.viewholder_friend_list, viewGroup, false);
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

        ValueEventListener friendListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<AbstractMap.SimpleImmutableEntry<String, Boolean>> uidEntries = new ArrayList<>();
                final Map<String, LatLng> locationMap = new HashMap<>();
                mUsers.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String uid = data.getKey();
                    if (data.child(MapBoxFBSchema.FriendsChild.LATITUDE).exists()) {
                        Double latitude = (Double) data.child(MapBoxFBSchema.FriendsChild.LATITUDE).getValue();
                        Double longitude = (Double) data.child(MapBoxFBSchema.FriendsChild.LONGITUDE).getValue();
                        locationMap.put(uid,
                                new LatLng(latitude, longitude));
                    }
                    Boolean isRequesting = (Boolean) data.child(MapBoxFBSchema.FriendsChild.REQUEST_LOCATION).getValue();
                    uidEntries.add(new AbstractMap.SimpleImmutableEntry<>(uid, isRequesting));

                }

                mUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (AbstractMap.SimpleImmutableEntry entry : uidEntries) {
                            String uid = entry.getKey().toString();
                            User user = dataSnapshot.child(uid)
                                    .getValue(User.class);
                            user.setRequesting((boolean) entry.getValue());

                            if (locationMap.containsKey(uid)) {
                                user.setLatLng(locationMap.get(uid));
                            }

                            mUsers.add(user);
                        }

                        notifyDataSetChanged();
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

    private class FriendHolder extends RecyclerView.ViewHolder
            implements OnMapReadyCallback {
        private User mUser;
        private File mPhotoFile;
        private GoogleMap mGoogleMap;

        private TextView mFriendNameTextView;
        private Button mRequestLocationButton;
        private Button mSendLocationButton;
        private MapView mMapView;
        private ImageView mFriendImageView;

        public FriendHolder(View view) {
            super(view);

            mFriendNameTextView = view.findViewById(R.id.viewholder_friend_list_name);
            mRequestLocationButton = view.findViewById(R.id.viewholder_friend_list_get_location);
            mSendLocationButton = view.findViewById(R.id.viewholder_friend_list_send_location);
            mFriendImageView = view.findViewById(R.id.viewholder_friend_list_image);
            mMapView = view.findViewById(R.id.viewholder_friend_list_map);

            mRequestLocationButton.setOnClickListener(requestLocationOnClickListener);
            mSendLocationButton.setOnClickListener(sendLocationOnClickListener);
            mFriendImageView.setOnClickListener(friendImageOnClickListener);

            if (mMapView != null) {
                // Initialise the MapView
                mMapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mMapView.getMapAsync(this);
            }
        }

        public void bind(User user) {
            mUser = user;

            mFriendNameTextView.setText(mUser.getName());

            if (mLocation == null) {
                mSendLocationButton.setEnabled(false);
            } else {
                mSendLocationButton.setEnabled(true);
            }

            if (user.isRequesting()) {
                mSendLocationButton.setBackgroundColor(getResources().getColor(R.color.colorBorder));
            } else {
                mSendLocationButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

            mPhotoFile = FriendList.get(getActivity()).getPhotoFile(mUser);
            if (mPhotoFile.exists()) {
                mFriendImageView.setImageBitmap(BitmapFactory.decodeFile(mPhotoFile.getPath()));
            } else {
                mFriendImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_pic));
            }

            GoogleMap map = mGoogleMap;
            if (map != null) {

            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng userLatLng = mUser.getLatLng();
            MapsInitializer.initialize(UserListFragment.this.getActivity());
            mGoogleMap = googleMap;
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (userLatLng != null) {
                setMapLocation(userLatLng);
            }
        }

        private void setMapLocation(LatLng userLatLng) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
            mGoogleMap.addMarker(new MarkerOptions().position(userLatLng));
        }

        private View.OnClickListener requestLocationOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendsReference.child(mUser.getUid())
                        .child(getUid())
                        .child(MapBoxFBSchema.FriendsChild.REQUEST_LOCATION)
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

                if (mLocation != null) {
                            friendRef.child(MapBoxFBSchema.FriendsChild.LATITUDE)
                                    .setValue(mLocation.getLatitude());

                            friendRef.child(MapBoxFBSchema.FriendsChild.LONGITUDE)
                                    .setValue(mLocation.getLongitude());

                            userRef.child(MapBoxFBSchema.FriendsChild.REQUEST_LOCATION)
                                    .setValue(false);

                }
            }
        };

        private View.OnClickListener friendImageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasReadExternalStoragePermission()) {
                    Uri uri = FileProvider.getUriForFile(getActivity(),
                            "com.joey.android.mapbox.fileprovider",
                            mPhotoFile);
                    Log.i(TAG, "" + uri);
                    // Create an Intent with action as ACTION_PICk
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    // Sets the type as image/*. This ensures only components of type image are selected
                    intent.setType("image/*");
                    // Put extra for gallery to return a cropped image
                    intent.putExtra("crop", true);
                    intent.putExtra("outputX", 200);
                    intent.putExtra("outputY", 200);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    // We pass an extra array with the accepted mime types.
                    // This will ensure only components with these MIME types as targets.
                    String[] mimeTypes = {"image/jpeg", "image/png"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                    List<ResolveInfo> galleryActivities = getActivity()
                            .getPackageManager().queryIntentActivities(intent,
                                    PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo activity : galleryActivities) {
                        getActivity().grantUriPermission(activity.activityInfo.packageName,
                                uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }

                    // Launching the Intent with chooser
                    Intent chooser = Intent.createChooser(intent, "Choose Image");
                    startActivityForResult(chooser, REQUEST_IMAGE_CODE);
                } else {
                    requestPermissions(STORAGE_PERMISSIONS,
                            REQUEST_STORAGE_PERMISSION) ;
                }
            }
        };
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
