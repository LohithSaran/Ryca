

package com.ryca.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.ProfileFragment;
import com.ryca.R;
import com.ryca.User;
import com.ryca.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class CircleFragment extends Fragment {

    private EditText searchBox;
    private RecyclerView searchedUserList;
    private UserAdapter userAdapter;
    private List<User> userList;

    FirebaseUser firebaseUser;

    EditText searchBar;

    public CircleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_circle, container, false);

        searchBox = view.findViewById(R.id.searchbox);
        searchedUserList = view.findViewById(R.id.searchedUserList);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);

        searchedUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        searchedUserList.setAdapter(userAdapter);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        searchBox.requestFocus();

        searchBox.post(() -> {
            if (searchBox.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(requireActivity().INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                String userID = user.getId();
                Bundle bundle = new Bundle();
                bundle.putString("userId", userID);
                bundle.putString("fragment", "creatorsShowroom");

                String firebaseUserID = firebaseUser != null ? firebaseUser.getUid() : "";

                if (firebaseUserID.equals(userID)) {
                    // If the clicked user is the current user, replace the fragment with ProfileFragment
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.framelayout, profileFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    // If not, proceed with the current method
                    CreatorsShowroom creatorsShowroomFragment = new CreatorsShowroom();
                    creatorsShowroomFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.framelayout, creatorsShowroomFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }

    });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchedUserList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                performSearch(charSequence.toString());
                searchedUserList.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchedUserList.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void performSearch(String query) {
        // Only perform the search if the query is not empty
        if (!query.isEmpty()) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Creators");

            String lowerCaseQuery = query.toLowerCase();
            usersRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userList.clear();

                    int count = 0; // Variable to count the number of items added to userList

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        boolean isMatch = false;

                        // Check if the search query matches any field inside the user data
                        for (DataSnapshot fieldSnapshot : userSnapshot.getChildren()) {
                            Object fieldValue = fieldSnapshot.getValue(); // Use Object type to handle different types

                            if (fieldValue != null && isMatch(fieldValue.toString(), lowerCaseQuery)) {
                                isMatch = true;
                                break;
                            }
                        }

                        // Optionally, you can also check other nested fields like categories
                        if (isMatch) {
                            String shopName = userSnapshot.child("Shop Name").getValue(String.class);

                            User user = new User();
                            user.setId(userId);
                            user.setUsername(shopName);
                            user.setAdd(userSnapshot.child("Location").getValue(String.class));
                            user.setCity(userSnapshot.child("City").getValue(String.class));
                            user.setImageurl(userSnapshot.child("Profile picture").getValue(String.class));

                            userList.add(user);
                            count++;

                            Log.d("UserResult", "Username: " + query + ", Location: " + user.getAdd() + ", Profile Picture: " + user.getImageurl());

                            // Check if 50 items have been added, and stop adding more items
                            if (count >= 30) {
                                break;
                            }
                        }
                    }

                    userAdapter.notifyDataSetChanged();
                    searchedUserList.setVisibility(userList.isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("DatabaseError", "Error: " + databaseError.getMessage());
                }
            });
        } else {
            // Clear the user list and hide the search result list when the query is empty
            userList.clear();
            userAdapter.notifyDataSetChanged();
            searchedUserList.setVisibility(View.GONE);
        }
    }



    private boolean isMatch(String fieldValue, String query) {
        return fieldValue != null && fieldValue.toLowerCase().replaceAll("\\s+", "").contains(query.toLowerCase().replaceAll("\\s+", ""));
    }

    private void getUserDetails(String uid, String query) {
        DatabaseReference creatorsRef = FirebaseDatabase.getInstance().getReference().child("Creators").child(uid);

        creatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String location = dataSnapshot.child("Location").getValue(String.class);
                    String profilePicture = dataSnapshot.child("Profile picture").getValue(String.class);

                    User user = new User();
                    user.setId(uid);
                    user.setUsername(query);
                    user.setAdd(location);
                    user.setImageurl(profilePicture);

                    userList.add(user);

                    Log.d("UserResult", "Username: " + query + ", Location: " + location + ", Profile Picture: " + profilePicture);
                    userAdapter.notifyDataSetChanged();
                    searchedUserList.setVisibility(userList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "Error: " + databaseError.getMessage());
            }
        });
    }
}



