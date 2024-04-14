package com.ryca;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private static List<PostModel> posts;
    private List<String> postIds;
    private Context context;

    public void setData(List<String> newPostIds) {
        this.postIds = newPostIds;
    }

    public PostAdapter(List<PostModel> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout here
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to views here
        PostModel post = posts.get(position);

        if (post.isSaved()) {
            holder.savedImageView.setImageResource(R.drawable.saved);
        } else {
            holder.savedImageView.setImageResource(R.drawable.save);
        }


        PostAdapterImage viewPagerAdapter = new PostAdapterImage(context, new ArrayList<>(post.getPostImageUrl()), Uri.class);
        holder.viewPager.setAdapter(viewPagerAdapter);

//        Picasso.get()
//                .load(post.getProfilePictureUrl())
//                .fit()
//                .centerCrop(Gravity.TOP)
//                .into(holder.profilePictureImageView);

        String profilePictureUrl = post.getProfilePictureUrl();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Picasso.get()
                    .load(profilePictureUrl)
                    .fit()
                    .centerCrop(Gravity.TOP)
                    .into(holder.profilePictureImageView);
        } else {
            holder.profilePictureImageView.setImageResource(R.drawable.profile);
        }

        //      Set other data similarly
        holder.usernameTextView.setText(post.getUsername());
        holder.addressTextView.setText(post.getAddress() +", " + post.getCity());
        holder.rate.setText("₹ " + post.getRating());
        holder.category.setText(post.getCategory());
        holder.description.setText(post.getDescription());

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView;
        public ViewPager viewPager;
        TextView usernameTextView;
        TextView addressTextView, rate, category, description, interaction1, interaction2;
        ImageView savedImageView, sharePost, Menu;
        // Add other views

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImageView = itemView.findViewById(R.id.profilepicturedp);
            usernameTextView = itemView.findViewById(R.id.creatorsnamedp);
            addressTextView = itemView.findViewById(R.id.creatorsaddressdp);
            rate = itemView.findViewById(R.id.ratedp);
            category = itemView.findViewById(R.id.categorydp);
            description = itemView.findViewById(R.id.descriptiondp);
            viewPager = itemView.findViewById(R.id.dppost);
            savedImageView = itemView.findViewById(R.id.savedp);
            sharePost = itemView.findViewById(R.id.sharedp);
            Menu = itemView.findViewById(R.id.dpmenu);

            addressTextView.setSelected(true);
            rate.setSelected(true);

            usernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get the post at the clicked position
                    PostModel clickedPost = posts.get(getAdapterPosition());

                    // Get the user ID of the post
                    String userID = clickedPost.getUserId();

                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Intent intent = new Intent(itemView.getContext(), ShowroomViewFromHome.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("fragment", currentUserId.equals(userID) ? "profile" : "creatorsShowroom");
                    intent.putExtras(bundle);
                    itemView.getContext().startActivity(intent);

                }
            });

            sharePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the position of the clicked item
                    int position = getAdapterPosition();
                    // Ensure the position is valid
                    if (position != RecyclerView.NO_POSITION) {
                        PostModel clickedPost = posts.get(position);
                        String postId = clickedPost.getPostId(); // Assuming PostModel has getPostId()
                        String userId = clickedPost.getUserId(); // Assuming PostModel has getUserId()

                        // Generate the dynamic link
                        createAndShareDynamicLink(postId, userId, v.getContext());
                    }
                }
            });



            profilePictureImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get the post at the clicked position
                    PostModel clickedPost = posts.get(getAdapterPosition());

                    // Get the user ID of the post
                    String userID = clickedPost.getUserId();

                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Intent intent = new Intent(itemView.getContext(), ShowroomViewFromHome.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("fragment", currentUserId.equals(userID) ? "profile" : "creatorsShowroom");
                    intent.putExtras(bundle);
                    itemView.getContext().startActivity(intent);

                }
            });

            // Set a click listener for the savedImageView
            savedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Handle save action here
                        handleSaveAction(position);

                    }
                }
            });


//            viewPager.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Get the post at the clicked position
//                    PostModel clickedPost = posts.get(getAdapterPosition());
//
//                    // Get the image URL of the post
//                    String imageUrl = clickedPost.getPostImageUrl();
//
//                    // Create an intent to start ImageViewActivity
//                    Intent intent = new Intent(itemView.getContext(), ImageViewActivity.class);
//
//                    // Put the image URL in the intent
//                    intent.putExtra("IMAGE_URL", imageUrl);
//
//                    // Start the activity
//                    itemView.getContext().startActivity(intent);
//                }
//            });


            Menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PostModel clickedPost = posts.get(getAdapterPosition());

                    ExhibitorsMenu(v);

                }
            });

        }

        private void ExhibitorsMenu(View v){
            Context context = v.getContext(); // Use itemView.getContext() instead of Context.
            PopupMenu popupMenu = new PopupMenu(context, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.exhibitors_exhibits_menu, popupMenu.getMenu());

            // Set click listener for menu items
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (item.getItemId() == R.id.reportExhibit) {
                        int position = getAdapterPosition();
                        // Ensure the position is valid
                        if (position != RecyclerView.NO_POSITION) {
                            PostModel clickedPost = posts.get(position);
                            String postId = clickedPost.getPostId(); // Assuming PostModel has getPostId()
                            String userId = clickedPost.getUserId(); // Assuming PostModel has getUserId()

                            // Generate the dynamic link
                            createAndRreportDynamicLink(postId, userId, v.getContext());
                        }
                    }
                    return true;
                }
            });

            // Show the popup menu
            popupMenu.show();
        }

        private void createAndRreportDynamicLink(String postId, String userId, Context context) {
            // Assuming you have already set up a domain and a link format
            // Replace "https://yourapp.page.link" with your actual domain Uri prefix
            // Replace "https://yourdomain.com/post" with the URL pattern you intend to use
            // String link = "https://ryca.page.link/?link=https://yourapp.com/post?postId=" + postId + "&userId=" + userId +"&apn=com.ryca" ;

            String deepLink = "https://yourapp.com/post?postId=" + postId + "&userId=" + userId;
            String recipientEmail = "rycaapp@gmail.com";
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(deepLink))
                    .setDomainUriPrefix("https://ryca.page.link") // Your dynamic link domain
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.ryca") // Your package name
                            .build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener(shortDynamicLink -> {
                        // Short link created
                        Uri dynamicLinkUri = shortDynamicLink.getShortLink();
                        // Now, share the link

                        String subject = "Feedback Regarding an Exhibit Experience ";

                        String mailto = "mailto:" + recipientEmail +
                                "?subject=" + Uri.encode(subject) +
                                "&body=" + Uri.encode("Check out this exhibit: " + dynamicLinkUri.toString());


                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse(mailto));

                        context.startActivity(intent);

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error reporting exhibit, please try again later.", Toast.LENGTH_LONG).show());
        }


        private void createAndShareDynamicLink(String postId, String userId, Context context) {
            // Assuming you have already set up a domain and a link format
            // Replace "https://yourapp.page.link" with your actual domain Uri prefix
            // Replace "https://yourdomain.com/post" with the URL pattern you intend to use
            // String link = "https://ryca.page.link/?link=https://yourapp.com/post?postId=" + postId + "&userId=" + userId +"&apn=com.ryca" ;

            String deepLink = "https://yourapp.com/post?postId=" + postId + "&userId=" + userId;
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(deepLink))
                    .setDomainUriPrefix("https://ryca.page.link") // Your dynamic link domain
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.ryca") // Your package name
                            .build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener(shortDynamicLink -> {
                        // Short link created
                        Uri dynamicLinkUri = shortDynamicLink.getShortLink();
                        // Now, share the link
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this post: " + dynamicLinkUri.toString());
                        context.startActivity(Intent.createChooser(shareIntent, "Share Post"));
                    })
                    .addOnFailureListener(e -> Log.w("DynamicLink", "Error creating dynamic link", e));
        }



        private void handleSaveAction(int position) {
            // Get the post at the clicked position
            PostModel clickedPost = posts.get(position);

            // Perform the save action here, e.g., update the database
            savePostToFirebase(clickedPost);
        }


        private void savePostToFirebase(PostModel post) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                String PostUser = post.getUserId();

                // Assuming postID is a unique identifier for each post
                String postID = post.getPostId();
                boolean saveCheck = post.isSaved();

                String timestamp = String.valueOf(System.currentTimeMillis());
                String combinedKey = timestamp + "_" + postID;


                DatabaseReference savedReference = FirebaseDatabase.getInstance()
                        .getReference("Saved")
                        .child(userId);

                if (!saveCheck) {


                    List<String> postImageUrls = post.getPostImageUrl();

                    if (!postImageUrls.isEmpty()) {
                        // Get the first element from the list and store it in a string
                        String firstImageUrl = postImageUrls.get(0);

                        savedReference.child(combinedKey).setValue(new HashMap<String, Object>() {{
                            put("userId", PostUser);
                            put("PostId", postID);
                            put("postImage", firstImageUrl);
                        }}, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    // Firebase operation successful, update the UI


                                    savedImageView.setImageResource(R.drawable.saved);
                                    // Update the isSaved field in the post model
                                    post.setSaved(true);

                                } else {
                                    Log.e("Firebase", "Error updating save status: " + error.getMessage());
                                }
                            }
                        });

                    }

                    // ...
                } else {

                    savedReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String snapshotKey = childSnapshot.getKey();
                                if (snapshotKey != null && snapshotKey.length() >= 20) {
                                    String last20Chars = snapshotKey.substring(snapshotKey.length() - 20);
                                    if (last20Chars.equals(postID)) {
                                        // Perform remove operation
                                        Log.d("Suffix", "Removing value with key: " + snapshotKey + " tof: " + post.isSaved());
                                        savedReference.child(snapshotKey).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error == null) {
                                                    // Firebase operation successful, update the UI
                                                    savedImageView.setImageResource(R.drawable.save);
                                                    // Update the isSaved field in the post model
                                                    post.setSaved(false);
                                                    Log.d("Suffix", "Removing value with key: " + snapshotKey + " tof: " + post.isSaved());
                                                } else {
                                                    Log.e("Firebase", "Error updating save status: " + error.getMessage());
                                                }
                                            }
                                        });
                                        break; // Stop iterating after finding the matching key
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle cancellation if needed
                        }
                    });
                }
            }
        }
    }
}

