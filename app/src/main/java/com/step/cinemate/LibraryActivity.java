package com.step.cinemate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.step.cinemate.Adapters.CategoriesAdapter;
import com.step.cinemate.Data.Category;
import com.step.cinemate.Data.Movie;
import com.step.cinemate.Services.BackendService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView categoriesRecyclerView;
    private List<Category> categories;
    private TextView userFullNameTextView;
    private ImageView userAvatarImageView;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_library);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Загрузка данных о пользователе
        userAvatarImageView = findViewById(R.id.avatarImageView);
        //userFullNameTextView = findViewById(R.id.);


        // Загрузка категорий;
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadCategories();
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
                            category.movies = Movie.getFromJSON(responseMovie.getResponseBody());
                            break;
                    }
                    getMovieRecurs(categories.subList(0, 3), index+1);
                });
    }

    private void movieLoaded(List<Category> categories) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LibraryActivity.this.categories = categories;

                CategoriesAdapter categoriesAdapter = new CategoriesAdapter(LibraryActivity.this, categories);
                categoriesRecyclerView.setAdapter(categoriesAdapter);
            }
        });
    }
}