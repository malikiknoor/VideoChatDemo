package com.iknoortech.videochatdemo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.SuggestionAdapter;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.listner.BlockClickListner;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import com.iknoortech.videochatdemo.model.BlockedUserList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.videochatdemo.helper.AppConstant.blockTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.friendRequestTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileAboutTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.reportTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userTable;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.getUserName;
import static com.iknoortech.videochatdemo.helper.AppUtils.seeFullImage;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;

public class AllUserActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView rvUser;
    private SuggestionAdapter suggestionAdapter;
    private ArrayList<String> friendIdList;
    private ArrayList<String> friendRequestIdList;
    private ArrayList<String> suggestionIdList;
    private ArrayList<String> blockIdList;
    private ArrayList<BlockedUserList> blockUserList;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabaseReference;
    private Dialog profileDialog;
    private String dialogUserImage;

    private FriendClickListner friendClickListner = new FriendClickListner() {
        @Override
        public void onClick(String type, String id) {
            openDialog(type, id);
        }
    };

    private BlockClickListner sendRequestClickListner = new BlockClickListner() {
        @Override
        public void onClick(String id) {
            sendFriendRequest(firebaseUser.getUid(), id);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        edtSearch = findViewById(R.id.editText10);
        rvUser = findViewById(R.id.rv_allUser);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendIdList = new ArrayList<>();
        suggestionIdList = new ArrayList<>();
        blockUserList = new ArrayList<>();
        blockIdList = new ArrayList<>();
        friendRequestIdList = new ArrayList<>();

        profileDialog = new Dialog(this);
        rvUser.setLayoutManager(new GridLayoutManager(this, 2));

        if (!AppUtils.isConnectionAvailable(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        suggestionAdapter = new SuggestionAdapter(this, suggestionIdList, mDatabaseReference, friendClickListner, sendRequestClickListner, 1);

        getFriendList();

    }

    private void getFriendList() {
        mDatabaseReference.child(userFriendListTableName).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendIdList.clear();
//                pb_friend.setVisibility(View.GONE);
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friendIdList.add(snapshot.getKey());
                    }

                }
                getFriendRequestList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFriendRequestList() {
        mDatabaseReference.child(friendRequestTableName).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendRequestIdList.clear();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friendRequestIdList.add(snapshot.getKey());
                    }
                    getBlockedUserList();
                } else {
                    getBlockedUserList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUsers() {
        mDatabaseReference.child(userTable).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                suggestionIdList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!friendIdList.contains(snapshot.getKey())) {
                        if (!snapshot.getKey().equalsIgnoreCase(firebaseUser.getUid())) {
                            if (!blockIdList.contains(snapshot.getKey())) {
                                if (!friendRequestIdList.contains(snapshot.getKey())) {
                                    suggestionIdList.add(snapshot.getKey());
                                }
                            }
                        }
                    }
                }
                rvUser.setAdapter(suggestionAdapter);
                suggestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getBlockedUserList() {
        mDatabaseReference.child(blockTableName).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blockUserList.clear();
                blockIdList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BlockedUserList blockedUserList = snapshot.getValue(BlockedUserList.class);
                    blockUserList.add(blockedUserList);
                    blockIdList.add(blockedUserList.getKey());
                }
                getAllUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openDialog(String type, final String id) {
        profileDialog.setCanceledOnTouchOutside(false);
        profileDialog.setContentView(R.layout.profile_dialog);
        profileDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileDialog.show();
        final String currentUserId = firebaseUser.getUid();

        final CircleImageView img_userDialog = profileDialog.findViewById(R.id.img_userDialog);
        final TextView dialog_userName = profileDialog.findViewById(R.id.textView18);
        final TextView dialog_userStatus = profileDialog.findViewById(R.id.textView22);
        final TextView tv_block = profileDialog.findViewById(R.id.tv_block);
        final TextView tv_report = profileDialog.findViewById(R.id.tv_report);
        final TextView dialog_userPhone = profileDialog.findViewById(R.id.textView24);
        final TextView dialog_userReport = profileDialog.findViewById(R.id.textView25);
        final TextView dialog_userFriends = profileDialog.findViewById(R.id.textView26);
        final ImageView phoneImage = profileDialog.findViewById(R.id.imageView6);
        Button btn_add = profileDialog.findViewById(R.id.button8);
        Button btn_message = profileDialog.findViewById(R.id.button7);
        Button btn_unfriend = profileDialog.findViewById(R.id.button6);

        if (type.equalsIgnoreCase("friend")) {
            btn_message.setVisibility(View.VISIBLE);
            btn_unfriend.setVisibility(View.VISIBLE);
            dialog_userPhone.setVisibility(View.VISIBLE);
            phoneImage.setVisibility(View.VISIBLE);
            btn_add.setVisibility(View.GONE);
        } else {
            btn_message.setVisibility(View.GONE);
            btn_unfriend.setVisibility(View.GONE);
            dialog_userPhone.setVisibility(View.GONE);
            phoneImage.setVisibility(View.GONE);
            btn_add.setVisibility(View.VISIBLE);
        }

        img_userDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeFullImage(AllUserActivity.this, img_userDialog, dialogUserImage, null);
            }
        });

        mDatabaseReference.child(reportTableName).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> reportList = new ArrayList<>();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        reportList.add(snapshot.getKey());
                    }

                    if (reportList.size() == 0) {
                        dialog_userReport.setVisibility(View.GONE);
                    } else {
                        dialog_userReport.setVisibility(View.VISIBLE);
                        dialog_userReport.setText(reportList.size() + " person reported as spam");
                    }
                } else {
                    dialog_userReport.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(userFriendListTableName).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ArrayList<String> idList = new ArrayList<>();
                    int mutualCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        idList.add(snapshot.getKey());
                    }

                    if (idList.size() == 0) {
                        dialog_userFriends.setText("No friend | No Mutual Friend");
                    } else {
                        for (int i = 0; i < idList.size(); i++) {
                            if (friendIdList.contains(idList.get(i))) {
                                mutualCount++;
                            }
                        }

                        if (mutualCount == 0) {
                            dialog_userFriends.setText(+idList.size() + " friend | No Mutual friend");
                        } else {

                            dialog_userFriends.setText(+idList.size() + " friend | " + mutualCount + " Mutual friend");

                        }
                    }
                } else {
                    dialog_userFriends.setText("No friend | No Mutual Friend");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFriendRequest(currentUserId, id);
            }
        });

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileDialog.dismiss();
                Intent intent = new Intent(AllUserActivity.this, UserMessageActivity.class);
                intent.putExtra("userid", id);
                startActivity(intent);
            }
        });

        btn_unfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromFriend("unfriend", id);
            }
        });

        mDatabaseReference.child(profileNameTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog_userName.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(profileAboutTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog_userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(profileImageTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Glide.with(AllUserActivity.this).load(dataSnapshot.child("imageUrl").getValue().toString())
                        .into(img_userDialog);

                dialogUserImage = dataSnapshot.child("imageUrl").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(userTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog_userPhone.setText(dataSnapshot.child("mobile").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tv_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockUser(currentUserId, id);
            }
        });

        tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportUser(currentUserId, id);
            }
        });
    }

    private void sendFriendRequest(String currentUserID, final String id) {
        Map requestMap = new HashMap();
        requestMap.put(currentUserID + "/" + id + "/request_type", "sent");
        requestMap.put(currentUserID + "/" + id + "/send_time", String.valueOf(System.currentTimeMillis()));
        requestMap.put(id + "/" + currentUserID + "/request_type", "received");
        requestMap.put(id + "/" + currentUserID + "/send_time", String.valueOf(System.currentTimeMillis()));

        mDatabaseReference.child(friendRequestTableName).updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(AllUserActivity.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                profileDialog.dismiss();
                sendNotification("Friend Request", firebaseUser.getUid(), id,
                        getUserName(AllUserActivity.this), "Sent you friend request");
            }
        });
    }

    private void reportUser(String currentUserID, final String id) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", currentUserID);
        mDatabaseReference.child(reportTableName).child(id).child(currentUserID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    removeFromFriend("report", id);
                } else {
                    Toast.makeText(AllUserActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void blockUser(final String currentUserID, final String id) {
        Map hashMap = new HashMap();
        hashMap.put(currentUserID + "/" + id + "/block_type", "me");
        hashMap.put(currentUserID + "/" + id + "/key", id);
        hashMap.put(id + "/" + currentUserID + "/block_type", "other");
        hashMap.put(id + "/" + currentUserID + "/key", currentUserID);

        mDatabaseReference.child(blockTableName).updateChildren(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if (databaseError == null) {
                    removeFromFriend("block", id);
                } else {
                    Toast.makeText(AllUserActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void removeFromFriend(final String type, String id) {

        Map hashMap = new HashMap();
        hashMap.put(firebaseUser.getUid() + "/" + id, null);
        hashMap.put(id + "/" + firebaseUser.getUid(), null);

        mDatabaseReference.child(userFriendListTableName).updateChildren(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if (databaseError == null) {
                    profileDialog.dismiss();
                    suggestionAdapter.notifyDataSetChanged();
                    if (type.equalsIgnoreCase("block")) {
                        Toast.makeText(AllUserActivity.this, "Blocked Successfully", Toast.LENGTH_SHORT).show();
                    } else if (type.equals("report")) {
                        Toast.makeText(AllUserActivity.this, "Report as spam successfully and removed from friends", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AllUserActivity.this, "User removed from friends", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AllUserActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}
