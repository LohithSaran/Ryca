package com.ryca.Profile;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.ryca.R;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<?> ImageUrls;
    private LayoutInflater layoutInflater;
    private OnImageClickListener listener;


    public ViewPagerAdapter(Context context, ArrayList<Uri> imageUrls, OnImageClickListener listener) {
        this.context = context;
        ImageUrls = imageUrls;
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;

        // Log the received image URLs
        for (Object imageUrl : ImageUrls) {
            Log.d("ImageURL", "Received image URL: " + imageUrl.toString());
        }
    }

    public ViewPagerAdapter(Context context, List<String> imageUrls, OnImageClickListener listener) {
        this.context = context;
        ImageUrls = (ArrayList<?>) imageUrls;
        layoutInflater = LayoutInflater.from(context);
    }

    public interface OnImageClickListener {
        void onImageClicked(int position);
    }

//    public ViewPagerAdapter(Context context, ArrayList<?> imageUrls, Class<?> type) {
//        this.context = context;
//        ImageUrls = convertUrls(imageUrls, type);
//        layoutInflater = LayoutInflater.from(context);
//    }

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

    public void updateImages(ArrayList<Uri> newImageUrls) {
        ImageUrls = newImageUrls;
        notifyDataSetChanged();
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

        Glide.with(context)
                .load(imageUri)
                .fitCenter()
                .into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageClicked(position);
                }

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
