package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.listner.FriendClickListner;
import com.iknoortech.videochatdemo.model.ParticipantList;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddGroupAllFriendsAdapter extends RecyclerView.Adapter<AddGroupAllFriendsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<? extends ParticipantList> participantLists;
    private DatabaseReference mUserDatabase;
    private FriendClickListner friendClickListner;

    public AddGroupAllFriendsAdapter(Context context, ArrayList<? extends ParticipantList> idList,
                                     DatabaseReference mUserDatabase, FriendClickListner friendClickListner) {
        this.context = context;
        this.participantLists = idList;
        this.mUserDatabase = mUserDatabase;
        this.friendClickListner = friendClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_create_group_all_friends, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int pos) {
        mUserDatabase.child(AppConstant.profileNameTable).child(participantLists.get(pos).getFriend_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
                participantLists.get(pos).setName(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.child(AppConstant.profileAboutTable).child(participantLists.get(pos).getFriend_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.child(AppConstant.profileImageTable).child(participantLists.get(pos).getFriend_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
                    participantLists.get(pos).setImage(dataSnapshot.child("imageUrl").getValue().toString());
                }catch (Exception e){
                    Log.d("TAG", "AddGroupAllFriendesAdapterError "+e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(participantLists.get(pos).isSelected()){
                    participantLists.get(pos).setSelected(false);
                }else{
                    participantLists.get(pos).setSelected(true);
                }
                if(friendClickListner != null){
                    friendClickListner.onClick("group", participantLists.get(pos).getFriend_id());
                }
                notifyDataSetChanged();
            }
        });

        if(participantLists.get(pos).isSelected()){
            holder.status.setVisibility(View.VISIBLE);
        }else{
            holder.status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return participantLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName, userStatus;
        private ImageView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.img_create_group_user);
            userName = itemView.findViewById(R.id.textView10);
            status = itemView.findViewById(R.id.imageView3);
            userStatus = itemView.findViewById(R.id.textView11);
        }
    }
}
