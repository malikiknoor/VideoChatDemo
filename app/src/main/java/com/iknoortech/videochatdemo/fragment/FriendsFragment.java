package com.iknoortech.videochatdemo.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.activity.AllUserActivity;
import com.iknoortech.videochatdemo.activity.UserMessageActivity;
import com.iknoortech.videochatdemo.adapter.FriendsAdapter;
import com.iknoortech.videochatdemo.adapter.SuggestionAdapter;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.listner.BlockClickListner;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import com.iknoortech.videochatdemo.model.BlockedUserList;
import de.hdodenhof.circleimageview.CircleImageView;

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


public class FriendsFragment extends Fragment {

    private RecyclerView rv_friends, rv_suggestion;
    private LinearLayout ll_NoFriend;
    private TextView tv_suggestion, btn_find;
    private FriendsAdapter friendAdapter;
    private SuggestionAdapter suggestionAdapter;
    private ArrayList<String> friendIdList;
    private ArrayList<String> friendRequestIdList;
    private ArrayList<String> suggestionIdList;
    private ArrayList<String> blockIdList;
    private ArrayList<BlockedUserList> blockUserList;
    private View view;
    private ProgressBar pb_friend;
    private TextView tv_noFriend;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        initView();
        return view;
    }

    private void initView() {

        rv_friends = view.findViewById(R.id.rv_friends);
        rv_suggestion = view.findViewById(R.id.rv_suggestion);
        tv_noFriend = view.findViewById(R.id.textView17);
        pb_friend = view.findViewById(R.id.pb_friend);
        tv_suggestion = view.findViewById(R.id.textView21);
        btn_find = view.findViewById(R.id.btn_find);
        ll_NoFriend = view.findViewById(R.id.ll_NoFriend);
        rv_friends.setHasFixedSize(true);
        rv_friends.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_suggestion.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendIdList = new ArrayList<>();
        suggestionIdList = new ArrayList<>();
        blockUserList = new ArrayList<>();
        blockIdList = new ArrayList<>();
        friendRequestIdList = new ArrayList<>();

        profileDialog = new Dialog(getActivity());

        if(!AppUtils.isConnectionAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AllUserActivity.class));
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        friendAdapter = new FriendsAdapter(getContext(), friendIdList, mDatabaseReference, friendClickListner);
        suggestionAdapter = new SuggestionAdapter(getContext(), suggestionIdList, mDatabaseReference, friendClickListner, sendRequestClickListner);

        getFriendList();
    }

    private void getFriendList() {
        mDatabaseReference.child(userFriendListTableName).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendIdList.clear();
                pb_friend.setVisibility(View.GONE);
                if (dataSnapshot.getValue() != null) {
                    tv_noFriend.setVisibility(View.GONE);
                    rv_friends.setVisibility(View.VISIBLE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friendIdList.add(snapshot.getKey());
                        rv_friends.setAdapter(friendAdapter);
                    }

                    if(friendIdList.size() != 0){
                        rv_friends.setVisibility(View.VISIBLE);
                        ll_NoFriend.setVisibility(View.GONE);
                    }else{
                        rv_friends.setVisibility(View.GONE);
                        ll_NoFriend.setVisibility(View.VISIBLE);
                    }
                    getFriendRequestList();
                } else {
                    if(friendIdList.size() != 0){
                        rv_friends.setVisibility(View.VISIBLE);
                        ll_NoFriend.setVisibility(View.GONE);
                    }else{
                        rv_friends.setVisibility(View.GONE);
                        ll_NoFriend.setVisibility(View.VISIBLE);
                    }
                    getFriendRequestList();
                }
                friendAdapter.notifyDataSetChanged();

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
                }else{
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
                                if(!friendRequestIdList.contains(snapshot.getKey())){
                                    suggestionIdList.add(snapshot.getKey());
                                }
                            }
                        }
                    }
                }

                if(suggestionIdList.size() == 0){
                    tv_suggestion.setVisibility(View.GONE);
                    rv_suggestion.setVisibility(View.GONE);
                }else{
                    tv_suggestion.setVisibility(View.VISIBLE);
                    rv_suggestion.setVisibility(View.VISIBLE);
                    rv_suggestion.setAdapter(suggestionAdapter);
                    suggestionAdapter.notifyDataSetChanged();
                }

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
                seeFullImage(getActivity(), img_userDialog, dialogUserImage, null);
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
                Intent intent = new Intent(getActivity(), UserMessageActivity.class);
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
                    Glide.with(getActivity()).load(dataSnapshot.child("imageUrl").getValue().toString())
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
                Toast.makeText(getActivity(), "Request sent successfully", Toast.LENGTH_SHORT).show();
                profileDialog.dismiss();
                sendNotification("Friend Request", firebaseUser.getUid(), id,
                        getUserName(getActivity()), "Sent you friend request");
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
                    Toast.makeText(getActivity(), "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                    friendAdapter.notifyDataSetChanged();
                    if (type.equalsIgnoreCase("block")) {
                        Toast.makeText(getActivity(), "Blocked Successfully", Toast.LENGTH_SHORT).show();
                    } else if (type.equals("report")) {
                        Toast.makeText(getActivity(), "Report as spam successfully and removed from friends", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "User removed from friends", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

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
