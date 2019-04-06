package com.joey.android.mapbox.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.joey.android.mapbox.database.MapBoxDbSchema.FriendTable;
import com.joey.android.mapbox.model.Friend;

import java.util.UUID;

public class FriendCursorWrapper extends CursorWrapper {
    public FriendCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Friend getFriend() {
        String uuidString = getString(getColumnIndex(FriendTable.Cols.UUID));
        String firstName = getString(getColumnIndex(FriendTable.Cols.FIRSTNAME));
        String lastName = getString(getColumnIndex(FriendTable.Cols.LASTNAME));

        Friend friend = new Friend(UUID.fromString(uuidString),
                firstName, lastName);

        return friend;
    }
}
