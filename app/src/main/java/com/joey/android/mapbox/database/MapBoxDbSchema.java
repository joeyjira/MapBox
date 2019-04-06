package com.joey.android.mapbox.database;

public class MapBoxDbSchema {
    public static final class FriendTable {
        public static final String NAME = "friends";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String FIRSTNAME = "firstName";
            public static final String LASTNAME = "lastName";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
        }
    }
}
