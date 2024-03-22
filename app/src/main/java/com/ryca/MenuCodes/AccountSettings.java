package com.ryca.MenuCodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ryca.LoginActivity;
import com.ryca.R;

import java.io.File;

public class AccountSettings extends AppCompatActivity {



    private RelativeLayout passwordFieldsLayout;
    private TextView changePasswordTextView, deleteAccount;
    private boolean isPasswordFieldsVisible = false;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String currentUserId = currentUser.getUid();

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
        ImageView showfields = findViewById(R.id.imageView5);
        ImageView backbtn = findViewById(R.id.backbtn);

        EditText currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        TextView changePasswordButton = findViewById(R.id.changePasswordButton);
        TextView accemailid = findViewById(R.id.accemailid);
        deleteAccount = findViewById(R.id.deleteAccount);

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

        showfields.setOnClickListener(new View.OnClickListener() {
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

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (currentUser != null) {
                    reAuthPasswordDialogue();
                 } else {
                    Toast.makeText(AccountSettings.this, "Error deleting data please try again later", Toast.LENGTH_SHORT).show();
                }

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


    private void reAuthPasswordDialogue() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Reauthentication Required");
            builder.setMessage("Please enter your password to continue:");

            // Set up the input field for the password
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String password = input.getText().toString();
                    reauthenticateUser(currentUser, password);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User canceled reauthentication, do nothing or handle accordingly
                }
            });

            // Show the dialog
            builder.show();
        } else {
            // No user is currently signed in
            Log.e("Firebase", "No user is currently signed in.");
        }
    }

    // Method to reauthenticate with password
    private void reauthenticateUser(FirebaseUser user, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showDeleteAccountDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Reauthentication failed, handle the error
                        Log.e("Firebase", "Error reauthenticating user: " + e.getMessage());
                        // Display error message to the user (optional)
                        // You can show a toast or dialog to inform the user about the authentication failure
                    }
                });
    }



    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Account Deletion");
        builder.setMessage("Are you sure you want to permanently delete your account? All your data will be deleted and cannot be retrieved again.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                 checkUserRole(currentUserId);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                 dialog.dismiss();
             }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void checkUserRole(String currentUserId) {
        DatabaseReference userRef = mDatabase.child("users").child(currentUserId).child("creator");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String creatorValue = dataSnapshot.getValue(String.class);

                    if ("0".equals(creatorValue)) {
                        // User is a regular user
                        deleteRegularUserData(currentUserId);
                    } else if ("1".equals(creatorValue)) {
                        // User is a creator
                        deleteCreatorData();
                    } else {
                        // Handle unexpected creator value
                    }
                } else {
                    // Handle case where user data doesn't exist
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors fetching user data
            }
        });
    }


    private void deleteRegularUserData(String currentUserId) {

        DatabaseReference userRef = mDatabase.child("users").child(currentUserId);
        DatabaseReference connectRef = mDatabase.child("Connects").child(currentUserId);
        DatabaseReference InterRef = mDatabase.child("Interaction").child(currentUserId);
        DatabaseReference SavedRef = mDatabase.child("Saved").child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Check if "ProfilePicture" field exists for the current user
                    DataSnapshot profilePictureSnapshot = dataSnapshot.child("Profile picture");
                    if (profilePictureSnapshot.exists()) {
                        String profilePicturePath = profilePictureSnapshot.getValue(String.class);
                        // Remove profile picture from Firebase Storage if available
                        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicturePath);
                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Profile picture deleted successfully
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure to delete profile picture
                                }
                            });
                        }
                    }

                    // After deleting data and profile picture, you can delete the user node itself
                    userRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User data deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure to delete user data
                        }
                    });
                } else {
                    // Handle case where user data doesn't exist
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors fetching user data
            }
        });

        connectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    connectRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        InterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    InterRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        SavedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SavedRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Logout the user
                            logoutUser();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle errors
                            Log.e("Firebase", "Error deleting user: " + e.getMessage());
                        }
                    });
        } else {
            // No user is currently signed in
            Log.e("Firebase", "No user is currently signed in.");
        }
    }

    // Method to delete creator data
    private void deleteCreatorData() {

        DatabaseReference userRef = mDatabase.child("users").child(currentUserId);
        DatabaseReference connectRef = mDatabase.child("Connects").child(currentUserId);
        DatabaseReference InterRef = mDatabase.child("Interaction").child(currentUserId);
        DatabaseReference SavedRef = mDatabase.child("Saved").child(currentUserId);
        DatabaseReference CreatorRef = mDatabase.child("Creators").child(currentUserId);
        DatabaseReference postRef = mDatabase.child("Post").child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Check if "ProfilePicture" field exists for the current user
                    DataSnapshot profilePictureSnapshot = dataSnapshot.child("Profile picture");
                    if (profilePictureSnapshot.exists()) {
                        String profilePicturePath = profilePictureSnapshot.getValue(String.class);
                        // Remove profile picture from Firebase Storage if available
                        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicturePath);
                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Profile picture deleted successfully
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure to delete profile picture
                                }
                            });
                        }
                    }

                    // After deleting data and profile picture, you can delete the user node itself
                    userRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User data deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure to delete user data
                        }
                    });
                } else {
                    // Handle case where user data doesn't exist
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors fetching user data
            }
        });

        CreatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CreatorRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        connectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    connectRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        InterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    InterRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        SavedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SavedRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });



        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each post ID
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    // Delete image from Firebase Storage
                    DatabaseReference postImageRef = mDatabase.child("Post").child(currentUserId).child(postId).child("imageURL");
                    postImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String imageUrl = dataSnapshot.getValue(String.class);
                                // Delete image from Firebase Storage
                                if (imageUrl != null) {
                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                             Log.d("Firebase", "Image deleted successfully");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle failure to delete image
                                            Log.e("Firebase", "Error deleting image: " + e.getMessage());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors fetching image URL
                            Log.e("Firebase", "Error fetching image URL: " + databaseError.getMessage());
                        }
                    });

                }
                // Remove the currentUserId node from the Post field
                postRef.removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors fetching post data
                Log.e("Firebase", "Error fetching post data: " + databaseError.getMessage());
            }
        });





        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Logout the user
                            logoutUser();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle errors
                            Log.e("Firebase", "Error deleting user: " + e.getMessage());
                        }
                    });
        } else {
            // No user is currently signed in
            Log.e("Firebase", "No user is currently signed in.");
        }

    }







    private void logoutUser() {
        // Clear session data or perform any other necessary logout actions
        // For Firebase Authentication logout:
        FirebaseAuth.getInstance().signOut();

        // Delete all shared preferences files
        deleteAllSharedPreferenceFiles();

        // Clear cached data
        clearCachedData();

        // Clear app data (including databases, files, etc.)
        clearAppData();

        // After logout, navigate to the login screen
        Intent intent = new Intent(AccountSettings.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the current activity to prevent going back to it with back button
    }


    private void deleteAllSharedPreferenceFiles() {
        // Get the directory where shared preference files are stored
        File sharedPrefsDir = new File(getApplicationInfo().dataDir + "/shared_prefs");

        // Get a list of all shared preference files
        File[] sharedPrefsFiles = sharedPrefsDir.listFiles();

        // Delete each shared preference file
        if (sharedPrefsFiles != null) {
            for (File file : sharedPrefsFiles) {
                file.delete();
            }
        }
    }

    private void clearCachedData() {
        // Clear cache directory in internal storage
        deleteDirectory(getCacheDir());

        // Clear cache directory in external storage (if available)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDirectory(getExternalCacheDir());
        }
    }

    private void deleteDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    private void clearAppData() {
        // Clear app data (including databases, files, etc.)
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear com.ryca");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}