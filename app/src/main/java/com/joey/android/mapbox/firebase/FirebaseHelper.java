package com.joey.android.mapbox.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joey.android.mapbox.model.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    private static FirebaseHelper sFirebaseHelper;

    private FirebaseUser mUser;
    private DatabaseReference mReference;
    private Callbacks mCallback;

    public static FirebaseHelper get() {
        if (sFirebaseHelper == null) {
            sFirebaseHelper = new FirebaseHelper();
        }

        return sFirebaseHelper;
    }

    private FirebaseHelper() {
        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void saveUser(FirebaseUser user, boolean isNewUser) {
        if (isNewUser) {
            String userUid = user.getUid();
            DatabaseReference usersReference = mReference.child("users").child(userUid);
            usersReference.child("name").setValue(user.getDisplayName());
            usersReference.child("photoUri").setValue(user.getPhotoUrl().toString());
            usersReference.child("email").setValue(user.getEmail());

            String email = encodeEmail(user.getEmail());
            DatabaseReference emailsReference = mReference.child("emails").child(email);
            emailsReference.setValue(user.getUid());
        }
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    public interface Callbacks {
        void onReceiveUid(String uid);

        void onReceiveName(String name);

        void onReceiveUsers(List<User> users);
    }
}
