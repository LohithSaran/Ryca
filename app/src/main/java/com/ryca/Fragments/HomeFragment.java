package com.ryca.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryca.PostAdapter;
import com.ryca.PostModel;
import com.ryca.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements AddCategoryAdapter.ItemRemovedCallback {

    private FirebaseDatabase database;
    private DatabaseReference connectsRef;
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
    int postCount;
    Map<String, String> PostIds = new HashMap<>();
    boolean isSaved;
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    int breakLoop =0 ;



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

        RecyclerView recyclerView = view.findViewById(R.id.postRecycler);

        recyclerView = view.findViewById(R.id.postRecycler); // Make sure to replace with your actual RecyclerView ID
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(postAdapter);
        addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });

        loadCategoriesList();
        loadSearchResults();

        return view;
    }


    private void loadCategoriesList() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SharedPrefs", requireContext().MODE_PRIVATE);
        String jsonCategories = sharedPreferences.getString("CategoriesList", null);
        if (jsonCategories != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AddCategoryModel>>() {}.getType();
            categories = gson.fromJson(jsonCategories, type); // Converts JSON to list
            categoryAdapter = new AddCategoryAdapter(requireContext(), categories,  this::onItemRemoved);

            raddedCategoryRecycler.setAdapter(categoryAdapter);
        }
    }
    private void saveCategoriesList() {
        Gson gson = new Gson();
        String jsonCategories = gson.toJson(categories); // Converts list to JSON
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SharedPrefs", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CategoriesList", jsonCategories);
        editor.apply();
    }

    public void saveSearchResults(List<Map<String, String>> searchResults) {
        // Convert the list to a JSON string
        Gson gson = new Gson();
        String json = gson.toJson(searchResults);

        // Save the JSON string to SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SearchResults", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("searchResults", json);
        Log.d("SharedddPref", json.toString());
        editor.apply();
    }

    public List<Map<String, String>> loadSearchResults() {
        // Retrieve the JSON string from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SearchResults", requireContext().MODE_PRIVATE);
        String json = sharedPreferences.getString("searchResults", null);

        // Convert the JSON string back to a list
        Gson gson = new Gson();
        Type type = new TypeToken<List<Map<String, String>>>() {}.getType();
        searchResults = gson.fromJson(json, type);
        return gson.fromJson(json, type);
    }
    public void onItemRemoved(List<AddCategoryModel> updatedList, String removedValue) {
        saveCategoriesList(updatedList); // Call the method to save the updated list to SharedPreferences
        Toast.makeText(requireContext(), "this: "+removedValue, Toast.LENGTH_SHORT).show();
        RemoveValueFromList(removedValue);
    }

    public void RemoveValueFromList(String removedValue) {
        Iterator<Map<String, String>> iterator = searchResults.iterator();
        while (iterator.hasNext()) {
            Map<String, String> entry = iterator.next();
            // Check if the map contains the specified value
            if (entry.containsValue(removedValue)) {
                // Remove the entry from the list
                iterator.remove();
                Log.d("Posttts", "After remove: "+ searchResults.toString());
            }
        }
        saveSearchResults(searchResults);
    }
    private void saveCategoriesList(List<AddCategoryModel> categories) {
        Gson gson = new Gson();
        String jsonCategories = gson.toJson(categories); // Convert list to JSON
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SharedPrefs", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CategoriesList", jsonCategories);
        editor.apply();
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
                    // Create a new Category object
                    AddCategoryModel category = new AddCategoryModel(categoryName);
                    // Add the new category to the list
                    categories.add(category);
                    // Notify the adapter of the change
                    categoryAdapter.notifyDataSetChanged();
                    saveCategoriesList();
                    searchInCreators(categoryName);
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
                                saveSearchResults(searchResults);
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
                            saveSearchResults(searchResults);
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
                    Toast.makeText(requireContext(), "Yes", Toast.LENGTH_SHORT).show();
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
                        Map<String, String> uniquePostIds = new HashMap<>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            if (breakLoop == 5) break; // Stop if we have collected 5 unique posts

                            String postId = postSnapshot.getKey();
                            String category = (String) postSnapshot.child("category").getValue();
                            // Check if the category matches and the postId is unique
                            if (category != null && category.toLowerCase().equals(result.get(userId).toLowerCase()) && !uniquePostIds.containsKey(postId)) {
                                uniquePostIds.put(postId, userId); // Store postId and userId
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

                                                checkThePostId(postId, isSaved -> {
                                                    PostModel post = new PostModel(profilePicture, shopName, location, imageURL, prodPrice, category, imgDesc, userId, postId, isSaved);
                                                    postList.add(post);
                                                    postAdapter.notifyDataSetChanged();
                                                    // Any additional logic that depends on the isSaved value
                                                });
                                                DisplayAllPost(searchResults);

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

                                                // Assuming PostModel and postAdapter are previously defined and set up
                                                checkThePostId(postId, isSaved -> {
                                                    PostModel post = new PostModel(profilePicture, shopName, location, imageURL, prodPrice, category, imgDesc, userId, postId, isSaved);
                                                    postList.add(post);
                                                    postAdapter.notifyDataSetChanged();
                                                    // Any additional logic that depends on the isSaved value
                                                });

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



    public void DisplayAllPost(List<Map<String, String>> searchResults){

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



}