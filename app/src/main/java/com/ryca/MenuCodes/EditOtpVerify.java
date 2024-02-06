package com.ryca.MenuCodes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ryca.R;

import java.util.concurrent.TimeUnit;

public class EditOtpVerify extends AppCompatActivity {

    private EditText otpEditText;
    private TextView resendOtpTextView;
    private String phoneNumber;
    private String verificationId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_otp_verify);

        firebaseAuth = FirebaseAuth.getInstance();

        otpEditText = findViewById(R.id.updateotpverify);
        resendOtpTextView = findViewById(R.id.updateresendotp);

        // Extract phone number passed from the previous activity
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sendVerificationCode(phoneNumber);
        } else {
            // Handle the case where phoneNumber is not provided
            Toast.makeText(this, "Phone number is empty or not provided", Toast.LENGTH_SHORT).show();
        }

        resendOtpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check again if phoneNumber is not empty before resending
                if (phoneNumber != null && !phoneNumber.isEmpty()) {

                    sendVerificationCode(phoneNumber);
                } else {
                    Toast.makeText(EditOtpVerify.this, "Phone number is empty or not provided", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView verifyOtpButton = findViewById(R.id.updatebuttonVerifyOtp);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform OTP verification logic here
                String enteredOtp = otpEditText.getText().toString();
                if (!enteredOtp.isEmpty()) {
                    verifyCode(enteredOtp);
                } else {
                    Toast.makeText(EditOtpVerify.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                90,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // If the automatic verification is successful, you can handle it here
                        // For manual entry, we won't reach here
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(EditOtpVerify.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("verification","Verification Failed: " + e.getMessage());

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        Toast.makeText(EditOtpVerify.this, "OTP Sent Successfully", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // Redirect the user to another activity after successful verification
        updateUserData();
    }

    private void redirectToAnotherActivity() {
        finish(); // Close the current activity if needed
    }

    private void updateUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get a reference to the user's node in the database
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            // Update the specific fields with the entered data
            userRef.child("Whatsapp number").setValue(phoneNumber).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(EditOtpVerify.this, "Your whatsapp number " +phoneNumber+ " has been updated successfully!", Toast.LENGTH_SHORT).show();

                }
            });

            redirectToAnotherActivity();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }
}
