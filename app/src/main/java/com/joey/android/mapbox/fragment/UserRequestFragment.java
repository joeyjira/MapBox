package com.joey.android.mapbox.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.joey.android.mapbox.R;
import com.joey.android.mapbox.activity.SignInActivity;
import com.joey.android.mapbox.model.User;

import java.util.List;

public class UserRequestFragment extends Fragment {
    private static final String TAG = "UserRequestFragment";

    private RecyclerView mUserRequestRecyclerView;

    public static UserRequestFragment newInstance() {
        return new UserRequestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mUserRequestRecyclerView = view.findViewById(R.id.recycler_view_map_box);
        mUserRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private View.OnClickListener signOutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = SignInActivity.newIntent(getActivity());
            startActivity(intent);
        }
    };

    private class UserRequestAdapter extends RecyclerView.Adapter<UserRequestHolder> {
        List<User> mUsers;

        public UserRequestAdapter(List<User> users) {

        }

        @NonNull
        @Override
        public UserRequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            return new UserRequestHolder();
        }

        @Override
        public void onBindViewHolder(@NonNull UserRequestHolder userRequestHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private class UserRequestHolder extends RecyclerView.ViewHolder {
        public UserRequestHolder(View view) {
            super(view);
        }
    }
}
