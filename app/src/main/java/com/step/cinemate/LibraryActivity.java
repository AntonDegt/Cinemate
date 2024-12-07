package com.step.cinemate;

import static com.step.cinemate.Services.LoginService.getLoginDataFromBackend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.step.cinemate.Adapters.CategoriesAdapter;
import com.step.cinemate.Adapters.CategoriesButtonAdapter;
import com.step.cinemate.Adapters.MoviesAdapter;
import com.step.cinemate.Data.Category;
import com.step.cinemate.Data.Movie;
import com.step.cinemate.Services.BackendService;
import com.step.cinemate.Services.LoginService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LibraryActivity extends AppCompatActivity implements MoviesAdapter.OnMovieClickListener{

    private RecyclerView categoriesRecyclerView;
    private RecyclerView categoriesButtonRecyclerView;
    private RecyclerView moviesContinueWatchingRecyclerView;
    private RecyclerView moviesRecommendationRecyclerView;

    private List<Category> categories;
    private TextView userFullNameTextView, userLoginTextView;
    private ImageView avatarButtonImageView, avatarImageView;
    private LinearLayout accountBar;
    private boolean isMenuVisible = false;

    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility", "MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_library);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLibrary), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Загрузка данных о пользователе
        avatarButtonImageView = findViewById(R.id.avatarButtonImageView);
        avatarImageView = findViewById(R.id.avatarImageView);
        userFullNameTextView = findViewById(R.id.userFullNameTextView);
        userLoginTextView = findViewById(R.id.userLoginTextView);
        getLoginDataFromBackend(this, () -> {
            runOnUiThread(() -> {
                // Загрузка изображения с помощью Glide
                Glide.with(this)
                        .load(LoginService.userAvatar)
                        .circleCrop()
                        .placeholder(R.drawable.user)
                        .into(avatarButtonImageView);
                Glide.with(this)
                        .load(LoginService.userAvatar)
                        .circleCrop()
                        .placeholder(R.drawable.user)
                        .into(avatarImageView);
                userFullNameTextView.setText(LoginService.userFirstName + "\n" + LoginService.userSurname);
                userLoginTextView.setText("@" + LoginService.userName);
            });
        });

        // Загрузка категорий
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        moviesContinueWatchingRecyclerView = findViewById(R.id.moviesContinueWatchingRecyclerView);
        moviesContinueWatchingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        loadCountinueWatch();

        moviesRecommendationRecyclerView = findViewById(R.id.moviesRecommendationRecyclerView);
        moviesRecommendationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        categoriesButtonRecyclerView = findViewById(R.id.categoriesButtonRecyclerView);
        categoriesButtonRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));



        accountBar = findViewById(R.id.accountBar);
        // Показать/скрыть меню при нажатии на avatarButtonImageView
        avatarButtonImageView.setOnClickListener(v -> {
            if (isMenuVisible) {
                accountBar.setVisibility(View.GONE);
                isMenuVisible = false;
            } else {
                accountBar.setVisibility(View.VISIBLE);
                isMenuVisible = true;
            }
        });

        // Скрыть меню при нажатии за его пределами
        findViewById(R.id.mainLibrary).setOnTouchListener((v, event) -> {
            if (isMenuVisible && event.getAction() == MotionEvent.ACTION_DOWN) {
                accountBar.setVisibility(View.GONE);
                isMenuVisible = false;
            }
            return false;
        });
    }




    // Метод для асинхронной загрузки данных
    private void loadCategories() {

        BackendService.sendGetRequest(
                getResources().getString(R.string.categories),
                new HashMap<>(),
                responseCategory -> {
                    // Обрабатываем результат в коллбэке
                    System.out.println("Response: " + "Get Categories");
                    System.out.println("Response Code: " + responseCategory.getResponseCode());
                    System.out.println("Response Body: " + responseCategory.getResponseBody());

                    List<Category> categories = Category.getFromJSON(responseCategory.getResponseBody());

                    getMovieRecurs(categories, 0);
                });
    }
    private void getMovieRecurs(List<Category> categories, int index) {
        if (index >= categories.size()) {
            movieLoaded(categories);
            return;
        }

        Category category = categories.get(index);
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", category.id.toString());

        BackendService.sendGetRequest(
                getResources().getString(R.string.movies),
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
                            category.movies = new ArrayList<>();
                            break;

                        case 200:
                            category.movies = Movie.getFromJSON(responseMovie.getResponseBody(), "data");
                            break;
                    }

                    getMovieRecurs(categories, index+1);
                });
    }
    private void movieLoaded(List<Category> categories) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LibraryActivity.this.categories = categories;

                int count = categories.size();//Math.min(categories.size(), 5);
                CategoriesAdapter categoriesAdapter =
                        new CategoriesAdapter(LibraryActivity.this, categories.subList(0, count), LibraryActivity.this);
                categoriesRecyclerView.setAdapter(categoriesAdapter);

                CategoriesButtonAdapter categoriesButtonAdapter = new CategoriesButtonAdapter(LibraryActivity.this, categories);
                categoriesButtonRecyclerView.setAdapter(categoriesButtonAdapter);
            }
        });
    }
    private void loadRecomendation(){
        BackendService.sendGetRequest(
                getResources().getString(R.string.get_priority_list),
                new HashMap<>(),
                response -> {
                    System.out.println("Response: " + "Get Recomended Watch");
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    if (response.getResponseCode() == 200) {
                        List<Movie> movies = Movie.getFromJSON(response.getResponseBody(), "data");

                        MoviesAdapter adapter = new MoviesAdapter(this, movies, this);
                        moviesRecommendationRecyclerView.setAdapter(adapter);
                    }

                    loadCategories();
                }
        );
    }
    private void loadCountinueWatch(){
        BackendService.sendGetRequest(
                getResources().getString(R.string.get_list_timecode),
                new HashMap<>(),
                response -> {
                    System.out.println("Response: " + "Get Countinue Watch");
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    if (response.getResponseCode() == 200) {
                        List<Movie> movies = Movie.getFromJSON(response.getResponseBody(), "moviePauses");

                        MoviesAdapter adapter = new MoviesAdapter(this, movies, this);
                        moviesContinueWatchingRecyclerView.setAdapter(adapter);
                    }

                    loadRecomendation();
                }
        );
    }
    @Override
    public void onMovieClick(UUID movieId) {
        // создание объекта Intent для запуска MovieActivity
        Intent intent = new Intent(this, MovieActivity.class);
        // передача объекта
        intent.putExtra("id", movieId);
        // запуск MovieActivity
        startActivity(intent);
    }

    public void notificationButtonClick(View view) {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }
    public void manageButtonClick(View view) {
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
    }

    public void exitButtonClick(View view) {
        LoginService.clearLoginData(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("toLogin", "true");
        startActivity(intent);
        finish();
    }
}