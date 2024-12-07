package com.step.cinemate;

import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.step.cinemate.Data.Category;
import com.step.cinemate.Data.Movie;
import com.step.cinemate.Services.BackendService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MovieActivity extends AppCompatActivity {

    ImageView loadingGif;
    ScrollView mainContentLayout;
    FrameLayout loadingLayout;
    ImageView movieMainImageView;
    TextView movieCategoryTextView, movieNameTextView;
    YouTubePlayerView youtube_player_view;
    private float currentTime = 0;
    TextView movieReleaseYearTextView, movieDurationTextView, movieDescriptionTextView;
    ImageView movieLikeImageView, movieFavoriteImageView; TextView movieLikeCountTextView;

    public Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mainContentLayout = findViewById(R.id.movie_mainContentLayout);
        loadingLayout = findViewById(R.id.movie_loadingLayout);
        loadingGif = findViewById(R.id.movie_loadingGif);
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(loadingGif);

        mainContentLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);


        movieMainImageView = findViewById(R.id.movieMainImageView);
        movieCategoryTextView = findViewById(R.id.movieCategoryTextView);
        movieNameTextView = findViewById(R.id.movieNameTextView);
        movieReleaseYearTextView = findViewById(R.id.movieReleaseYearTextView);
        movieDurationTextView = findViewById(R.id.movieDurationTextView);
        movieDescriptionTextView = findViewById(R.id.movieDescriptionTextView);

        youtube_player_view = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youtube_player_view);

        movieLikeImageView = findViewById(R.id.movie_likeImageView);
        movieLikeCountTextView = findViewById(R.id.movie_likeCountTextView);
        movieFavoriteImageView = findViewById(R.id.movie_favoriteImageView);

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            setPage(UUID.fromString(Objects.requireNonNull(arguments.get("id")).toString()));
        } else {
            finish();
        }
    }

    private void setPage(UUID movieId) {

        Map<String, String> params = new HashMap<>();

        BackendService.sendGetRequest(
                (getResources().getString(R.string.movie) + movieId.toString()),
                params,
                responseMovie -> {
                    // Обрабатываем результат в коллбэке
                    System.out.println("Response Code: " + responseMovie.getResponseCode());
                    System.out.println("Response Body: " + responseMovie.getResponseBody());

                    int code = responseMovie.getResponseCode();
                    switch (code) {
                        case -1:
                        case 400:
                        case 401:
                            break;

                        case 200:
                            runOnUiThread(() -> {
                                try {
                                    this.movie = new Movie(responseMovie.getResponseBody());

                                    setLikeUi();
                                    favoriteImageUpload();

                                    youtube_player_view.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                        @Override
                                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                            String videoId = movie.url.substring(movie.url.lastIndexOf("/") + 1);
                                            BackendService.sendGetRequest(
                                                    (getResources().getString(R.string.get_timecode) + movieId.toString()),
                                                    new HashMap<>(),
                                                    responseTimecode -> {

                                                        switch (responseTimecode.getResponseCode()) {
                                                            case 400:
                                                                youTubePlayer.cueVideo(videoId, 0);
                                                                break;

                                                            case 200:
                                                                JSONObject jsonResponse = new JSONObject(responseTimecode.getResponseBody());
                                                                String pauseTimeString = jsonResponse.getString("pauseTime");

                                                                // Разделяем строку на компоненты
                                                                String[] parts = pauseTimeString.split(":");
                                                                if (parts.length != 3) {
                                                                    throw new IllegalArgumentException("Invalid time format. Expected HH:MM:SS");
                                                                }

                                                                // Преобразуем строки в числа
                                                                int hours = Integer.parseInt(parts[0]);
                                                                int minutes = Integer.parseInt(parts[1]);
                                                                int seconds = Integer.parseInt(parts[2]);

                                                                // Считаем общее количество секунд
                                                                int pauseTime = hours * 3600 + minutes * 60 + seconds;

                                                                youTubePlayer.cueVideo(videoId, pauseTime);
                                                            break;
                                                        }

                                                    });
                                        }

                                        @Override
                                        public void onCurrentSecond(@NonNull YouTubePlayer player, float second) {
                                            currentTime = second; // Обновляем текущую позицию видео

                                        }
                                    });

                                    // Загрузка изображения с помощью Glide
                                    Glide.with(this)
                                            .load(movie.pictureURL)
                                            .placeholder(R.drawable.image_not_found) // Замените на ваш файл-заглушку
                                            .into(movieMainImageView);

                                    Map<String, String> p = new HashMap<>();

                                    BackendService.sendGetRequest(
                                            getResources().getString(R.string.subcategories) + movie.categoryId.toString(),
                                            new HashMap<>(),
                                            responseCategory -> {
                                                // Обрабатываем результат в коллбэке
                                                System.out.println("Response: " + "Get Categories");
                                                System.out.println("Response Code: " + responseCategory.getResponseCode());
                                                System.out.println("Response Body: " + responseCategory.getResponseBody());

                                                switch (responseCategory.getResponseCode()) {
                                                    case -1:
                                                    case 400:
                                                    case 401:
                                                        break;

                                                    case 200:
                                                        Category category = new Category(responseCategory.getResponseBody());
                                                        runOnUiThread(() -> {
                                                            movieCategoryTextView.setText(category.name);
                                                        });
                                                        break;
                                                }
                                            });

                                    movieNameTextView.setText(movie.title);
                                    movieReleaseYearTextView.setText(movie.releaseYear + "");
                                    movieDurationTextView.setText(movie.duration);
                                    movieDescriptionTextView.setText(movie.description);

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            mainContentLayout.setVisibility(View.VISIBLE);
                            loadingLayout.setVisibility(View.GONE);
                            break;
                    }
                });
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Сохраняем текущий тайм-код видео
        if (youtube_player_view != null) {
            super.onPause();

            // Request
            Map<String, String> params = new HashMap<>();
            params.put("movieId ", movie.id.toString());
            params.put("pauseTime ", formatTime((int)currentTime));

            BackendService.sendPostRequest(
                    getResources().getString(R.string.save_timecode),
                    params,
                    new HashMap<>(),
                    responseSave -> {
                        System.out.println("Save timecode \nResponse Code: " +
                                responseSave.getResponseCode() +
                                "\nResponse Body" + responseSave.getResponseBody());

                    },
                    "multipart/form-data");
        }
    }
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private boolean liked = false;
    private void setLikeUi(){
        // Likes
        Map<String, String> paramsLikes = new HashMap<>();
        paramsLikes.put("movieId", movie.id.toString());

        BackendService.sendGetRequest(
                getResources().getString(R.string.like_get_status),
                paramsLikes,
                responseLikes -> {
                    System.out.println("Get likes \nResponse Code: " +
                            responseLikes.getResponseCode() +
                            "\nResponse Body" + responseLikes.getResponseBody());

                    switch (responseLikes.getResponseCode()){
                        case 200:

                            JSONObject jsonResponse = new JSONObject(responseLikes.getResponseBody());
                            // Заполняем поля объекта Category
                            boolean status = jsonResponse.getBoolean("isLiked");

                            runOnUiThread(() -> {
                                liked = status;
                                if (liked) movieLikeImageView.setImageResource(R.drawable.ic_thumbs_up_fill);
                                else movieLikeImageView.setImageResource(R.drawable.ic_thumbs_up);

                                movieLikeCountTextView.setText("" + movie.likeCount);
                            });

                            break;
                    }
                });
    }
    public void likeImageViewClick(View view) {
        Map<String, String> paramsLikes = new HashMap<>();
        paramsLikes.put("movieId", movie.id.toString());

        Map<String, Boolean> paramsBooleanLikes = new HashMap<>();
        paramsLikes.put("IsLiked", String.valueOf(!liked));
        paramsLikes.put("IsDisliked", String.valueOf(false));

        if (!liked) {
            movieLikeImageView.setImageResource(R.drawable.ic_thumbs_up_fill);
            movieLikeCountTextView.setText("" + (movie.likeCount + 1));
            liked = true;
        } else {
            setLikeUi();
        }

        //BackendService.sendPostRequest(
        //        getResources().getString(R.string.like_set_status),
        //        paramsLikes,
        //        new HashMap<>(),
        //        paramsBooleanLikes,
        //        responseLikes -> {
        //            System.out.println("Set likes \nResponse Code: " +
        //                    responseLikes.getResponseCode() +
        //                    "\nResponse Body" + responseLikes.getResponseBody());
        //
        //            runOnUiThread(() -> {
        //                setLikeUi();
        //            });
        //        },
        //        "multipart/form-data");
    }

    public void favoriteImageClick(View view) {
        Map<String, String> params = new HashMap<>();
        params.put("movieId", movie.id.toString());

        BackendService.sendGetRequest(
                getResources().getString(R.string.get_favorite),
                params,
                responseFav -> {
                    System.out.println("Get from favorite \nResponse Code: " +
                            responseFav.getResponseCode() +
                            "\nResponse Body" + responseFav.getResponseBody());

                    switch (responseFav.getResponseCode()){
                        case 200:
                            boolean inFavorite = false;

                            List<Movie> movies = Movie.getFromJSON(responseFav.getResponseBody(), "favoriteMovies");
                            for(Movie mov: movies) {
                                if (mov.id.equals(movie.id)) {
                                    inFavorite = true;
                                    break;
                                }
                            }

                            if (inFavorite) {
                                Map<String, String> paramsDel = new HashMap<>();
                                paramsDel.put("movieId", movie.id.toString());

                                BackendService.sendDeleteRequest(
                                        getResources().getString(R.string.delete_favorite),
                                        paramsDel,
                                        responseDelFav -> {
                                            System.out.println("Delete from favorite \nResponse Code: " +
                                                    responseDelFav.getResponseCode() +
                                                    "\nResponse Body" + responseDelFav.getResponseBody());

                                            switch (responseDelFav.getResponseCode()) {
                                                case 200:
                                                    setFavoriteUi(false);
                                                    break;
                                            }
                                        });

                            } else {
                                Map<String, String> paramsAdd = new HashMap<>();
                                paramsAdd.put("movieId", movie.id.toString());

                                BackendService.sendPostRequest(
                                        getResources().getString(R.string.add_favorite),
                                        paramsAdd,
                                        new HashMap<>(),
                                        responseAddFav -> {
                                            System.out.println("Add to favorite \nResponse Code: " +
                                                    responseAddFav.getResponseCode() +
                                                    "\nResponse Body" + responseAddFav.getResponseBody());

                                            switch (responseAddFav.getResponseCode()) {
                                                case 200:
                                                    setFavoriteUi(true);
                                                    break;
                                            }
                                        },
                                        "multipart/form-data");
                            }

                            break;
                    }
                });
    }

    public void favoriteImageUpload() {
        Map<String, String> params = new HashMap<>();
        params.put("movieId", movie.id.toString());

        BackendService.sendGetRequest(
                getResources().getString(R.string.get_favorite),
                params,
                responseFav -> {
                    System.out.println("Get from favorite \nResponse Code: " +
                            responseFav.getResponseCode() +
                            "\nResponse Body" + responseFav.getResponseBody());

                    switch (responseFav.getResponseCode()){
                        case 200:
                            boolean inFavorite = false;

                            List<Movie> movies = Movie.getFromJSON(responseFav.getResponseBody(), "favoriteMovies");
                            for(Movie mov: movies) {
                                if (mov.id.equals(movie.id)) {
                                    setFavoriteUi(true);
                                    inFavorite = true;
                                    break;
                                } else {
                                    setFavoriteUi(false);
                                }
                            }

                            break;
                    }
                });
    }

    private void setFavoriteUi(boolean state){
        if (state) {
            movieFavoriteImageView.setImageResource(R.drawable.ic_xmark);
        } else {
            movieFavoriteImageView.setImageResource(R.drawable.ic_plus);
        }
    }
}