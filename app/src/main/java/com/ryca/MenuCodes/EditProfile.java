package com.ryca.MenuCodes;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.ryca.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class EditProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView Updatebtn;
    private EditText shopdesc, shopname, shopaddress, shopcity;
    private TextView updatePhotoText;
    private ProgressBar progressBar;

    private ImageView profimage;
    private Uri imageUri;

    private StorageReference storageRef;
    private DatabaseReference dbReference,CreatorRef;
    private StorageTask<UploadTask.TaskSnapshot> mUploadTask;
    private String UserIdState;
    private static final int REQUEST_CODE_PERMISSION = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Updatebtn = findViewById(R.id.updatebtn);
        profimage = findViewById(R.id.updateshopprof);
        shopdesc = findViewById(R.id.updateshopdesc);
        shopaddress = findViewById(R.id.updateshopaddress);
        shopcity = findViewById(R.id.updateCity);
        shopname = findViewById(R.id.updateshopname);
        updatePhotoText = findViewById(R.id.textView14);
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbReference = FirebaseDatabase.getInstance().getReference("users");
        CreatorRef = FirebaseDatabase.getInstance().getReference("Creators");


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userId = currentUser.getUid();

        DatabaseReference userRef = dbReference.child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve existing values
                    String Creator = snapshot.child("creator").getValue(String.class);
                    UserIdState = Creator;
                    if ("0".equals(Creator)) {

                        shopdesc.setVisibility(View.GONE);
                        shopaddress.setVisibility(View.GONE);
                        shopcity.setVisibility(View.GONE);
                        shopname.setHint("Update you username");
                        updatePhotoText.setText("Update your profile picture");

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        Toast.makeText(this, "Update the field you want, Not all the fields are necessary.", Toast.LENGTH_LONG).show();

        profimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                    chooseImageBelow11();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    chooseImagee();
                }

            }
        });

        Updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

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
            String enteredShopAddress = shopaddress.getText().toString();
            String enteredShopCity = shopcity.getText().toString();

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
                        String existingShopAddress = dataSnapshot.child("Location").getValue(String.class);
                        String existingShopCity = dataSnapshot.child("City").getValue(String.class);

//                        if (TextUtils.isEmpty(enteredShopName) && TextUtils.isEmpty(enteredShopDesc) && imageUri == null) {
//                            // Display a toast or alert indicating that at least one field is necessary
//                            Toast.makeText(EditProfile.this, "At least one field is necessary.", Toast.LENGTH_SHORT).show();
//                            return; // Exit the method without proceeding further
//                        }

                        String FinalShopname = "";
                        String FinalShopdesc = "";
                        String FinalShopaddress = "";
                        String FinalShopcity = "";
                        // Check if the fields are empty and don't overwrite existing data
                        if (!TextUtils.isEmpty(enteredShopName) ) {
                            // Update username
                            userRef.child("username").setValue(enteredShopName);

                            if (UserIdState != null) {
                                if ("1".equals(UserIdState)) {
                                    creatorRef.child("Shop Name").setValue(enteredShopName);
                                }
                            }

                            FinalShopname = enteredShopName;
                        }

                        if (TextUtils.isEmpty(enteredShopName) ) {
                            // Update username
                            userRef.child("username").setValue(existingShopName);
                            if (UserIdState != null) {
                                if ("1".equals(UserIdState)) {
                                    creatorRef.child("Shop Name").setValue(existingShopName);
                                }
                            }
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
                            creatorRef.child("Shop Description").setValue(existingShopDesc);
                            FinalShopdesc = existingShopDesc;
                        }

                        if (!TextUtils.isEmpty(enteredShopAddress) ) {
                            // Update shop description
                            userRef.child("Location").setValue(enteredShopAddress);
                            creatorRef.child("Location").setValue(enteredShopAddress);
                            FinalShopaddress = enteredShopAddress;
                        }

                        if (TextUtils.isEmpty(enteredShopAddress) ) {
                            // Update shop description
                            userRef.child("Location").setValue(existingShopAddress);
                            creatorRef.child("Location").setValue(existingShopAddress);
                            FinalShopaddress = existingShopAddress;
                        }

                        if (!TextUtils.isEmpty(enteredShopCity) ) {
                            // Update shop description
                            userRef.child("City").setValue(enteredShopCity);
                            creatorRef.child("City").setValue(enteredShopCity);
                            FinalShopcity = enteredShopCity;
                        }

                        if (TextUtils.isEmpty(enteredShopCity) ) {
                            // Update shop description
                            userRef.child("City").setValue(existingShopCity);
                            creatorRef.child("City").setValue(existingShopCity);
                            FinalShopcity = existingShopCity;
                        }


                        if (imageUri != null) {
                            // Upload the image to Firebase Storage and update the profile picture field
                            uploadImage(userId, FinalShopname, FinalShopdesc, FinalShopaddress, FinalShopcity);
                        }

//                        if (imageUri == null) {
//                            // If Profile Picture field is empty, set user's Google account image
//                            setGoogleAccountImage();
//                        }

                        Toast.makeText(EditProfile.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
//
//                        // Navigate to ProfileFragment or any other desired activity
//                        Intent intent = new Intent(EditProfile.this, HomeActivity.class);
//                        intent.putExtra("fragment", "home");
//                        startActivity(intent);
                        finish();
                    } else {
                        // Handle the case where the user data doesn't exist in the database
                        Toast.makeText(EditProfile.this, "User data does not exist in the database.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors
                    Toast.makeText(EditProfile.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void chooseImagee() {
        // Check if permission is granted

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(EditProfile.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSION);
        }
        else {
            ActivityCompat.requestPermissions(EditProfile.this,new String[]{Manifest.permission.READ_MEDIA_IMAGES}
                    ,REQUEST_CODE_PERMISSION);
        }

    }

    private void chooseImageBelow11() {
        // Check if permission is granted

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION);
        } else {
            // Permission is already granted, proceed with your logic
            openImagePicker();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied, can't update profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied, can't update profile picture", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    private void openImagePicker() {

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

    private void uploadImage(String userId, String shopName, String shopDescription, String finalShopaddress, String finalShopcity) {
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
                                    userRef.child("Location").setValue(finalShopaddress);
                                    userRef.child("City").setValue(finalShopcity);
                                    creatorRef.child("Shop Description").setValue(shopDescription);
                                    creatorRef.child("Location").setValue(finalShopaddress);
                                    creatorRef.child("City").setValue(finalShopcity);
                                    if (UserIdState != null) {
                                        if ("1".equals(UserIdState)) {
                                            creatorRef.child("Shop Name").setValue(shopName);
                                            creatorRef.child("Profile picture").setValue(downloadUri.toString());

                                        }
                                    }
                                    // Additional actions after updating the user's data
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failed upload
                            Toast.makeText(EditProfile.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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