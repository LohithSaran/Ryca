package com.ryca.MenuCodes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.ryca.LoginActivity;
import com.ryca.R;

import java.io.File;

public class Settings extends AppCompatActivity {

    ImageView about,gethelp,termandcond,accsettings;
    TextView abouttxt,gethelptxt,termandcondtxt,accsettingstxt,logouttxt;
    RelativeLayout aboutrl,gethelprl,termandconrl,accsettingsrl, logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        about = findViewById(R.id.aboutgobtn);
        gethelp = findViewById(R.id.gethelpbtn);
        termandcond = findViewById(R.id.termsbtn);
        accsettings = findViewById(R.id.accSettingsbtn);

        abouttxt = findViewById(R.id.abouttext);
        gethelptxt = findViewById(R.id.getHelp);
        termandcondtxt = findViewById(R.id.termstext);
        accsettingstxt = findViewById(R.id.accsettingstext);
        logouttxt = findViewById(R.id.logoutext);

        aboutrl = findViewById(R.id.relativLayoutAboutUs);
        gethelprl = findViewById(R.id.relativLayoutGetHelp);
        termandconrl = findViewById(R.id.relativLayoutTerms);
        accsettingsrl = findViewById(R.id.relativAcctSettings);
        logout = findViewById(R.id.relativLayoutLogout);


        accsettingsrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, AccountSettings.class);
                startActivity(intent);
            }
        });

        logouttxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout action here
                // For example, clear session data and navigate to login screen
                showLogoutConfirmationDialog();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout action here
                // For example, clear session data and navigate to login screen
                showLogoutConfirmationDialog();

            }
        });

    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Confirmation");
        builder.setMessage("Are you sure you want to log out? Your app's updated features will be deleted when you log out. It's not a big issue as you can always update to the latest version on the Play Store.\n\nNote: Before logging in again, it is recommended to update the app in the Play Store to benefit from the latest features.");

        // Add buttons
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Perform logout action
                logoutUser();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User cancelled the dialog, do nothing
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void logoutUser() {
        // Clear session data or perform any other necessary logout actions
        // For Firebase Authentication logout:
        FirebaseAuth.getInstance().signOut();

        // Delete all shared preferences files
        deleteAllSharedPreferenceFiles();

        // Clear cached data
        clearCachedData();

        // Clear app data (including databases, files, etc.)
        clearAppData();

        // After logout, navigate to the login screen
        Intent intent = new Intent(Settings.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close the current activity to prevent going back to it with back button
    }


    private void deleteAllSharedPreferenceFiles() {
        // Get the directory where shared preference files are stored
        File sharedPrefsDir = new File(getApplicationInfo().dataDir + "/shared_prefs");

        // Get a list of all shared preference files
        File[] sharedPrefsFiles = sharedPrefsDir.listFiles();

        // Delete each shared preference file
        if (sharedPrefsFiles != null) {
            for (File file : sharedPrefsFiles) {
                file.delete();
            }
        }
    }

    private void clearCachedData() {
        // Clear cache directory in internal storage
        deleteDirectory(getCacheDir());

        // Clear cache directory in external storage (if available)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteDirectory(getExternalCacheDir());
        }
    }

    private void deleteDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    private void clearAppData() {
        // Clear app data (including databases, files, etc.)
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear com.ryca");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}