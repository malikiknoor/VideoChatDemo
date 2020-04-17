package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.activity.GroupMessageActvity;
import com.iknoortech.videochatdemo.activity.GroupProfile;
import com.iknoortech.videochatdemo.activity.UserMessageActivity;
import com.iknoortech.videochatdemo.model.Chat;
import com.iknoortech.videochatdemo.model.ChatList;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.iknoortech.videochatdemo.helper.AppConstant.chatTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.onlineStatusTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupDescTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppUtils.getLastMsgDate;
import static com.iknoortech.videochatdemo.helper.AppUtils.seeFullImage;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ChatList> users;
    private DatabaseReference mDatabaseReference;
    private String theLastMessage;

    public ChatsAdapter(Context context, ArrayList<ChatList> users, DatabaseReference mDatabaseReference) {
        this.context = context;
        this.users = users;
        this.mDatabaseReference = mDatabaseReference;
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatsAdapter.ViewHolder holder, final int position) {

        if (users.get(position).getIsGroup().equals("true")) {
            getGroupInfo(users.get(position).getId(), holder, position);
            lastUserMessage(users.get(position).getId(), holder, true, users.get(position).getTime());
        } else {
            getUserInfo(users.get(position).getId(), holder, position);
            lastUserMessage(users.get(position).getId(), holder, false, users.get(position).getTime());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (users.get(position).getIsGroup().equals("true")) {
                    intent = new Intent(context, GroupMessageActvity.class);
                    intent.putExtra("groupId", users.get(position).getId());
                    intent.putExtra("groupName", holder.userName.getText());
                    intent.putExtra("groupImage", users.get(position).getImageUrl());
                } else {
                    intent = new Intent(context, UserMessageActivity.class);
                    intent.putExtra("userid", users.get(position).getId());
                }
                context.startActivity(intent);
            }
        });

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeFullImage(context, holder.userImage, users.get(position).getImageUrl(), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName, lastMsg, unreadText, lastMsgDate;
        private ImageView status;
        private View unreadView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.img_usersItem);
            userName = itemView.findViewById(R.id.tv_usersItem);
            status = itemView.findViewById(R.id.img_usersItemStatus);
            lastMsg = itemView.findViewById(R.id.tv_lastMsg);
            unreadView = itemView.findViewById(R.id.view3);
            unreadText = itemView.findViewById(R.id.textView32);
            lastMsgDate = itemView.findViewById(R.id.textView49);
        }
    }

    private void getUserInfo(String id, final ViewHolder holder, final int position) {
        mDatabaseReference.child(profileNameTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(profileImageTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("imageUrl").getValue().toString().equals("default")) {
                    holder.userImage.setBackgroundResource(R.drawable.ic_user);
                } else {
                    Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
                }
                users.get(position).setImageUrl(dataSnapshot.child("imageUrl").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(onlineStatusTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.child("status").getValue().toString().equals("online")
                            || dataSnapshot.child("status").getValue().toString().equals("typing...")) {
                        holder.status.setVisibility(View.VISIBLE);
                    } else {
                        holder.status.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroupInfo(String id, final ViewHolder holder, final int position) {
        mDatabaseReference.child(groupNameTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("groupName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(groupImageTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.child("groupImageUrl").getValue().toString().equals("default")) {
                        holder.userImage.setBackgroundResource(R.drawable.ic_group);
                    } else {
                        Glide.with(context).load(dataSnapshot.child("groupImageUrl").getValue().toString()).into(holder.userImage);
                    }
                    users.get(position).setImageUrl(dataSnapshot.child("groupImageUrl").getValue().toString());
                }catch (Exception e){
                    Log.d("TAG", "ChatAdapterGroupImageError: "+e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void lastUserMessage(final String userId, final ViewHolder holder, final boolean isGroup, String time) {
        holder.lastMsgDate.setText(getLastMsgDate(Long.parseLong(time)));
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference.child(chatTableName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unreadMsg = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    assert firebaseUser != null;
                    try {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                            if (chat.getReceiver().equals(firebaseUser.getUid())) {
                                if (chat.isIsseen()) {
                                    holder.unreadView.setVisibility(View.GONE);
                                    holder.unreadText.setVisibility(View.GONE);
                                    holder.lastMsgDate.setTextColor(Color.parseColor("#a4a4a4"));
                                } else {
                                    holder.unreadView.setVisibility(View.VISIBLE);
                                    holder.unreadText.setVisibility(View.VISIBLE);
                                    unreadMsg = (unreadMsg + 1);
                                    holder.unreadText.setText(String.valueOf(unreadMsg));
                                    holder.lastMsgDate.setTextColor(Color.parseColor("#0067CE"));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                switch (theLastMessage) {
                    case "default":
                        if(isGroup){
                            mDatabaseReference.child(groupDescTable).child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    holder.lastMsg.setText(dataSnapshot.child("groupDesc").getValue().toString());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }else {
                            holder.lastMsg.setText("No Message");
                        }
                        break;
                    default:
                        holder.lastMsg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
