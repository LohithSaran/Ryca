package com.ryca.Fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ryca.R;

import java.util.List;

public class AddCategoryAdapter extends RecyclerView.Adapter<AddCategoryAdapter.ViewHolder> {
    private List<AddCategoryModel> categories;
    private Context context;
    private ItemRemovedCallback callback;

    public AddCategoryAdapter(Context context, List<AddCategoryModel> categories, ItemRemovedCallback callback) {
        this.context = context;
        this.categories = categories;
        this.callback = callback;
    }


    public interface ItemRemovedCallback {
        void onItemRemoved(List<AddCategoryModel> updatedList, String removedValue);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AddCategoryModel category = categories.get(position);
        holder.categoryName.setText(category.getName());

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                String removedValue = category.getName();
                 if (clickedPosition != RecyclerView.NO_POSITION) {
                    // Remove the item from the list
                    categories.remove(clickedPosition);
                    // Notify the adapter of the item removal
                    notifyItemRemoved(clickedPosition);
                    if(callback != null) {
                        callback.onItemRemoved(categories,removedValue);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryName;
        public ImageView cancelBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textView3);
            cancelBtn = itemView.findViewById(R.id.imageView11);
        }
    }
}
