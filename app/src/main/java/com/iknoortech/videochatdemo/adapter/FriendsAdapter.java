package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.activity.UserMessageActivity;
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> idList;
    private DatabaseReference mDatabaseReference;
    private FriendClickListner friendClickListner;
    private ArrayList<String> imageIdList = new ArrayList<>();

    public FriendsAdapter(Context context, ArrayList<String> idList, DatabaseReference mDatabaseReference,
                          FriendClickListner friendClickListner) {
        this.context = context;
        this.idList = idList;
        this.mDatabaseReference = mDatabaseReference;
        this.friendClickListner = friendClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int pos) {
        mDatabaseReference.child(AppConstant.profileNameTable).child(idList.get(pos)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(AppConstant.profileAboutTable).child(idList.get(pos)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(AppConstant.profileImageTable).child(idList.get(pos)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("imageUrl").getValue().toString().equals("default")){
                    holder.userImage.setBackgroundResource(R.drawable.ic_user);
                }else {
                    Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
                }
                imageIdList.add(dataSnapshot.child("imageUrl").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(AppConstant.onlineStatusTable).child(idList.get(pos)).addValueEventListener(new ValueEventListener() {
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendClickListner != null) {
                    friendClickListner.onClick("friend", idList.get(pos));
                }
            }
        });

        holder.msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserMessageActivity.class);
                intent.putExtra("userid", idList.get(pos));
                context.startActivity(intent);
            }
        });

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.seeFullImage(context, holder.userImage, imageIdList.get(pos), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return idList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userImage;
        private TextView userName, userStatus;
        private ImageView status;
        private Button msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.img_friend);
            userName = itemView.findViewById(R.id.textView19);
            status = itemView.findViewById(R.id.imageView4);
            userStatus = itemView.findViewById(R.id.textView20);
            msg = itemView.findViewById(R.id.button5);
        }
    }
}
