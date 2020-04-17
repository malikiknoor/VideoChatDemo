package com.iknoortech.videochatdemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.ShowParticipantsAdapter;
import com.iknoortech.videochatdemo.model.ParticipantList;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.iknoortech.videochatdemo.helper.AppConstant.chatListTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupMemberTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.uploadTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.sendNotification;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateGroupDesc;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateGroupImage;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateGroupName;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class CreateGroupSecoundActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView userImage;
    private ImageView editImage;
    private EditText edt_groupName, edt_groupDesc;
    private Button btn_create;
    private RecyclerView rv_participant;
    private ShowParticipantsAdapter participantsAdapter;
    private ArrayList<ParticipantList> participantLists;
    private Uri imageUri;
    private String groupId;
    private StorageReference mImageStorage;
    private static int RESULT_LOAD_IMAGE = 1;
    private ProgressDialog pd;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_secound);

        toolbar = findViewById(R.id.toolbar_createGroupSecound);
        userImage = findViewById(R.id.circleImageView2);
        edt_groupName = findViewById(R.id.editText4);
        edt_groupDesc = findViewById(R.id.editText5);
        editImage = findViewById(R.id.imageView8);
        btn_create = findViewById(R.id.button3);
        rv_participant = findViewById(R.id.rv_participant);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rv_participant.setLayoutManager(new GridLayoutManager(this, 3));
        participantLists = (ArrayList<ParticipantList>) getIntent().getSerializableExtra("participantList");
        pd = new ProgressDialog(this);
        pd.setMessage("Please wait....");
        pd.setCanceledOnTouchOutside(false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(chatListTableName);
        participantsAdapter = new ShowParticipantsAdapter(this, participantLists);
        rv_participant.setAdapter(participantsAdapter);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_groupName.getText().toString().isEmpty()) {
                    Toast.makeText(CreateGroupSecoundActivity.this, "Please enter your group subject",
                            Toast.LENGTH_SHORT).show();
                } else if (edt_groupDesc.getText().toString().isEmpty()) {
                    Toast.makeText(CreateGroupSecoundActivity.this, "Please provide group description",
                            Toast.LENGTH_SHORT).show();
                } else {
                    createGroup(edt_groupName.getText().toString(), edt_groupDesc.getText().toString());
                }
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), RESULT_LOAD_IMAGE);
            }
        });
    }

    private void createGroup(String groupName, String groupDesc) {
        pd.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        groupId = reference.push().getKey();

        reference = FirebaseDatabase.getInstance().getReference(groupTableName).child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("groupCreateDate", String.valueOf(System.currentTimeMillis()));
            hashMap.put("createdBy", firebaseUser.getUid());
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        userRef.child(firebaseUser.getUid()).child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userRef.child(firebaseUser.getUid()).child(groupId).child("id").setValue(groupId);
                    userRef.child(firebaseUser.getUid()).child(groupId).child("isGroup").setValue("true");
                    userRef.child(firebaseUser.getUid()).child(groupId).child("time").setValue(String.valueOf(System.currentTimeMillis()));
                } else {
                    userRef.child(firebaseUser.getUid()).child(groupId).child("time").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateGroupName(groupName, groupId);
        updateGroupDesc(groupDesc, groupId);

        if (imageUri == null) {
            updateGroupImage("default", groupId);
            pd.dismiss();
        } else {
            uploadImageToServer();
        }

        reference = FirebaseDatabase.getInstance().getReference(groupMemberTable)
                .child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("memberId", firebaseUser.getUid());
            hashMap.put("admin", true);
            hashMap.put("joinDate", String.valueOf(System.currentTimeMillis()));
            reference.child(firebaseUser.getUid()).updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < participantLists.size(); i++) {
            try {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("memberId", participantLists.get(i).getFriend_id());
                hashMap.put("admin", false);
                hashMap.put("joinDate", String.valueOf(System.currentTimeMillis()));
                reference.child(participantLists.get(i).getFriend_id()).updateChildren(hashMap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final int finalI = i;
            userRef.child(participantLists.get(i).getFriend_id()).child(groupId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userRef.child(participantLists.get(finalI).getFriend_id()).child(groupId).child("id").setValue(groupId);
                            userRef.child(participantLists.get(finalI).getFriend_id()).child(groupId).child("isGroup").setValue("true");
                            userRef.child(participantLists.get(finalI).getFriend_id()).child(groupId).child("time").setValue(String.valueOf(System.currentTimeMillis()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            sendNotification("Group", firebaseUser.getUid(), participantLists.get(i).getFriend_id(),
                    participantLists.get(i).getName(), "added to you in a group");

            if (i == (participantLists.size() - 1)) {
                if (imageUri == null) {
                    pd.dismiss();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }


    }

    private void uploadImageToServer() {
        final StorageReference filePath = mImageStorage.child(uploadTableName)
                .child(groupId + ".jpg");

        filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();
                    String downloadUrl = downUri.toString();
                    updateGroupImage(downloadUrl, groupId);
                    pd.dismiss();
                    Intent intent = new Intent(CreateGroupSecoundActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(userImage);
        }
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
