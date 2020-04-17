package com.iknoortech.videochatdemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.helper.AppUtils;
import com.iknoortech.videochatdemo.listner.BlockClickListner;
import com.iknoortech.videochatdemo.listner.GroupClickListner;
import com.iknoortech.videochatdemo.model.GroupDetails;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailsAdapter extends RecyclerView.Adapter<GroupDetailsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<GroupDetails> groupDetails;
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private DatabaseReference mReference;
    private FirebaseUser firebaseUser;
    private GroupClickListner clickListner;

    public GroupDetailsAdapter(Context context, ArrayList<GroupDetails> groupDetails, DatabaseReference mReference,
                               FirebaseUser firebaseUser, GroupClickListner clickListner) {
        this.context = context;
        this.groupDetails = groupDetails;
        this.mReference = mReference;
        this.firebaseUser = firebaseUser;
        this.clickListner = clickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        getUserImage(groupDetails.get(position).getMemberId(), holder);
        getUserName(groupDetails.get(position).getMemberId(), holder);
        getUserAbout(groupDetails.get(position).getMemberId(), holder);

        if(groupDetails.get(position).isAdmin()){
            holder.userAdmin.setVisibility(View.VISIBLE);
        }else{
            holder.userAdmin.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              clickListner.onClick(groupDetails.get(position).getMemberId(), holder.userName.getText().toString() ,groupDetails.get(position).isAdmin());
            }
        });

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.seeFullImage(context, holder.userImage, imageUrlList.get(position), null);
            }
        });
    }

    private void getUserAbout(String memberId, final ViewHolder holder) {
        mReference.child(AppConstant.profileAboutTable).child(memberId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserName(String memberId, final ViewHolder holder) {
        if(firebaseUser.getUid().equals(memberId)){
            holder.userName.setText("You");
        }else{
            mReference.child(AppConstant.profileNameTable).child(memberId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void getUserImage(String memberId, final ViewHolder holder) {
        mReference.child(AppConstant.profileImageTable).child(memberId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try{
                            Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
                            imageUrlList.add(dataSnapshot.child("imageUrl").getValue().toString());
                        }catch (Exception e){
                            Log.d("TAG", "GroupDetailsAdapterGlideError: "+e.getMessage());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView userName, userStatus, userAdmin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.img_groupDetails);
            userName = itemView.findViewById(R.id.textView44);
            userStatus = itemView.findViewById(R.id.textView45);
            userAdmin = itemView.findViewById(R.id.textView46);

        }
    }
}
