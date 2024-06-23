package com.ryca;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .transform(new ResizeTransformation(2400))
                    .into(imageView);
        } else {
            // Handle empty or null imageUrl, e.g., display a placeholder image
            imageView.setImageResource(R.drawable.profimage);
            Toast.makeText(this, "No image to show!", Toast.LENGTH_SHORT).show();
        }
    }

    public class ResizeTransformation implements Transformation {

        private final int maxSize;

        public ResizeTransformation(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result = null;

            if (source != null) {
                int width = source.getWidth();
                int height = source.getHeight();

                float bitmapRatio = (float) width / (float) height;

                if (bitmapRatio > 1) {
                    width = maxSize;
                    height = (int) (width / bitmapRatio);
                } else {
                    height = maxSize;
                    width = (int) (height * bitmapRatio);
                }

                result = Bitmap.createScaledBitmap(source, width, height, true);
                source.recycle();
            }

            return result;
        }

        @Override
        public String key() {
            return "resize()";
        }
    }


}