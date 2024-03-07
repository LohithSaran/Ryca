package com.ryca;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
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

        // Immediately handle intent to navigate to specific fragment if specified
        handleIntentNavigation();
    }

    private void handleIntentNavigation() {
        // Extract fragment type and possibly userId from the intent
        String fragmentType = getIntent().getStringExtra("fragment");
        String userId = getIntent().getStringExtra("userId");

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
                    break;
                case "creatorsShowroom":
                    if (userId != null) {
                        selectedFragment = CreatorsShowroom.newInstance(userId);
                    }
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, selectedFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }else {
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
                        Fragment selectedFragment = null;
                        int itemId = item.getItemId();
                        if (itemId == R.id.homenav) {
                            selectedFragment = new HomeFragment();
                        } else if (itemId == R.id.circlenav) {
                            selectedFragment = new CircleFragment();
                        } else if (itemId == R.id.profilenav) {
                            selectedFragment = new ProfileFragment();
                            // Re-enable navigation here if needed
                            // Disable navigation to prevent multiple clicks
                            isNavigationEnabled = false;
                            // Add your existing code for handling the profile navigation item click
                            // Optionally, you can enable navigation after a certain delay or operation completion
                            new Handler().postDelayed(() -> isNavigationEnabled = true, 750); // Enable after 2 seconds
                            // Or enable it after some specific operation completes
                        }

                        if (selectedFragment != null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.framelayout, selectedFragment)
                                    .addToBackStack(null)
                                    .commit();
                            return true;
                        }
                    }
                    return false; // Disable navigation if isNavigationEnabled is false
                }
            };

    public Bundle getMyData(String userId) {
        Bundle hm = new Bundle();
        hm.putString("userId",userId);
       // Toast.makeText(this, "HI :"+userId , Toast.LENGTH_SHORT).show();

        return hm;
    }

}
