package com.joey.android.mapbox.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joey.android.mapbox.model.Mail;
import com.joey.android.mapbox.R;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {
    private static final String TAG = "MyBoxFragment";

    private RecyclerView mInboxRecyclerView;

    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        mInboxRecyclerView = view.findViewById(R.id.map_box_recycler_view);
        mInboxRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mInboxRecyclerView.setAdapter(new InboxAdapter());
        }
    }

    private class InboxHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Mail mMail;

        public InboxHolder(View myBoxView) {
            super(myBoxView);


        }

        public void bind(Mail mail) {
            mMail = mail;

        }

        @Override
        public void onClick(View v) {

        }
    }

    private class InboxAdapter extends RecyclerView.Adapter<InboxHolder> {
        private List<Mail> mMails;

        public InboxAdapter() {
            mMails = new ArrayList<>();

            mMails.add(new Mail("Anne Nguyen", "Wake me up!"));
            mMails.add(new Mail("Anne Nguyen", "Wake me up!"));
            mMails.add(new Mail("Anne Nguyen", "Wake me up!"));
            mMails.add(new Mail("Anne Nguyen", "Wake me up!"));
            mMails.add(new Mail("Anne Nguyen", "Wake me up!"));
        }

        @NonNull
        @Override
        public InboxHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_inbox, viewGroup, false);
            return new InboxHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InboxHolder inboxHolder, int i) {
            Mail mail = mMails.get(i);
            inboxHolder.bind(mail);
        }

        @Override
        public int getItemCount() {
            return mMails.size();
        }
    }
}
