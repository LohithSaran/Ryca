package com.ryca.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import com.ryca.R;
import com.ryca.User;
import com.ryca.UserAdapter;

import java.util.ArrayList;
import java.util.Collections;
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

//                String firebaseUserID = firebaseUser != null ? firebaseUser.getUid() : "";
//
//                if (firebaseUserID.equals(userID)) {
//                    // If the clicked user is the current user, replace the fragment with ProfileFragment
//                    ProfileFragment profileFragment = new ProfileFragment();
//                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                    fragmentTransaction.replace(R.id.framelayout, profileFragment);
//                    fragmentTransaction.addToBackStack(null);
//                    fragmentTransaction.commit();
//                    Toast.makeText(requireContext(), "This is not a joke, brother", Toast.LENGTH_SHORT).show();
//
//                } else {
                // If not, proceed with the current method
                CreatorsShowroom creatorsShowroomFragment = new CreatorsShowroom();
                creatorsShowroomFragment.setArguments(bundle);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.framelayout, creatorsShowroomFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
//            }

        });


        DisplayConnectUsers();

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
                final long[] usersAdded = {0};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String connectID = ds.getValue(String.class);

                    DatabaseReference ConnectIDRef = ConnectedUserRef.child(connectID);
                    ConnectIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String ShopName = snapshot.child("Shop Name").getValue(String.class);
                            String Location = snapshot.child("Location").getValue(String.class);
                            String city = snapshot.child("City").getValue(String.class);
                            String ProfilePicture = snapshot.child("Profile picture").getValue(String.class);

                            User user = new User();
                            user.setId(connectID);
                            user.setUsername(ShopName);
                            user.setAdd(Location);
                            user.setCity(city);
                            user.setImageurl(ProfilePicture);

                            ConnectuserList.add(user);
                            Collections.reverse(ConnectuserList);
                            usersAdded[0]++;

                            // Notify the adapter only when all the user data is added

                            ConnectuserAdapter.notifyDataSetChanged();

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

}