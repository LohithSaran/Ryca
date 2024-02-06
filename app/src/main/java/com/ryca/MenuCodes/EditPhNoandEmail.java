package com.ryca.MenuCodes;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ryca.R;

public class EditPhNoandEmail extends AppCompatActivity {

    EditText updateMobNo, updateEmailId;
    TextView headText;
    ImageView nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ph_noand_email);

        updateEmailId = findViewById(R.id.updateEmail);
        updateMobNo = findViewById(R.id.updatePhoneNumber);
        nextBtn = findViewById(R.id.nextBtnui);
        headText = findViewById(R.id.textViewhead);

        Intent intent = getIntent();
        if (intent != null) {
            boolean fromWhere = intent.getBooleanExtra("FromWhere", false);

            if (fromWhere) {
                updateMobNo.setVisibility(View.GONE);
                headText.setText("Update contact Email ID");
            } else {
                updateMobNo.setVisibility(View.VISIBLE);
                updateEmailId.setVisibility(View.GONE);
                headText.setText("Update whatapp number");
            }

            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String updateEmail = updateEmailId.getText().toString();
                    String updateWpNumber = updateMobNo.getText().toString();

                    // checks if there is any data entered in the field
                    if (fromWhere == true){
                        if (!TextUtils.isEmpty(updateEmail)){
                            updateInfo(fromWhere, updateEmail, updateWpNumber);

                        }
                        else {
                            finish();
                        }
                    }

                    if (fromWhere != true){
                        if (!TextUtils.isEmpty(updateWpNumber)){
                            updateInfo(fromWhere, updateEmail, updateWpNumber);
                        }
                    }
                    else {
                        finish();
                    }

                   // Toast.makeText(EditPhNoandEmail.this, "Update Email: " + updateEmail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void updateInfo(boolean fromWhere, String updateEmailInfo, String updateWpNumber) {
        if (fromWhere == true) {

            if (updateEmailInfo != null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Get a reference to the user's node in the database
                    String uid = user.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                    userRef.child("Email ID").setValue(updateEmailInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditPhNoandEmail.this, "Your Email ID " + updateEmailInfo + " has been updated successfully!  ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

            }
            else {
                Toast.makeText(this, "Fill the fucker", Toast.LENGTH_SHORT).show();
            }
        }

        if (fromWhere == false) {
            if (updateWpNumber != null) {

                    // Check if the phone number starts with the country code
                    if (!updateWpNumber.startsWith("+91")) {
                        // If not, add the country code for India
                        updateWpNumber = "+91" + updateWpNumber;
                    }

                    // Format the phone number to international E.164 format
                    updateWpNumber = PhoneNumberUtils.formatNumberToE164(updateWpNumber, "IN");

                    Intent intent = new Intent(EditPhNoandEmail.this, EditOtpVerify.class);
                    intent.putExtra("phoneNumber", updateWpNumber);
                    startActivity(intent);
                    finish();

            }
            else {
                Toast.makeText(this, "Please enter your whatsapp number first", Toast.LENGTH_SHORT).show();
            }

        }

    }
}