package com.iknoortech.videochatdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.iknoortech.videochatdemo.model.GroupDetails;
import com.iknoortech.videochatdemo.model.ItemSelectedInGroup;
import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.services.common.SafeToast;

public class AddParticipantAdapter extends RecyclerView.Adapter<AddParticipantAdapter.ViewHolder> {

    private Context context;
    private DatabaseReference mReference;
    private ArrayList<String> alreadyInGroup;
    private ArrayList<ItemSelectedInGroup> friendIdList;
    private FriendClickListner friendClickListner;

    public AddParticipantAdapter(Context context, DatabaseReference mReference,
                                 ArrayList<String> alreadyInGroup, ArrayList<ItemSelectedInGroup> friendIdList,
                                 FriendClickListner friendClickListner) {
        this.context = context;
        this.mReference = mReference;
        this.alreadyInGroup = alreadyInGroup;
        this.friendIdList = friendIdList;
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
        mReference.child(AppConstant.profileNameTable).child(friendIdList.get(pos).getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.userName.setText(dataSnapshot.child("userName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (alreadyInGroup.contains(friendIdList.get(pos).getId())) {
            holder.userStatus.setText("This user is already is in this group");
        } else {
            mReference.child(AppConstant.profileAboutTable).child(friendIdList.get(pos).getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.userStatus.setText(dataSnapshot.child("userStatus").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mReference.child(AppConstant.profileImageTable).child(friendIdList.get(pos).getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Glide.with(context).load(dataSnapshot.child("imageUrl").getValue().toString()).into(holder.userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyInGroup.contains(friendIdList.get(pos).getId())) {
                    Toast.makeText(context, "This user is already is in this group", Toast.LENGTH_SHORT).show();
                } else {
                    if (friendIdList.get(pos).isSelected()) {
                        friendIdList.get(pos).setSelected(false);
                    } else {
                        friendIdList.get(pos).setSelected(true);
                    }

                    if (friendClickListner != null) {
                        friendClickListner.onClick("group", friendIdList.get(pos).getId());
                    }
                    notifyDataSetChanged();
                }
            }
        });

        if(friendIdList.get(pos).isSelected()){
            holder.status.setVisibility(View.VISIBLE);
        }else{
            holder.status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return friendIdList.size();
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
