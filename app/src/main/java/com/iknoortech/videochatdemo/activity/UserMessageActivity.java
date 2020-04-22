package com.iknoortech.videochatdemo.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.MessageAdapter;
import com.iknoortech.videochatdemo.model.BlockedUserList;
import com.iknoortech.videochatdemo.model.Chat;

import static com.iknoortech.videochatdemo.helper.AppConstant.blockTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.chatListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.chatTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.onlineStatusTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.getMyPrettyDate;
import static com.iknoortech.videochatdemo.helper.AppUtils.seeFullImage;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;


public class UserMessageActivity extends AppCompatActivity {

    private CircleImageView userImage;
    private TextView username, status, blockText;
    private EditText edt_message;
    private ImageView send;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private DatabaseReference mBlockedRference;
    private DatabaseReference mOnlineRference;
    private DatabaseReference mFriendRference;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private ArrayList<Chat> mChat;
    private ValueEventListener seenListener;
    private String userId, blockType, userImageLink;
    private boolean notify = false;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_message);

        Toolbar toolbar = findViewById(R.id.messagetoolbar);
        setSupportActionBar(toolbar);
        userImage = findViewById(R.id.img_messageImage);
        username = findViewById(R.id.tv_messageUsername);
        status = findViewById(R.id.tv_messageStatus);
        edt_message = findViewById(R.id.edt_typeMsg);
        send = findViewById(R.id.img_sendMsg);
        recyclerView = findViewById(R.id.rv_msg);
        blockText = findViewById(R.id.tv_blockText);
        rootView = findViewById(R.id.rootView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        userId = getIntent().getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        mBlockedRference = FirebaseDatabase.getInstance().getReference(blockTableName);
        mOnlineRference = FirebaseDatabase.getInstance().getReference(onlineStatusTable);
        mFriendRference = FirebaseDatabase.getInstance().getReference(userFriendListTableName).child(firebaseUser.getUid());

        edt_message.addTextChangedListener(new TextWatcher() {

            boolean isTyping = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            private Timer timer = new Timer();
            private final long DELAY = 1000; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                if (!isTyping) {
                    userStatus("typing...");
                    // Send notification for start typing event
                    isTyping = true;
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                isTyping = false;
                                userStatus("online");
                            }
                        },
                        DELAY
                );
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeFullImage(UserMessageActivity.this, userImage, userImageLink, null);
            }
        });

        getUser();
        isBlocked();
    }

    private void isBlocked() {
        mBlockedRference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BlockedUserList> blockedIdList = new ArrayList<>();
                if (dataSnapshot.getValue() == null) {
                    blockType = "not";
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BlockedUserList blockedUserList = snapshot.getValue(BlockedUserList.class);
                        blockedIdList.add(blockedUserList);
                    }
                }

                for (int i = 0; i < blockedIdList.size(); i++) {
                    if (blockedIdList.get(i).getKey().contains(userId)) {
                        blockType = blockedIdList.get(i).getBlock_type();
                        break;
                    } else if (i == (blockedIdList.size() - 1)) {
                        blockType = "not";
                        break;
                    }
                }

                if (blockType.equals("not")) {
                    send.setVisibility(View.VISIBLE);
                    edt_message.setVisibility(View.VISIBLE);
                    blockText.setVisibility(View.GONE);
                    isFriend();
                } else {
                    send.setVisibility(View.GONE);
                    edt_message.setVisibility(View.GONE);
                    blockText.setVisibility(View.VISIBLE);
                    if (blockType.equals("other")) {
                        blockText.setText("You are Blocked by this user");
                        status.setVisibility(View.GONE);
                    } else {
                        blockText.setText("You have Blocked this user to unblock this user click here.");
                        blockText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                blockText.setClickable(false);
                                unblockUser(userId);
                            }
                        });
                    }
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

        mBlockedRference.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if (databaseError == null) {
                    isFriend();
                    Toast.makeText(UserMessageActivity.this, "You have successfully unblocked this user", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserMessageActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void getUser() {
        reference.child(profileNameTable).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child(profileImageTable).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("imageUrl").getValue().toString().equals("default")) {
                    userImage.setBackgroundResource(R.drawable.ic_user_toolbar);
                } else {
                    Glide.with(getApplicationContext()).load(dataSnapshot.child("imageUrl").getValue().toString()).into(userImage);
                }
                userImageLink = dataSnapshot.child("imageUrl").getValue().toString();
                readMessage(firebaseUser.getUid(), userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mOnlineRference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if(dataSnapshot.child("status").getValue().toString().equals("online") ||
                            dataSnapshot.child("status").getValue().toString().equals("logout") ||
                            dataSnapshot.child("status").getValue().toString().equals("typing...") ||
                            dataSnapshot.child("status").getValue().toString().equals("login")){
                        status.setText(dataSnapshot.child("status").getValue().toString());
                    }else{
                        status.setText(getMyPrettyDate(Long.parseLong(dataSnapshot.child("status").getValue().toString())));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userId);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = edt_message.getText().toString();
                msg = msg.trim();
                if (msg.equals("")) {
                    Toast.makeText(UserMessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(firebaseUser.getUid(), userId, msg);
                    edt_message.setText("");
                    msg = "";
                }
            }
        });
    }

    private void isFriend() {
        mFriendRference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) {
                    send.setVisibility(View.GONE);
                    edt_message.setVisibility(View.GONE);
                    blockText.setVisibility(View.VISIBLE);
                    blockText.setText("This user is not in your friend list.");
                    status.setVisibility(View.GONE);
                } else {
                    if (dataSnapshot.getValue() != null) {
                        send.setVisibility(View.VISIBLE);
                        edt_message.setVisibility(View.VISIBLE);
                        status.setVisibility(View.VISIBLE);
                        blockText.setVisibility(View.GONE);
                    } else {
                        send.setVisibility(View.GONE);
                        edt_message.setVisibility(View.GONE);
                        blockText.setVisibility(View.VISIBLE);
                        blockText.setText("This user is not in your friend list.");
                        status.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myId, final String userId) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference(chatTableName);
        try {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mChat.clear();
                    Chat chat = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                        if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                            chat.setMessageId(snapshot.getKey());
                            mChat.add(chat);
                        }
                        adapter = new MessageAdapter(UserMessageActivity.this, mChat);
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference(chatTableName);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String date = (simpleDateFormat.format(new Date()));
        Date mDate = null;
        try {
            mDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String timeInMilliseconds = String.valueOf(mDate.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("messageDate", timeInMilliseconds);
        hashMap.put("isseen", false);
        hashMap.put("isEdited", false);
        hashMap.put("deleteForMe", "");
        hashMap.put("deleteForEveryone", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aaa");
        String time = dateFormat.format(new Date(System.currentTimeMillis()));
        hashMap.put("time", time);
        reference.child(chatTableName).push().setValue(hashMap);

        final DatabaseReference senderUserRef = FirebaseDatabase.getInstance().getReference(chatListTableName)
                .child(firebaseUser.getUid())
                .child(userId);

        senderUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    senderUserRef.child("id").setValue(userId);
                    senderUserRef.child("isGroup").setValue("false");
                    senderUserRef.child("time").setValue(String.valueOf(System.currentTimeMillis()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference receiverUserRef = FirebaseDatabase.getInstance().getReference(chatListTableName)
                .child(userId)
                .child(firebaseUser.getUid());

        receiverUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    receiverUserRef.child("id").setValue(firebaseUser.getUid());
                    receiverUserRef.child("isGroup").setValue("false");
                    receiverUserRef.child("time").setValue(String.valueOf(System.currentTimeMillis()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference(profileNameTable).child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (notify) {
                    sendNotification("New Message", firebaseUser.getUid(),receiver,
                            dataSnapshot.child("userName").getValue().toString(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userStatus("online");
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        userStatus(String.valueOf(System.currentTimeMillis()));
        currentUser("none");
    }
}
