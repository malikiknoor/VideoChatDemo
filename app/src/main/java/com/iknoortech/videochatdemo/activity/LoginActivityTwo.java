package com.iknoortech.videochatdemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.helper.AppConstant;

import static com.iknoortech.videochatdemo.helper.AppConstant.mobileTableName;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setFirebaseUserID;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setMobileNumber;
import static com.iknoortech.videochatdemo.helper.AppPrefrences.setUserLoggedOut;
import static com.iknoortech.videochatdemo.helper.AppUtils.convertVideoTime;
import static com.iknoortech.videochatdemo.helper.AppUtils.updateFirebaseToken;

public class LoginActivityTwo extends AppCompatActivity {

    private EditText edt_otp;
    private Button next;
    private TextView tv_notOtp, tv_alreadyLogin;
    private boolean isLogin = false, isRegister = false;
    private String phone;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    private FirebaseAuth auth;
    private ProgressDialog pd;
    private OtpTimer otpTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_two);
        auth = FirebaseAuth.getInstance();

        isLogin = getIntent().getBooleanExtra("userLogin", false);
        isRegister = getIntent().getBooleanExtra("userRegister", false);
        phone = getIntent().getStringExtra("mobile");

        edt_otp = findViewById(R.id.editText3);
        next = findViewById(R.id.button2);
        tv_notOtp = findViewById(R.id.textView6);
        tv_alreadyLogin = findViewById(R.id.textView8);
        tv_notOtp.setClickable(false);
        tv_notOtp.setEnabled(false);

        if (isLogin) {
            tv_alreadyLogin.setVisibility(View.VISIBLE);
        } else {
            tv_alreadyLogin.setVisibility(View.GONE);
        }

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        sendOtp();
        varifyMobile();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRegister) {
                    if (isLogin) {
                        pd.show();
                        String otp = edt_otp.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                        SigninWithPhone(credential);
                    } else {
                        if (edt_otp.getText().toString().isEmpty() || edt_otp.getText().length() < 6) {
                            Toast.makeText(LoginActivityTwo.this, "Please enter 6 digit otp", Toast.LENGTH_SHORT).show();
                        } else {
                            pd.show();
                            String otp = edt_otp.getText().toString();
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                            SigninWithPhone(credential);
                        }
                    }
                } else {
                    pd.show();
                    String otp = edt_otp.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                    SigninWithPhone(credential);
                }
            }
        });

        tv_notOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_notOtp.setClickable(false);
                tv_notOtp.setEnabled(false);
                pd.show();
                sendOtp();
                varifyMobile();
            }
        });
    }

    private void varifyMobile() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phone,                     // Phone number to verify
                2,                           // Timeout duration
                TimeUnit.MINUTES,                // Unit of timeout
                LoginActivityTwo.this,        // Activity (for callback binding)
                mCallback);
    }

    private void sendOtp() {
        auth = FirebaseAuth.getInstance();
        pd.dismiss();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//                Toast.makeText(LoginActivityTwo.this, "verification completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivityTwo.this, "OTP Sent failed, Please click on Retry.", Toast.LENGTH_SHORT).show();
                tv_notOtp.setClickable(true);
                tv_notOtp.setEnabled(true);
                tv_notOtp.setText("Retry");
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(LoginActivityTwo.this, "OTP has been sent on " + phone, Toast.LENGTH_SHORT).show();
                otpTimer = new OtpTimer(120000, 1000);
                otpTimer.start();
            }
        };
    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isRegister) {
                                loginUser();
                            } else {
                                pd.dismiss();
                                Intent intent = new Intent(LoginActivityTwo.this, CompleteProfileActivity.class);
                                intent.putExtra("mobile", phone);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            pd.dismiss();
                            Toast.makeText(LoginActivityTwo.this, "Incorrect OTP, Please enter correct otp", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(AppConstant.onlineStatusTable)
                .child(auth.getCurrentUser().getUid());
        HashMap hashMap = new HashMap();
        hashMap.put("status", "login");
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    setUserLoggedOut(LoginActivityTwo.this, false);
                    setMobileNumber(LoginActivityTwo.this, phone);
                    setFirebaseUserID(LoginActivityTwo.this, auth.getCurrentUser().getUid());
                    updateFirebaseToken(LoginActivityTwo.this);
                    updateMobileList();
                    Intent intent = new Intent(LoginActivityTwo.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivityTwo.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateMobileList() {
        final DatabaseReference mMobileReference = FirebaseDatabase.getInstance().getReference(mobileTableName)
                .child(auth.getCurrentUser().getUid());

        mMobileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mMobileReference.child("status").setValue("login");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class OtpTimer extends CountDownTimer {

        public OtpTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String time = convertVideoTime(millisUntilFinished);
            tv_notOtp.setText(time + " Mins");
        }

        @Override
        public void onFinish() {
            tv_notOtp.setClickable(true);
            tv_notOtp.setEnabled(true);
            tv_notOtp.setText("Did't get Otp ?");
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }
}
