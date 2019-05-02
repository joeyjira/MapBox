package com.joey.android.mapbox.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.joey.android.mapbox.fragment.InboxFragment;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.fragment.UserListFragment;
import com.joey.android.mapbox.fragment.UserProfileFragment;
import com.joey.android.mapbox.fragment.UserRequestFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String EXTRA_AUTHENTICATED_USER = "authenticatedUser";

    private static final int REQUEST_ERROR = 0;


    private TextView mTextMessage;
    private GoogleApiClient mClient;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private FragmentManager mFragmentManager;
    private Fragment mUserListFragment;
    private Fragment mUserRequestFragment;
    private Fragment mUserProfileFragment;
    private Fragment mActiveFragment;

    public static Intent newIntent(Context packageContext, FirebaseUser user) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        intent.putExtra(EXTRA_AUTHENTICATED_USER, user);
        return intent;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friend_box:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserListFragment)
                            .commit();
                    mActiveFragment = mUserListFragment;
                    return true;
                case R.id.navigation_friend_request:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserRequestFragment)
                            .commit();
                    mActiveFragment = mUserRequestFragment;
                    return true;
                case R.id.navigation_settings:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserProfileFragment)
                            .commit();
                    mActiveFragment = mUserProfileFragment;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        mUser = getIntent().getParcelableExtra(EXTRA_AUTHENTICATED_USER);
        mUserListFragment = UserListFragment.newInstance();
        mUserRequestFragment = UserRequestFragment.newInstance();
        mUserProfileFragment = UserProfileFragment.newInstance();
        mActiveFragment = mUserListFragment;

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFragmentManager = getSupportFragmentManager();

//        if (fragment == null) {
//            fragment = UserListFragment.newInstance();
//            fm.beginTransaction()
//                    .add(R.id.main_container, fragment)
//                    .commit();
//        }

        mFragmentManager.beginTransaction()
                .add(R.id.main_container, mActiveFragment)
                .add(R.id.main_container, mUserRequestFragment)
                .add(R.id.main_container, mUserProfileFragment)
                .hide(mUserRequestFragment)
                .hide(mUserProfileFragment)
                .commit();

        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_friend_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_friend:
                Intent intent = AddFriendActivity.newIntent(this);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability
                    .getErrorDialog(this, errorCode, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // Leave if services are unavailable.
                                    finish();
                                }
                            });

            errorDialog.show();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }
}
