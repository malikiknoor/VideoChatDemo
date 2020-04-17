package com.iknoortech.videochatdemo.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.activity.MainActivity;
import com.iknoortech.videochatdemo.activity.UserMessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String user = remoteMessage.getData().get("user");
        String sented = remoteMessage.getData().get("sented");
        Intent intent = new Intent(this, UserMessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = preferences.getString("currentUser", "none");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert sented != null;
        if (firebaseUser != null && sented.equals(firebaseUser.getUid())){
            if(!currentUser.equals(user)) {
                showNotification(remoteMessage);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String channelId = "channel-01";
        String channelName = "Iknoor Chat";

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = null;
        if(title.equalsIgnoreCase("New Message") || title.equalsIgnoreCase("Group")){
            intent = new Intent(this, UserMessageActivity.class);
        }else{
            intent = new Intent(this, MainActivity.class);
        }
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_chat)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.ic_chat))
                .setContentTitle(title)
                .setContentText(body);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        int i = 0;
        if(j > 0){
            i = j;
        }

        notificationManager.notify(i, mBuilder.build());
    }
}
