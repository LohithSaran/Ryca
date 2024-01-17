package com.ryca;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.ryca.Fragments.CircleFragment;
import com.ryca.Fragments.CreatorsShowroom;
import com.ryca.Fragments.HomeFragment;

public class HomeActivity extends AppCompatActivity {

    private boolean isNavigationEnabled = true;
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Check for intent extra to determine the initial fragment
        String fragmentType = getIntent().getStringExtra("fragment");
        if (fragmentType != null) {
            Fragment selectedFragment = null;

            switch (fragmentType) {
                case "home":
                    selectedFragment = new HomeFragment();
                    break;
                case "circle":
                    selectedFragment = new CircleFragment();
                    break;
                case "profile":
                    selectedFragment = new ProfileFragment();
                    // Set the selected item to the "Profile" menu item
                    bottomNavigationView.setSelectedItemId(R.id.profilenav);
                    break;
                case "creatorsShowroom":
                    // Handle navigation to CreatorsShowroom here
                     handleCreatorsShowroomNavigation();
                    selectedFragment = new CreatorsShowroom();
                    bottomNavigationView.setSelectedItemId(R.id.circlenav);

                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {

            // If no fragment extra, default to HomeFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, new HomeFragment())
                    .addToBackStack(null)
                    .commit();

        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    if (isNavigationEnabled) {
                        Fragment selectFragment = null;

                        int itemId = item.getItemId();

                        if (itemId == R.id.homenav) {
                            selectFragment = new HomeFragment();
                        } else if (itemId == R.id.circlenav) {
                            selectFragment = new CircleFragment();
                        } else if (itemId == R.id.profilenav) {
                            selectFragment = new ProfileFragment();

                            // Disable navigation to prevent multiple clicks
                            isNavigationEnabled = false;

                            // Add your existing code for handling the profile navigation item click

                            // Optionally, you can enable navigation after a certain delay or operation completion
                            new Handler().postDelayed(() -> isNavigationEnabled = true, 750); // Enable after 2 seconds
                            // Or enable it after some specific operation completes
                        }

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.framelayout, selectFragment)
                                .addToBackStack(null)
                                .commit();

                        return true;
                    }
                    return false; // Disable navigation if isNavigationEnabled is false
                }
            };



    private void handleCreatorsShowroomNavigation() {
        // Get necessary data for CreatorsShowroom from intent extras

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String userId = extras.getString("userId");
            // rest of the code

            CreatorsShowroom creatorsShowroom = CreatorsShowroom.newInstance(userId);

            getMyData(userId);


            // Perform fragment transaction to replace the current fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.framelayout, creatorsShowroom)
                    .addToBackStack(null)
                    .commit();

        }
    }


    public Bundle getMyData(String userId) {
        Bundle hm = new Bundle();
        hm.putString("userId",userId);
       // Toast.makeText(this, "HI :"+userId , Toast.LENGTH_SHORT).show();

        return hm;
    }


//    private void handleCreatorsShowroomNavigation() {
//        // Get necessary data for CreatorsShowroom from intent extras
//        String userId = getIntent().getStringExtra("userId");
//
//        // Create an instance of CreatorsShowroomFragment
//        CreatorsShowroom creatorsShowroom = new CreatorsShowroom();
//
//        // Pass any necessary data to the fragment (e.g., user details)
//        Bundle bundle = new Bundle();
//        bundle.putString("userId", userId);
//        // Add more data if needed
//
//        creatorsShowroom.setArguments(bundle);
//
//        // Perform fragment transaction to replace the current fragment
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.framelayout, creatorsShowroom)
//                .addToBackStack(null)
//                .commit();
//    }



    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        // After logging out, redirect the user to the login screen
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish the current activity to prevent going back to the home screen
    }
}
