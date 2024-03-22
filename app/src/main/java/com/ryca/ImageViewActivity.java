package com.ryca;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView imageView = findViewById(R.id.full_image_view);
        ImageView ryca = findViewById(R.id.imageView3);

        // Retrieve image URL from intent
        String imageUrl = getIntent().getStringExtra("IMAGE_URL");
        String fromWhere = getIntent().getStringExtra("fromWhere");

        if ("1".equals(fromWhere)) {
            ryca.setVisibility(View.GONE);
        }

        // Load image into ImageView
        Picasso.get()
                .load(imageUrl)
                .into(imageView);

//
//        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                // Detect swipe down gesture
//                if (e1.getY() < e2.getY()) {
//                    // Finish the activity when a swipe-down gesture is detected
//                    finish();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        imageView.setOnTouchListener(new ImageView.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // Pass the touch events to the GestureDetector
//                gestureDetector.onTouchEvent(event);
//                return true;
//            }
//        });
    }




}