package com.iknoortech.videochatdemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.BlockAdapter;
import com.iknoortech.videochatdemo.listner.BlockClickListner;
import com.iknoortech.videochatdemo.model.BlockedUserList;

import static com.iknoortech.videochatdemo.helper.AppConstant.blockTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.getMyPrettyDate;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class BlockedUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar pb;
    private TextView tv_noBlocked;
    private FirebaseUser firebaseUser;
    private DatabaseReference mBlockReference;
    private DatabaseReference mUserReference;
    private ArrayList<BlockedUserList> blockUserList;
    private BlockAdapter blockAdapter;

    private BlockClickListner clickListner = new BlockClickListner() {
        @Override
        public void onClick(String id) {
            unblockUser(id);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_user);

        toolbar = findViewById(R.id.toolbar_block);
        recyclerView = findViewById(R.id.rv_block);
        pb = findViewById(R.id.progressBar2);
        tv_noBlocked = findViewById(R.id.textView23);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        blockUserList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mBlockReference = FirebaseDatabase.getInstance().getReference(blockTableName);
        mUserReference = FirebaseDatabase.getInstance().getReference();
        blockAdapter = new BlockAdapter(this, blockUserList, clickListner, mUserReference);
        getBlockedUserList();

    }

    private void getBlockedUserList() {
        mBlockReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blockUserList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BlockedUserList blockedUserList = snapshot.getValue(BlockedUserList.class);
                    if(blockedUserList.getBlock_type().equals("me")){
                        blockUserList.add(blockedUserList);
                    }
                }
                pb.setVisibility(View.GONE);
                if(blockUserList.size() == 0){
                    tv_noBlocked.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else{
                    tv_noBlocked.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(blockAdapter);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void unblockUser(String userId) {
        Map hashMap = new HashMap();
        hashMap.put(firebaseUser.getUid() + "/" + userId + "/block_type", null);
        hashMap.put(firebaseUser.getUid() + "/" + userId + "/key", null);
        hashMap.put(userId + "/" + firebaseUser.getUid() + "/block_type", null);
        hashMap.put(userId + "/" + firebaseUser.getUid() + "/key", null);

        mBlockReference.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if (databaseError == null) {
                    Toast.makeText(BlockedUserActivity.this, "You have successfully unblocked this user", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BlockedUserActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

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
