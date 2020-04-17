package com.iknoortech.videochatdemo.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.FriendRequestAdapter;
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.model.RequestList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tv_noReq;
    private DatabaseReference mFriendsDatabse;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId = "";
    private FriendRequestAdapter friendRequestAdapter;
    private View view;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friend_request, container, false);
        initView();
        return view;
    }

    private void initView() {
        recyclerView = view.findViewById(R.id.rv_friendReq);
        tv_noReq = view.findViewById(R.id.textView16);
        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();
        mFriendsDatabse = FirebaseDatabase.getInstance().getReference().child(AppConstant.friendRequestTableName)
                .child(mCurrentUserId);

        if(!AppUtils.isConnectionAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Query query = mFriendsDatabse.orderByChild("send_time");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                ArrayList<String> keyList = new ArrayList<>();
                ArrayList<RequestList> requestArrayList = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        keyList.add(snapshot.getKey());
                        RequestList requestList = snapshot.getValue(RequestList.class);
                        requestArrayList.add(requestList);
                    }
                    if (keyList.size() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        tv_noReq.setVisibility(View.GONE);
                        Collections.reverse(keyList);
                        Collections.reverse(requestArrayList);
                        friendRequestAdapter = new FriendRequestAdapter(getActivity(), mUserDatabase, keyList, requestArrayList, mCurrentUserId);
                        recyclerView.setAdapter(friendRequestAdapter);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tv_noReq.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    initView();
//                }
//            }, 20);
//        }
//    }

}
