package com.joey.android.mapbox.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;

import com.joey.android.mapbox.fragment.AddUserFragment;

public class AddFriendActivity extends SingleFragmentActivity {
    private static final String TAG = "AddFriendActivity";

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, AddFriendActivity.class);
        return intent;
    }
    @Override
    protected Fragment createFragment() {
        return AddUserFragment.newInstance();
    }

    @Override
    @LayoutRes
    protected int getLayoutResId() {
        return super.getLayoutResId();
    }
}
