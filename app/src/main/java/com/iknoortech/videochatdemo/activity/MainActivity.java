package com.iknoortech.videochatdemo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.adapter.ViewPagerAdapter;
import com.iknoortech.videochatdemo.fragment.ChatFragment;
import com.iknoortech.videochatdemo.fragment.FriendRequestFragment;
import com.iknoortech.videochatdemo.fragment.FriendsFragment;
import com.iknoortech.videochatdemo.helper.AppConstant;
import com.iknoortech.videochatdemo.helper.AppPrefrences;
import com.iknoortech.videochatdemo.model.Chat;
import com.iknoortech.videochatdemo.model.TokenList;

import static com.iknoortech.videochatdemo.helper.AppConstant.chatTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.mobileTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.tokenTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.uploadTableName;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.getFirebaseToken;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserLoggedOut;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserName;
import static com.iknoortech.videochatdemo.helper.AppUtils.isConnectionAvailable;
import static com.iknoortech.videochatdemo.helper.AppUtils.userStatus;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView tv_noInternet;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private boolean isUserLoggedOut = false, onDoubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.maintoolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tv_noInternet = findViewById(R.id.tv_noInternet);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Your session is expired, Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            setUserLoggedOut(MainActivity.this, true);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    matchToken();
                }
            }, 100);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && type != null && "text/plain".equals(type)) {
            Toast.makeText(this, ""+intent.getStringExtra("android.intent.extra.TEXT"), Toast.LENGTH_SHORT).show();
        }

        startInternetCycle();
    }

    private void startInternetCycle(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isConnectionAvailable(MainActivity.this)){
                    tv_noInternet.setVisibility(View.GONE);
                }else{
                    tv_noInternet.setVisibility(View.VISIBLE);
                }
                startInternetCycle();
            }
        }, 1500);
    }

    private void matchToken() {
        reference = FirebaseDatabase.getInstance().getReference(tokenTableName).child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!isUserLoggedOut) {
                    TokenList tokenList = dataSnapshot.getValue(TokenList.class);
                    try {
                        if (!tokenList.getToken().equals(getFirebaseToken(MainActivity.this))) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setCancelable(false);
                            builder.setTitle("Already Login");
                            builder.setMessage("You have been logged in on another device");
                            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    finish();
                                    setUserLoggedOut(MainActivity.this, true);
                                }
                            });
                            builder.show();
                        } else {
                            getUserDetails();
                        }
                    }catch (Exception e){
                        matchToken();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserDetails() {
        if (firebaseUser == null) {
            Toast.makeText(this, "Your session is expired, Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            setUserLoggedOut(this, true);
        } else {
            reference = FirebaseDatabase.getInstance().getReference(chatTableName);
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.addFragment(new ChatFragment(), "Chat");
            viewPagerAdapter.addFragment(new FriendsFragment(), "Friend");
            viewPagerAdapter.addFragment(new FriendRequestFragment(), "Request");
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int unread = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                            unread++;
                        }
                    }

                    if (unread == 0) {
                        tabLayout.getTabAt(0).setText("Chat");
                    } else {
                        tabLayout.getTabAt(0).setText("(" + unread + ")Chat");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            reference = FirebaseDatabase.getInstance().getReference().child(AppConstant.profileNameTable)
                    .child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    setUserName(MainActivity.this, dataSnapshot.child("userName").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            reference = FirebaseDatabase.getInstance().getReference().child(AppConstant.profileImageTable)
                    .child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    AppPrefrences.setUserImage(MainActivity.this, dataSnapshot.child("imageUrl").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            reference = FirebaseDatabase.getInstance().getReference().child(AppConstant.friendRequestTableName)
                    .child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int unRespond = 0;
                    if (dataSnapshot != null) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            unRespond++;
                        }
                        if (unRespond != 0) {
                            tabLayout.getTabAt(2).setText("(" + unRespond + ")" + " Request");
                        } else {
                            tabLayout.getTabAt(2).setText("Request");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.logout:
                openLogoutDialog();
                return true;

            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;

            case R.id.blockList:
                startActivity(new Intent(this, BlockedUserActivity.class));
                return true;

//            case R.id.newGroup:
//                startActivity(new Intent(this, CreateGroupActivity.class));
//                return true;
        }

        return true;
    }

    private void openLogoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure, you want to logout ?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                userStatus("logout");
                updateMobileList();
                clearTokenFromDatabse();
                isUserLoggedOut = true;
                FirebaseAuth.getInstance().signOut();
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                startActivity(new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                setUserLoggedOut(MainActivity.this, true);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateMobileList() {
        final DatabaseReference mMobileReference = FirebaseDatabase.getInstance().getReference(mobileTableName)
                .child(firebaseUser.getUid());

        mMobileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mMobileReference.child("status").setValue("logout");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void clearTokenFromDatabse() {
        final DatabaseReference mTokenReference = FirebaseDatabase.getInstance().getReference(tokenTableName)
                .child(firebaseUser.getUid());
        mTokenReference.removeValue();
    }

    @Override
    public void onBackPressed() {
        if (onDoubleBackPressed) {
            finish();
            System.exit(0);
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Please Back again to exit", Toast.LENGTH_SHORT).show();
            onDoubleBackPressed = true;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onDoubleBackPressed = false;
            }
        }, 2000);
    }

    @Override
    protected void onStart() {
        userStatus("online");
        super.onStart();
    }

    @Override
    protected void onUserLeaveHint() {
        if (!isUserLoggedOut) {
            userStatus(String.valueOf(System.currentTimeMillis()));
        }
        super.onUserLeaveHint();
    }

    public void openSearch(View view) {
        startActivity(new Intent(this, SearchActivity.class));
    }
}
