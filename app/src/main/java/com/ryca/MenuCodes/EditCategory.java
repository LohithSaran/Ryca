package com.ryca.MenuCodes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class EditCategory extends AppCompatActivity {

    List<EditCategoryModel> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        // Initialize dataList before using it
        dataList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerEditCategory);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        EditCategoryAdapter editcategoryAdapter = new EditCategoryAdapter(this, dataList);
        recyclerView.setAdapter(editcategoryAdapter);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Assuming you have a reference to your database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Creators").child(userId).child("Category");

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                        // Assuming each category contains a field named "data"
                        String dataValue = categorySnapshot.getValue(String.class);

                        // Create an instance of your data model and add it to the list
                        EditCategoryModel categoryModel = new EditCategoryModel(dataValue);
                        dataList.add(categoryModel);
                    }

                    editcategoryAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }
    }
}
