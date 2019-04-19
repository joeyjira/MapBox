package com.joey.android.mapbox.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.model.FriendList;
import com.joey.android.mapbox.R;

import java.io.File;
import java.util.List;

public class UserListFragment extends Fragment {
    private static final String TAG = "UserListFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] STORAGE_PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CODE = 2;

    private RecyclerView mFriendListRecyclerView;
    private FriendListAdapter mAdapter;

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        updateUI();
        Log.i(TAG, "Fragment has resumed");
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

    private void findLocation() {
//        LocationRequest request = LocationRequest.create();
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        request.setNumUpdates(1);
//        request.setInterval(0);
        ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        LocationServices.getFusedLocationProviderClient(getActivity())
                .getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i(TAG, "Got a fix: " + location);
                    }
                });
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
        FriendList friendList = FriendList.get(getActivity());
        List<User> users = friendList.getFriends();

        if (mAdapter == null) {
            mAdapter = new FriendListAdapter(users);
            mFriendListRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setUsers(users);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private class FriendListAdapter extends RecyclerView.Adapter<FriendHolder> {
        private List<User> mUsers;

        public FriendListAdapter(List<User> users) {
            mUsers = users;
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

        public void setUsers(List<User> users) {
            mUsers = users;
        }
    }

    private class FriendHolder extends RecyclerView.ViewHolder
            implements OnMapReadyCallback {
        private User mUser;
        private File mPhotoFile;
        private GoogleMap mGoogleMap;

        private TextView mFriendNameTextView;
        private Button mLocationButton;
        private MapView mMapView;
        private ImageView mFriendImageView;

        public FriendHolder(View view) {
            super(view);

            mFriendNameTextView = view.findViewById(R.id.viewholder_friend_list_name);
            mLocationButton = view.findViewById(R.id.viewholder_friend_list_get_location);
            mFriendImageView = view.findViewById(R.id.viewholder_friend_list_image);
            mMapView = view.findViewById(R.id.viewholder_friend_list_map);

            mLocationButton.setOnClickListener(locationOnClickListener);
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
            Log.i(TAG, "" + UserListFragment.this.getActivity());
            MapsInitializer.initialize(UserListFragment.this.getActivity());
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.448849,-121.898045), 13f));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(37.448849,-121.898045)));
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void setMapLocation() {

        }

        private View.OnClickListener locationOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasLocationPermission()) {
                    findLocation();
                } else {
                    requestPermissions(LOCATION_PERMISSIONS,
                            REQUEST_LOCATION_PERMISSIONS);
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

}
