package com.ryca.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.ryca.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchExhibitorAdapter extends RecyclerView.Adapter<SearchExhibitorAdapter.ViewHolder> {

    private Context context;
    private List<SearchExhibitorList> exhibitorList;

    public SearchExhibitorAdapter(Context context, List<SearchExhibitorList> exhibitorList) {
        this.context = context;
        this.exhibitorList = exhibitorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.circle_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchExhibitorList exhibitor = exhibitorList.get(position);
        holder.shopName.setText(exhibitor.getShopName());
        holder.shopDescription.setText(exhibitor.getShopDescription());
        holder.location.setText(exhibitor.getLocation()  + ", " + exhibitor.getCity());

        // Use Picasso or another library to load images
       // Picasso.get().load(exhibitor.getProfilePicture()).into(holder.profilePicture);

        String profilePictureUrl = exhibitor.getProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Picasso.get().load(profilePictureUrl).into(holder.profilePicture);
        } else {
            holder.profilePicture.setImageResource(R.drawable.profile);
        }

        // Assuming you have an ImageView for each of the latest 3 post images
        holder.postImage1.setVisibility(View.GONE); // Initially set to GONE
        holder.postImage2.setVisibility(View.GONE); // Initially set to GONE
        holder.postImage3.setVisibility(View.GONE); // Initially set to GONE

        if (!exhibitor.getImageUrls().isEmpty()) {
            holder.postImage1.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(exhibitor.getImageUrls().get(0))
                    .fit()
                    .centerCrop()
                    .into(holder.postImage1);

            if (exhibitor.getImageUrls().size() > 1) {
                holder.postImage2.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(exhibitor.getImageUrls().get(1))
                        .fit()
                        .centerCrop()
                        .into(holder.postImage2);
            }

            if (exhibitor.getImageUrls().size() > 2) {
                holder.postImage3.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(exhibitor.getImageUrls().get(2))
                        .fit()
                        .centerCrop()
                        .into(holder.postImage3);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String userID = exhibitor.getUserId();

                Bundle bundle = new Bundle();
                bundle.putString("userId", userID);
                bundle.putString("fragment", "creatorsShowroom");

                CreatorsShowroom creatorsShowroomFragment = new CreatorsShowroom();
                creatorsShowroomFragment.setArguments(bundle);

                // Use itemView as the context to get the FragmentManager
                FragmentManager fragmentManager = ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager();

                // Add the transaction to the back stack before committing
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.framelayout, creatorsShowroomFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });

    }

    @Override
    public int getItemCount() {
        return exhibitorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView shopName, shopDescription, city, location;
        public ImageView profilePicture, postImage1, postImage2, postImage3;

        public ViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.creatorsnamedp);
            shopDescription = itemView.findViewById(R.id.banner);
            location = itemView.findViewById(R.id.creatorsaddressdp );
            profilePicture = itemView.findViewById(R.id.profilepicturedp);
            postImage1 = itemView.findViewById(R.id.updateExhibit1);
            postImage2 = itemView.findViewById(R.id.updateExhibit2);
            postImage3 = itemView.findViewById(R.id.updateExhibit3);

            location.setSelected(true);
        }
    }
}
