package com.ryca.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ryca.PostAdapter;
import com.ryca.PostModel;
import com.ryca.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment implements AddCategoryAdapter.ItemRemovedCallback {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String currentUserId = user.getUid();

    private ImageView addCategoryBtn;
    private List<AddCategoryModel> categories = new ArrayList<>();
    private AddCategoryAdapter categoryAdapter;
    RecyclerView raddedCategoryRecycler;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mCreatorsRef;
    List<Map<String, String>> searchResults = new ArrayList<>(); // List of maps, each containing a separate search result
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    int breakLoop =0 ;
    Map<String, String> uniquePostIds = new HashMap<>();
    RecyclerView recyclerView;
    Map<String, String> postUserMap = new HashMap<>();
    Map<String, String> AllPostUserMap = new HashMap<>();
    private boolean loading = false; // Flag to track loading state
    private boolean isLastPage = false;
    private boolean isLoading = false;
    ProgressBar paginationProgressBar;
    int count = 0;







    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Initialize addCategoryBtn
        addCategoryBtn = view.findViewById(R.id.AddCategorybtn);

        mDatabase = FirebaseDatabase.getInstance();
        mCreatorsRef = mDatabase.getReference("Creators");

        raddedCategoryRecycler = view.findViewById(R.id.addedCategoryRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false);
        raddedCategoryRecycler.setLayoutManager(layoutManager);

        categoryAdapter = new AddCategoryAdapter(requireContext(), categories, this::onItemRemoved);

        raddedCategoryRecycler.setAdapter(categoryAdapter);

        recyclerView = view.findViewById(R.id.postRecycler); // Make sure to replace with your actual RecyclerView ID
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);
        paginationProgressBar = view.findViewById(R.id.paginationProgressBar);

        // Example function to show progress bar


        populateTheCategories();

        addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });



        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                // Load more if we're not currently loading, we haven't loaded all items, and the user has scrolled to the bottom
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isLoading = true; // Set loading state to prevent multiple load calls
                        showLoading(); // Optionally show a loading indicator
                        fetchAndDisplayPosts();
                    }
                }
            }
        });

        return view;
    }


    private void fetchAndDisplayPosts() {
        DatabaseReference interactionRef = FirebaseDatabase.getInstance().getReference().child("Interaction").child(currentUserId);
        // Clear the postUserMap before starting to fetch new data
        postUserMap.clear();
        interactionRef.limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int postCount = 0;
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot postSnapshot : categorySnapshot.getChildren()) {
                        if (postCount >= 20) {
                            break; // Stop adding more posts once 20 posts are retrieved
                        }
                        String postId = postSnapshot.getKey();
                        String userId = postSnapshot.getValue(String.class);
                        // Check if the postId is already present in AllPostUserMap
                        if (!AllPostUserMap.containsKey(postId)) {
                            // If not present, add to both postUserMap and AllPostUserMap
                            postUserMap.put(postId, userId);
                            AllPostUserMap.put(postId, userId); // Assuming you want to keep track of all seen posts
                            postCount++; // Increment the post counter
                            count++;

                        }
                    }
                }
                // Now that we have our map populated, display the posts
                if (postUserMap.isEmpty()) {
                    Toast.makeText(getContext(), "No more Exhibits to display, Add another category to see more exhibits.", Toast.LENGTH_LONG).show();
                    hideLoading();
                } else {
                    loading = false;
                    displayPosts(postUserMap);
                    Toast.makeText(getContext(), "Count is : " + count, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching post-user pairs", databaseError.toException());
            }
        });
    }


    private void displayPosts(Map<String, String> postUserMap) {
        if (loading) return; // Return if already loading posts
        loading = true; // Set loading to true since we're starting to load posts
        if (postUserMap != null) {

            for (Map.Entry<String, String> entry : postUserMap.entrySet()) {
                final String postId = entry.getKey();
                final String userId = entry.getValue();

                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Post").child(userId).child(postId);
                DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference().child("Creators").child(userId);

                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String prodPrice = dataSnapshot.child("prodprice").getValue(String.class);
                        String imgDesc = dataSnapshot.child("imgdesc").getValue(String.class);
                        String imageURL = dataSnapshot.child("imageURL").getValue(String.class);
                        String category = dataSnapshot.child("category").getValue(String.class);

                        creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot creatorSnapshot) {
                                String shopName = creatorSnapshot.child("Shop Name").getValue(String.class);
                                String profilePicture = creatorSnapshot.child("Profile picture").getValue(String.class);
                                String location = creatorSnapshot.child("Location").getValue(String.class);

                                if (profilePicture != null && shopName!= null && location != null && imageURL != null && prodPrice != null && category != null && imgDesc != null && userId != null && postId != null) {

                                    checkThePostId(postId, isSaved -> {
                                        PostModel post = new PostModel(profilePicture, shopName, location, imageURL, prodPrice, category, imgDesc, userId, postId, isSaved);
                                        postList.add(post); // Assuming postList is accessible here
                                        postAdapter.notifyDataSetChanged(); // Assuming postAdapter is accessible and set up
                                    });
                                    hideLoading();

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("Firebase", "Failed to read creator details", databaseError.toException());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Failed to read post details", databaseError.toException());
                    }
                });
            }
        }

        isLoading = false;

    }


    public void showLoading() {
        paginationProgressBar.setVisibility(View.VISIBLE);
    }

    // Example function to hide progress bar
    public void hideLoading() {
        paginationProgressBar.setVisibility(View.GONE);
    }


    private void populateTheCategories() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Adjust if you fetch for different users
        DatabaseReference interactionRef = FirebaseDatabase.getInstance().getReference().child("Interaction").child(userId);

        interactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> fetchedCategories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    fetchedCategories.add(categoryName);
                }
                // Now that you have your categories, update your UI accordingly
                updateCategories(fetchedCategories);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to fetch categories", databaseError.toException());
                // Handle error
            }
        });
    }


    private void updateCategories(List<String> fetchedCategories) {
        // Clear existing categories
        this.categories.clear();

        // Convert each String category name into AddCategoryModel and add to the list
        for (String categoryName : fetchedCategories) {
            this.categories.add(new AddCategoryModel(categoryName)); // Assuming AddCategoryModel has a constructor that accepts a String
        }

        // Notify the adapter of the change
        categoryAdapter.notifyDataSetChanged();

        if (fetchedCategories != null) {
            fetchAndDisplayPosts();
        }
    }



    // All the methods related to addCategory has been collapsed can expand later
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.custom_dialog_add_category, null);
        builder.setView(view);

        EditText editTextCategory = view.findViewById(R.id.edit_text_category);
        ImageView buttonSave = view.findViewById(R.id.button_save);
        ImageView buttonCancel = view.findViewById(R.id.button_cancel);

        final AlertDialog dialog = builder.create();
        dialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = editTextCategory.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    // Check if the category name already exists, ignoring case
                    boolean exists = false;
                    for (AddCategoryModel existingCategory : categories) {
                        if (existingCategory.getName().equalsIgnoreCase(categoryName)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        // If the category name doesn't exist, add the new category
                        AddCategoryModel category = new AddCategoryModel(categoryName);
                        categories.add(category);
                        // Notify the adapter of the change
                        categoryAdapter.notifyDataSetChanged();
                        //saveCategoriesList();
                        isLoading = false;
                        searchInCreators(categoryName);
                    } else {
                        // If the category name exists, show a Toast message
                        Toast.makeText(getActivity(), "Category already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



    // Calling of this method is causing the actual issue solve it
    public void searchInCreators(String searchText) {
        Query query = mCreatorsRef.orderByKey();

        query.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean finalSearchResult = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    boolean searchFound = false;

                    // Check in Category field
                    DataSnapshot categorySnapshot = userSnapshot.child("Category");
                    if (categorySnapshot.exists()) {
                        for (DataSnapshot category : categorySnapshot.getChildren()) {
                            String categoryValue = (String) category.getValue();
                            if (categoryValue != null && categoryValue.toLowerCase().contains(searchText.toLowerCase())) {
                                Map<String, String> result = new HashMap<>();
                                result.put(userId, searchText);
                                searchResults.add(result);
                                searchFound = true;
                                finalSearchResult = true;
                                //saveSearchResults(searchResults);
                                Log.d("Posttts", searchResults.toString());
                                break;
                            }
                        }
                    }

                    // If search not found in Category, check in Shop Description
                    if (!searchFound) {
                        String shopDescription = (String) userSnapshot.child("Shop Description").getValue();
                        if (shopDescription != null && shopDescription.toLowerCase().contains(searchText.toLowerCase())) {
                            Map<String, String> result = new HashMap<>();
                            result.put(userId, searchText);
                            searchResults.add(result);
                            finalSearchResult = true;
                            //saveSearchResults(searchResults);
                        }
                    }

                }
                if (!finalSearchResult) {
                    Toast.makeText(requireContext(), "Sorry there are no Exhibitors with the product you are searching, Please try some other categories", Toast.LENGTH_LONG).show();
                }

                if (postAdapter.getItemCount() == 0) {
                    displayFivePost(searchResults);
                    Toast.makeText(requireContext(), "No", Toast.LENGTH_SHORT).show();
                }else {
                    DisplayAllPost(searchResults);
                    Toast.makeText(requireContext(), "Yes :" + searchResults, Toast.LENGTH_LONG).show();
                    //Log.d("Posttts", "SecondCall "+ searchResults);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    public void displayFivePost(List<Map<String, String>> searchResults) {

        for (Map<String, String> result : searchResults) {
            for (String userId : result.keySet()) {
                DatabaseReference userPostRef = FirebaseDatabase.getInstance().getReference().child("Post").child(userId);
                userPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This map will store up to 5 unique postIds for each userId
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if (breakLoop == 5) break; // Stop if we have collected 5 unique posts

                            String postId = postSnapshot.getKey();
                            String category = (String) postSnapshot.child("category").getValue();
                            String description = (String) postSnapshot.child("imgdesc").getValue();
                            // Check if the category matches and the postId is unique
                            if (category != null && category.toLowerCase().contains(result.get(userId).toLowerCase())
                                    || description != null && description.toLowerCase().contains(result.get(userId).toLowerCase())
                                    && !uniquePostIds.containsKey(postId)) {
                                uniquePostIds.put(postId, userId); // Store postId and userId
                                Log.d("checkkk", uniquePostIds.toString());
                                breakLoop++;

                            }
                        }

                        if (breakLoop == 5) {

                            // Now, iterate over the uniquePostIds to fetch additional details for each post
                            for (String postId : uniquePostIds.keySet()) {

                                //  uniquePostIds.values(); work on this for putting the below code in another method
                                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Post").child(userId).child(postId);

                                postRef.addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String prodPrice = (String) dataSnapshot.child("prodprice").getValue();
                                        String imgDesc = (String) dataSnapshot.child("imgdesc").getValue();
                                        String imageURL = (String) dataSnapshot.child("imageURL").getValue();
                                        String category = (String) dataSnapshot.child("category").getValue();

                                        DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference().child("Creators").child(userId);
                                        creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot creatorSnapshot) {

                                                String shopName = (String) creatorSnapshot.child("Shop Name").getValue();
                                                String profilePicture = (String) creatorSnapshot.child("Profile picture").getValue();
                                                String location = (String) creatorSnapshot.child("Location").getValue();

                                                // Assuming PostModel and postAdapter are previously defined and set up
                                                if (profilePicture != null && shopName!= null && location != null && imageURL != null && prodPrice != null && category != null && imgDesc != null && userId != null && postId != null) {

                                                    checkThePostId(postId, isSaved -> {
                                                        PostModel post = new PostModel(profilePicture, shopName, location, imageURL, prodPrice, category, imgDesc, userId, postId, isSaved);
                                                        postList.add(post);
                                                        postAdapter.notifyDataSetChanged();
                                                        // Any additional logic that depends on the isSaved value
                                                    });

                                                }
                                                // Log the details for debugging purposes
                                                Log.d("PostDetails", "postId: " + postId + ", prodPrice: " + prodPrice + ", imgDesc: " + imgDesc + ", imageURL: " + imageURL + ", category: " + category + ", shopName: " + shopName + ", profilePicture: " + profilePicture);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e("Firebase", "Failed to read creator details", databaseError.toException());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("Firebase", "Failed to read post details", databaseError.toException());
                                    }
                                });
                            }
                            DisplayAllPost(searchResults);
                        }
                        if (breakLoop < 5 && breakLoop > 0){

                            // Now, iterate over the uniquePostIds to fetch additional details for each post
                            for (String postId : uniquePostIds.keySet()) {

                                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Post").child(userId).child(postId);
                                postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String prodPrice = (String) dataSnapshot.child("prodprice").getValue();
                                        String imgDesc = (String) dataSnapshot.child("imgdesc").getValue();
                                        String imageURL = (String) dataSnapshot.child("imageURL").getValue();
                                        String category = (String) dataSnapshot.child("category").getValue();

                                        DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference().child("Creators").child(userId);
                                        creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot creatorSnapshot) {

                                                String shopName = (String) creatorSnapshot.child("Shop Name").getValue();
                                                String profilePicture = (String) creatorSnapshot.child("Profile picture").getValue();
                                                String location = (String) creatorSnapshot.child("Location").getValue();

                                                if (profilePicture != null && shopName!= null && location != null && imageURL != null && prodPrice != null && category != null && imgDesc != null && userId != null && postId != null) {

                                                    // Assuming PostModel and postAdapter are previously defined and set up
                                                    checkThePostId(postId, isSaved -> {
                                                        PostModel post = new PostModel(profilePicture, shopName, location, imageURL, prodPrice, category, imgDesc, userId, postId, isSaved);
                                                        postList.add(post);
                                                        postAdapter.notifyDataSetChanged();
                                                        // Any additional logic that depends on the isSaved value
                                                    });
                                                }

                                                // Log the details for debugging purposes
                                                Log.d("PostDetails", "postId: " + postId + ", prodPrice: " + prodPrice + ", imgDesc: " + imgDesc + ", imageURL: " + imageURL + ", category: " + category + ", shopName: " + shopName + ", profilePicture: " + profilePicture);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e("Firebase", "Failed to read creator details", databaseError.toException());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("Firebase", "Failed to read post details", databaseError.toException());
                                    }
                                });
                            }
                            DisplayAllPost(searchResults);

                        }

                        if (breakLoop == 0) {
                            DisplayAllPost(searchResults);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Firebase", "Failed to fetch user posts", databaseError.toException());
                    }
                });
            }
        }
    }



    public void DisplayAllPost(List<Map<String, String>> searchResults) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Post");
        DatabaseReference interactionRef = FirebaseDatabase.getInstance().getReference().child("Interaction");

        // Counter to keep track of completed Firebase operations
        AtomicInteger completedOperations = new AtomicInteger();

        // Total operations count
        int totalOperations = searchResults.size(); /* assume each user has X posts to check */;

        for (Map<String, String> result : searchResults) {
            for (String userId : result.keySet()) {
                String searchCategory = result.get(userId);

                postRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String postId = postSnapshot.getKey();

                            if (uniquePostIds != null && uniquePostIds.containsKey(postId)) {
                                continue;
                            }

                            String category = postSnapshot.child("category").getValue(String.class);
                            String imgdesc = postSnapshot.child("imgdesc").getValue(String.class);

                            if ((category != null && category.toLowerCase().contains(searchCategory.toLowerCase())) ||
                                    (imgdesc != null && imgdesc.toLowerCase().contains(searchCategory.toLowerCase()))) {
                                interactionRef.child(currentUserId).child(searchCategory).child(postId).setValue(userId);
                            }
                        }

                        // Check if all operations are completed
                        if (completedOperations.incrementAndGet() == totalOperations) {
                            // All operations are complete, clear searchResults
                            searchResults.clear();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Failed to fetch posts", databaseError.toException());
                    }
                });
            }
        }
        if (postAdapter.getItemCount() == 1) {
            // Ensure this runs on the UI thread if you're changing UI elements or interacting with the UI in any way
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fetchAndDisplayPosts();
                }
            });
        }
    }



    public interface PostCheckCallback {
        void onCheckCompleted(boolean isSaved);
    }
    public void checkThePostId(String PostId, PostCheckCallback callback) {
        DatabaseReference savedReference = FirebaseDatabase.getInstance().getReference("Saved")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isSavedTemp = false; // Use a temporary flag
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postKey = postSnapshot.getKey();
                    if (postKey != null && postKey.length() >= 20) {
                        String postKeySuffix = postKey.substring(postKey.length() - 20);
                        String postIdSuffix = PostId.substring(PostId.length() - 20);
                        if (postKeySuffix.equals(postIdSuffix)) {
                            isSavedTemp = true;
                            break;
                        }
                    }
                }
                callback.onCheckCompleted(isSavedTemp); // Use the callback to return the result
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking save status: " + databaseError.getMessage());
                callback.onCheckCompleted(false); // Call the callback with false on error
            }
        });
    }


    public void onItemRemoved(List<AddCategoryModel> updatedList, String removedValue) {
        Toast.makeText(requireContext(), "this: "+removedValue, Toast.LENGTH_SHORT).show();
        RemoveValueFromFirebase(removedValue);
    }

    public void RemoveValueFromFirebase(String removedValue) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Interaction").child(currentUserId);

        // Remove the category node from Firebase
        ref.child(removedValue).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the category from the local list
                Iterator<AddCategoryModel> iterator = categories.iterator();
                while (iterator.hasNext()) {
                    AddCategoryModel categoryModel = iterator.next();
                    if (categoryModel.getName().equalsIgnoreCase(removedValue)) {
                        iterator.remove(); // Remove the matching category from the list
                        break; // Assuming unique category names, we can break after finding the match
                    }
                }

                // Notify the adapter of the change
                categoryAdapter.notifyDataSetChanged();

                Toast.makeText(requireContext(), "Category removed successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to remove category.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}