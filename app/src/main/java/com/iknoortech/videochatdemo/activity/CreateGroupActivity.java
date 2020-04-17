package com.iknoortech.videochatdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.AddGroupAllFriendsAdapter;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import com.iknoortech.videochatdemo.model.ParticipantList;

import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class CreateGroupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tv_selectedCount, tv_noFriend, tv_next;
    private RecyclerView rv_allFriends;
    private AddGroupAllFriendsAdapter allFriendsAdapter;
    private DatabaseReference mUserReferance;
    private DatabaseReference mFriendsReference;
    private FirebaseUser mFirebaseUser;
    private ArrayList<ParticipantList> friendsList;

    private FriendClickListner clickListner = new FriendClickListner() {
        @Override
        public void onClick(String type, String id) {
            int selectedCount = 0;
            int unselectedCount = 0;
            for (int i = 0; i < friendsList.size() ; i++) {
                if(friendsList.get(i).isSelected()){
                    selectedCount++;
                } else {
                    unselectedCount++;
                }
            }

            if((unselectedCount - selectedCount) == friendsList.size()){
                tv_selectedCount.setText("Add participants");
                tv_next.setVisibility(View.GONE);
            }else{
                tv_selectedCount.setText(selectedCount+" of "+friendsList.size());
                tv_next.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        toolbar = findViewById(R.id.toolbar_createGroup);
        tv_selectedCount = findViewById(R.id.tv_toolbarFriendsCount);
        rv_allFriends = findViewById(R.id.rv_groupAllFriends);
        tv_noFriend = findViewById(R.id.textView9);
        tv_next = findViewById(R.id.tv_nextCreateGroup);
        progressBar = findViewById(R.id.progressBar3);
        rv_allFriends.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        friendsList = new ArrayList<>();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserReferance = FirebaseDatabase.getInstance().getReference();
        mFriendsReference = FirebaseDatabase.getInstance().getReference(userFriendListTableName)
                .child(mFirebaseUser.getUid());

        tv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < friendsList.size(); ) {
                    if(!friendsList.get(i).isSelected()){
                        friendsList.remove(i);
                    }else{
                        i++;
                    }
                }
                Intent intent = new Intent(CreateGroupActivity.this, CreateGroupSecoundActivity.class);
                intent.putExtra("participantList", friendsList);
                startActivity(intent);
                finish();
            }
        });

        getFriendsList();
    }

    private void getFriendsList() {
        allFriendsAdapter = new AddGroupAllFriendsAdapter(CreateGroupActivity.this, friendsList, mUserReferance, clickListner);
        mFriendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();
                progressBar.setVisibility(View.GONE);
                if(dataSnapshot.getValue() != null){
                    tv_noFriend.setVisibility(View.GONE);
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        ParticipantList participantList = snapshot.getValue(ParticipantList.class);
                        friendsList.add(participantList);
                    }
                    rv_allFriends.setAdapter(allFriendsAdapter);
                }else{
                    tv_noFriend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        userStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        userStatus(String.valueOf(System.currentTimeMillis()));
        super.onPause();
    }

}
