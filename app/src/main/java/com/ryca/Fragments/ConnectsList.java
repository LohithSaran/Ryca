package com.ryca.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class ConnectsList extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView ConnectUserRecycler;
    private UserAdapter ConnectuserAdapter;
    private List<User> ConnectuserList;
    FirebaseUser firebaseUser;
    TextView followingTextView;
    TextView textViewFollowCount;


    public ConnectsList() {

    }


    public static ConnectsList newInstance(String param1, String param2) {
        ConnectsList fragment = new ConnectsList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_connects_list, container, false);

        ConnectUserRecycler = view.findViewById(R.id.connectlistrecycler);
        ConnectuserList = new ArrayList<>();
        ConnectuserAdapter = new UserAdapter(getContext(), ConnectuserList);

        ConnectUserRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ConnectUserRecycler.setAdapter(ConnectuserAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        followingTextView = view.findViewById(R.id.following); // Assuming you've already defined this TextView in your layout
        textViewFollowCount = view.findViewById(R.id.followers);

        textViewFollowCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), "Followers list can't be viewed!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView backbtn = view.findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requireActivity().onBackPressed();
            }
        });


        ConnectuserAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
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


        DisplayConnectUsers();
        checkUserTypeAndShowFollowCount();

        return view;
    }

    private void DisplayConnectUsers() {
        String userId = firebaseUser.getUid();
        DatabaseReference ConnectRef = FirebaseDatabase.getInstance().getReference().child("Connects").child(userId).child("Following");
        DatabaseReference ConnectedUserRef = FirebaseDatabase.getInstance().getReference().child("Creators");

        ConnectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = snapshot.getChildrenCount();
                // Update the TextView with the total number of following users
                followingTextView.setText("Connected with "+ "(" +String.valueOf(totalUsers) +")"  );

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String connectID = ds.getValue(String.class);
                    String connectIdKey = ds.getKey();

                    DatabaseReference ConnectIDRef = ConnectedUserRef.child(connectID);
                    ConnectIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String ShopName = snapshot.child("Shop Name").getValue(String.class);
                            String Location = snapshot.child("Location").getValue(String.class);
                            String city = snapshot.child("City").getValue(String.class);
                            String ProfilePicture = snapshot.child("Profile picture").getValue(String.class);

                            if (ShopName != null && !ShopName.isEmpty() &&
                                    Location != null && !Location.isEmpty() &&
                                    city != null && !city.isEmpty()) {

                                User user = new User();
                                user.setId(connectID);
                                user.setUsername(ShopName);
                                user.setAdd(Location);
                                user.setCity(city);
                                user.setImageurl(ProfilePicture);

                                ConnectuserList.add(user);
                                ConnectuserAdapter.notifyDataSetChanged();
                            } else {
                                // Remove connectID from the field if any required field is empty
                                DatabaseReference connectIDRef = ConnectRef.child(connectIdKey);
                                connectIDRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ConnectUserRemoved", "ConnectUser with ID: " + connectID + " removed because one or more fields are empty");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("ConnectUserRemoveError", "Failed to remove ConnectUser with ID: " + connectID, e);
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled if needed
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }


    private void checkUserTypeAndShowFollowCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the "creator" field exists and retrieve its value
                    if (dataSnapshot.hasChild("creator")) {
                        String creatorValue = dataSnapshot.child("creator").getValue(String.class);

                        // Check if the user is marked as a creator
                        if ("1".equals(creatorValue)) {
                            // The user is a creator, so call the method to show follow count
                            showFollowCount();
                        }
                        // If the value is not "1", do nothing as the user is not a creator
                    } else {
                        // Handle the case where "creator" field is missing, if needed
                        Log.d("UserCheck", "The 'creator' field is missing for user: " + currentUserId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("FirebaseError", "Failed to check user type: " + databaseError.getMessage());
                }
            });
        } else {
            // Handle the case where there is no signed-in user
            Log.d("UserCheck", "No user is currently signed in.");
        }
    }

    private void showFollowCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference ConnectRef = FirebaseDatabase.getInstance().getReference().child("Connects").child(userId).child("Followers");

            ConnectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long followersCount = dataSnapshot.getChildrenCount(); // Get the count of followers
                    // Find the TextView and make it visible
                    textViewFollowCount.setVisibility(View.VISIBLE); // Make the TextView visible
                    textViewFollowCount.setText("("+String.valueOf(followersCount)+ ")" + " Followers"); // Set the followers count
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("FirebaseError", "Failed to get followers count: " + databaseError.getMessage());
                }
            });
        } else {
            // Handle the case where there is no signed-in user
            Log.d("UserCheck", "No user is currently signed in.");
        }
    }

}