package com.joey.android.mapbox.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;

import com.joey.android.mapbox.R;
import com.joey.android.mapbox.fragment.FriendProfileFragment;
import com.joey.android.mapbox.fragment.UserSearchFragment;
import com.joey.android.mapbox.model.User;

public class FriendProfileActivity extends SingleFragmentActivity {
    private static final String TAG = "FriendProfileActivity";

    private static final String EXTRA_USER = "com.joey.android.mapbox.activity.FriendProfileActivity.EXTRA_USER";
    private static final String EXTRA_IMAGE = "com.joey.android.mapbox.activity.FriendProfileActivity.EXTRA_IMAGE";

    public static Intent newIntent(Context packageContext, User user, Bitmap image) {
        Intent intent = new Intent(packageContext, FriendProfileActivity.class);
        intent.putExtra(EXTRA_USER, user);
        intent.putExtra(EXTRA_IMAGE, image);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();

        User user = intent.getParcelableExtra(EXTRA_USER);
        Bitmap image = intent.getParcelableExtra(EXTRA_IMAGE);

        getSupportActionBar().setTitle("");

        return FriendProfileFragment.newInstance(user, image);
    }
}
