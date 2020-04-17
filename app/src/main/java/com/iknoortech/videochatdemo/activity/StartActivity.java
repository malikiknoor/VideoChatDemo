package com.iknoortech.videochatdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import com.iknoortech.videochatdemo.R;
import io.fabric.sdk.android.services.common.SafeToast;

import static com.iknoortech.videochatdemo.helper.AppPrefrences.isUserLoggedOut;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        FirebaseApp.initializeApp(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUserLoggedOut(StartActivity.this)) {
                    startActivity(new Intent(StartActivity.this, LoginActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 1000);
    }
}
