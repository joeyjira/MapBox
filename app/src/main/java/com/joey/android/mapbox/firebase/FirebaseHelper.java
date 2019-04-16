package com.joey.android.mapbox.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    // Class that uses this method must implement FirebaseHelper.Callbacks
    public void getUidFromEmail(String email, final Callbacks callbacks) {
        DatabaseReference emailRef = mReference.child("emails").child(encodeEmail(email));

        emailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uid;
                if (dataSnapshot.exists()) {
                    uid = dataSnapshot.getValue().toString();
                } else {
                    uid = null;
                }

                callbacks.onReceiveUid(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // Class that uses this method must implement FirebaseHelper.Callbacks
    public void getUserFromUid(String uid, final Callbacks callback) {
        DatabaseReference userRef = mReference.child("users").child(uid).child("name");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name;
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.getValue().toString();
                } else {
                    name = null;
                }

                callback.onReceiveName(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addUserRequest(String uid) {
        DatabaseReference friendRef = mReference.child("friends")
                .child(uid).child(mUser.getUid());
        final DatabaseReference friendReq = mReference.child("friendRequests")
                .child(uid).child(mUser.getUid());

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    friendReq.setValue(false);
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void isFriend(String uid) {
        DatabaseReference friendRef = mReference.child("friends").child(uid).child(mUser.getUid());

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isFriend = dataSnapshot.exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    private String decodeEmail(String email) {
        return email.replace(",", ".");
    }

    public interface Callbacks {
        void onReceiveUid(String uid);

        void onReceiveName(String name);
    }
}
