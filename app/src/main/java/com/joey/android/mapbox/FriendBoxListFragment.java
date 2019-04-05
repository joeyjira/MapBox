package com.joey.android.mapbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class FriendBoxListFragment extends Fragment {
    private static final String TAG = "FriendBoxListFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_IMAGE_CODE = 1;

    private RecyclerView mFriendBoxRecyclerView;

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
            // Create an Intent with action as ACTION_PICk
            Intent intent = new Intent(Intent.ACTION_PICK);
            // Sets the type as image/*. This ensures only components of type image are selected
            intent.setType("image/*");
            // We pass an extra array with the accepted mime types.
            // This will ensure only components with these MIME types as targets.
            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            // Launching the Intent
            startActivityForResult(intent, REQUEST_IMAGE_CODE);
        }
    };

    public static FriendBoxListFragment newInstance() {
        return new FriendBoxListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_box_list, container, false);

        mFriendBoxRecyclerView = view.findViewById(R.id.map_box_recycler_view);
        mFriendBoxRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupAdapter() {
        if (isAdded()) {
            mFriendBoxRecyclerView.setAdapter(new FriendBoxAdapter());
        }
    }

    private void findLocation() {
//        LocationRequest request = LocationRequest.create();
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        request.setNumUpdates(1);
//        request.setInterval(0);

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

    private class FriendBoxHolder extends RecyclerView.ViewHolder
            implements OnMapReadyCallback {
        private FriendBox mFriendBox;
        private GoogleMap mGoogleMap;

        private TextView mFriendNameTextView;
        private Button mLocationButton;
        private MapView mMapView;
        private ImageView mFriendImageView;

        public FriendBoxHolder(View friendBoxView) {
            super(friendBoxView);

            mFriendNameTextView = friendBoxView.findViewById(R.id.friend_name);

            mLocationButton = friendBoxView.findViewById(R.id.button_get_location);
            mLocationButton.setOnClickListener(locationOnClickListener);

            mFriendImageView = friendBoxView.findViewById(R.id.friend_pic);
            mFriendImageView.setOnClickListener(friendImageOnClickListener);

            mMapView = friendBoxView.findViewById(R.id.friend_box_map_view);
            Log.i(TAG, "" + mMapView);
            if (mMapView != null) {
                // Initialise the MapView
                mMapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mMapView.getMapAsync(this);
            }
        }

        public void bind(FriendBox friendBox) {
            mFriendBox = friendBox;
            mFriendNameTextView.setText(friendBox.getName());

            GoogleMap map = mGoogleMap;
            if (map != null) {

            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.i(TAG, "" + FriendBoxListFragment.this.getActivity());
            MapsInitializer.initialize(FriendBoxListFragment.this.getActivity());
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.448849,-121.898045), 13f));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(37.448849,-121.898045)));
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void setMapLocation() {

        }
    }

    private class FriendBoxAdapter extends RecyclerView.Adapter<FriendBoxHolder> {
        private List<FriendBox> mFriendBoxes;

        public FriendBoxAdapter() {
            mFriendBoxes = new ArrayList<>();
            mFriendBoxes.add(new FriendBox("Anne", "Nguyen"));
            mFriendBoxes.add(new FriendBox("Nixon", "Yiu"));
            mFriendBoxes.add(new FriendBox("Kevin", "Lam"));
            mFriendBoxes.add(new FriendBox("James", "Huang"));
            mFriendBoxes.add(new FriendBox("Michael", "Lau"));
            mFriendBoxes.add(new FriendBox("Joey", "Jirasevijinda"));
        }

        @NonNull
        @Override
        public FriendBoxHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_friend_box, viewGroup, false);
            return new FriendBoxHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendBoxHolder friendBoxHolder, int i) {
            FriendBox friendBox = mFriendBoxes.get(i);
            friendBoxHolder.bind(friendBox);
        }

        @Override
        public int getItemCount() {
            return mFriendBoxes.size();
        }
    }
}
