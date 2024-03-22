package com.ryca.MenuCodes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

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
import com.ryca.R;

import java.util.List;

public class EditCategoryAdapter extends RecyclerView.Adapter<EditCategoryAdapter.ViewHolder> {

    private List<EditCategoryModel> dataList;
    private Context context;

    public EditCategoryAdapter(Context context, List<EditCategoryModel> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_edit_categoryitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EditCategoryModel data = dataList.get(position);
        holder.categoryNameTextView.setText(data.getCategoryName());

         holder.EditCategory.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 showEditPopupDialog(data);
             }
         });

         holder.DeleteCategory.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDeletePopupDialog(data);
             }
         });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        ImageView EditCategory, DeleteCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryName);
            EditCategory = itemView.findViewById(R.id.editCategoryicon);
            DeleteCategory = itemView.findViewById(R.id.deleteCategoryicon);
        }
    }

    private void showEditPopupDialog(EditCategoryModel position) {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Category " + "\"" + position.getCategoryName() + "\"" + " with ");

        // Create a LinearLayout to hold the views
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Initialize views in the dialog
        EditText editText = new EditText(context);
        Button saveButton = new Button(context);

        layout.addView(editText);
        builder.setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nextText = editText.getText().toString().trim();

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userId = currentUser.getUid();

                if (!nextText.isEmpty()) {
                    String currentCategoryName = position.getCategoryName();

                    // Update in Creators section
                    DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference("Creators")
                            .child(userId)
                            .child("Category");
                    creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Get the current value
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String compareValue = (String) ds.getValue();

                                // Updated to use equalsIgnoreCase for case-insensitive comparison
                                if (compareValue.equalsIgnoreCase(nextText)) {
                                    Toast.makeText(context, "The category " + nextText + " already exist, try different category name", Toast.LENGTH_LONG).show();
                                    break;
                                } else {
                                    // Use equalsIgnoreCase to check category name case-insensitively
                                    if (compareValue.equalsIgnoreCase(currentCategoryName)) {
                                        String keyToEdit = ds.getKey();

                                        creatorsRef.child(keyToEdit).setValue(nextText).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(context, "Your category " + nextText + " updated successfully!", Toast.LENGTH_SHORT).show();

                                                if (context instanceof Activity) {
                                                    Activity activity = (Activity) context;
                                                    Intent intent = new Intent(activity, EditCategory.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                    activity.overridePendingTransition(0, 0);
                                                    activity.finish();

                                                    activity.overridePendingTransition(0, 0);
                                                    activity.startActivity(intent);
                                                }

                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors if any
                        }
                    });

                    // Update in Post section
                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post")
                            .child(userId);

                    postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String postId = postSnapshot.getKey();
                                DatabaseReference categoryRef = postRef.child(postId).child("category");

                                // Check if the category matches the currentCategoryName
                                categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String postCategory = dataSnapshot.getValue(String.class);

                                        // Updated to use equalsIgnoreCase for case-insensitive comparison
                                        if (postCategory != null && postCategory.equalsIgnoreCase(currentCategoryName)) {
                                            // Update the category to nextText
                                            categoryRef.setValue(nextText);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle errors if any
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors if any
                        }
                    });

                    // Notify the adapter that the data has changed
                    notifyDataSetChanged();
                }

                dialog.dismiss();
            }
        });

        // Set negative button (Cancel)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog when the user clicks cancel
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void showDeletePopupDialog(EditCategoryModel categoryModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Category " + "\"" + categoryModel.getCategoryName() + "\"");
        builder.setMessage("Are you sure you want to delete all posts with this category?");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Count the number of posts with the specified category
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post").child(userId);
                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int postCount = 0;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String postCategory = postSnapshot.child("category").getValue(String.class);
                            if (postCategory != null && postCategory.equalsIgnoreCase(categoryModel.getCategoryName())) {
                                postCount++;
                            }
                        }

                        // Display a confirmation dialog with the post count
                        showConfirmDeleteDialog(categoryModel, postCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors if any
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showConfirmDeleteDialog(EditCategoryModel categoryModel, int postCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to permanently delete " + postCount + " posts?");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete category from Creators
                DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference("Creators").child(userId).child("Category");
                creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String compareValue = ds.getValue(String.class).trim();
                            if (compareValue != null && compareValue.equalsIgnoreCase(categoryModel.getCategoryName())) {
                                String keyToDelete = ds.getKey();
                                creatorsRef.child(keyToDelete).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Delete all posts with the specified category
                                        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Post").child(userId);
                                        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                    String postId = postSnapshot.getKey();
                                                    String postCategory = postSnapshot.child("category").getValue(String.class);
                                                    String imageUrl = postSnapshot.child("imageURL").getValue(String.class); // Retrieve the image URL


                                                    if (postCategory != null && postCategory.equalsIgnoreCase(categoryModel.getCategoryName())) {

                                                        if (imageUrl != null && !imageUrl.isEmpty()) {
                                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                                            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

                                                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Log.d("Storage", "Image successfully deleted");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("Storage", "Failed to delete image", e);
                                                                }
                                                            });
                                                        }

                                                        // Delete the post
                                                        postRef.child(postId).removeValue();
                                                    }
                                                }

                                                // Notify the user that posts have been deleted
                                                Toast.makeText(context, postCount + " exhibits deleted", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle errors if any
                                            }
                                        });

                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                                        userRef.child("No of post").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // Get the current value of No of post
                                                    int currentPostCount = dataSnapshot.getValue(Integer.class);

                                                    // Subtract postCount and update the value
                                                    int newPostCount = currentPostCount - postCount;
                                                    userRef.child("No of post").setValue(newPostCount);

                                                    // Notify the user that posts and category have been deleted
                                                    Toast.makeText(context, postCount + " exhibit and category deleted", Toast.LENGTH_SHORT).show();

                                                    if (context instanceof Activity) {
                                                        Activity activity = (Activity) context;
                                                        Intent intent = new Intent(activity, EditCategory.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                        activity.overridePendingTransition(0, 0);
                                                        activity.finish();

                                                        activity.overridePendingTransition(0, 0);
                                                        activity.startActivity(intent);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle errors if any
                                            }
                                        });
                                    }
                                });
                                // Break after finding and deleting the category to avoid unnecessary iterations
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors if any
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
