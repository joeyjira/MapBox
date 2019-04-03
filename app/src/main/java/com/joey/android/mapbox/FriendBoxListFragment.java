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

        return view;
    }

    private class FriendBoxHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public FriendBoxHolder(View friendBoxView) {
            super(friendBoxView);
        }

        public void bind() {

        }

        @Override
        public void onClick(View v) {

        }
    }

    private class FriendBoxAdapter extends RecyclerView.Adapter<FriendBoxHolder> {
        private List<FriendBox> mFriendBoxes;

        public FriendBoxAdapter(List<FriendBox> friendBoxes) {
            mFriendBoxes = friendBoxes;
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

        }

        @Override
        public int getItemCount() {
            return mFriendBoxes.size();
        }
    }
}
