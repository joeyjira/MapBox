package com.joey.android.mapbox.fragment;

import android.Manifest;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.joey.android.mapbox.activity.AddFriendActivity;
import com.joey.android.mapbox.model.Friend;
import com.joey.android.mapbox.model.FriendList;
import com.joey.android.mapbox.R;

import java.util.List;

public class FriendListFragment extends Fragment {
    private static final String TAG = "FriendListFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_IMAGE_CODE = 1;

    private RecyclerView mFriendListRecyclerView;

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

    public static FriendListFragment newInstance() {
        return new FriendListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_box_list, container, false);

        mFriendListRecyclerView = view.findViewById(R.id.map_box_recycler_view);
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_friend_list, menu);

        MenuItem addFriendItem = menu.findItem(R.id.add_friend);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_friend:
                Intent intent = AddFriendActivity.newIntent(getActivity());
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "Fragment has been started");
    }

    @Override
    public void onResume() {
        super.onResume();

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

    private void setupAdapter() {
        if (isAdded()) {
            List<Friend> friends = FriendList.get(getActivity()).getFriends();
            mFriendListRecyclerView.setAdapter(new FriendListAdapter(friends));
        }
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

    private class FriendHolder extends RecyclerView.ViewHolder
            implements OnMapReadyCallback {
        private Friend mFriend;
        private GoogleMap mGoogleMap;

        private TextView mFriendNameTextView;
        private Button mLocationButton;
        private MapView mMapView;
        private ImageView mFriendImageView;

        public FriendHolder(View friendBoxView) {
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

        public void bind(Friend friend) {
            mFriend = friend;
            mFriendNameTextView.setText(friend.getName());

            GoogleMap map = mGoogleMap;
            if (map != null) {

            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.i(TAG, "" + FriendListFragment.this.getActivity());
            MapsInitializer.initialize(FriendListFragment.this.getActivity());
            mGoogleMap = googleMap;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.448849,-121.898045), 13f));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(37.448849,-121.898045)));
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void setMapLocation() {

        }
    }

    private class FriendListAdapter extends RecyclerView.Adapter<FriendHolder> {
        private List<Friend> mFriends;

        public FriendListAdapter(List<Friend> friends) {
            mFriends = friends;
            mFriends.add(new Friend("Anne", "Nguyen"));
            mFriends.add(new Friend("Nixon", "Yiu"));
            mFriends.add(new Friend("Kevin", "Lam"));
            mFriends.add(new Friend("James", "Huang"));
            mFriends.add(new Friend("Michael", "Lau"));
            mFriends.add(new Friend("Joey", "Jirasevijinda"));
        }

        @NonNull
        @Override
        public FriendHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_friend_box, viewGroup, false);
            return new FriendHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendHolder friendHolder, int i) {
            Friend friend = mFriends.get(i);
            friendHolder.bind(friend);
        }

        @Override
        public int getItemCount() {
            return mFriends.size();
        }
    }
}
