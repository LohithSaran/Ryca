package com.ryca.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ryca.R;
public class CreatorRegistration extends AppCompatActivity {

    private EditText storeAddressEditText;
    private EditText cityNameEditText;
    private EditText contEmailEditText;
    private EditText contWpNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_registration);

        // Initialize EditText fields
        storeAddressEditText = findViewById(R.id.storeaddress);
        cityNameEditText = findViewById(R.id.cityname);
        contEmailEditText = findViewById(R.id.contemail);
        contWpNumberEditText = findViewById(R.id.contwpnumber);
        ImageView nextButton = findViewById(R.id.nextbtn);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform validation and registration logic here
                if (validateFields()) {
                    // Check if the user entered a mobile number
                    String contWpNumber = contWpNumberEditText.getText().toString();
                    if (isMobileNumber(contWpNumber)) {
                        // If a mobile number is entered, start the OtpRegistrationActivity
                        startOtpRegistrationActivity();
                    } else {
                        // Otherwise, proceed with normal registration
                        performRegistration();
                    }
                }
            }
        });
    }

    private boolean validateFields() {
        // Perform validation for empty fields or any other conditions
        String storeAddress = storeAddressEditText.getText().toString();
        String cityName = cityNameEditText.getText().toString();
        String contEmail = contEmailEditText.getText().toString();
        String contWpNumber = contWpNumberEditText.getText().toString();

        if (storeAddress.isEmpty() || cityName.isEmpty() || contEmail.isEmpty() || contWpNumber.isEmpty()) {
            // Display an error message if any field is empty
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }



        // Add more validation as needed

        return true;
    }

    private boolean isMobileNumber(String number) {
        // You can implement your own logic to determine if the input is a valid mobile number
        // For simplicity, let's assume a mobile number should contain only digits and have a minimum length
        return number.matches("\\d{10,}");
    }

    private void startOtpRegistrationActivity() {
        // Retrieve the data from the EditText fields
        String storeAddress = storeAddressEditText.getText().toString();
        String cityName = cityNameEditText.getText().toString();
        String contEmail = contEmailEditText.getText().toString();
        String phoneNumber = contWpNumberEditText.getText().toString();

        // Check if the phone number is not empty
        if (!phoneNumber.isEmpty()) {
            // Check if the phone number starts with the country code
            if (!phoneNumber.startsWith("+91")) {
                // If not, add the country code for India
                phoneNumber = "+91" + phoneNumber;
            }

            // Format the phone number to international E.164 format
            phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN");

            // Check if the formatted phone number is not empty
            if (!phoneNumber.isEmpty()) {
                // Start the OtpRegistrationActivity with all the data
                Intent intent = new Intent(CreatorRegistration.this, OtpRegistration.class);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("storeAddress", storeAddress);
                intent.putExtra("cityName", cityName);
                intent.putExtra("contEmail", contEmail);
                startActivity(intent);
            } else {
                // Display an error message if the formatted phone number is empty
                Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Display an error message if the phone number is empty
            Toast.makeText(this, "Phone number is empty", Toast.LENGTH_SHORT).show();
        }
    }



    private void performRegistration() {
        // Add logic to register the creator
        // This is where you can interact with Firebase or any other backend service

        // After successful registration, you can navigate to another activity or perform any desired action
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        // Add additional actions if needed
    }
}