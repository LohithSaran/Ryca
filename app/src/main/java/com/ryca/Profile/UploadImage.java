package com.ryca.Profile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.ryca.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UploadImage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView uploadbtn, imgchoose;
    //    private TextView showuploadimg;
    private EditText imgdesc, prodprice, category;
    private ProgressBar progressBar;
    private ImageView imageView, backbtn;

    private Uri imageuri;

    private StorageReference storageRef;
    private DatabaseReference dbreference,creatorRef,userRef;
    private StorageTask mUploadTask;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private boolean isUploading = false;
    private ProgressDialog progressDialog;
    private static final int REQUEST_CODE_PERMISSION = 101;
    private Context context;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        uploadbtn = findViewById(R.id.uploadbtn);
        imgchoose = findViewById(R.id.imagechoose);
        prodprice = findViewById(R.id.price);
        category = findViewById(R.id.category);
        imgdesc = findViewById(R.id.description);
        progressBar = findViewById(R.id.progress);
        imageView = findViewById(R.id.imageview);
        backbtn = findViewById(R.id.backbtn);
        final boolean[] isInitialSelection = {true};

        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        dbreference = FirebaseDatabase.getInstance().getReference("Post");
        creatorRef = FirebaseDatabase.getInstance().getReference("Creators");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        Spinner categorySpinner = findViewById(R.id.spinner); // Replace R.id.category with the actual ID of your spinner

        loadCategories(categorySpinner);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);


        CheckDevice();

        category.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Open the spinner dropdown programmatically
                categorySpinner.performClick();
                return false;
            }
        });

        category.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Open the spinner dropdown programmatically
                    categorySpinner.performClick();
                }
            }
        });


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if it's the initial selection
                if (isInitialSelection[0]) {
                    isInitialSelection[0] = false;
                } else {
                    // Set the selected item in the categoryEditText
                    category.setText(parentView.getItemAtPosition(position).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        imgchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CheckDevice();
            }

        });


        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if an upload is already in progress
                if (isUploading) {
                    Toast.makeText(UploadImage.this, "Upload is in progress, please wait...", Toast.LENGTH_SHORT).show();
                } else {
                    // Set isUploading to true to indicate that an upload is starting
                    if (mUploadTask != null && mUploadTask.isInProgress()) {
                        Toast.makeText(UploadImage.this, "Upload is in progress", Toast.LENGTH_SHORT).show();
                        // Set isUploading to false to allow future uploads
                        isUploading = false;
                    } else {
                        uploadFile();
                    }
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });


    }

    public void CheckDevice() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            chooseImageBelow11();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            chooseImagee();
        }

    }

    private void chooseImagee() {
        // Check if permission is granted

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(UploadImage.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSION);
        }
        else {
            ActivityCompat.requestPermissions(UploadImage.this,new String[]{Manifest.permission.READ_MEDIA_IMAGES}
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker();
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(UploadImage.this);
                    builder.setTitle("Requesting permission");
                    builder.setMessage("We promise we don't misuse your data." + "\n" + "Your device will not allow to access photos to upload without you providing the permission, so allow permission by settings -> Permissions -> Photos and videos -> Allow.");
                    builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog or handle any other action
                            try {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                                startActivityIfNeeded(intent, 101);

                            } catch (Exception e) {

                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                startActivityIfNeeded(intent, 101);
                            }
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setCancelable(false); // Prevent the dialog from being dismissed by tapping outside
                    builder.show();

                } else {

                    chooseImagee();
                }
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                if (requestCode == REQUEST_CODE_PERMISSION) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        openImagePicker();
                    } else {
                        showPermissionInstructionsDialog();
                    }

                }
            }
    }

    private void showPermissionInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImage.this);
        builder.setTitle("Permission Required");
        builder.setMessage("We promise we don't misuse your data." + "\n" + "Your device will not allow to access photos to upload without you providing the permission, so allow permission by clicking settings -> Permissions -> Storage -> Allow.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open app settings to allow permission
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog or handle any other action
                dialog.dismiss();
            }
        });
        builder.setCancelable(false); // Prevent the dialog from being dismissed by tapping outside
        builder.show();
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
            // Launch the image cropping activity
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped"));

            UCrop.Options options = new UCrop.Options();


            options.setToolbarTitle("Crop Photo");
            //options.setToolbarWidgetColor(getResources().getColor(R.color.app_color));


            // Set the maximum width and height based on image orientation
            if (isPortraitOrientation(sourceUri)) {
                options.withMaxResultSize(1080, 1920); // Portrait
            } else {
                options.withMaxResultSize(1920, 1080); // Landscape
            }

            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .start(this);
        }

        // Handle result from UCrop activity
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                // Update the imageuri variable
                imageuri = resultUri;

                // Load the image using a Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(imageuri.getPath());
                imageView.setImageBitmap(bitmap);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);
            Toast.makeText(this, "Error cropping image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to check if the image is in portrait orientation
    private boolean isPortraitOrientation(Uri imageUri) {
        try {
            ExifInterface exif = new ExifInterface(imageUri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Default to false if unable to determine orientation
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



    private void uploadFile() {
        if (imageuri != null && !imgdesc.getText().toString().trim().isEmpty()
                && !prodprice.getText().toString().trim().isEmpty()
                && !category.getText().toString().trim().isEmpty())
        {
            isUploading = true;
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." +
                    getFileExtension(imageuri));
            progressDialog.show();

            mUploadTask = fileReference.putFile(imageuri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> progressBar.setProgress(0), 500);
                        progressDialog.dismiss();

                        Toast.makeText(UploadImage.this, "Upload successful", Toast.LENGTH_SHORT).show();

                        fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String postId = dbreference.push().getKey();

                            // Get the current date
                            String currentDate = getCurrentDate();

                            // Create an Upload object with the required data
                            Upload upload = new Upload(
                                    imgdesc.getText().toString().trim(),
                                    prodprice.getText().toString().trim(),
                                    category.getText().toString().trim(),
                                    downloadUri.toString(),
                                    currentDate
                            );

                            // Get the current user ID
                            if (user != null) {
                                String userId = user.getUid();

                                // Reference to the "Creators" field
                                DatabaseReference creatorsRef = creatorRef.child(userId).child("Category");

                                // Check if the category already exists (case-insensitive)
                                String newCategory = category.getText().toString().trim().toLowerCase();

                                creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        boolean categoryExists = false;

                                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                            String existingCategory = categorySnapshot.getValue(String.class);
                                            if (existingCategory != null && existingCategory.toLowerCase().equals(newCategory)) {
                                                // Category already exists, set the flag to true
                                                categoryExists = true;
                                                break;
                                            }
                                        }
//
//                                        if (!categoryExists) {
//                                            // Category does not exist, proceed with adding it
//                                            long categoryCounter = dataSnapshot.getChildrenCount();
//
//                                            // Increment the counter
//                                            categoryCounter++;
//                                            creatorsRef.child("category" + categoryCounter).setValue(category.getText().toString().trim());
//                                        }

                                        if (!categoryExists) {

                                            long categoryCounter = dataSnapshot.getChildrenCount();

                                            categoryCounter++;

                                            long timestamp = System.currentTimeMillis();

                                            String key = "category" + categoryCounter + "_" + timestamp;

                                            creatorsRef.child(key).setValue(category.getText().toString().trim());
                                        }


                                        // Update the database with the new Upload object
                                        dbreference.child(userId).child(postId).setValue(upload);

                                        updateUserPostCount();

                                        imageView.setImageResource(R.drawable.profimage); // Set your default image resource

                                        // Clear EditText fields
                                        imgdesc.getText().clear();
                                        imgdesc.clearFocus();
                                        prodprice.getText().clear();
                                        category.getText().clear();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle the error
                                        Toast.makeText(UploadImage.this, "Error checking category existence: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        isUploading = false;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UploadImage.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Additional error handling if needed
                        isUploading = false;
                        progressDialog.dismiss();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressBar.setProgress((int) progress);
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }



    private void loadCategories(Spinner categorySpinner) {

        String userId = user.getUid();

        DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference("Creators").child(userId).child("Category");
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadImage.this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(adapter);

        // Retrieve unique categories
        creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> uniqueCategories = new ArrayList<>();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String category = categorySnapshot.getValue(String.class);

                    if (category != null) {
                        uniqueCategories.add(category);
                    }
                }


                // Clear the existing categories list
                categories.clear();

                // Add the unique categories to the categories list
                categories.addAll(uniqueCategories);


                // Create an adapter with a custom layout for the selected item
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(UploadImage.this, android.R.layout.simple_spinner_item, categories) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        // Set an empty view for the selected item text
                        View view = super.getView(position, convertView, parent);
                        ((TextView) view.findViewById(android.R.id.text1)).setText("");
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        // Use the default dropdown layout for the dropdown view
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                // Specify the layout resource to use for the dropdown list
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Set the adapter to the spinner
                categorySpinner.setAdapter(adapter);

                // Set the default selected item to the first one in the list
                if (!categories.isEmpty()) {
                    categorySpinner.setSelection(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(UploadImage.this, "Error loading categories: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Helper method to get the current date
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }



    private void updateUserPostCount() {

        String userId = user.getUid();
        DatabaseReference usersRef = userRef.child(userId);

        // Retrieve the current post count
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the postCount value is a String
                Object postCountValue = dataSnapshot.child("No of post").getValue();
                if (postCountValue != null) {
                    if (postCountValue instanceof String) {
                        // Convert the String to a long
                        long currentPostCount = Long.parseLong((String) postCountValue);

                        // Increment the post count
                        currentPostCount++;

                        // Update the post count in the database
                        usersRef.child("No of post").setValue(currentPostCount);
                    } else if (postCountValue instanceof Long) {
                        // If the value is already a Long, just increment it
                        long currentPostCount = (long) postCountValue;
                        usersRef.child("No of post").setValue(currentPostCount + 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(UploadImage.this, "Error updating post count: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
