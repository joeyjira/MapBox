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

import java.util.HashMap;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    private static FirebaseHelper sFirebaseHelper;

    private FirebaseUser mUser;
    private DatabaseReference mReference;

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

            String email = encodeEmail(user.getEmail());
            DatabaseReference emailsReference = mReference.child("emails").child(email);
            emailsReference.setValue(user.getUid());
        }
    }

    public void getUidFromEmail(String email, final Callbacks callbacks) {
        DatabaseReference emailRef = mReference.child("emails").child(encodeEmail(email));

        emailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getValue().toString();
                callbacks.updateUid(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getUserFromUid(String uid, final Callbacks callbacks) {
        DatabaseReference userRef = mReference.child("users").child(uid).child("name");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.getValue().toString();
                    callbacks.updateName(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addUserRequest(String uid) {
        mReference.child("friendRequests").child(uid).push().setValue(mUser.getUid());
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    private String decodeEmail(String email) {
        return email.replace(",", ".");
    }

    public interface Callbacks {
        abstract void updateUid(String uid);

        abstract void updateName(String name);
    }
}
