package com.ryca;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ryca.Fragments.CreatorsShowroom;
import com.ryca.Fragments.HomeFragment;

public class ShowroomViewFromHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showroom_view_from_home);

        // Retrieve extras from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String fragmentType = extras.getString("fragment");
            String userId = extras.getString("userId");
            if (fragmentType != null) {
                // Replace the FrameLayout with the appropriate fragment
                Fragment fragment;
                switch (fragmentType) {
                    case "profile":
                        fragment = new ProfileFragment();
                        break;
                    case "creatorsShowroom":
                        fragment = CreatorsShowroom.newInstance(userId);
                        break;
                    default:
                        fragment = new HomeFragment(); // Default fragment
                        break;
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.framelayout, fragment)
                        .commit();
            }
        }
    }

    public Bundle getMyData(String userId) {
        Bundle hm = new Bundle();
        hm.putString("userId",userId);
        return hm;
    }
}