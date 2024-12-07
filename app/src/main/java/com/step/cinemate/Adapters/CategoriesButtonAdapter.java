package com.step.cinemate.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.step.cinemate.Data.Category;
import com.step.cinemate.R;

import java.util.List;

public class CategoriesButtonAdapter extends RecyclerView.Adapter<CategoriesButtonAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> items;

    public CategoriesButtonAdapter(Context context, List<Category> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.caterory_button_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = items.get(position);
        holder.categoryTitle.setText(category.name);

        Glide.with(context)
                .load(category.pictureURL)
                .placeholder(R.drawable.image_not_found) // Замените на ваш файл-заглушку
                .into(holder.categoryImage);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        ImageView categoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryButtonTitleTextView);
            categoryImage = itemView.findViewById(R.id.categoryButtonImageVew);
        }
    }
}

