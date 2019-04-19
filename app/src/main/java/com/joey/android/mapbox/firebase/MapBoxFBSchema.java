package com.joey.android.mapbox.firebase;

public class MapBoxFBSchema {
    public static final String USERS = "users";
    public static final String EMAILS = "emails";
    public static final String FRIENDREQUESTS = "friendRequests";
    public static final String FRIENDS = "friends";

    public static final class Reference {
        public static String USERS = "users";
        public static String EMAILS = "emails";
        public static String FRIEND_REQUESTS = "friendRequests";
        public static String FRIENDS = "friends";
    }

    public static final class UsersChild {
        public static String UID = "uid";
        public static String NAME = "name";
        public static String EMAIL = "email";
        public static String PHOTO_URI = "photoUri";
    }
}
