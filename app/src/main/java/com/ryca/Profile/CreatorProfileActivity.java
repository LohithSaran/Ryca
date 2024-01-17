package com.ryca.Profile;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ryca.HomeActivity;
import com.ryca.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class CreatorProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView finishbtn;
    private EditText shopdesc, shopname;
    private TextView skipfornow;
    private ProgressBar progressBar;

    private ImageView profimage;
    private Uri imageUri;

    private StorageReference storageRef;
    private DatabaseReference dbReference,CreatorRef;
    private StorageTask<UploadTask.TaskSnapshot> mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_profile);

        finishbtn = findViewById(R.id.btnfinish);
        profimage = findViewById(R.id.newshopprof);
        shopdesc = findViewById(R.id.shopdesc);
        shopname = findViewById(R.id.shopname);
//        skipfornow = findViewById(R.id.skipfornow);
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbReference = FirebaseDatabase.getInstance().getReference("users");
        CreatorRef = FirebaseDatabase.getInstance().getReference("Creators");

        profimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        finishbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

//        skipfornow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // If skipfornow is pressed, close the current activity and navigate to ProfileFragment
//                Intent intent = new Intent(CreatorProfileActivity.this, HomeActivity.class);
//                intent.putExtra("fragment", "profile");
//                startActivity(intent);
//                finish();
//            }
//        });
    }

    private void updateProfile() {
        // Retrieve the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get the user ID
            String userId = currentUser.getUid();

            // Retrieve entered data
            String enteredShopName = shopname.getText().toString();
            String enteredShopDesc = shopdesc.getText().toString();

            // Update the profile fields based on the entered data
            DatabaseReference userRef = dbReference.child(userId);
            DatabaseReference creatorRef = CreatorRef.child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve existing values
                        String existingShopName = dataSnapshot.child("username").getValue(String.class);
                        String existingShopDesc = dataSnapshot.child("shop description").getValue(String.class);

                        if (TextUtils.isEmpty(enteredShopName) && TextUtils.isEmpty(enteredShopDesc) && imageUri == null) {
                            // Display a toast or alert indicating that at least one field is necessary
                            Toast.makeText(CreatorProfileActivity.this, "At least one field is necessary.", Toast.LENGTH_SHORT).show();
                            return; // Exit the method without proceeding further
                        }

                        String FinalShopname = "";
                        String FinalShopdesc = "";

                        // Check if the fields are empty and don't overwrite existing data
                        if (!TextUtils.isEmpty(enteredShopName) ) {
                            // Update username
                            userRef.child("username").setValue(enteredShopName);
                            creatorRef.child("Shop Name").setValue(enteredShopName);
                            FinalShopname = enteredShopName;
                        }

                        if (TextUtils.isEmpty(enteredShopName) ) {
                            // Update username
                            userRef.child("username").setValue(existingShopName);
                            creatorRef.child("Shop Name").setValue(existingShopName);
                            FinalShopname = existingShopName;

                        }

                        if (!TextUtils.isEmpty(enteredShopDesc) ) {
                            // Update shop description
                            userRef.child("shop description").setValue(enteredShopDesc);
                            creatorRef.child("Shop Description").setValue(enteredShopDesc);
                            FinalShopdesc = enteredShopDesc;
                        }

                        if (TextUtils.isEmpty(enteredShopDesc) ) {
                            // Update shop description
                            userRef.child("shop description").setValue(existingShopDesc);
                            creatorRef.child("Shop Description").setValue(enteredShopDesc);
                            FinalShopdesc = existingShopDesc;
                        }

                        if (imageUri != null) {
                            // Upload the image to Firebase Storage and update the profile picture field
                            uploadImage(userId, FinalShopname, FinalShopdesc);
                        }

                        if (imageUri == null) {
                            // If Profile Picture field is empty, set user's Google account image
                            setGoogleAccountImage();
                        }

                        Toast.makeText(CreatorProfileActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to ProfileFragment or any other desired activity
                        Intent intent = new Intent(CreatorProfileActivity.this, HomeActivity.class);
                        intent.putExtra("fragment", "home");
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle the case where the user data doesn't exist in the database
                        Toast.makeText(CreatorProfileActivity.this, "User data does not exist in the database.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors
                    Toast.makeText(CreatorProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped"));

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1) // Set desired aspect ratio if needed
                    .start(this);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageUri = resultUri;
                // Display the cropped image in your ImageView
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                profimage.setImageBitmap(bitmap);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);
            Toast.makeText(this, "Error cropping image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(String userId, String shopName, String shopDescription) {
        if (imageUri != null) {
            // Generate a unique filename with a timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "ProfilePictures/" + userId + "_" + timestamp + "." + getFileExtension(imageUri);

            // Create a reference to the image file in Firebase Storage
            StorageReference fileReference = storageRef.child(fileName);

            // Upload the file to Firebase Storage
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful upload
                            // You can get the download URL of the uploaded image
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // Save the download URL to the database or perform any desired action
                                    DatabaseReference userRef = dbReference.child(userId);
                                    DatabaseReference creatorRef = CreatorRef.child(userId);
                                    userRef.child("Profile picture").setValue(downloadUri.toString());
                                    userRef.child("username").setValue(shopName);
                                    userRef.child("shop description").setValue(shopDescription);
                                    creatorRef.child("Shop Name").setValue(shopName);
                                    creatorRef.child("Shop Description").setValue(shopDescription);
                                    creatorRef.child("Profile picture").setValue(downloadUri.toString());

                                    // Additional actions after updating the user's data
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failed upload
                            Toast.makeText(CreatorProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void setGoogleAccountImage() {
        // Obtain the user's GoogleSignInAccount object
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userId = currentUser.getUid();

        DatabaseReference creatorRef = CreatorRef.child(userId);


        if (account != null) {
            Uri photoUrl = account.getPhotoUrl();
            if (photoUrl != null) {

                creatorRef.child("Profile picture").setValue(photoUrl.toString());

            }
        }
    }




    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        // Get the file extension from the mime type
        String extension = mime.getExtensionFromMimeType(cr.getType(uri));

        // If the extension is still null, provide a default extension
        if (extension == null || extension.isEmpty()) {
            extension = "jpg";  // Change this to the desired default extension
        }

        return extension;
    }


}
