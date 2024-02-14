package com.ryca.Profile;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ryca.ImageViewActivity;
import com.ryca.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SinglePostAdapter extends RecyclerView.Adapter<SinglePostAdapter.ViewHolder> {
    private  List<SinglePostModel> posts;

    public SinglePostAdapter(List<SinglePostModel> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to views here
        SinglePostModel post = posts.get(position);

        if (post.isSaved()) {
            holder.savedImageView.setImageResource(R.drawable.saved);
            Log.d("TrueOrFalse", "TrueFAdapt :" + post.getPostId() );
            // Check this in the log as the post is visible it gets the id on the log it may help in interaction

        } else {
            holder.savedImageView.setImageResource(R.drawable.save);
            Log.d("TrueOrFalse", "FalseFAdapt :" + post.getPostId() );

        }

        Picasso.get()
                .load(post.getPostImageUrl())
                .fit()
                .centerCrop(Gravity.TOP)
                .into(holder.postImageView);
        // Example: Set profile picture using an image loading library like Glide
        Picasso.get()
                .load(post.getProfilePictureUrl())
                .fit()
                .centerCrop(Gravity.TOP)
                .into(holder.profilePictureImageView);

        //      Set other data similarly
        holder.usernameTextView.setText(post.getUsername());
        holder.addressTextView.setText(post.getAddress());
        holder.rate.setText("₹ " + post.getRating());
        holder.category.setText(post.getCategory());
        holder.description.setText(post.getDescription());
        // Inside onBindViewHolder method


    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView, postImageView;
        TextView usernameTextView;
        TextView addressTextView, rate, category, description, interaction1,interaction2;
        ImageView savedImageView, Menu;
        // Add other views

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImageView = itemView.findViewById(R.id.profilepicturedp);
            usernameTextView = itemView.findViewById(R.id.creatorsnamedp);
            addressTextView = itemView.findViewById(R.id.creatorsaddressdp);
            rate = itemView.findViewById(R.id.ratedp);
            category = itemView.findViewById(R.id.categorydp);
            description = itemView.findViewById(R.id.descriptiondp);
            postImageView = itemView.findViewById(R.id.dppost);
            savedImageView = itemView.findViewById(R.id.savedp);
            Menu = itemView.findViewById(R.id.dpmenu);


            // Set a click listener for the savedImageView
            savedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Handle save action here
                        handleSaveAction(position);
                    }
                }
            });


            postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the post at the clicked position
                    SinglePostModel clickedPost = posts.get(getAdapterPosition());

                    // Get the image URL of the post
                    String imageUrl = clickedPost.getPostImageUrl();

                    // Create an intent to start ImageViewActivity
                    Intent intent = new Intent(itemView.getContext(), ImageViewActivity.class);

                    // Put the image URL in the intent
                    intent.putExtra("IMAGE_URL", imageUrl);

                    // Start the activity
                    itemView.getContext().startActivity(intent);
                }
            });


            Menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SinglePostModel clickedPost = posts.get(getAdapterPosition());

                    if (clickedPost.isMenu()){
                        profileMenu(v);
                    }else {
                        ExhibitorsMenu(v);
                    }
                }
            });

        }





        private void profileMenu(View v){
            Context context = v.getContext(); // Use itemView.getContext() instead of Context.
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.profile_exhibits_menu, popupMenu.getMenu());

            // Set click listener for menu items
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (item.getItemId() == R.id.editExhibit) {
                        SinglePostModel clickedPost = posts.get(getAdapterPosition());
                        String price = clickedPost.getRating(); // Replace with method to get price
                        String description = clickedPost.getDescription(); // Replace with method to get description
                        String category = clickedPost.getCategory();
                        String postId = clickedPost.getPostId();
                        String userId = clickedPost.getUserId();
                        showEditProfilePopup(v, price, description, category, postId, userId );
                        return true;
                    }
                    if (item.getItemId() == R.id.deleteExhibit) {
                        SinglePostModel clickedPost = posts.get(getAdapterPosition());
                        String postId = clickedPost.getPostId();
                        handleDeleteAction(postId, v);
                    }
                    return true;
                }
            });

            // Show the popup menu
            popupMenu.show();
        }


        // the edit logic Alert box is shrunk
        private void showEditProfilePopup(View anchorView, String price, String description, String currentCategory, String postId, String userID) {
            // Inflate the layout for the popup menu
            LayoutInflater inflater = LayoutInflater.from(anchorView.getContext());
            View popupView = inflater.inflate(R.layout.edit_exhibit_profile, null);

            // Get references to EditText and Spinner
            EditText editText1 = popupView.findViewById(R.id.editPrice);
            EditText editText2 = popupView.findViewById(R.id.editDesc);
            Spinner spinner = popupView.findViewById(R.id.categorySpinner);

            // Set text for EditText fields
            editText1.setText(price);
            editText2.setText(description);

            // Fetch data for the Spinner from Firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference()
                        .child("Creators")
                        .child(userId)
                        .child("Category");
                categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> categories = new ArrayList<>();
                        int selectedPosition = 0; // Variable to store the position of the current category
                        int position = 0; // Variable to track the position while iterating
                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            String category = categorySnapshot.getValue(String.class);
                            if (category != null) {
                                categories.add(category);
                                // Check if the current category matches the selected category
                                if (category.equals(currentCategory)) {
                                    selectedPosition = position;
                                }
                                position++;
                            }
                        }
                        // Create an ArrayAdapter using the categories list
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(anchorView.getContext(), android.R.layout.simple_spinner_item, categories);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        // Set the adapter to the spinner
                        spinner.setAdapter(adapter);
                        // Set the selection to the position of the current category
                        spinner.setSelection(selectedPosition);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Log.e(TAG, "Failed to read category data.", databaseError.toException());
                    }
                });
            }

            // Create a dialog to display the popup menu
            AlertDialog.Builder builder = new AlertDialog.Builder(anchorView.getContext());
            builder.setView(popupView);

            // Add button listeners
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Retrieve input from EditText fields and Spinner
                    String updatePrice = editText1.getText().toString();
                    String updateDesc = editText2.getText().toString();
                    String selectedCategory = spinner.getSelectedItem().toString();

                    // Update data in Firebase

                    updateDataInFirebase(userID, postId, updatePrice, updateDesc, selectedCategory, anchorView);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle cancel button click
                    dialog.dismiss();
                }
            });

            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void updateDataInFirebase(String userId, String postId, String updatePrice, String updateDesc, String selectedCategory, View anchorView) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                    .child("Post")
                    .child(userId)
                    .child(postId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("prodprice", updatePrice);
            updates.put("imgdesc", updateDesc);
            updates.put("category", selectedCategory);
            postRef.updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Data updated successfully
                            Context context = anchorView.getContext();
                            Toast.makeText(context, "Changes updated successfully, reload this page to see changes.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            Context context = anchorView.getContext();
                            Log.e(TAG, "Failed to update data.", e);
                            Toast.makeText(context, "Failed to update data, Please try again later", Toast.LENGTH_LONG).show();
                        }
                    });
        }


        // the delete logic Alert box is shrunk
        private void handleDeleteAction(String postId, View v) {
            Context context = v.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Deletion Confirmation");
            builder.setMessage("Are you sure, you want to permanently delete this exhibit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Yes button, proceed with deletion
                            deletePost(postId);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog, do nothing
                        }
                    });
            // Create the AlertDialog object and show it
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void deletePost(String postId) {
            // The rest of the deletion logic goes here
            // Get the reference to the current user's posts in the database
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                        .child("Post")
                        .child(userId)
                        .child(postId);

                // Add a ValueEventListener to retrieve the imageURL from the database
                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get the imageURL
                        String imageURL = dataSnapshot.child("imageURL").getValue(String.class);

                        // Delete the post data
                        postRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Post data deleted successfully
                                        // Now delete the image from Firebase Storage
                                        deleteImageFromStorage(imageURL);

                                        // Decrease post count after deleting the post
                                        decreasePostCount(userId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to delete post data
                                        Log.e(TAG, "Failed to delete post data: " + e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Log.e(TAG, "Failed to read post data: " + databaseError.getMessage());
                    }
                });
            }
        }

        private void deleteImageFromStorage(String imageURL) {
            // Get reference to the image file in Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

            // Delete the file
            storageRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image deleted successfully
                            // Handle any additional actions, if needed
                            Log.d(TAG, "Image deleted successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete image
                            Log.e(TAG, "Failed to delete image: " + e.getMessage());
                        }
                    });
        }

        private void decreasePostCount(String userId) {
            // Get reference to the user's post count in the database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("No of post");

            // Read the current post count value and decrement it by 1
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long currentPostCount = dataSnapshot.getValue(Long.class);
                    if (currentPostCount != null) {
                        // Decrement the post count by 1
                        long newPostCount = currentPostCount - 1;

                        // Update the post count in the database
                        userRef.setValue(newPostCount)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Post count updated successfully
                                        Log.d(TAG, "Post count decreased successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to update post count
                                        Log.e(TAG, "Failed to decrease post count: " + e.getMessage());
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                    Log.e(TAG, "Failed to read post count: " + databaseError.getMessage());
                }
            });
        }



        
        private void ExhibitorsMenu(View v){
            Context context = v.getContext(); // Use itemView.getContext() instead of Context.
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.exhibitors_exhibits_menu, popupMenu.getMenu());

            // Set click listener for menu items
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (item.getItemId() == R.id.reportExhibit) {

                    }
                    if (item.getItemId() == R.id.blockExhibitor) {

                    }
                    return true;
                }
            });

            // Show the popup menu
            popupMenu.show();
        }


        private void handleSaveAction(int position) {
            // Get the post at the clicked position
            SinglePostModel clickedPost = posts.get(position);

            // Perform the save action here, e.g., update the database
            savePostToFirebase(clickedPost);
        }


        private void savePostToFirebase(SinglePostModel post) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                String PostUser = post.getUserId();

                // Assuming postID is a unique identifier for each post
                String postID = post.getPostId();
                boolean saveCheck = post.isSaved();

                String timestamp = String.valueOf(System.currentTimeMillis());
                String combinedKey = timestamp + "_" + postID;


                DatabaseReference savedReference = FirebaseDatabase.getInstance()
                        .getReference("Saved")
                        .child(userId);

                if (!saveCheck) {


                    savedReference.child(combinedKey).setValue(new HashMap<String, Object>() {{
                        put("userId", PostUser);
                        put("PostId", postID);
                        put("postImage", post.getPostImageUrl());
                    }}, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                // Firebase operation successful, update the UI


                                savedImageView.setImageResource(R.drawable.saved);
                                // Update the isSaved field in the post model
                                post.setSaved(true);

                            } else {
                                Log.e("Firebase", "Error updating save status: " + error.getMessage());
                            }
                        }
                    });
                    // ...
                } else {

                    savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String snapshotKey = childSnapshot.getKey();
                                if (snapshotKey != null && snapshotKey.length() >= 20) {
                                    String last20Chars = snapshotKey.substring(snapshotKey.length() - 20);
                                    if (last20Chars.equals(postID)) {
                                        // Perform remove operation
                                        Log.d("Suffix", "Removing value with key: " + snapshotKey + " tof: " + post.isSaved());
                                        savedReference.child(snapshotKey).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error == null) {
                                                    // Firebase operation successful, update the UI
                                                    savedImageView.setImageResource(R.drawable.save);
                                                    // Update the isSaved field in the post model
                                                    post.setSaved(false);
                                                    Log.d("Suffix", "Removing value with key: " + snapshotKey + " tof: " + post.isSaved());
                                                } else {
                                                    Log.e("Firebase", "Error updating save status: " + error.getMessage());
                                                }
                                            }
                                        });
                                        break; // Stop iterating after finding the matching key
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle cancellation if needed
                        }
                    });
                }
            }
        }

    }
}
