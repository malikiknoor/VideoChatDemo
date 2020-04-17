package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.iknoortech.videochatdemo.listner.BlockClickListner;
import com.iknoortech.videochatdemo.model.BlockedUserList;
import de.hdodenhof.circleimageview.CircleImageView;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BlockedUserList> userLists;
    private BlockClickListner clickListner;
    private DatabaseReference mUserDatabase;

    public BlockAdapter(Context context, ArrayList<BlockedUserList> userLists, BlockClickListner clickListner, DatabaseReference mUserDatabase) {
        this.context = context;
        this.userLists = userLists;
        this.clickListner = clickListner;
        this.mUserDatabase = mUserDatabase;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_block, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int pos) {

        String id = userLists.get(pos).getKey();

        mUserDatabase.child(AppConstant.profileNameTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.child(AppConstant.profileImageTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserDatabase.child(AppConstant.profileAboutTable).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListner != null) {
                    clickListner.onClick(userLists.get(pos).getKey());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName, userStatus;
        private Button unblock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.img_block);
            userName = itemView.findViewById(R.id.tv_blockUserName);
            userStatus = itemView.findViewById(R.id.tv_blockUserStatus);
            unblock = itemView.findViewById(R.id.btn_unblock);
        }
    }
}
