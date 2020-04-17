package com.iknoortech.videochatdemo.offlineHelper;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iknoortech.videochatdemo.activity.LoginActivity;

import androidx.annotation.NonNull;
import io.fabric.sdk.android.Fabric;

import static com.iknoortech.videochatdemo.helper.AppConstant.onlineStatusTable;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.isUserLoggedOut;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserLoggedOut;

public class IknoorChat extends Application {

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        if (!isUserLoggedOut(this)) {
            if (mAuth.getCurrentUser() != null) {
                mUserDatabase = FirebaseDatabase.getInstance().getReference()
                        .child(onlineStatusTable).child(mAuth.getCurrentUser().getUid());

                mUserDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            mUserDatabase.child("status").onDisconnect().setValue(String.valueOf(System.currentTimeMillis()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                Toast.makeText(this, "Your session is expired, Please login again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                setUserLoggedOut(this, true);
            }
        }
    }
}
