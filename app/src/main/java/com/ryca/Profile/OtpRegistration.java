package com.ryca.Profile;

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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ryca.R;

import java.util.concurrent.TimeUnit;


    public class OtpRegistration extends AppCompatActivity {

        private EditText otpEditText;
        private TextView resendOtpTextView;
        private String phoneNumber;
        private String verificationId;
        private FirebaseAuth firebaseAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_otp_registration);

            firebaseAuth = FirebaseAuth.getInstance();

            otpEditText = findViewById(R.id.otpverify);
            resendOtpTextView = findViewById(R.id.resendotp);

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
                        Toast.makeText(OtpRegistration.this, "Phone number is empty or not provided", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ImageView verifyOtpButton = findViewById(R.id.buttonVerifyOtp);

            verifyOtpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Perform OTP verification logic here
                    String enteredOtp = otpEditText.getText().toString();
                    if (!enteredOtp.isEmpty()) {
                        verifyCode(enteredOtp);
                    } else {
                        Toast.makeText(OtpRegistration.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(OtpRegistration.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("verification","Verification Failed: " + e.getMessage());

                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            verificationId = s;
                            Toast.makeText(OtpRegistration.this, "OTP Sent Successfully", Toast.LENGTH_LONG).show();
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
            // Replace this with your logic to redirect the user to another activity
            Intent intent = new Intent(OtpRegistration.this, CreatorProfileActivity.class);
            startActivity(intent);
            finish(); // Close the current activity if needed
        }

        private void updateUserData() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Get a reference to the user's node in the database
                String uid = user.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

                // Update the specific fields with the entered data
                userRef.child("Whatsapp number").setValue(phoneNumber);
                userRef.child("Location").setValue(getIntent().getStringExtra("storeAddress"));
                userRef.child("City").setValue(getIntent().getStringExtra("cityName"));
                userRef.child("Email ID").setValue(getIntent().getStringExtra("contEmail"));
                userRef.child("creator").setValue("1"); // Update creator field to 1


                // Update information under "Creators" field
                DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference("Creators").child(uid);
                creatorsRef.child("City").setValue(getIntent().getStringExtra("cityName"));
                creatorsRef.child("Location").setValue(getIntent().getStringExtra("storeAddress") + " " +
                        getIntent().getStringExtra("cityName"));
                creatorsRef.child("Shop Name").setValue(""); // Leave the value empty
                creatorsRef.child("Shop Description").setValue(""); // Leave the value empty
                creatorsRef.child("Category").setValue(""); // Leave the value empty


                // Continue with opening CreatorProfileActivity
                redirectToAnotherActivity();
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
