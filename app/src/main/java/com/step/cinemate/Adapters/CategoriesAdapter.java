package com.step.cinemate.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.step.cinemate.Data.Category;
import com.step.cinemate.R;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categories;

    public CategoriesAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_template, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryTitle.setText(category.name);

        // Настраиваем адаптер для списка фильмов внутри категории
        MoviesAdapter movieAdapter = new MoviesAdapter(context, category.movies);
        holder.moviesRecyclerView.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView moviesRecyclerView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            moviesRecyclerView = itemView.findViewById(R.id.moviesRecyclerView);

            // Настройка горизонтальной прокрутки для фильмов
            moviesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
    }
}


