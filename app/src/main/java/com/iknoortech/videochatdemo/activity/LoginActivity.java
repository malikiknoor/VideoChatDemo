package com.iknoortech.videochatdemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.model.RegisteredMobileNumbers;

import static com.iknoortech.videochatdemo.helper.AppConstant.mobileTableName;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setFirebaseToken;

public class LoginActivity extends AppCompatActivity {

    private EditText edt_mobile;
    private Button next;
    private ProgressDialog pd;
    private DatabaseReference mMobileReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt_mobile = findViewById(R.id.editText2);
        next = findViewById(R.id.button);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading....");
        pd.setCancelable(false);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_mobile.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
                } else {
                    pd.show();
                    isMobileRegistered(edt_mobile.getText().toString());
                }
            }
        });

        getFirebaseToken();
    }

    private void getFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            setFirebaseToken(LoginActivity.this, task.getResult().getToken());
                        } else {
                            getFirebaseToken();
                        }
                    }
                });
    }
    private void isMobileRegistered(final String phone) {
        mMobileReference = FirebaseDatabase.getInstance().getReference(mobileTableName);
        final ArrayList<RegisteredMobileNumbers> mobileLists = new ArrayList<>();
        final Intent intent = new Intent(LoginActivity.this, LoginActivityTwo.class);
        try {
            mMobileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pd.dismiss();
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            RegisteredMobileNumbers mobile = snapshot.getValue(RegisteredMobileNumbers.class);
                            mobileLists.add(mobile);
                        }

                        if (mobileLists.size() != 0) {
                            for (int i = 0; i < mobileLists.size(); i++) {
                                if (mobileLists.get(i).getMobile().equals(phone)) {
                                    if (mobileLists.get(i).getStatus().equals("login")) {
                                        intent.putExtra("userRegister", true);
                                        intent.putExtra("userLogin", true);
                                    } else {
                                        intent.putExtra("userRegister", true);
                                        intent.putExtra("userLogin", false);
                                    }
                                    break;
                                }
                                if (i == (mobileLists.size() - 1)) {
                                    intent.putExtra("userRegister", false);
                                    intent.putExtra("userLogin", false);
                                    break;
                                }
                            }
                        }
                    }
                    intent.putExtra("mobile", phone);
                    startActivity(intent);
                    finish();
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}