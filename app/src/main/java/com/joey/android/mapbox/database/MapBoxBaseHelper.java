package com.joey.android.mapbox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.joey.android.mapbox.database.MapBoxDbSchema.FriendTable;

public class MapBoxBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MapBoxBaseHelper";
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "mapBox.db";

    public MapBoxBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FriendTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                FriendTable.Cols.UUID + ", " +
                FriendTable.Cols.FIRSTNAME + ", " +
                FriendTable.Cols.LASTNAME + ", " +
                FriendTable.Cols.LONGITUDE + ", " +
                FriendTable.Cols.LATITUDE +
                ")"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
