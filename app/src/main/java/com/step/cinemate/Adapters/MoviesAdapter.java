package com.step.cinemate.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.step.cinemate.Data.Movie;
import com.step.cinemate.R;

import java.util.List;
import java.util.UUID;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private final Context context;
    private final List<Movie> movies;
    private final OnMovieClickListener listener; // Интерфейс для обработки кликов

    public MoviesAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_template, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Загрузка изображения с помощью Glide
        Glide.with(context)
                .load(movie.pictureURL)
                .placeholder(R.drawable.image_not_found) // Замените на ваш файл-заглушку
                .into(holder.movieImage);

        // Обработка нажатия на изображение
        holder.movieImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie.id); // Передаем ID фильма
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView movieImage;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage = itemView.findViewById(R.id.movieImage);
        }
    }

    // Интерфейс для передачи кликов
    public interface OnMovieClickListener {
        void onMovieClick(UUID movieId);
    }
}
