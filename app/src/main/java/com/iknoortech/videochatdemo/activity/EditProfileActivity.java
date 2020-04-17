package com.iknoortech.videochatdemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.iknoortech.videochatdemo.R;

import static com.iknoortech.videochatdemo.helper.AppUtils.getMyPrettyDate;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateGroupDesc;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateGroupName;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateUserAbout;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateUserName;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.btn_EditUpdate);
        toolbar = findViewById(R.id.toolbar_edit);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final String key = getIntent().getStringExtra("key");
        String value = getIntent().getStringExtra("value");
        final boolean isGroup = getIntent().getBooleanExtra("isGroup", false);

        if(isGroup){
            getSupportActionBar().setTitle("Edit Group");
        }else{
            getSupportActionBar().setTitle("Edit Profile");
        }

        editText.setText(value);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "This field is required", Toast.LENGTH_SHORT).show();
                } else {
                    if (isGroup) {
                        String groupId = getIntent().getStringExtra("groupId");
                        if(key.equals("groupName")){
                            updateGroupName(editText.getText().toString(), groupId);
                            Toast.makeText(EditProfileActivity.this, "Group Name updated successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            updateGroupDesc(editText.getText().toString(), groupId);
                            Toast.makeText(EditProfileActivity.this, "Group Description updated successfully", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    } else {
                        updateUserProfile(key, editText.getText().toString());
                    }
                }
            }
        });
    }

    private void updateUserProfile(String key, String value) {
        if (key.equals("userName")) {
            updateUserName(value);
        } else {
            updateUserAbout(value);
        }
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
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
