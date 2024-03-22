package com.ryca.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
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

public class SearchFragment extends Fragment {


    private EditText searchBox;
    private RecyclerView searchedUserList;
    private SearchExhibitorAdapter userAdapter;
    private List<SearchExhibitorList> userList;

    FirebaseUser firebaseUser;

    EditText searchBar;
    TextView beginningText;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBox = view.findViewById(R.id.searchboxRedirect);
        beginningText = view.findViewById(R.id.beginningText);
        searchedUserList = view.findViewById(R.id.ExhibitorsShowcase);
        userList = new ArrayList<>();
        userAdapter = new SearchExhibitorAdapter(getContext(), userList);

        searchedUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        searchedUserList.setAdapter(userAdapter);

        searchBar = view.findViewById(R.id.searchboxRedirect);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        searchBox.setFocusable(false);
        searchBox.setClickable(false);

        ShowExhibitorsList();

        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the CircleFragment
                CircleFragment circleFragment = new CircleFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.framelayout, circleFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    public void ShowExhibitorsList() {

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference connectsRef = FirebaseDatabase.getInstance().getReference().child("Connects").child(currentUserId).child("Following");
        DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference().child("Creators");
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Post");

        // Fetch following userIds
        connectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userIdSnapshot : snapshot.getChildren()) {
                    String followingUserId = (String) userIdSnapshot.getValue();

                    // Fetch creator details
                    creatorsRef.child(followingUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot creatorSnapshot) {
                            String shopName = creatorSnapshot.child("Shop Name").getValue(String.class);
                            String shopDescription = creatorSnapshot.child("Shop Description").getValue(String.class);
                            String profilePicture = creatorSnapshot.child("Profile picture").getValue(String.class);
                            String city = creatorSnapshot.child("City").getValue(String.class);
                            String location = creatorSnapshot.child("Location").getValue(String.class);

                            // Fetch latest 3 post image URLs
                            postRef.child(followingUserId).orderByKey().limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                                    ArrayList<String> imageUrls = new ArrayList<>();
                                    for (DataSnapshot post : postSnapshot.getChildren()) {
                                        String imageUrl = post.child("imageURL").getValue(String.class);
                                        imageUrls.add(imageUrl);
                                    }
                                    // Reverse to get the latest images at the beginning
                                    Collections.reverse(imageUrls);


                                    if (shopName != null && !shopName.isEmpty() &&
                                            location != null && !location.isEmpty() &&
                                            city != null && !city.isEmpty() ) {

                                         SearchExhibitorList model = new SearchExhibitorList(shopName, shopDescription, profilePicture, city, location,followingUserId , imageUrls);
                                        userList.add(model);

                                        userAdapter.notifyDataSetChanged();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Firebase", "Failed to fetch posts", error.toException());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Failed to fetch creator details", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch following userIds", error.toException());
            }
        });
    }

}