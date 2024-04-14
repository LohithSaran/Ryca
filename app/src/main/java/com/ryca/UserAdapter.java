package com.ryca;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;

    FirebaseUser firebaseUser;

    public UserAdapter(Context mcContext, List<User> mUser) {
        this.mContext = mcContext;
        this.mUser = mUser;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.search_user_item, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUser.get(position);

        // Add a null check for user
        if (user != null) {
            holder.username.setText(user.getUsername());
            holder.address.setText(user.getAdd() + ", " + user.getCity());

            // Add a null check for user.getImageurl()
            if (user.getImageurl() != null) {
                Picasso.get()
                        .load(user.getImageurl())
                        .fit()
                        .centerCrop(Gravity.TOP)
                        .into(holder.userProfile);
            }else {
                holder.userProfile.setImageResource(R.drawable.profile);
            }

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(user);
                }
            }
        });
    }




    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView address;
        public ImageView userProfile;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.creatorsname);
            address = itemView.findViewById(R.id.searchaddress);
            userProfile = itemView.findViewById(R.id.profilePicturecs);

            address.setSelected(true);

        }
    }


}
