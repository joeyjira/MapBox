package com.joey.android.mapbox;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FriendBoxListFragment extends Fragment {
    private static final String TAG = "FriendBoxListFragment";

    private RecyclerView mFriendBoxRecyclerView;

    public static FriendBoxListFragment newInstance() {
        return new FriendBoxListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_box_list, container, false);

        mFriendBoxRecyclerView = view.findViewById(R.id.friend_box_recycler_view);
        mFriendBoxRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mFriendBoxRecyclerView.setAdapter(new FriendBoxAdapter());
        }
    }

    private class FriendBoxHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private FriendBox mFriendBox;

        private TextView mFriendNameTextView;

        public FriendBoxHolder(View friendBoxView) {
            super(friendBoxView);

            mFriendNameTextView = friendBoxView.findViewById(R.id.friend_name);
        }

        public void bind(FriendBox friendBox) {
            mFriendBox = friendBox;
            mFriendNameTextView.setText(friendBox.getName());
        }

        @Override
        public void onClick(View v) {

        }
    }

    private class FriendBoxAdapter extends RecyclerView.Adapter<FriendBoxHolder> {
        private List<FriendBox> mFriendBoxes;

        public FriendBoxAdapter() {
            mFriendBoxes = new ArrayList<>();
            mFriendBoxes.add(new FriendBox("Anne", "Nguyen"));
            mFriendBoxes.add(new FriendBox("Nixon", "Yiu"));
            mFriendBoxes.add(new FriendBox("Kevin", "Lam"));
            mFriendBoxes.add(new FriendBox("James", "Huang"));
            mFriendBoxes.add(new FriendBox("Michael", "Lau"));
            mFriendBoxes.add(new FriendBox("Joey", "Jirasevijinda"));
        }

        @NonNull
        @Override
        public FriendBoxHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_friend_box, viewGroup, false);
            return new FriendBoxHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendBoxHolder friendBoxHolder, int i) {
            FriendBox friendBox = mFriendBoxes.get(i);
            friendBoxHolder.bind(friendBox);
        }

        @Override
        public int getItemCount() {
            return mFriendBoxes.size();
        }
    }
}
