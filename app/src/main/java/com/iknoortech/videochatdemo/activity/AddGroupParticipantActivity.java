
package com.iknoortech.videochatdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.AddParticipantAdapter;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import com.iknoortech.videochatdemo.model.ItemSelectedInGroup;

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
import java.util.HashMap;

import static com.iknoortech.videochatdemo.helper.AppConstant.chatListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupMemberTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;

public class AddGroupParticipantActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tv_selectedCount, tv_next, tv_noFriend;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ArrayList<ItemSelectedInGroup> friendIdList;
    private ArrayList<String> alreadyInGroup;
    private DatabaseReference mReference;
    private AddParticipantAdapter participantAdapter;
    private FirebaseUser firebaseUser;
    private ArrayList<String> selectedFriends = new ArrayList<>();
    private String groupId = "", groupName = "";

    private FriendClickListner friendClickListner = new FriendClickListner() {
        @Override
        public void onClick(String type, String id) {
            if(selectedFriends.contains(id)){
                int pos = selectedFriends.indexOf(id);
                selectedFriends.remove(pos);
            }else{
                selectedFriends.add(id);
            }
            int selectedCount = selectedFriends.size();
            tv_selectedCount.setText(selectedCount + " of " + friendIdList.size());
            if(selectedCount == 0 ){
                tv_next.setVisibility(View.GONE);
            }else{
             tv_next.setVisibility(View.VISIBLE);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_participant);

        recyclerView = findViewById(R.id.rv_addParticipant);
        tv_selectedCount = findViewById(R.id.tv_toolbarFriendsCountAdd);
        tv_next = findViewById(R.id.tv_nextCreateGroupAdd);
        tv_noFriend = findViewById(R.id.textView47);
        toolbar = findViewById(R.id.toolbar_addParticipant);
        progressBar = findViewById(R.id.progressBar4);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        alreadyInGroup = (ArrayList<String>) getIntent().getSerializableExtra("alreadyInGroup");
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        friendIdList = new ArrayList<>();
        mReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getFriendsList();

        tv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               addMameberInGroup();
            }
        });
    }

    private void addMameberInGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupMemberTable)
                .child(groupId);
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(chatListTableName);

        for (int i = 0; i < selectedFriends.size(); i++) {
            try {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("memberId", selectedFriends.get(i));
                hashMap.put("admin", false);
                hashMap.put("joinDate", String.valueOf(System.currentTimeMillis()));
                reference.child(selectedFriends.get(i)).updateChildren(hashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int finalI = i;
            userRef.child(selectedFriends.get(i)).child(groupId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userRef.child(selectedFriends.get(finalI)).child(groupId).child("id").setValue(groupId);
                            userRef.child(selectedFriends.get(finalI)).child(groupId).child("isGroup").setValue("true");
                            userRef.child(selectedFriends.get(finalI)).child(groupId).child("time").setValue(String.valueOf(System.currentTimeMillis()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            sendNotification("Group", firebaseUser.getUid(), selectedFriends.get(i),
                    groupName, "added to you in a group");
            if (i == (selectedFriends.size() - 1)) {
                finish();
                break;
            }
        }
    }

    private void getFriendsList() {
        mReference.child(userFriendListTableName).child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressBar.setVisibility(View.GONE);
                        friendIdList.clear();
                        if (dataSnapshot.getChildren() != null) {
                            recyclerView.setVisibility(View.VISIBLE);
                            tv_noFriend.setVisibility(View.GONE);
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ItemSelectedInGroup selectedInGroup = new ItemSelectedInGroup();
                                selectedInGroup.setId(snapshot.child("friend_id").getValue().toString());
                                selectedInGroup.setSelected(false);
                                friendIdList.add(selectedInGroup);
                            }
                            participantAdapter = new AddParticipantAdapter(AddGroupParticipantActivity.this, mReference,
                                    alreadyInGroup, friendIdList, friendClickListner);
                            recyclerView.setAdapter(participantAdapter);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tv_noFriend.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
