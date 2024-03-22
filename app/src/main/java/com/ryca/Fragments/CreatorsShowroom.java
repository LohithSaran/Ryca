package com.ryca.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ryca.HomeActivity;
import com.ryca.ImageViewActivity;
import com.ryca.Profile.ProfileGridAdapter;
import com.ryca.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreatorsShowroom extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_USER_ID = "userId";

    private String mParam1;
    private String mParam2;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout profileLayout;
    private int appBarLayoutHeight;
    FirebaseAuth authProfile;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    Bundle args = getArguments();
    TextView connectCount, address;
    int ModifyConnectCount;
    String ReportName;


    String userId;

    public CreatorsShowroom() {
        // Required empty public constructor
    }

    public static CreatorsShowroom newInstance(String userId) {
        CreatorsShowroom fragment = new CreatorsShowroom();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_creators_showroom, container, false);

        appBarLayout = rootView.findViewById(R.id.appBarLayout);
        profileLayout = rootView.findViewById(R.id.collapsingToolbar);
        TextView connect = rootView.findViewById(R.id.connectcs);

        ImageView sortRateImageView = rootView.findViewById(R.id.sortRatecs);
        ImageView whatsapp = rootView.findViewById(R.id.whatsappcs);
        ImageView menucs = rootView.findViewById(R.id.menucs);

        connectCount = rootView.findViewById(R.id.connectcount);
        address = rootView.findViewById(R.id.bio);
        address.setSelected(true);

        Bundle args = getArguments();

        if (args != null) {

            String USERid = args.getString(ARG_USER_ID);

            HomeActivity activity = (HomeActivity) getActivity();

            Bundle result = activity.getMyData(USERid);
            userId = result.getString("userId").toString().trim();


        } else {
            Toast.makeText(requireContext(), "Error retrieving the data, please try again later", Toast.LENGTH_SHORT).show();
        }

        populateCategorySpinner(rootView);
        sortRateImageView.setOnClickListener(v -> showSortRateDialog(rootView));

        DatabaseReference connectref = FirebaseDatabase.getInstance().getReference("Connects").child(userId).child("Followers");


        connectref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followingCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Increment the count for each child
                    followingCount++;
                }
                DatabaseReference countRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                countRef.child("No of followers").setValue(followingCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded() && activity.getApplicationContext() != null) {
                            if (dataSnapshot.exists()) {


                                // Retrieve user data
                                String profilePictureUrl = dataSnapshot.child("Profile picture").getValue(String.class);
                                String username = dataSnapshot.child("username").getValue(String.class);
                                String location = dataSnapshot.child("Location").getValue(String.class);
                                String city = dataSnapshot.child("City").getValue(String.class);
                                String whatsappNumber = dataSnapshot.child("Whatsapp number").getValue(String.class);
                                String emailContact = dataSnapshot.child("Email ID").getValue(String.class);
                                int connectsCount = dataSnapshot.child("No of followers").getValue(Integer.class);
                                int postCount = dataSnapshot.child("No of post").getValue(Integer.class);
//                                Object postCountObject = dataSnapshot.child("No of post").getValue();
//                                Long postCount = 0L; // Default value if conversion fails or data is null
//
//                                if (postCountObject instanceof Long) {
//                                    postCount = (Long) postCountObject;
//                                } else if (postCountObject instanceof String) {
//                                    try {
//                                        postCount = Long.parseLong((String) postCountObject);
//                                    } catch (NumberFormatException e) {
//                                        // Handle the case where the String cannot be parsed to Long
//                                        e.printStackTrace(); // You might want to log this error for debugging
//                                    }
//                                }
                                String description = dataSnapshot.child("shop description").getValue(String.class);

                                // Set user data in the respective views
                                ImageView profilePictureImageView = rootView.findViewById(R.id.profilePicturecs);
                                TextView usernameTextView = rootView.findViewById(R.id.username);
                                TextView bioTextView = rootView.findViewById(R.id.bio);
                                TextView postCountTextView = rootView.findViewById(R.id.postcountcs);
                                TextView descriptionTextView = rootView.findViewById(R.id.storedescriptioncs);
                                ImageView email = rootView.findViewById(R.id.emailcs);


                                profilePictureImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String imageUrl = profilePictureUrl;

                                        // Create an intent to start ImageViewActivity
                                        Intent intent = new Intent(getContext(), ImageViewActivity.class);

                                        // Put the image URL in the intent
                                        intent.putExtra("IMAGE_URL", imageUrl);
                                        intent.putExtra("fromWhere", "1");

                                        startActivity(intent);
                                    }
                                });

                                whatsapp.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String whatsAppUrl = "https://wa.me/"+whatsappNumber+"?text=Interested Inquiry Regarding Your Establishments on Ryca App";

                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(whatsAppUrl));
                                        startActivity(intent);
                                    }
                                });

                                email.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String subject = "Inquiry Regarding Your Establishments on Ryca App";
                                        String mailto = "mailto:" + emailContact + "?subject=" + Uri.encode(subject);


                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.setData(Uri.parse(mailto));

                                        startActivity(intent);
                                    }
                                });

                                // Set profile picture
                                if (TextUtils.isEmpty(profilePictureUrl)) {
                                    // If Profile Picture field is empty, set user's Google account image
                                    setGoogleAccountImage(profilePictureImageView);
                                } else {
                                    // If Profile Picture field is not empty, load and set the image using a library like Picasso or Glide
                                    // Example using Glide:
                                    int targetWidth = profilePictureImageView.getWidth();
                                    int targetHeight = profilePictureImageView.getHeight();

                                    // Load and set the image using Picasso with crop and resize
                                    Picasso.get()
                                            .load(profilePictureUrl)
                                            .fit()
                                            .centerCrop(Gravity.TOP)
                                            .into(profilePictureImageView);
                                }

                                // Set other user information
                                usernameTextView.setText(username);
                                bioTextView.setText(String.format("%s, %s", location, city));
                                postCountTextView.setText(postCount + " Exhibit");
                                connectCount.setText(connectsCount + " Connects");
                                ModifyConnectCount = connectsCount;
                                descriptionTextView.setText(description);



                                // Check the "creator" field and adjust visibility
                                String creatorValueStr = dataSnapshot.child("creator").getValue(String.class);

                                if (creatorValueStr != null) {
                                    try {
                                        int creatorValue = Integer.parseInt(creatorValueStr);

                                        if (creatorValue == 1) {
                                            // User is a creator (1), show all fields
                                            showAllFields(rootView);
                                        } else {
                                            // User is not a creator (0), hide some fields
                                            hideSomeFields(rootView);
                                        }
                                    } catch (NumberFormatException e) {
                                        // Handle the case where the value cannot be parsed as an integer
                                        Toast.makeText(requireContext(), "Invalid exhibitors value format", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Handle the case where the value is null
                                    Toast.makeText(requireContext(), "Exhibitors value is null", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors
                        Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

       String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


// Set the initial text of connectTextView based on whether the user is already following the creator
        checkIfFollowing(connect, currentUserId, userId);


        connect.setOnClickListener(v -> {
            // Use the current user's ID and the creator's ID to handle the follow action
            handleFollowAction(currentUserId, userId, connect);
        });


            showProfileInfo();

            // Check if the AppBarLayout is fully collapsed, if yes, hide the profile information
            if (appBarLayout.getHeight() + appBarLayout.getTop() == 0) {
                // hideProfileInfo();
            } else {
                showProfileInfo();
            }

            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isImageVisible = false;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    int range = appBarLayout.getTotalScrollRange();

                    // If the vertical offset is equal to the total scroll range, the AppBarLayout is fully expanded
                    if (verticalOffset == 0) {
                        if (!isImageVisible) {
                            // The AppBarLayout is fully expanded, make the uploadImage visible
                            isImageVisible = true;
//                        uploadImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (isImageVisible) {
                            // The AppBarLayout is not fully expanded, make the uploadImage invisible
                            isImageVisible = false;
//                        uploadImage.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });

            // Measure the height of the AppBarLayout
            appBarLayout.post(() -> {
                appBarLayoutHeight = appBarLayout.getHeight();
            });

            final TextView usernameTextView = rootView.findViewById(R.id.username);
            final TextView bioTextView = rootView.findViewById(R.id.bio);

            //String userId = "gs7QGmnXauS9JgTIy97TGD1JPEC2";

            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Post").child(userId);

            postsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> postUrls = new ArrayList<>();
                    List<String> postKeys = new ArrayList<>();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        // Assuming each post has an "imageUrl" field
                        String imageUrl = postSnapshot.child("imageURL").getValue(String.class);
                        String postKey = postSnapshot.getKey();


                        if (imageUrl != null) {
                            postUrls.add(imageUrl);
                        }
                        if (postKey != null) {
                            postKeys.add(postKey);
                        }
                    }

                    // Reverse the order of postUrls to display the newest posts first
                    Collections.reverse(postUrls);


                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (isAdded()) {
                            if (userSnapshot.exists()) {
                                // Retrieve user data
                                String profilePictureUrl = userSnapshot.child("Profile picture").getValue(String.class);
                                String username = userSnapshot.child("username").getValue(String.class);
                                String address = userSnapshot.child("Location").getValue(String.class);
                                String city = userSnapshot.child("City").getValue(String.class);
                                String userIdd = userSnapshot.getKey();
                                ReportName = username;
                                RecyclerView photoGrid = rootView.findViewById(R.id.photoGridcs);
                                Context context = getContext(); // Safe to call getContext() after checking isAdded()
                                if (context != null) {
                                    photoGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // Adjust the span count as needed
                                    photoGrid.setAdapter(new ProfileGridAdapter(requireContext(), postUrls,
                                            profilePictureUrl, username, address, userId, postKeys, city, true, false));
                                }

                            }
                        }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error, if any
                    Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            RecyclerView photoGrid = rootView.findViewById(R.id.photoGridcs);
            photoGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    // Check the scroll direction
                    if (dy > 0) {
                        // Scrolling up, make the profile information visible
                        showProfileInfo();
                    } else {
                        // Scrolling down, hide the profile information
                        // hideProfileInfo();
                    }
                }
            });

            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isProfileInfoVisible = true;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    // Calculate the percentage of collapse
                    float percentage = (float) Math.abs(verticalOffset) / appBarLayoutHeight;

                    // Decide when to show/hide the profile information based on the percentage
                    if (percentage >= 0.8 && isProfileInfoVisible) {
                        // hideProfileInfo();
                        isProfileInfoVisible = false;
                    } else if (percentage < 0.8 && !isProfileInfoVisible) {
                        showProfileInfo();
                        isProfileInfoVisible = true;
                    }
                }
            });

        menucs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }

            private void showPopupMenu(View v) {

                PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.exhibitor_showroom_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.reportShowroom) {

                            String subject = "Feedback Regarding an Exhibitor ";
                            String recipientEmail = "rycaapp@gmail.com";

                            if (ReportName != null) {
                                String mailto = "mailto:" + recipientEmail +
                                        "?subject=" + Uri.encode(subject) +
                                        "&body=" + Uri.encode("Check out this Exhibitor: " + ReportName );


                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse(mailto));

                                startActivity(intent);
                            }


                            return true;
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
        return rootView;
    }


    private void checkIfFollowing(TextView connect, String currentUserId, String creatorId) {
        DatabaseReference connectsRef = FirebaseDatabase.getInstance().getReference("Connects");

        // Check if the current user is already following the creator
        connectsRef.child(currentUserId).child("Following").orderByValue().equalTo(creatorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User is already following the creator
                            connect.setText("Connected");
                            connect.setBackgroundResource(R.drawable.connected_bc);
                        } else {
                            // User is not following the creator
                            connect.setText("Connect");
                            connect.setBackgroundResource(R.drawable.backgroundlook);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error, if any
                    }
                });
    }

    private void handleFollowAction(String currentUserId, String creatorId, TextView connect) {
        DatabaseReference connectsRef = FirebaseDatabase.getInstance().getReference("Connects");

        // Check if the current user is already following the creator
        connectsRef.child(currentUserId).child("Following").orderByValue().equalTo(creatorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User is already following the creator, show alert for disconnection
                            showDisconnectAlert(currentUserId, creatorId,connect);
                        } else {
                            // User is not following the creator, proceed with following
                            DatabaseReference connectsRef = FirebaseDatabase.getInstance().getReference("Connects");

                            // Update current user's Following with an incremented counter
                            connectsRef.child(currentUserId).child("Following").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    long followingCount = dataSnapshot.getChildrenCount() + 1;
                                    String followingId = "id" + followingCount + "_" + System.currentTimeMillis();
                                    connectsRef.child(currentUserId).child("Following").child(followingId).setValue(creatorId);
                                    connect.setText("Connected");
                                    connect.setBackgroundResource(R.drawable.connected_bc);

                                    connectCount.setText(++ModifyConnectCount + " Connects");
                                }


                            @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle the error, if any
                                }
                            });

                            // Update creator's Followers with an incremented counter
                            connectsRef.child(creatorId).child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    long followersCount = dataSnapshot.getChildrenCount() + 1;
                                    String followersId = "id" + followersCount + "_" + System.currentTimeMillis();
                                    connectsRef.child(creatorId).child("Followers").child(followersId).setValue(currentUserId);
                                    connect.setText("Connected");
                                    connect.setBackgroundResource(R.drawable.connected_bc);

                                    //connectCount.setText(++ModifyConnectCount + " Connects");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle the error, if any
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error, if any
                    }
                });
    }


    private void showDisconnectAlert(String currentUserId, String creatorId, TextView connect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Disconnect");
        builder.setMessage("Are you sure you want to disconnect from this exhibitor?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirmed disconnection, proceed to disconnect
            disconnectUser(currentUserId, creatorId, connect);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // User chose not to disconnect, do nothing
        });

        builder.create().show();
    }

    private void disconnectUser(String currentUserId, String creatorId, TextView connect) {
        DatabaseReference connectsRef = FirebaseDatabase.getInstance().getReference("Connects");

        // Remove the corresponding IDs from both user and creator
        removeIdFromConnects(connectsRef.child(currentUserId).child("Following"), creatorId);
        removeIdFromConnects(connectsRef.child(creatorId).child("Followers"), currentUserId);
        connect.setText("Connect");
        connect.setBackgroundResource(R.drawable.backgroundlook);
        connectCount.setText(--ModifyConnectCount + " Connects");
    }

    private void removeIdFromConnects(DatabaseReference ref, String targetId) {
        ref.orderByValue().equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    childSnapshot.getRef().removeValue(); // Remove the matching child
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error, if any
            }
        });
    }

    private void populateCategorySpinner(View rootView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            //String userId = currentUser.getUid();

            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Creators").child(userId).child("Category");

            categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    List<String> categoryList = new ArrayList<>();

                    // Add "All" at the top of the list
                    categoryList.add("All");

                    for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                        String category = categorySnapshot.getValue(String.class);

                        if (category != null) {
                            categoryList.add(category);
                        }
                    }

                    Spinner sortCategorySpinner = rootView.findViewById(R.id.sortcategoryspinnercs);

                    if (categoryList != null) {
                        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryList);
                        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sortCategorySpinner.setAdapter(categoryAdapter);
                        sortCategorySpinner.setSelection(0);
                    } else {
                        Toast.makeText(getContext(), "There is an error in displaying the list.", Toast.LENGTH_SHORT).show();                    }
                    // Set "All" as the default selection

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        Spinner sortCategorySpinner = rootView.findViewById(R.id.sortcategoryspinnercs);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.sort_categories, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sortCategorySpinner.setAdapter(adapter);

        sortCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection here
                String selectedCategory = parentView.getItemAtPosition(position).toString();

                // Call a method to filter and display posts based on the selected category
                filterAndDisplayPosts(selectedCategory,rootView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }
    private void filterAndDisplayPosts(String selectedCategory, View rootView) {

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Post").child(userId);

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> postUrls = new ArrayList<>();
                List<String> postKeys = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postCategory = postSnapshot.child("category").getValue(String.class);


                    if ("All".equalsIgnoreCase(selectedCategory) || selectedCategory.equalsIgnoreCase(postCategory)) {
                        // If "All" is selected or the post's category matches the selected category
                        String imageUrl = postSnapshot.child("imageURL").getValue(String.class);
                        String postKey = postSnapshot.getKey();


                        if (imageUrl != null) {
                            postUrls.add(imageUrl);
                        }
                        if (postKey != null) {
                            postKeys.add(postKey);
                        }
                    }
                }


                    // Reverse the list to display newest posts first
                    Collections.reverse(postUrls);

                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (isAdded()) {
                            if (userSnapshot.exists()) {
                                // Retrieve user data
                                String profilePictureUrl = userSnapshot.child("Profile picture").getValue(String.class);
                                String username = userSnapshot.child("username").getValue(String.class);
                                String address = userSnapshot.child("Location").getValue(String.class);
                                String city = userSnapshot.child("City").getValue(String.class);
                                String userIdd = userSnapshot.getKey();
                                RecyclerView photoGrid = rootView.findViewById(R.id.photoGridcs);
                                Context context = getContext(); // Safe to call getContext() after checking isAdded()
                                if (context != null) {
                                    photoGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // Adjust the span count as needed
                                    photoGrid.setAdapter(new ProfileGridAdapter(requireContext(), postUrls,
                                            profilePictureUrl, username, address, userId, postKeys, city, true, false));
                                }

                            }
                        }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showSortRateDialog(View rootView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by Price");

        // Set up the layout for the dialog
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Create EditText views for min and max rates
        EditText etMinRate = new EditText(requireContext());
        etMinRate.setHint("Min Price");
        etMinRate.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etMinRate);

        EditText etMaxRate = new EditText(requireContext());
        etMaxRate.setHint("Max Price");
        etMaxRate.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etMaxRate);

        builder.setView(layout);
        String selectedCategory = getSelectedCategoryFromSpinner(rootView);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Handle the user's input
            String minRate = etMinRate.getText().toString().trim();
            String maxRate = etMaxRate.getText().toString().trim();

            // Check if the fields are empty and set default values
            if (minRate.isEmpty()) {
                minRate = "0";
            }

            if (maxRate.isEmpty()) {
                // Set maxRate to a value representing infinity
                maxRate = String.valueOf(Double.POSITIVE_INFINITY);
            }

            // Call a method to filter and display posts based on the entered rates
            filterAndDisplayPostsByRate(minRate, maxRate, selectedCategory, rootView);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do nothing, close the dialog
        });

        builder.create().show();
    }


    private String getSelectedCategoryFromSpinner(View rootView) {
        Spinner sortCategorySpinner = rootView.findViewById(R.id.sortcategoryspinnercs);
        return sortCategorySpinner.getSelectedItem().toString();
    }

    private void filterAndDisplayPostsByRate(String minRate, String maxRate, String selectedCategory,View rootView) {


        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Post").child(userId);

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> postUrls = new ArrayList<>();
                List<String> postKeys = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Check the category of the post
                    String postCategory = postSnapshot.child("category").getValue(String.class);

                    if (selectedCategory.equals("All") || selectedCategory.equals(postCategory)) {
                        // Category matches or All is selected, proceed with rate filtering
                        // Assuming each post has a "prodprice" field
                        String prodPrice = postSnapshot.child("prodprice").getValue(String.class);

                        if (prodPrice != null && isWithinRateRange(prodPrice, minRate, maxRate)) {
                            // Post falls within the rate range, add its URL to the list
                            String imageUrl = postSnapshot.child("imageURL").getValue(String.class);
                            String postKey = postSnapshot.getKey();


                            if (imageUrl != null) {
                                postUrls.add(imageUrl);
                            }
                            if (postKey != null) {
                                postKeys.add(postKey);
                            }
                        }
                    }
                }

                    Collections.reverse(postUrls);
                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (isAdded()) {
                            if (userSnapshot.exists()) {
                                // Retrieve user data
                                String profilePictureUrl = userSnapshot.child("Profile picture").getValue(String.class);
                                String username = userSnapshot.child("username").getValue(String.class);
                                String address = userSnapshot.child("Location").getValue(String.class);
                                String city = userSnapshot.child("city").getValue(String.class);
                                String userIdd = userSnapshot.getKey();
                                RecyclerView photoGrid = rootView.findViewById(R.id.photoGridcs);
                                Context context = getContext(); // Safe to call getContext() after checking isAdded()
                                if (context != null) {
                                    photoGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // Adjust the span count as needed
                                    photoGrid.setAdapter(new ProfileGridAdapter(requireContext(), postUrls,
                                            profilePictureUrl, username, address, userId, postKeys, city, true, false));
                                }

                            }
                        }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error, if any
                Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to check if a price is within the specified range
    private boolean isWithinRateRange(String price, String minRate, String maxRate) {
        try {
            double priceValue = Double.parseDouble(price);
            double minRateValue = Double.parseDouble(minRate);
            double maxRateValue = Double.parseDouble(maxRate);

            return priceValue >= minRateValue && priceValue <= maxRateValue;
        } catch (NumberFormatException e) {
            // Handle the case where parsing fails, e.g., invalid numeric input
            return false;
        }
    }


    private void setGoogleAccountImage(ImageView imageView) {
        // Obtain the user's GoogleSignInAccount object
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        if (account != null) {
            Uri photoUrl = account.getPhotoUrl();
            if (photoUrl != null) {
                // Load and set the image using Picasso or any other image loading library
                int targetWidth = imageView.getWidth();
                int targetHeight = imageView.getHeight();

                // Load and set the image using Picasso with crop and resize
                Picasso.get()
                        .load(photoUrl)
                        .fit()
                        .centerCrop(Gravity.TOP)
                        .into(imageView);
            }
        }
    }


    private void showAllFields(View rootView) {
        // Set visibility for all fields to VISIBLE
        TextView username2 = rootView.findViewById(R.id.username2cs);
        ImageView imageView = rootView.findViewById(R.id.emailcs);
        ImageView imageView3 = rootView.findViewById(R.id.whatsappcs);
        TextView storedescription = rootView.findViewById(R.id.storedescriptioncs);

        username2.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView3.setVisibility(View.VISIBLE);
        storedescription.setVisibility(View.VISIBLE);
    }

    private void hideSomeFields(View rootView) {
        // Set visibility for some fields to INVISIBLE
        TextView username2 = rootView.findViewById(R.id.username2cs);
        ImageView imageView = rootView.findViewById(R.id.emailcs);
        ImageView imageView3 = rootView.findViewById(R.id.whatsappcs);
        TextView storedescription = rootView.findViewById(R.id.storedescriptioncs);

        username2.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        storedescription.setVisibility(View.INVISIBLE);
    }

    private void hideProfileInfo() {
        profileLayout.setVisibility(View.INVISIBLE);
    }

    private void showProfileInfo() {
        profileLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }
}
