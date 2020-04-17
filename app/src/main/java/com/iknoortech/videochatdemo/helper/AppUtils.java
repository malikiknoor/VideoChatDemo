package com.iknoortech.videochatdemo.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.activity.ImageViewActivity;
import com.iknoortech.videochatdemo.activity.ProfileActivity;
import com.iknoortech.videochatdemo.notification.ApiInterface;
import com.iknoortech.videochatdemo.notification.Data;
import com.iknoortech.videochatdemo.notification.MyResponse;
import com.iknoortech.videochatdemo.notification.RetrofitClient;
import com.iknoortech.videochatdemo.notification.Sender;
import com.iknoortech.videochatdemo.notification.Token;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.iknoortech.videochatdemo.helper.AppConstant.BASE_URL;
import static com.iknoortech.videochatdemo.helper.AppConstant.onlineStatusTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileAboutTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupDescTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.groupNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileImageTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.profileNameTable;
import static com.iknoortech.videochatdemo.helper.AppConstant.tokenTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.uploadTableName;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.getFirebaseToken;

@SuppressLint("NewApi")
public class AppUtils {

    public static void userStatus(String status) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(onlineStatusTable).child(firebaseUser.getUid());
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUserName(String status) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(profileNameTable).child(firebaseUser.getUid());
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userName", status);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUserImage(String url) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(profileImageTable).child(firebaseUser.getUid());
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("imageUrl", url);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGroupName(String name, String groupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupNameTable).child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupName", name);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGroupDesc(String description, String groupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupDescTable).child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupDesc", description);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGroupImage(String url, String groupId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(groupImageTable).child(groupId);
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupImageUrl", url);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUserAbout(String status) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(profileAboutTable).child(firebaseUser.getUid());
        try {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userStatus", status);
            reference.updateChildren(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertVideoTime(long millis) {
        String videoTime, hourString, minutesString, secoundsString;
        long secoundsInMilli = 1000;
        long minutesInMilli = secoundsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;

        long hours = millis / hoursInMilli;
        millis = millis % hoursInMilli;

        long minutes = millis / minutesInMilli;
        millis = millis % minutesInMilli;

        long secounds = millis / secoundsInMilli;

        if (hours == 0) {
            if (minutes >= 0 && minutes <= 9) {
                minutesString = "0" + minutes;
            } else {
                minutesString = String.valueOf(minutes);
            }

            if (secounds >= 0 && secounds <= 9) {
                secoundsString = "0" + secounds;
            } else {
                secoundsString = String.valueOf(secounds);
            }

            videoTime = minutesString + ":" + secoundsString;
        } else {

            if (hours >= 0 && hours <= 9) {
                hourString = "0" + hours;
            } else {
                hourString = String.valueOf(hours);
            }

            if (minutes >= 0 && minutes <= 9) {
                minutesString = "0" + minutes;
            } else {
                minutesString = String.valueOf(minutes);
            }

            if (secounds >= 0 && secounds <= 9) {
                secoundsString = "0" + secounds;
            } else {
                secoundsString = String.valueOf(secounds);
            }

            videoTime = hourString + ":" + minutesString + ":" + secoundsString;
        }

        return videoTime;
    }

    public static void updateFirebaseToken(Context context) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(tokenTableName)
                .child(firebaseUser.getUid());
        HashMap hashMap = new HashMap();
        hashMap.put("token", getFirebaseToken(context));
        reference.updateChildren(hashMap);
    }

    public static String getMyPrettyDate(long neededTimeMilis) {
        Calendar nowTime = Calendar.getInstance();
        Calendar neededTime = Calendar.getInstance();
        neededTime.setTimeInMillis(neededTimeMilis);

        if ((neededTime.get(Calendar.YEAR) == nowTime.get(Calendar.YEAR))) {

            if ((neededTime.get(Calendar.MONTH) == nowTime.get(Calendar.MONTH))) {

                if (neededTime.get(Calendar.DATE) - nowTime.get(Calendar.DATE) == 1) {
                    //here return like "Tomorrow at 12:00 AM/PM"
                    return "Tomorrow at " + DateFormat.format("hh:mm aaa", neededTime);

                } else if (nowTime.get(Calendar.DATE) == neededTime.get(Calendar.DATE)) {
                    //here return like "Today at 12:00 AM/PM"
                    return "Today at " + DateFormat.format("hh:mm aaa", neededTime);

                } else if (nowTime.get(Calendar.DATE) - neededTime.get(Calendar.DATE) == 1) {
                    //here return like "Yesterday at 12:00 AM/PM"
                    return "Yesterday at " + DateFormat.format("hh:mm aaa", neededTime);

                } else {
                    //here return like "May 31, 12:00 AM/PM"
                    return DateFormat.format("MMMM d, hh:mm aaa", neededTime).toString();
                }

            } else {
                //here return like "May 31, 12:00 AM/PM"
                return DateFormat.format("MMMM d, hh:mm aaa", neededTime).toString();
            }

        } else {
            //here return like "May 31 2010, 12:00 AM/PM" - it's a different year we need to show it
            return DateFormat.format("MMMM dd yyyy, hh:mm aaa", neededTime).toString();
        }
    }

    public static String getMyPrettyOnlyDate(long neededTimeMilis) {
        Calendar nowTime = Calendar.getInstance();
        Calendar neededTime = Calendar.getInstance();
        neededTime.setTimeInMillis(neededTimeMilis);

        if ((neededTime.get(Calendar.YEAR) == nowTime.get(Calendar.YEAR))) {

            if ((neededTime.get(Calendar.MONTH) == nowTime.get(Calendar.MONTH))) {

                if (neededTime.get(Calendar.DATE) - nowTime.get(Calendar.DATE) == 1) {
                    //here return like "Tomorrow at 12:00 AM/PM"
                    return "Tomorrow";

                } else if (nowTime.get(Calendar.DATE) == neededTime.get(Calendar.DATE)) {
                    //here return like "Today at 12:00 AM/PM"
                    return "Today";

                } else if (nowTime.get(Calendar.DATE) - neededTime.get(Calendar.DATE) == 1) {
                    //here return like "Yesterday at 12:00 AM/PM"
                    return "Yesterday";

                } else {
                    //here return like "May 31, 12:00 AM/PM"
                    return DateFormat.format("MMMM dd yyyy", neededTime).toString();
                }

            } else {
                //here return like "May 31, 12:00 AM/PM"
                return DateFormat.format("MMMM dd yyyy", neededTime).toString();
            }

        } else {
            //here return like "May 31 2010, 12:00 AM/PM" - it's a different year we need to show it
            return DateFormat.format("MMMM dd yyyy", neededTime).toString();
        }
    }

    public static String getLastMsgDate(long neededTimeMilis) {
        Calendar nowTime = Calendar.getInstance();
        Calendar neededTime = Calendar.getInstance();
        neededTime.setTimeInMillis(neededTimeMilis);

        if ((neededTime.get(Calendar.YEAR) == nowTime.get(Calendar.YEAR))) {

            if ((neededTime.get(Calendar.MONTH) == nowTime.get(Calendar.MONTH))) {

                if (neededTime.get(Calendar.DATE) - nowTime.get(Calendar.DATE) == 1) {
                    //here return like "Tomorrow at 12:00 AM/PM"
                    return "Tomorrow";

                } else if (nowTime.get(Calendar.DATE) == neededTime.get(Calendar.DATE)) {
                    //here return like "Today at 12:00 AM/PM"
                    return ""+DateFormat.format("hh:mm aaa", neededTime);

                } else if (nowTime.get(Calendar.DATE) - neededTime.get(Calendar.DATE) == 1) {
                    //here return like "Yesterday at 12:00 AM/PM"
                    return "Yesterday";

                } else {
                    //here return like "May 31, 12:00 AM/PM"
                    return DateFormat.format("dd/MM/yy", neededTime).toString();
                }

            } else {
                //here return like "May 31, 12:00 AM/PM"
                return DateFormat.format("dd/MM/yy", neededTime).toString();
            }

        } else {
            //here return like "May 31 2010, 12:00 AM/PM" - it's a different year we need to show it
            return DateFormat.format("dd/MM/yy", neededTime).toString();
        }
    }

    public static void sendNotification(final String header, final String currentUserId, final String receiver, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(tokenTableName);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUserId, R.mipmap.ic_launcher, username +
                            ": " + msg, header,
                            receiver);
                    Sender sender = new Sender(data, token.getToken());
                    ApiInterface apiInterface = RetrofitClient.getClient(BASE_URL).create(ApiInterface.class);
                    apiInterface.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
//                                    Toast.makeText(UserMessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static boolean isConnectionAvailable(Context ctx) {
        ConnectivityManager mManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mManager.getActiveNetworkInfo();
        return (mNetworkInfo != null) && (mNetworkInfo.isConnected());
    }

    public static String getTimeAgo(long millis) {

        long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - millis);
        long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - millis);
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - millis);

        if (seconds < 60) {
            return String.valueOf(seconds) + " seconds ago";
        } else if (minutes == 1) {
            return String.valueOf(minutes) + " minute ago";
        } else if (minutes < 60) {
            return String.valueOf(minutes) + " minutes ago";
        } else if (hours == 1) {
            return String.valueOf(hours) + " hour ago";
        } else if (hours < 24) {
            return String.valueOf(hours) + " hours ago";
        } else if (days == 1) {
            return String.valueOf(days) + " day ago";
        } else if (days < 30) {
            return String.valueOf(days) + " days ago";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aaa");
            return formatter.format(new Date(millis));
        }
    }

    public static void uploadImageToServer(final Context context, Bitmap bitmap,
                                           final boolean isGroup, final String id) {
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = mImageStorage.child(uploadTableName)
                .child(id + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Uploading image...");
        pd.setCancelable(false);
        pd.show();

        final UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downUri = task.getResult();
                            if(isGroup){
                                updateGroupImage(downUri.toString(), id);
                            }else{
                                updateUserImage(downUri.toString());
                            }
                        }
                        pd.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void seeFullImage(Context context, CircleImageView myImage, String imageUrl, ImageView imageView){
        Intent intent = new Intent(context, ImageViewActivity.class);
        Pair[] pairs = new Pair[1];
        if(myImage == null){
            pairs[0] = new Pair<View, String>(imageView, "fullImage");
        }else{
            pairs[0] = new Pair<View, String>(myImage, "fullImage");
        }
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pairs);
        intent.putExtra("fullImage", imageUrl);
        context.startActivity(intent, options.toBundle());
    }

    public static Bitmap rotateImageIfRequired(Bitmap bitmap, String selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}