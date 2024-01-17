package com.ryca.Profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ryca.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileGridAdapter extends RecyclerView.Adapter<ProfileGridAdapter.PostViewHolder> {
    private List<String> postUrls; // List of post image URLs
    private Context context; // Add a reference to the context

    private String profilePictureUrl;
    private String username;
    private String address;
    private String userId;
    private List<String> PostId;
    private boolean fragmentState;


    public ProfileGridAdapter(Context context, List<String> postUrls,
                              String profilePictureUrl, String username, String address, String userId, List<String> PostId, boolean b) {
        this.context = context;
        this.postUrls = postUrls;
        this.profilePictureUrl = profilePictureUrl;
        this.username = username;
        this.address = address;
        this.userId = userId;
        this.PostId = PostId;
        this.fragmentState = b;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // Load and display post image using Picasso
        String postUrl = postUrls.get(position);
        String postkeyy = PostId.get(position);
        Picasso.get().load(postUrl).into(holder.postImageView);

        // Add a click listener to each item to show a Toast with the post URL
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + postkeyy, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, SinglePostView.class);
            // Add extra data (user and post details) to the intent
            // Replace the placeholders with actual data from your model or database
            intent.putExtra("username", username);
            intent.putExtra("userAddress", address);
            intent.putExtra("userProfileImage", profilePictureUrl);
            intent.putExtra("userId", userId);
            intent.putExtra("FromFragment", fragmentState);
            intent.putExtra("ClickedPosition", postkeyy);
            intent.putStringArrayListExtra("PostKeyArray", (ArrayList<String>) PostId);

            // Start SinglePostView activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postUrls.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
        }
    }
}
