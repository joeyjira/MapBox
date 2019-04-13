package com.joey.android.mapbox.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.joey.android.mapbox.model.User;

public class FriendCursorWrapper extends CursorWrapper {
    private static final String TAG = "FriendCursorWrapper";

    public FriendCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public User getFriend() {
        return new User("askdlfjlsaf", "joey jirasev", "asdf@gmail.com");
    }
}
