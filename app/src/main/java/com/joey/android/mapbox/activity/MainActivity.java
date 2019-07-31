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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.fragment.UserSearchFragment;
import com.joey.android.mapbox.fragment.UserListFragment;
import com.joey.android.mapbox.fragment.UserProfileFragment;
import com.joey.android.mapbox.fragment.UserRequestFragment;

public class MainActivity extends AppCompatActivity
        implements UserProfileFragment.GoogleSignOut {
    private static final String TAG = "MainActivity";

    private static final String EXTRA_AUTHENTICATED_USER = "authenticatedUser";

    private static final int REQUEST_ERROR = 0;


    private TextView mTextMessage;
    private GoogleApiClient mClient;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    private FragmentManager mFragmentManager;
    private Fragment mUserListFragment;
    private Fragment mUserRequestFragment;
    private Fragment mUserSearchFragment;
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
            ActionBar actionBar = getSupportActionBar();

            switch (item.getItemId()) {
                case R.id.navigation_friend_box:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserListFragment)
                            .commit();
                    actionBar.setTitle(R.string.friends);
                    mActiveFragment = mUserListFragment;
                    return true;
                case R.id.navigation_search:
                    Fragment userSearchFragment = UserSearchFragment.newInstance();
                    mFragmentManager.beginTransaction()
                            .remove(mUserSearchFragment)
                            .add(R.id.main_container, userSearchFragment)
                            .hide(mActiveFragment)
                            .show(userSearchFragment)
                            .commit();
                    actionBar.setTitle(R.string.search);
                    mUserSearchFragment = userSearchFragment;
                    mActiveFragment = mUserSearchFragment;
                    return true;
                case R.id.navigation_friend_request:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserRequestFragment)
                            .commit();
                    actionBar.setTitle(R.string.requests);

                    mActiveFragment = mUserRequestFragment;
                    return true;
                case R.id.navigation_settings:
                    mFragmentManager.beginTransaction()
                            .hide(mActiveFragment)
                            .show(mUserProfileFragment)
                            .commit();
                    actionBar.setTitle(R.string.settings);
                    mActiveFragment = mUserProfileFragment;
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MapBoxTheme);
        setContentView(R.layout.activity_main);

        mUser = getIntent().getParcelableExtra(EXTRA_AUTHENTICATED_USER);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.firebase_token))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Create required fragments
        mUserListFragment = UserListFragment.newInstance();
        mUserRequestFragment = UserRequestFragment.newInstance();
        mUserSearchFragment = UserSearchFragment.newInstance();
        mUserProfileFragment = UserProfileFragment.newInstance();
        mActiveFragment = mUserListFragment;

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportActionBar().setTitle(R.string.friends);

        mFragmentManager = getSupportFragmentManager();

        mFragmentManager.beginTransaction()
                .add(R.id.main_container, mUserListFragment)
                .add(R.id.main_container, mUserSearchFragment)
                .add(R.id.main_container, mUserRequestFragment)
                .add(R.id.main_container, mUserProfileFragment)
                .hide(mUserSearchFragment)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void googleSignOut() {
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
}
