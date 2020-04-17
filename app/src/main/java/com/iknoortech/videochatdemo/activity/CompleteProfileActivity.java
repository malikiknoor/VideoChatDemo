package com.iknoortech.videochatdemo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ObbInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.helper.AppUtils;

import static com.iknoortech.videochatdemo.helper.AppConstant.mobileTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.uploadTableName;
import static com.iknoortech.videochatdemo.helper.AppConstant.userTable;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setFirebaseUserID;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setMobileNumber;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserImage;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserLoggedOut;
import static com.iknoortech.videochatdemo.helper.AppUtils.rotateImageIfRequired;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateFirebaseToken;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateUserImage;
import static com.iknoortech.videochatdemo.helper.AppUtils.uploadImageToServer;
import static com.iknoortech.videochatdemo.helper.RealPathUtil.getPath;

public class CompleteProfileActivity extends AppCompatActivity {

    private CircleImageView userImage;
    private ImageView editImage;
    private EditText edt_userName, edt_userStatus;
    private Button btn_finish;
    private DatabaseReference mMobileReference;
    private DatabaseReference mUserReference;
    private FirebaseUser mAuth;
    private String mobile, imgUrl = "";
    private static final int IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        userImage = findViewById(R.id.circularImageView4);
        editImage = findViewById(R.id.imageView13);
        edt_userName = findViewById(R.id.editText6);
        edt_userStatus = findViewById(R.id.editText7);
        btn_finish = findViewById(R.id.button2);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        mobile = getIntent().getStringExtra("mobile");

        mMobileReference = FirebaseDatabase.getInstance().getReference(mobileTableName);
        mUserReference = FirebaseDatabase.getInstance().getReference(userTable).child(mAuth.getUid());

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_userName.getText().toString().isEmpty()) {
                    Toast.makeText(CompleteProfileActivity.this, "Please set your nick name", Toast.LENGTH_SHORT).show();
                } else {
                    if (edt_userStatus.getText().toString().isEmpty()) {
                        registerUser(edt_userName.getText().toString(), "Hi, I am using Iknoor Chat app");
                    } else {
                        registerUser(edt_userName.getText().toString(), edt_userStatus.getText().toString());
                    }
                }
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkExternalStoragePermission()) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), IMAGE_REQUEST);

                } else {
                    requestExternalStoragePermission();
                }
            }
        });

    }

    private void registerUser(String name, String userStatus) {
        HashMap<String, String> registerHash = new HashMap<>();
        registerHash.put("id", mAuth.getUid());
        registerHash.put("mobile", mobile);
        registerHash.put("accountCreatedDate", String.valueOf(System.currentTimeMillis()));
        AppUtils.userStatus("login");
        AppUtils.updateUserName(name);
        updateUserImage("default");
        AppUtils.updateUserAbout(userStatus);
        mUserReference.setValue(registerHash).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    enterMobile();
                } else {
                    Toast.makeText(CompleteProfileActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enterMobile() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("mobile", mobile);
        hashMap.put("status", "login");
        hashMap.put("uId", mAuth.getUid());
        mMobileReference.child(mAuth.getUid()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (bitmap == null) {
                        loginUser();
                    } else {
                        uploadImageToServer();
                    }
                } else {
                    Toast.makeText(CompleteProfileActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openUtilityDialog(this, "External Storage permission is required. Please allow this permission in App Settings.");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1011);
        }
    }

    private void openUtilityDialog(final Context ctx, final String messageID) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(ctx, R.style.Theme_AppCompat_Light);
        dialog.setMessage(messageID);
        dialog.setCancelable(false);
        dialog.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 0);
                } else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    openUtilityDialog(this, "You Have To Give Permission From Your Device Setting To go in Setting Please Click on Settings Button");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(imageStream);
            try {
                bitmap = rotateImageIfRequired(bitmap, getPath(this, selectedImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
            userImage.setImageBitmap(bitmap);
        }
    }

    private void loginUser() {
        updateFirebaseToken(CompleteProfileActivity.this);
        setUserLoggedOut(CompleteProfileActivity.this, false);
        setMobileNumber(CompleteProfileActivity.this, mobile);
        setUserImage(CompleteProfileActivity.this, imgUrl);
        setFirebaseUserID(CompleteProfileActivity.this, mAuth.getUid());
        Intent intent = new Intent(CompleteProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void uploadImageToServer() {
        StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = mImageStorage.child(uploadTableName)
                .child(mAuth.getUid() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();
        final ProgressDialog pd = new ProgressDialog(this);
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
                            imgUrl = downUri.toString();
                            updateUserImage(imgUrl);
                            loginUser();
                        }
                        pd.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(CompleteProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
