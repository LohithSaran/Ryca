package com.ryca.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinglePostView extends AppCompatActivity {

    private ArrayList<String> postKeyArray;
    private boolean loading = true;
    int counter;
    boolean isSaved;
    private List<SinglePostModel> SinglepostList;
    private SinglePostAdapter singlePostAdapter;

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


        // Retrieve extras from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            String username = intent.getStringExtra("username");
            String userAddress = intent.getStringExtra("userAddress");
            String userProfileImage = intent.getStringExtra("userProfileImage");
            String userId = intent.getStringExtra("userId");
            String position = intent.getStringExtra("ClickedPosition");
            boolean fromFragment = intent.getBooleanExtra("FromFragment", false);
            postKeyArray = intent.getStringArrayListExtra("PostKeyArray");

            Toast.makeText(this, "number" + position, Toast.LENGTH_SHORT).show();



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
                DisplaySinglePost(username,userAddress,userProfileImage,postKey,userId);

            }

        }
    }

    private void DisplaySinglePost(String username, String userAddress, String userProfileImage, String postKey, String userId) {

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
                            SinglePostModel Singlepost = new SinglePostModel(userProfileImage, username, userAddress, postImageUrl, rating, category, description, userId, postKey, isSaved);

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
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(PostId);

        savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isSaved = dataSnapshot.exists();
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
