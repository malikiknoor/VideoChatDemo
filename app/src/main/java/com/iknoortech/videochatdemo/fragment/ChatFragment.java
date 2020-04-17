package com.iknoortech.videochatdemo.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.iknoortech.videochatdemo.adapter.ChatsAdapter;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.model.ChatList;
import io.fabric.sdk.android.services.common.SafeToast;

import static com.iknoortech.videochatdemo.helper.AppConstant.chatListTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.isConnectionAvailable;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatsAdapter adapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference mChatReference;
    private DatabaseReference mDatabaseReference;
    private TextView tv_noChat;
    private View view;
    private ProgressBar pb_chatList;
    private ArrayList<ChatList> chatLists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        initView();
        return view;
    }

    private void initView() {
        recyclerView = view.findViewById(R.id.rv_chats);
        tv_noChat = view.findViewById(R.id.tv_noChat);
        pb_chatList = view.findViewById(R.id.pb_chatList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        chatLists = new ArrayList<>();

        if (!isConnectionAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        mChatReference = FirebaseDatabase.getInstance().getReference(chatListTableName)
                .child(firebaseUser.getUid());
        Query query = mChatReference.orderByChild("time");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pb_chatList.setVisibility(View.GONE);
                chatLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    chatLists.add(chatList);
                }

                if (chatLists.size() != 0) {
                    tv_noChat.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Collections.reverse(chatLists);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new ChatsAdapter(getContext(), chatLists, mDatabaseReference);
                            recyclerView.setAdapter(adapter);
                        }
                    }, 100);
                } else {
                    tv_noChat.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
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
