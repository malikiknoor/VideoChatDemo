package com.iknoortech.videochatdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.helper.AppUtils;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.iknoortech.videochatdemo.helper.AppConstant.groupImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupMemberTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;

public class GroupMessageActvity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView groupName, groupStatus;
    private ImageView groupInfo;
    private String groupId, groupImageLink;
    private CircleImageView img_toolbarGroup;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message_actvity);

        toolbar = findViewById(R.id.toolbarGroupMessage);
        groupName = findViewById(R.id.tv_groupName);
        groupStatus = findViewById(R.id.tv_groupStatus);
        groupInfo = findViewById(R.id.img_groupInfo);
        img_toolbarGroup = findViewById(R.id.img_toolbarGroup);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        groupId = getIntent().getStringExtra("groupId");
        groupName.setText(getIntent().getStringExtra("groupName"));

        groupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupMessageActvity.this, GroupProfile.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        img_toolbarGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.seeFullImage(GroupMessageActvity.this, img_toolbarGroup, groupImageLink, null);
            }
        });

        getGroupInfo();

    }

    private void getGroupInfo() {
        mDatabaseReference.child(groupMemberTable).child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    mDatabaseReference.child(profileNameTable).child(snapshot.getKey())
                            .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(groupStatus.getText().toString().isEmpty()){
                                groupStatus.setText(dataSnapshot.child("userName").getValue().toString());
                            }else{
                                groupStatus.setText(groupStatus.getText()+", "+dataSnapshot.child("userName").getValue().toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(groupImageTable).child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Glide.with(GroupMessageActvity.this).load(dataSnapshot.child("groupImageUrl")
                        .getValue()).into(img_toolbarGroup);
                groupImageLink = dataSnapshot.child("groupImageUrl").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
