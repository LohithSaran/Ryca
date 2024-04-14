package com.ryca;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class Registration extends AppCompatActivity {

    EditText enteremailid, enterusername, enterpassword;
    ImageView signupbtn, contgoogbtn, showpassword;
    TextView loginpagebtn;
    ProgressDialog progressDialog;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("users");
    private static final String PREF_NAME = "MyAppPreferences";

    private static final int RC_SIGN_IN = 9001; // You can choose any value, it's a request code for the Google Sign-In activity.

    private FirebaseAuth.AuthStateListener authStateListener;
    private boolean isRegistrationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        enteremailid = findViewById(R.id.enteremailid);
        enterusername = findViewById(R.id.enterusername);
        enterpassword = findViewById(R.id.enterpassword);
        signupbtn = findViewById(R.id.signupbtn);
        contgoogbtn = findViewById(R.id.creatacntbtn);
        loginpagebtn = findViewById(R.id.loginpgbtn);
        showpassword = findViewById(R.id.showpassw);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int inputType = enterpassword.getInputType();
                if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    enterpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showpassword.setImageResource(R.drawable.showpsw);
                } else {
                    enterpassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showpassword.setImageResource(R.drawable.hidepsw);
                }
                enterpassword.setSelection(enterpassword.getText().length());
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                . requestScopes(new Scope(Scopes.EMAIL))
                .requestEmail()
                .requestIdToken("315441533670-l8fcsnuhpec6mco0lj1iec8e46kck059.apps.googleusercontent.com")
                .build();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // Handle connection failure
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        contgoogbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                progressDialog.show();
//                progressDialog.dismiss();
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }

        });


        loginpagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Registration.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = enteremailid.getText().toString();
                String username = enterusername.getText().toString();
                String password = enterpassword.getText().toString();

                // Define your restrictions
                int minPasswordLength = 6;


                if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    showErrorDialog("Please fill in all fields");
                } else if (password.length() < minPasswordLength) {
                    showErrorDialog("Password must be at least " + minPasswordLength + " characters long");
                } else if (!isValidEmail(email)) {
                    showErrorDialog("Please enter a valid email address");
                } else {
                    progressDialog.show();
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Registration.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> emailTask) {
                                                            if (emailTask.isSuccessful()) {
                                                                Toast.makeText(Registration.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();

                                                                // Show the verification dialog
                                                                showVerificationDialog();

                                                                // Save the username in the database
                                                                String uid = user.getUid();
                                                                DatabaseReference userRef = usersRef.child(uid);
                                                                userRef.child("username").setValue(username);
                                                                userRef.child("Location").setValue("");
                                                                userRef.child("City").setValue("");
                                                                userRef.child("CurrentLocation").setValue("");
                                                                userRef.child("shop description").setValue("");
                                                                userRef.child("No of post").setValue(0);
                                                                userRef.child("No of followers").setValue(0);
                                                                userRef.child("Profile picture").setValue("");
                                                                userRef.child("Whatsapp number").setValue("");
                                                                userRef.child("Email ID").setValue("");
                                                                userRef.child("creator").setValue("0");
                                                                progressDialog.dismiss();
                                                            } else {
                                                                // If sending verification email fails, delete the user
                                                                user.delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> deleteTask) {
                                                                                if (deleteTask.isSuccessful()) {
                                                                                    showErrorDialog("Account deleted due to unverified email.");
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        // If registration fails, display a message to the user.
                                        String errorMessage = task.getException().getMessage();
                                        showErrorDialog("Registration failed: " + errorMessage);
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }


            }
        });

    }



    private void onLoginSuccess() {
        // Set the flag to indicate that the user is now logged in
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean("isUserLoggedIn", true).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }



    private void showVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Email")
                .setMessage("Verify the link that was sent to your email address.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (user.isEmailVerified()) {
                                            // User is verified, log them in and redirect to HomeActivity


                                            // THE PROBLEM IS HERE

                                            onLoginSuccess();

                                            Intent intent = new Intent(Registration.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(Registration.this, "Email not verified. Please verify and press continue.", Toast.LENGTH_LONG).show();
                                            //showErrorDialog("Email not verified. Please verify and try again to log in.");
                                            showVerificationDialog();

                                        }
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Resend Verification", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Registration.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                                            } else {
                                                showErrorDialog("Failed to send verification email, try to log in or continue with google.");

                                            }
                                        }
                                    });
                        }
                    }
                })
                .show();
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                // Check if the user already exists in Firebase Authentication
                                boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                                if (isNewUser) {
                                    // This is a new user, proceed with your logic
                                    String googleUsername = account.getDisplayName();

                                    String uid = user.getUid();
                                    DatabaseReference userRef = usersRef.child(uid);
                                    userRef.child("username").setValue(googleUsername);
                                    userRef.child("Location").setValue("");
                                    userRef.child("City").setValue("");
                                    userRef.child("CurrentLocation").setValue("");
                                    userRef.child("shop description").setValue("");
                                    userRef.child("No of post").setValue(0);
                                    userRef.child("No of followers").setValue(0);
                                    userRef.child("Profile picture").setValue("");
                                    userRef.child("Whatsapp number").setValue("");
                                    userRef.child("Email ID").setValue("");
                                    userRef.child("creator").setValue("0");
                                }
                                onLoginSuccess();
                                Intent intent = new Intent(Registration.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Registration.this, "Authentication Failed.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }



    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle("Error")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
