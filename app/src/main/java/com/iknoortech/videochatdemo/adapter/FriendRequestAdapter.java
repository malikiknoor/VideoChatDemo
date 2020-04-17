package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
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
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.model.RequestList;

import static com.iknoortech.videochatdemo.helper.AppConstant.friendRequestTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.userFriendListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userTable;
import static com.iknoortech.videochatdemo.helper.AppUtils.getTimeAgo;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;


public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context context;
    private DatabaseReference mUserDatabase;
    private ArrayList<String> requestIdLists;
    private ArrayList<RequestList> requestLists;
    private String currentUserId = "";
    private ArrayList<String> imageList = new ArrayList<>();

    public FriendRequestAdapter(Context context, DatabaseReference mUserDatabase,
                                ArrayList<String> requestIdLists, ArrayList<RequestList> requestLists,
                                String currentUserId) {
        this.context = context;
        this.mUserDatabase = mUserDatabase;
        this.requestLists = requestLists;
        this.requestIdLists = requestIdLists;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.sendTime.setText(getTimeAgo(Long.parseLong(requestLists.get(position).getSend_time())));

        startTimer(holder.sendTime, Long.parseLong(requestLists.get(position).getSend_time()));

        mUserDatabase.child(profileNameTable).child(requestIdLists.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
                if (requestLists.get(position).getRequest_type().equalsIgnoreCase("sent")) {
                    holder.ll_receive.setVisibility(View.GONE);
                    holder.ll_send.setVisibility(View.VISIBLE);
                } else {
                    holder.ll_receive.setVisibility(View.VISIBLE);
                    holder.ll_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.child(profileImageTable).child(requestIdLists.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("imageUrl").getValue().toString().equals("default")) {
                    holder.userImage.setBackgroundResource(R.drawable.ic_user);
                } else {
                    Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
                }
                imageList.add(dataSnapshot.child("imageUrl").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.accpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest(requestIdLists.get(position));
            }
        });

        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ignoreFriendRequest(requestIdLists.get(position));
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ignoreFriendRequest(requestIdLists.get(position));
            }
        });

        holder.reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReminder(requestIdLists.get(position), holder.userName.getText().toString());
            }
        });

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageList.get(position).equals("default")){
                    Log.d("TAG", "No Image in request");
                }else{
                    AppUtils.seeFullImage(context, null, imageList.get(position), holder.userImage);
                }
            }
        });
    }

    private void startTimer(final TextView sendTime, final long parseLong) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendTime.setText(getTimeAgo(parseLong));
                startTimer(sendTime, parseLong);
            }
        }, 10000);
    }

    @Override
    public int getItemCount() {
        return requestLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImage;
        private TextView userName;
        private TextView ignore, accpet, cancel, reminder, sendTime;
        private LinearLayout ll_send, ll_receive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.circleImageView);
            userName = itemView.findViewById(R.id.textView14);
            ignore = itemView.findViewById(R.id.btn_reqIgnore);
            accpet = itemView.findViewById(R.id.btn_reqAccept);
            cancel = itemView.findViewById(R.id.btn_reqCancel);
            reminder = itemView.findViewById(R.id.btn_sendReminder);
            ll_send = itemView.findViewById(R.id.ll_send);
            ll_receive = itemView.findViewById(R.id.ll_receive);
            sendTime = itemView.findViewById(R.id.textView48);
        }
    }

    private void acceptFriendRequest(final String userId) {
        final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        final Map friendsMap = new HashMap();
        DatabaseReference mMobileReference = FirebaseDatabase.getInstance().getReference(userTable).child(userId);
        mMobileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                friendsMap.put(userFriendListTableName + "/" + currentUserId + "/" + userId + "/friend_id", userId);
                friendsMap.put(userFriendListTableName + "/" + currentUserId + "/" + userId + "/acceptDate", String.valueOf(System.currentTimeMillis()));
                friendsMap.put(userFriendListTableName + "/" + userId + "/" + currentUserId + "/friend_id", currentUserId);
                friendsMap.put(userFriendListTableName + "/" + userId + "/" + currentUserId + "/acceptDate", String.valueOf(System.currentTimeMillis()));

                friendsMap.put(friendRequestTableName + "/" + currentUserId + "/" + userId, null);
                friendsMap.put(friendRequestTableName + "/" + userId + "/" + currentUserId, null);

                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(context, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ignoreFriendRequest(final String userId) {
        final DatabaseReference mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child(friendRequestTableName);
        mFriendReqDatabase.child(currentUserId).child(userId)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendReqDatabase.child(userId).child(currentUserId)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Friend Request Removed Seccussfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendReminder(final String id, final String userName){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(friendRequestTableName)
                .child(currentUserId).child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long sentTime = Long.parseLong(dataSnapshot.child("send_time").getValue().toString());
                long currentTime = System.currentTimeMillis();
                long diff = currentTime - sentTime;
                if(diff <= 86400000){
                    Toast.makeText(context, "You can send reminder after 24 Hours.", Toast.LENGTH_SHORT).show();
                }else{
                    Map requestMap = new HashMap();
                    requestMap.put(currentUserId + "/" + id + "/request_type", "sent");
                    requestMap.put(currentUserId + "/" + id + "/send_time", String.valueOf(System.currentTimeMillis()));
                    requestMap.put(id + "/" + currentUserId + "/request_type", "received");
                    requestMap.put(id + "/" + currentUserId + "/send_time", String.valueOf(System.currentTimeMillis()));
                    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    mDatabaseReference.child(friendRequestTableName).updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            sendNotification(currentUserId, "Request reminder" ,id, userName, "Hi, I am waiting for your response");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
