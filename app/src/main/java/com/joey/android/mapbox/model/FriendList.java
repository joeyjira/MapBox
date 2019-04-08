package com.joey.android.mapbox.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.joey.android.mapbox.database.FriendCursorWrapper;
import com.joey.android.mapbox.database.MapBoxBaseHelper;
import com.joey.android.mapbox.database.MapBoxDbSchema.FriendTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendList {
    private static final String TAG = "FriendList";

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
        Log.i(TAG, "FriendName:" + friend.getFirstName());
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
        Log.i(TAG, "Cursor checked 1");
        try {
            cursor.moveToFirst();
            Log.i(TAG, "Cursor checked 2");
            while (!cursor.isAfterLast()) {
                friends.add(cursor.getFriend());
                cursor.moveToNext();
                Log.i(TAG, "Cursor checked 3");
            }
        } finally {
            cursor.close();
        }

        Log.i(TAG, "Friends:" + friends);
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

    public File getPhotoFile(Friend friend) {
        String folderName = "images";
        String folder = mContext.getFilesDir().getAbsolutePath() +
                File.separator + folderName;

        File subFolder = new File(folder);

        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }

        File filesDir = new File(subFolder, friend.getPhotoFilename());

        return filesDir;
    }

    private static ContentValues getContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(FriendTable.Cols.UUID, friend.getId().toString());
        values.put(FriendTable.Cols.FIRSTNAME, friend.getFirstName());
        values.put(FriendTable.Cols.LASTNAME, friend.getLastName());

        return values;
    }
}
