package com.ryca.Profile;

import android.os.Bundle;
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
                    String postKey = postSnapshot.getKey();


                    if (imageUrl != null) {
                        postUrls.add(imageUrl);
                    }
                    if (postKey != null) {
                        postKeys.add(postKey);
                    }
                }

                // Reverse the order of postUrls to display the newest posts first
                Collections.reverse(postUrls);

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
                            photoGrid.setAdapter(new ProfileGridAdapter(SavedPost.this, postUrls,
                                    profilePictureUrl,  username,  address, userId, postKeys, false, false));

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