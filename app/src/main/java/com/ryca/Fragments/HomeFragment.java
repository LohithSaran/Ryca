
package com.ryca.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.PostAdapter;
import com.ryca.PostModel;
import com.ryca.Profile.SavedPost;
import com.ryca.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FusedLocationProviderClient fusedLocationClient;
    private String currentAddress;
    private String currentFollowingUserId;
    private int currentIndex = 0; // Keep track of the current index in the followingUserIds list
    private static final int POSTS_TO_LOAD = 20;
    private int currentPage = 1;
    private static final int POSTS_PER_PAGE = 20;

    private Map<String, Integer> userPostIndices = new HashMap<>();


    private int currentUserIndex = 0;
    private List<String> followingUserIds;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String followingUserId;

    boolean isSaved;
    boolean categoryExists = false;


    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check and request location permissions at runtime
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, get the user's location
            getCurrentLocation();
        } else {
            // If permission is not granted, request it
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.postRecycler);

        recyclerView = view.findViewById(R.id.postRecycler); // Make sure to replace with your actual RecyclerView ID
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);

        ImageView savedImage = view.findViewById(R.id.savedposthp);

        savedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), SavedPost.class);
                startActivity(intent);
            }
        });

        // Call method to retrieve data from Firebase
        fetchFollowingUserIds();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if the user has scrolled to the bottom
                if (!recyclerView.canScrollVertically(1)) {
                    Toast.makeText(requireContext(), "avlo dha pa mudinchi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }


    private void fetchFollowingUserIds() {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference("Connects").child(userId).child("Following");

        followingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingUserIds = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String followingUserId = snapshot.getValue(String.class);
                    followingUserIds.add(followingUserId);
                }
                GetUserAndPostId();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("Firebase", "Error fetching following user IDs: " + databaseError.getMessage());
            }
        });


    }


    private void GetUserAndPostId() {
        // Check if there are any users to process
        if (followingUserIds.isEmpty()) {
            // All users have been processed
            return;
        }

        // Get the current user ID
        String currentUserId = followingUserIds.get(currentIndex);

        // Get the current post index for the user
        Integer currentIndexObj = userPostIndices.get(currentUserId);
        final int[] currentPostIndex = {(currentIndexObj != null) ? currentIndexObj : 0};

        DatabaseReference userPostsReference = FirebaseDatabase.getInstance().getReference("Post").child(currentUserId);

        // Adjust the query to load only the next set of posts based on the current page
        int postsToLoad = POSTS_PER_PAGE * currentPage;
        userPostsReference.limitToLast(postsToLoad).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postsSnapshot) {
                if (postsSnapshot.exists() && postsSnapshot.getChildrenCount() > 0) {
                    // Check if there are more posts for the user
                    if (currentPostIndex[0] < postsSnapshot.getChildrenCount()) {
                        // Reverse the order of postSnapshots list
                        List<DataSnapshot> postSnapshots = new ArrayList<>();
                        for (DataSnapshot postSnapshot : postsSnapshot.getChildren()) {
                            // If the post is not already displayed, proceed to display it
                            postSnapshots.add(0, postSnapshot); // Add each post at the beginning of the list
                        }

                        // Reverse the iteration through postSnapshots list
                        if (currentPostIndex[0] >= 0 && currentPostIndex[0] < postSnapshots.size()) {
                            // Retrieve the current post snapshot
                            DataSnapshot currentPostSnapshot = postSnapshots.get(currentPostIndex[0]);
                            String thisPostId = String.valueOf(currentPostSnapshot.getKey());
                            checkThePostId(thisPostId, currentUserId);
                            isAlreadyDisplayed(currentPostSnapshot, currentUserId);

                            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

                            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        // Retrieve user data
                                        String profilePictureUrl = userSnapshot.child("Profile picture").getValue(String.class);
                                        String username = userSnapshot.child("username").getValue(String.class);
                                        String address = userSnapshot.child("Location").getValue(String.class);
                                        String userIdd = userSnapshot.getKey();

                                        // Retrieve post data
                                        String postImageUrl = currentPostSnapshot.child("imageURL").getValue(String.class);
                                        String rating = currentPostSnapshot.child("prodprice").getValue(String.class);
                                        String category = currentPostSnapshot.child("category").getValue(String.class);
                                        String description = currentPostSnapshot.child("imgdesc").getValue(String.class);

                                        if (!categoryExists) {
                                            fetchUserAndPosts(profilePictureUrl, username, address, postImageUrl, rating, category, description, currentUserId, thisPostId);
                                        } else {
                                            Log.d("postIdtohide", "showitt :" + currentPostSnapshot.getKey());
                                            Log.d("postIdtohide", "showid :" + userIdd);
                                        }
                                    } else {
                                        Log.e("Firebase", "User data not found");
                                    }

                                    // Increment the post index for the user
                                    userPostIndices.put(currentUserId, currentPostIndex[0] + 1);

                                    if (followingUserIds.size() != 0) {
                                        currentIndex = (currentIndex + 1) % followingUserIds.size();
                                    }

                                    // Check if the current page is fully loaded
                                    if (currentPostIndex[0] >= postsToLoad) {
                                        // Move to the next page and reset the current post index
                                        currentPage++;
                                        currentPostIndex[0] = 0;
                                    }

                                    // Call the method again for the next user
                                    GetUserAndPostId();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Error fetching user data: " + error.getMessage());
                                    // Move to the next user even if there's an error
                                    if (followingUserIds.size() != 0) {
                                        currentIndex = (currentIndex + 1) % followingUserIds.size();
                                    }
                                    GetUserAndPostId();
                                }
                            });
                        }
                    } else {
                        Log.e("Firebase", "No more posts found for user: " + currentUserId);

                        // User has no more posts, remove from the list
                        followingUserIds.remove(currentIndex);
                        userPostIndices.remove(currentUserId);

                        // Move to the next user
                        if (followingUserIds.size() != 0) {
                            currentIndex = (currentIndex + 1) % followingUserIds.size();
                        }

                        // Reset the current page for the next user
                        currentPage = 1;

                        // Call the method again for the next user
                        GetUserAndPostId();
                    }
                } else {
                    Log.e("Firebase", "No posts found for user: " + currentUserId);

                    // User has no more posts, remove from the list
                    followingUserIds.remove(currentIndex);
                    userPostIndices.remove(currentUserId);

                    // Move to the next user
                    if (followingUserIds.size() != 0) {
                        currentIndex = (currentIndex + 1) % followingUserIds.size();
                    }

                    // Reset the current page for the next user
                    currentPage = 1;

                    // Call the method again for the next user
                    GetUserAndPostId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching posts: " + error.getMessage());

                // Move to the next user
                if (followingUserIds.size() != 0) {
                    currentIndex = (currentIndex + 1) % followingUserIds.size();
                }

                // Reset the current page for the next user
                currentPage = 1;

                // Call the method again for the next user
                GetUserAndPostId();
            }
        });
    }






    private void fetchUserAndPosts(String profilePictureUrl, String username, String address, String postImageUrl, String rating,
                                   String category, String description, String userId, String PostId) {


        // Create a new PostModel object with user data and add it to the list
        PostModel post = new PostModel(profilePictureUrl, username, address, postImageUrl, rating, category, description,userId, PostId, isSaved);
        postList.add(post);

        // Notify the adapter after fetching the post for the current user
        postAdapter.notifyDataSetChanged();

    }


    private void isAlreadyDisplayed(DataSnapshot postSnapshot, String currentUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference("Interaction");
        DatabaseReference creatorsRef = creatorRef.child(userId).child(currentUserId)
                .child(postSnapshot.getKey());

        creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    categoryExists = true;
                } else {

                    categoryExists = false;

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkThePostId(String PostId, String userId) {

        DatabaseReference savedReference = FirebaseDatabase.getInstance().getReference("Saved")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                        }
                        else {
                            isSaved = false;
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking save status: " + databaseError.getMessage());
                // Default to false in case of error
            }
        });
    }


    private void getCurrentLocation() {
        // Check for location permission again before requesting updates
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (isAdded() && locationResult.getLastLocation() != null) {
                        Location location = locationResult.getLastLocation();
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        getAddressFromLocation(latitude, longitude);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {

        if (!isAdded()) {
            return;
        }
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Example components of the address
                String street;

                // Check if Thoroughfare (street name) is available
                if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
                    street = address.getThoroughfare();
                } else if (address.getSubThoroughfare() != null && !address.getSubThoroughfare().isEmpty()) {
                    street = address.getSubThoroughfare();
                } else {
                    // If neither Thoroughfare nor SubThoroughfare is available, save the coordinates
                    street =   latitude + ", " + longitude;
                }

                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();

                // Create a full address string
                currentAddress = street + ", " + city + ", " + state + ", " + country + ", " + postalCode;

                // Now you can use 'currentAddress' as needed in your app
                Log.v("homeAddress", currentAddress);

                if (addresses != null && addresses.size() > 0) {
                    // ... Existing code ...

                    // Create a full address string
                    currentAddress = street + ", " + city + ", " + state + ", " + country + ", " + postalCode;

                    // Update currentAddress in Firebase under the user's field
                    updateCurrentAddressInFirebase(currentAddress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void updateCurrentAddressInFirebase(String address) {
        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Update the currentAddress in the database
        mDatabase.child("users").child(userId).child("CurrentLocation").setValue(address);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the user's location
                getCurrentLocation();
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
