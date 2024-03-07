package com.ryca;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.ryca.Profile.SinglePostAdapter;
import com.ryca.Profile.SinglePostModel;

import java.util.ArrayList;
import java.util.List;

public class DeepLinkActivity extends AppCompatActivity {


    private List<SinglePostModel> SinglepostList;
    private SinglePostAdapter singlePostAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link); // Make sure you have a layout file named activity_deep_link.xml

        SinglepostList = new ArrayList<>();
        singlePostAdapter = new SinglePostAdapter(SinglepostList);

        RecyclerView recyclerView = findViewById(R.id.DeepLinkRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(singlePostAdapter);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        Uri deepLink = null;
                        if (data != null) {
                            deepLink = data.getLink();
                        }

                        if (deepLink != null) {
                            // Extract the postId and userId from the deep link
                            String postId = deepLink.getQueryParameter("postId");
                            String userId = deepLink.getQueryParameter("userId");
                            Log.d("DynamicLink", "Received deep link: " + deepLink.toString() + " userID :" + userId + " PostId" +postId);
                            Toast.makeText(DeepLinkActivity.this, "This :" + userId + "~" +postId, Toast.LENGTH_LONG).show();
                            // Use postId and userId to load and display the appropriate content
                            PassDataToDisplaySinglePost(userId, postId,false);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DeepLinkActivity", "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void PassDataToDisplaySinglePost(String userid, String postKey, boolean fromMenu) {


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Creators").child(userid);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Retrieve user data
                        String profilePictureUrl = snapshot.child("Profile picture").getValue(String.class);
                        String Username = snapshot.child("Shop Name").getValue(String.class);
                        String address = snapshot.child("Location").getValue(String.class);

                        DisplaySinglePost(Username, address, profilePictureUrl, postKey, userid, fromMenu);

                       // Log.d("checkkk" + "itismess" , Username+ address+ profilePictureUrl + " ~ " + postKeySuffix2 + " ~ " + userid);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


    }




    private void DisplaySinglePost(String username, String userAddress, String userProfileImage, String postKey, String userId, boolean fromMenu) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Post").child(userId).child(postKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    checkThePostId(postKey, userId, new DeepLinkActivity.OnCheckPostIdCallback() {
                        @Override
                        public void onCallback(boolean isSaved) {
                            String postImageUrl = snapshot.child("imageURL").getValue(String.class);
                            String rating = snapshot.child("prodprice").getValue(String.class);
                            String category = snapshot.child("category").getValue(String.class);
                            String description = snapshot.child("imgdesc").getValue(String.class);

                            Log.d("TrueOrFalse", "statusBS :" + isSaved + " ~ " + postKey);
                            SinglePostModel Singlepost = new SinglePostModel(userProfileImage, username, userAddress, postImageUrl, rating, category, description, userId, postKey, isSaved, fromMenu, true);

                            SinglepostList.add(Singlepost);
                            Log.d("SinglePostViewwww", "values :" + userProfileImage + " : " + username + " : " + userAddress + " : " + postImageUrl + " : " + rating + category + " : " + description + " : " + userId + " : " + postKey + " : " + isSaved);
                            singlePostAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }


    public interface OnCheckPostIdCallback {
        void onCallback(boolean isSaved);
    }


    public void checkThePostId(String PostId, String userId, DeepLinkActivity.OnCheckPostIdCallback callback) {
        DatabaseReference savedReference = FirebaseDatabase.getInstance().getReference("Saved")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isSaved = false;

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postKey = postSnapshot.getKey();

                    // Check if postKey is not null and has at least 20 characters
                    if (postKey != null && postKey.length() >= 20) {
                        String postKeySuffix = postKey.substring(14); // Get the last 20 characters omitting the first 14
                        String postIdSuffix = PostId.substring(14); // Get the last 20 characters omitting the first 14

                        // Check if the last 20 characters match
                        if (PostId.equals(postKeySuffix)) {
                            // Match found, set isSaved to true
                            isSaved = true;
                            break; // You can break if you only want to find the first match
                        } else {
                            isSaved = false;
                        }
                    }
                }

                callback.onCallback(isSaved);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking save status: " + databaseError.getMessage());
                // Default to false in case of error
                callback.onCallback(false);
            }
        });
    }

}
