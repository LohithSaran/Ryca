package com.ryca.MenuCodes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ryca.R;

public class AccountSettings extends AppCompatActivity {



    private RelativeLayout passwordFieldsLayout;
    private TextView changePasswordTextView;
    private boolean isPasswordFieldsVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // Initialize views
        passwordFieldsLayout = findViewById(R.id.passwordFieldsLayout);
        changePasswordTextView = findViewById(R.id.changePassword);

        ImageView showPassw = findViewById(R.id.showpassw);
        ImageView showPassww = findViewById(R.id.showpassww);
        ImageView showPasswww = findViewById(R.id.showpasswww);
        ImageView backbtn = findViewById(R.id.backbtn);

        EditText currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        ImageView changePasswordButton = findViewById(R.id.changePasswordButton);
        TextView accemailid = findViewById(R.id.accemailid);

// Get the current user from Firebase Authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Check if the user is signed in
        if (user != null) {
            // Get the email of the signed-in user
            String email = user.getEmail();

            // Set the email to the TextView
            accemailid.setText(email);
        } else {
            Toast.makeText(this, "Failed to load your account email id. Please try again later.", Toast.LENGTH_LONG).show();
        }

// Set OnClickListener for the changePasswordButton
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text entered by the user
                String currentPassword = currentPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                // Perform validation (e.g., check if new password matches confirm password)
                if (newPassword.length() < 6) {
                    // Show error message if the new password is less than 6 characters
                    Toast.makeText(AccountSettings.this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    // Show error message if new password and confirm password don't match
                    Toast.makeText(AccountSettings.this, "New password and confirm password don't match", Toast.LENGTH_LONG).show();
                    return;
                }

                // Use Firebase Authentication's updatePassword method to change the password
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password updated successfully
                                        Toast.makeText(AccountSettings.this, "Password updated successfully", Toast.LENGTH_LONG).show();
                                    } else {
                                        // Password update failed
                                        Toast.makeText(AccountSettings.this, "Failed to update password. Please try again later.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });


// Set OnClickListener for the show password icons
        showPassw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(currentPasswordEditText, showPassw);
            }
        });

        showPassww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(newPasswordEditText, showPassww);
            }
        });

        showPasswww.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(confirmPasswordEditText, showPasswww);
            }
        });

        // Set click listener for "Change Password" TextView
        changePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordFieldsVisibility();
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Method to toggle the visibility of password fields layout
    private void togglePasswordFieldsVisibility() {
        if (isPasswordFieldsVisible) {
            // If password fields are visible, hide them
            passwordFieldsLayout.setVisibility(View.GONE);
            isPasswordFieldsVisible = false;
        } else {
            // If password fields are not visible, show them
            passwordFieldsLayout.setVisibility(View.VISIBLE);
            isPasswordFieldsVisible = true;
        }
    }

    private void togglePasswordVisibility(EditText editText, ImageView showPasswordIcon) {
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showPasswordIcon.setImageResource(R.drawable.hidepsw);
        } else {
            // Hide the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showPasswordIcon.setImageResource(R.drawable.showpsw);
        }
        // Move cursor to the end of the text
        editText.setSelection(editText.getText().length());
    }

}