package com.ryca;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapterImage extends PagerAdapter {

    private Context context;
    private ArrayList<?> ImageUrls;
    private LayoutInflater layoutInflater;

    public PostAdapterImage(Context context, ArrayList<Uri> imageUrls) {
        this.context = context;
        ImageUrls = imageUrls;
        layoutInflater = LayoutInflater.from(context);

        // Log the received image URLs
        for (Object imageUrl : ImageUrls) {
            Log.d("ImageURL", "Received image URL: " + imageUrl.toString());
        }
    }

    public PostAdapterImage(Context context, List<String> imageUrls) {
        this.context = context;
        ImageUrls = (ArrayList<?>) imageUrls;
        layoutInflater = LayoutInflater.from(context);
    }

    public PostAdapterImage(Context context, ArrayList<?> imageUrls, Class<?> type) {
        this.context = context;
        ImageUrls = convertUrls(imageUrls, type);
        layoutInflater = LayoutInflater.from(context);
    }

    private ArrayList<?> convertUrls(ArrayList<?> urls, Class<?> type) {
        ArrayList<Object> convertedUrls = new ArrayList<>(urls.size());
        for (Object url : urls) {
            if (type == Uri.class && url instanceof String) {
                convertedUrls.add(Uri.parse((String) url));
            } else {
                convertedUrls.add(url);
            }
        }
        return convertedUrls;
    }

    @Override
    public int getCount() {
        return ImageUrls.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.showimageslayout, container, false);
        ImageView imageView = view.findViewById(R.id.SelectedImage);

        // Directly use the Uri from ImageUrls
        Uri imageUri = (Uri) ImageUrls.get(position);

        Picasso.get()
                .load(imageUri)
                .fit()
                .centerCrop(Gravity.TOP)
                .into(imageView);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start the ImageViewActivity
                Intent intent = new Intent(context, ImageViewActivity.class);

                // Put the image URL in the intent
                intent.putExtra("IMAGE_URL", imageUri.toString()); // Convert Uri to String

                // Start the activity
                context.startActivity(intent);
            }
        });


        container.addView(view);
        Log.d("ViewPager", "Setting up for position: " + position + ", Image URL: " + imageUri.toString());

        return view;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
