package com.ryca.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavedPost extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    int count ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_post);

        ImageView backbtn = findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });


        String userId = user.getUid();

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Saved").child(userId);

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> postUrls = new ArrayList<>();
                List<String> postKeys = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each post has an "imageUrl" field
                    String imageUrl = postSnapshot.child("postImage").getValue(String.class);
                    String postId = postSnapshot.child("PostId").getValue(String.class);
                    String userId = postSnapshot.child("userId").getValue(String.class);
                    String postKey = postSnapshot.getKey();
                    DatabaseReference postCheckRef = FirebaseDatabase.getInstance().getReference("Post").child(userId).child(postId);

                    postCheckRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot postCheckSnapshot) {

                            if (postCheckSnapshot.exists() && imageUrl != null) {
                                postUrls.add(imageUrl);
                                postKeys.add(postKey);
                                count++;
                                Log.d("Posttts" , "PostKey " + count + " : "+ postKey + "\n" + "ImageUrl " + count + " : " + imageUrl  );
                            } else {
                                // Post doesn't exist in "Post" node, remove it from "Saved"
                                postsRef.child(postKey).removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("Firebase", "Removed unmatched saved post: " + postId);
                                    } else {
                                        Log.e("Firebase", "Failed to remove unmatched saved post: " + postId, task.getException());
                                    }
                                });
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Failed to check post existence", databaseError.toException());
                        }
                    });
                }
//                Collections.reverse(postKeys);

                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        if (userSnapshot.exists()) {
                            // Retrieve user data
                            String profilePictureUrl = userSnapshot.child("Profile picture").getValue(String.class);
                            String username = userSnapshot.child("username").getValue(String.class);
                            String address = userSnapshot.child("Location").getValue(String.class);
                            String userIdd = userSnapshot.getKey();
                            RecyclerView photoGrid = findViewById(R.id.photoGridsaved);
                            photoGrid.setLayoutManager(new GridLayoutManager(SavedPost.this, 3)); // Adjust the span count as needed
//                            Collections.reverse(postUrls);
                            Collections.reverse(postUrls);
                            photoGrid.setAdapter(new ProfileGridAdapter(SavedPost.this, postUrls,
                                    profilePictureUrl,  username,  address, userId, postKeys, "", false, false));

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error, if any
                Toast.makeText(SavedPost.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}