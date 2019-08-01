package com.joey.android.mapbox.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.model.User;
import com.joey.android.mapbox.util.ProfileImageUtility;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendProfileFragment extends FirebaseFragment
        implements OnMapReadyCallback {
    private static final String TAG = "FriendProfileFragment";

    private static final String ARG_USER = "user_arg";
    private static final String ARG_PROFILE_IMAGE = "profile_image";

    private User mUser;
    private Bitmap mProfileImage;
    private GoogleMap mGoogleMap;

    private CircleImageView mProfileImageView;
    private TextView mProfileNameView;
    private MapView mMapView;

    public static FriendProfileFragment newInstance(User user, Bitmap image) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putParcelable(ARG_PROFILE_IMAGE, image);

        FriendProfileFragment fragment = new FriendProfileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = getArguments().getParcelable(ARG_USER);
        mProfileImage = getArguments().getParcelable(ARG_PROFILE_IMAGE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        mProfileImageView = view.findViewById(R.id.fragment_friend_profile_image);
        mMapView = view.findViewById(R.id.fragment_friend_profile_map);
        mProfileNameView = view.findViewById(R.id.fragment_friend_profile_name);

        mProfileImageView.setImageBitmap(mProfileImage);
        mProfileNameView.setText(mUser.getName());

        mMapView.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng userLatLng = mUser.getLatLng();
        Log.i(TAG, "LATLONG:" + userLatLng);
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
}
