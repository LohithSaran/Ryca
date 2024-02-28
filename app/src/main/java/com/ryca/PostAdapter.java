package com.ryca;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.Fragments.CreatorsShowroom;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private static List<PostModel> posts;
    private List<String> postIds;

    public void setData(List<String> newPostIds) {
        this.postIds = newPostIds;
    }

    public PostAdapter(List<PostModel> posts) {
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
        PostModel post = posts.get(position);

        if (post.isSaved()) {
            holder.savedImageView.setImageResource(R.drawable.saved);
        } else {
            holder.savedImageView.setImageResource(R.drawable.save);
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

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView, postImageView;
        TextView usernameTextView;
        TextView addressTextView, rate, category, description, interaction1, interaction2;
        ImageView savedImageView;
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
            interaction1 = itemView.findViewById(R.id.interaction1);
            interaction2 = itemView.findViewById(R.id.interaction2);

            usernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get the post at the clicked position
                    PostModel clickedPost = posts.get(getAdapterPosition());

                    // Get the user ID of the post
                    String userID = clickedPost.getUserId();

                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("fragment", "creatorsShowroom");

                    CreatorsShowroom creatorsShowroomFragment = new CreatorsShowroom();
                    creatorsShowroomFragment.setArguments(bundle);

                    // Use itemView as the context to get the FragmentManager
                    FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();

                    // Add the transaction to the back stack before committing
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayout, creatorsShowroomFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });

            profilePictureImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    // Get the post at the clicked position
                    PostModel clickedPost = posts.get(getAdapterPosition());

                    // Get the user ID of the post
                    String userID = clickedPost.getUserId();

                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("fragment", "creatorsShowroom");

                    CreatorsShowroom creatorsShowroomFragment = new CreatorsShowroom();
                    creatorsShowroomFragment.setArguments(bundle);

                    // Use itemView as the context to get the FragmentManager
                    FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();

                    // Add the transaction to the back stack before committing
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayout, creatorsShowroomFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });


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
                    PostModel clickedPost = posts.get(getAdapterPosition());

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

        }




        private void handleSaveAction(int position) {
            // Get the post at the clicked position
            PostModel clickedPost = posts.get(position);

            // Perform the save action here, e.g., update the database
            savePostToFirebase(clickedPost);
        }


        private void savePostToFirebase(PostModel post) {
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

