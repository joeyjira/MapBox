package com.joey.android.mapbox.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.joey.android.mapbox.database.FriendCursorWrapper;
import com.joey.android.mapbox.database.MapBoxBaseHelper;
import com.joey.android.mapbox.database.MapBoxDbSchema.FriendTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendList {
    private static FriendList sFriendList;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static FriendList get(Context context) {
        if (sFriendList == null) {
            sFriendList = new FriendList(context);
        }

        return sFriendList;
    }

    private FriendList(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new MapBoxBaseHelper(mContext)
                .getWritableDatabase();
    }

    public void addFriend(Friend friend) {
        ContentValues values = getContentValues(friend);

        mDatabase.insert(FriendTable.NAME, null, values);
    }

    public void updateFriend(Friend friend) {
        String uuidString = friend.getId().toString();
        ContentValues values = getContentValues(friend);

        mDatabase.update(FriendTable.NAME, values,
                FriendTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private FriendCursorWrapper queryFriends(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                FriendTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new FriendCursorWrapper(cursor);
    }

    public List<Friend> getFriends() {
        List<Friend> friends = new ArrayList<>();

        FriendCursorWrapper cursor = queryFriends(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                friends.add(cursor.getFriend());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return friends;
    }

    public Friend getFriend(UUID id) {
        FriendCursorWrapper cursor = queryFriends(
                FriendTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getFriend();
        } finally {
            cursor.close();
        }
    }

    private static ContentValues getContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(FriendTable.Cols.UUID, friend.getId().toString());
        values.put(FriendTable.Cols.FIRSTNAME, friend.getFirstName());
        values.put(FriendTable.Cols.LASTNAME, friend.getLastName());

        return values;
    }
}
