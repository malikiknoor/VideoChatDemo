package com.iknoortech.videochatdemo.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.GroupDetailsAdapter;
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.listner.GroupClickListner;
import com.iknoortech.videochatdemo.model.GroupDetails;
import de.hdodenhof.circleimageview.CircleImageView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.iknoortech.videochatdemo.helper.AppConstant.blockTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.friendRequestTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupReportTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileAboutTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupMemberTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.reportTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.uploadTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userTable;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.getUserName;
import static com.iknoortech.videochatdemo.helper.AppUtils.seeFullImage;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;
import static com.iknoortech.videochatdemo.helper.AppUtils.uploadImageToServer;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class GroupProfile extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView userImage;
    private ImageView editUserImage;
    private RelativeLayout rl_addParticipant;
    private LinearLayout ll_exit;
    private TextView groupName, createdBy, groupDesc, exitGroup, reportGroup;
    private DatabaseReference mDatabaseReference;
    private String groupId, currentUserId, userImageLink;
    private RecyclerView rv_groupDetails;
    private GroupDetailsAdapter groupDetailsAdapter;
    private FirebaseUser firebaseUser;
    private ArrayList<GroupDetails> memberIdList;
    private ArrayList<String> alreadyMamberList;
    private ArrayList<String> friendList;
    private static final int IMAGE_REQUEST = 1;
    private StorageReference mImageStorage;
    private boolean isYouAdmin = false;
    private Dialog profileDialog;

    private GroupClickListner clickListner = new GroupClickListner() {
        @Override
        public void onClick(String id, String name, boolean isAdmin) {
            if (!firebaseUser.getUid().equals(id)) {
                if (isYouAdmin) {
                    isYouAdminDialog(id, name, isAdmin);
                } else {
                    isYouNotAdminDialog(id, name);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        initView();
    }

    private void initView() {

        toolbar = findViewById(R.id.groupToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userImage = findViewById(R.id.circleImageView3);
        editUserImage = findViewById(R.id.imageView10);
        groupName = findViewById(R.id.textView13);
        createdBy = findViewById(R.id.textView36);
        groupDesc = findViewById(R.id.textView42);
        exitGroup = findViewById(R.id.textView37);
        reportGroup = findViewById(R.id.textView38);
        ll_exit = findViewById(R.id.ll_exit);
        rv_groupDetails = findViewById(R.id.rv_groupDetails);
        rl_addParticipant = findViewById(R.id.rl_addParticipant);
        rv_groupDetails.setLayoutManager(new LinearLayoutManager(this));
        memberIdList = new ArrayList<>();
        alreadyMamberList = new ArrayList<>();
        friendList = new ArrayList<>();
        profileDialog = new Dialog(this);

        groupId = getIntent().getStringExtra("groupId");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = firebaseUser.getUid();
        rl_addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupProfile.this, AddGroupParticipantActivity.class);
                intent.putExtra("alreadyInGroup", alreadyMamberList);
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupName", groupName.getText().toString());
                startActivity(intent);
            }
        });

        editUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileImageDialog();
            }
        });

        exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromGroup(currentUserId, "You");
            }
        });

        reportGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportThisGroup();
            }
        });

        groupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEdit("groupName", groupName.getText().toString());
            }
        });

        groupDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEdit("groupDesc", groupDesc.getText().toString());
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeFullImage(GroupProfile.this, userImage, userImageLink, null);
            }
        });

        getGroupInfo();
    }

    private void goToEdit(String key, String value) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("key", key);
        intent.putExtra("value", value);
        intent.putExtra("isGroup", true);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void getGroupInfo() {
        /*getting group image*/
        mDatabaseReference.child(groupImageTable).child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Glide.with(GroupProfile.this).load(dataSnapshot.child("groupImageUrl")
                                .getValue()).into(userImage);

                        userImageLink = dataSnapshot.child("groupImageUrl").getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        /*getting group name*/
        mDatabaseReference.child(AppConstant.groupNameTable).child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (groupName != null) {
                    groupName.setText(dataSnapshot.child("groupName").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*getting group created by*/
        mDatabaseReference.child(AppConstant.groupTableName).child(groupId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                long timeInMilli = Long.parseLong(dataSnapshot.child("groupCreateDate").getValue().toString());
                final String dateString = formatter.format(new Date(timeInMilli));

                /*getting user name*/
                mDatabaseReference.child(AppConstant.profileNameTable).child(dataSnapshot.child("createdBy")
                        .getValue().toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (createdBy != null) {
                            createdBy.setText("created by " + dataSnapshot.child("userName").getValue() + " on " + dateString);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*getting description*/
        mDatabaseReference.child(AppConstant.groupDescTable).child(groupId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (groupDesc != null) {
                    groupDesc.setText(dataSnapshot.child("groupDesc").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*getting memeber List*/
        mDatabaseReference.child(AppConstant.groupMemberTable).child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        memberIdList.clear();
                        alreadyMamberList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            GroupDetails groupDetails = snapshot.getValue(GroupDetails.class);
                            alreadyMamberList.add(groupDetails.getMemberId());
                            if (firebaseUser.getUid().equals(groupDetails.getMemberId())) {
                                memberIdList.add(0, groupDetails);
                                if (groupDetails.isAdmin()) {
                                    rl_addParticipant.setVisibility(View.VISIBLE);
                                    isYouAdmin = true;
                                } else {
                                    rl_addParticipant.setVisibility(View.GONE);
                                    isYouAdmin = false;
                                }
                            } else {
                                memberIdList.add(groupDetails);
                            }
                        }

                        if (alreadyMamberList.contains(firebaseUser.getUid())) {
                            ll_exit.setVisibility(View.VISIBLE);
                            editUserImage.setVisibility(View.VISIBLE);
                            groupName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_24dp, 0);
                            groupName.setClickable(true);
                            groupDesc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_24dp, 0);
                            groupDesc.setClickable(true);
                        } else {
                            ll_exit.setVisibility(View.GONE);
                            editUserImage.setVisibility(View.GONE);
                            groupName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            groupName.setClickable(false);
                            groupDesc.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            groupDesc.setClickable(false);
                        }

                        groupDetailsAdapter = new GroupDetailsAdapter(GroupProfile.this, memberIdList, mDatabaseReference, firebaseUser, clickListner);
                        rv_groupDetails.setAdapter(groupDetailsAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mDatabaseReference.child(userFriendListTableName).child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendList.clear();
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                friendList.add(snapshot.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void changeProfileImageDialog() {
        CharSequence[] items = {"Gallery", "Remove profile pic"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfile.this);
        builder.setTitle("Select one");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                if (pos == 0) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), IMAGE_REQUEST);
                } else if (pos == 1) {
                    removeProfilePic();
                }
            }
        });
        builder.show();
    }

    private void removeProfilePic() {

        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference().child(uploadTableName).child(groupId + ".jpg");

        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupImageTable)
                        .child(groupId);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("groupImageUrl", "default");
                reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userImage.setImageDrawable(null);
                            Toast.makeText(GroupProfile.this, "Group profile pic is removed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupProfile.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(GroupProfile.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            uploadImageToServer(this, bitmap, true, groupId);
        }
    }

    private void isYouAdminDialog(final String id, final String name, boolean isAdmin) {
        CharSequence[] items;
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfile.this);
        builder.setTitle("Select one");

        if (friendList.contains(id)) {
            if (isAdmin) {
                items = new CharSequence[]{"View " + name, "Dismiss as admin", "Message " + name, "Remove " + name};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            openViewDialog("friend", id);
                        } else if (position == 1) {
                            dismissGroupAdmin(id);
                        } else if (position == 2) {
                            sendMessage(id);
                        } else if (position == 3) {
                            removeFromGroup(id, name);
                        }
                    }
                });
            } else {
                items = new CharSequence[]{"View " + name, "Make group admin", "Message " + name, "Remove " + name};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            openViewDialog("friend", id);
                        } else if (position == 1) {
                            makeGroupAdmin(id);
                        } else if (position == 2) {
                            sendMessage(id);
                        } else if (position == 3) {
                            removeFromGroup(id, name);
                        }
                    }
                });
            }
        } else {
            if (isAdmin) {
                items = new CharSequence[]{"View " + name, "Dismiss as admin", "Send request " + name, "Remove " + name};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            openViewDialog("suggestion", id);
                        } else if (position == 1) {
                            dismissGroupAdmin(id);
                        } else if (position == 2) {
                            sendFriendRequest(currentUserId, id);
                        } else if (position == 3) {
                            removeFromGroup(id, name);
                        }
                    }
                });
            } else {
                items = new CharSequence[]{"View " + name, "Make group admin", "Send request " + name, "Remove " + name};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            openViewDialog("suggestion", id);
                        } else if (position == 1) {
                            makeGroupAdmin(id);
                        } else if (position == 2) {
                            sendFriendRequest(currentUserId, id);
                        } else if (position == 3) {
                            removeFromGroup(id, name);
                        }
                    }
                });
            }
        }

        builder.show();
    }

    private void isYouNotAdminDialog(final String id, String name) {
        CharSequence[] items;
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfile.this);
        builder.setTitle("Select one");

        if (friendList.contains(id)) {
            items = new CharSequence[]{"View " + name, "Message " + name};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (position == 0) {
                        openViewDialog("friend", id);
                    } else if (position == 1) {
                        sendMessage(id);
                    }
                }
            });
        } else {
            items = new CharSequence[]{"View " + name, "Send request " + name};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (position == 0) {
                        openViewDialog("suggestion", id);
                    } else if (position == 1) {
                        sendFriendRequest(currentUserId, id);
                    }
                }
            });
        }

        builder.show();
    }

    private void openViewDialog(String type, final String id) {
        profileDialog.setCanceledOnTouchOutside(false);
        profileDialog.setContentView(R.layout.profile_dialog);
        profileDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileDialog.show();

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
                            if (friendList.contains(idList.get(i))) {
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
                sendMessage(id);
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
                Glide.with(GroupProfile.this).load(dataSnapshot.child("imageUrl").getValue().toString())
                        .into(img_userDialog);
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
                Toast.makeText(GroupProfile.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                profileDialog.dismiss();
                sendNotification("Friend Request", firebaseUser.getUid(), id,
                        getUserName(GroupProfile.this), "Sent you friend request");
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
                    if (type.equalsIgnoreCase("block")) {
                        Toast.makeText(GroupProfile.this, "Blocked Successfully", Toast.LENGTH_SHORT).show();
                    } else if (type.equals("report")) {
                        Toast.makeText(GroupProfile.this, "Report as spam successfully and removed from friends", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GroupProfile.this, "User removed from friends", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupProfile.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GroupProfile.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

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
                    Toast.makeText(GroupProfile.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage(String id) {
        Intent intent = new Intent(GroupProfile.this, UserMessageActivity.class);
        intent.putExtra("userid", id);
        startActivity(intent);
    }

    private void makeGroupAdmin(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupMemberTable)
                .child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("admin", true);
            reference.child(id).updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissGroupAdmin(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupMemberTable)
                .child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("admin", false);
            reference.child(id).updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFromGroup(final String id, final String name) {
        int pos = alreadyMamberList.indexOf(id);
        alreadyMamberList.remove(pos);
        if (alreadyMamberList.contains(firebaseUser.getUid())) {
            ll_exit.setVisibility(View.VISIBLE);
        } else {
            ll_exit.setVisibility(View.GONE);
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupMemberTable)
                .child(groupId);
        try {
            reference.child(id).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull
                        DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(GroupProfile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GroupProfile.this, name + " removed successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportThisGroup() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupReportTable);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ArrayList<String> reportList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            reportList.add(snapshot.getKey());
                        } catch (Exception e) {
                            Log.d("TAG", "report list log :- " + e.getMessage());
                        }
                    }
                    if (reportList.contains(currentUserId)) {
                        Toast.makeText(GroupProfile.this, "You have already reported against this group.", Toast.LENGTH_SHORT).show();
                    } else {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", currentUserId);
                        hashMap.put("reportedDated", String.valueOf(System.currentTimeMillis()));
                        reference.child(currentUserId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupProfile.this, "You have successfully reported this group as spam", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(GroupProfile.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", currentUserId);
                    hashMap.put("reportedDated", String.valueOf(System.currentTimeMillis()));
                    reference.child(currentUserId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(GroupProfile.this, "You have successfully reported this group as spam", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GroupProfile.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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