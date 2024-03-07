package com.ryca.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

// if you can't figure what you have done check the last stored ryca backup in hard disk that is the check point
// this code is the logic for the saved single post with some bugs that doesn't let us to display post in single post find the issue and clear it



public class SinglePostView extends AppCompatActivity {

    private ArrayList<String> postKeyArray;
    private boolean loading = true;
    int counter;
    boolean isSaved;

    String userID;
    private List<SinglePostModel> SinglepostList;
    private SinglePostAdapter singlePostAdapter;
    FirebaseAuth mAuth;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String userId = currentUser.getUid();
    DatabaseReference savedReference = FirebaseDatabase.getInstance().getReference("Saved").child(userId);
    TextView toptext;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_view);

        SinglepostList = new ArrayList<>();
        singlePostAdapter = new SinglePostAdapter(SinglepostList);

        RecyclerView recyclerView = findViewById(R.id.singleviewpost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(singlePostAdapter);
        toptext = findViewById(R.id.topText);

        ImageView backbtn = findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });


        // Retrieve extras from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String userAddress = intent.getStringExtra("userAddress");
            String userProfileImage = intent.getStringExtra("userProfileImage");
            String userId = intent.getStringExtra("userId");
            String position = intent.getStringExtra("ClickedPosition");
            boolean fromFragment = intent.getBooleanExtra("FromFragment", false);
            boolean fromMenu = intent.getBooleanExtra("FromMenu", false);
            postKeyArray = intent.getStringArrayListExtra("PostKeyArray");

            Toast.makeText(this, "number" + position, Toast.LENGTH_SHORT).show();


            if (fromFragment) {

                toptext.setText("Exhibition");
                for (int i = 0; i < postKeyArray.size(); i++) {
                    String postKey = postKeyArray.get(i);

                    if (postKey.equals(String.valueOf(position))) {
                        counter = i;
                        Log.d("SinglePostVieww", "countinLoop :" + counter);
                        break;
                    }
                }


                Collections.reverse(postKeyArray);
                Log.d("SinglePostVieww", "count :" + counter);
                for (int i = counter; i < postKeyArray.size(); i++) {
                    String postKey = postKeyArray.get(i);

                    Log.d("SinglePostVieww", "PostKey " + i + ": " + postKey);
                    DisplaySinglePost(username, userAddress, userProfileImage, postKey, userId, fromMenu,false);

                }
            }

            if (!fromFragment) {

                toptext.setText("Saved Exhibits");
                for (int i = 0; i < postKeyArray.size(); i++) {
                    String postKey = postKeyArray.get(i);

                    if (postKey.equals(String.valueOf(position))) {
                        counter = i;
                        Log.d("SinglePostVieww", "countinLoop :" + counter);
                        break;
                    }
                }


                Collections.reverse(postKeyArray);
                Log.d("SinglePostVieww", "count :" + counter);
                for (int i = counter; i < postKeyArray.size(); i++) {
                    String postKey = postKeyArray.get(i);

                    savedReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userIDd = (String) snapshot.child("userId").getValue();

                            PassDataToDisplaySinglePost( userIDd, postKey,fromMenu);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

        }
    }



    private void PassDataToDisplaySinglePost(String userid, String postKey, boolean fromMenu) {

        if (userid != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Creators").child(userid);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Retrieve user data
                        String profilePictureUrl = snapshot.child("Profile picture").getValue(String.class);
                        String Username = snapshot.child("Shop Name").getValue(String.class);
                        String address = snapshot.child("Location").getValue(String.class);

                        String postKeySuffix2 = postKey.substring(14);
                        DisplaySinglePost(Username, address, profilePictureUrl, postKeySuffix2, userid, fromMenu,true);

                        //Log.d("checkkk" + "itismess" , Username+ address+ profilePictureUrl + " ~ " + postKeySuffix2 + " ~ " + userid);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }




    private void DisplaySinglePost(String username, String userAddress, String userProfileImage, String postKey, String userId, boolean fromMenu, boolean NavigationToProfile) {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Post").child(userId).child(postKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    checkThePostId(postKey, userId, new OnCheckPostIdCallback() {
                        @Override
                        public void onCallback(boolean isSaved) {
                            String postImageUrl = snapshot.child("imageURL").getValue(String.class);
                            String rating = snapshot.child("prodprice").getValue(String.class);
                            String category = snapshot.child("category").getValue(String.class);
                            String description = snapshot.child("imgdesc").getValue(String.class);

                            Log.d("TrueOrFalse", "statusBS :" + isSaved + " ~ " + postKey);
                            SinglePostModel Singlepost = new SinglePostModel(userProfileImage, username, userAddress, postImageUrl, rating, category, description, userId, postKey, isSaved, fromMenu,NavigationToProfile);

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


    public void checkThePostId(String PostId, String userId, OnCheckPostIdCallback callback) {
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



// old and normal method to set value of isSaved variable incase the above code shows any error try this but need to solve variable affect problem
//package com.ryca.Profile;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.ryca.R;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class SinglePostView extends AppCompatActivity {
//
//    private ArrayList<String> postKeyArray;
//    private boolean loading = true;
//    int counter;
//    boolean isSaved;
//    private List<SinglePostModel> SinglepostList;
//    private SinglePostAdapter singlePostAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_single_post_view);
//
//        SinglepostList = new ArrayList<>();
//        singlePostAdapter = new SinglePostAdapter(SinglepostList);
//
//        RecyclerView recyclerView = findViewById(R.id.singleviewpost);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setAdapter(singlePostAdapter);
//
//
//        // Retrieve extras from the Intent
//        Intent intent = getIntent();
//        if (intent != null) {
//            String username = intent.getStringExtra("username");
//            String userAddress = intent.getStringExtra("userAddress");
//            String userProfileImage = intent.getStringExtra("userProfileImage");
//            String userId = intent.getStringExtra("userId");
//            String position = intent.getStringExtra("ClickedPosition");
//            boolean fromFragment = intent.getBooleanExtra("FromFragment", false);
//            postKeyArray = intent.getStringArrayListExtra("PostKeyArray");
//
//            Toast.makeText(this, "number" + position, Toast.LENGTH_SHORT).show();
//
//
//
//            for (int i = 0; i < postKeyArray.size(); i++) {
//                String postKey = postKeyArray.get(i);
//
//                if (postKey.equals(String.valueOf(position))) {
//                    counter = i;
//                    Log.d("SinglePostVieww", "countinLoop :" + counter);
//                    break;
//                }
//            }
//
//
//            Collections.reverse(postKeyArray);
//            Log.d("SinglePostVieww", "count :" + counter);
//            for (int i = counter; i < postKeyArray.size(); i++) {
//                String postKey = postKeyArray.get(i);
//
//                    Log.d("SinglePostVieww", "PostKey " + i + ": " + postKey);
//                    DisplaySinglePost(username,userAddress,userProfileImage,postKey,userId);
//
//            }
//
//        }
//    }
//
//    private void DisplaySinglePost(String username, String userAddress, String userProfileImage, String postKey, String userId) {
//
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Post").child(userId).child(postKey);
//
//
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//
//                    Log.d("SinglePostViewww", "shots :"+ postKey);
//
//                    checkThePostId(postKey, userId);
//                    String postImageUrl = snapshot.child("imageURL").getValue(String.class);
//                    String rating = snapshot.child("prodprice").getValue(String.class);
//                    String category = snapshot.child("category").getValue(String.class);
//                    String description = snapshot.child("imgdesc").getValue(String.class);
//
//                    Log.d("TrueOrFalse", "statusBS :" + isSaved +  " ~ "  + postKey);
//                    SinglePostModel Singlepost = new SinglePostModel(userProfileImage, username, userAddress, postImageUrl, rating, category, description,userId, postKey, isSaved);
//
//                    SinglepostList.add(Singlepost);
//                    Log.d("SinglePostViewwww", "values :"+ userProfileImage+ " : " + username+ " : " +userAddress+" : " + postImageUrl+ " : " +rating+ category+ " : " +description+ " : " +userId+ " : " +postKey+ " : " +isSaved);
//                    singlePostAdapter.notifyDataSetChanged();
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void checkThePostId(String PostId, String userId) {
//
//        DatabaseReference savedReference = FirebaseDatabase.getInstance().getReference("Saved")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(PostId);
//
//        savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//
//                    // Toast.makeText(requireContext(), "position : " + userId + "~~" + PostId, Toast.LENGTH_SHORT).show();
//                    isSaved  = true;
//                    Log.d("TrueOrFalse", "True :" + dataSnapshot.getKey() );
//                }
//                else {
//
//                    isSaved = false;
//                    Log.d("TrueOrFalse", "False :" + dataSnapshot.getKey() );
//
//                }
//
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("Firebase", "Error checking save status: " + databaseError.getMessage());
//                // Default to false in case of error
//            }
//        });
//    }
//
//}
