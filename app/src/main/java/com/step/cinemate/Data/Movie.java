package com.step.cinemate.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Movie {

    public UUID id;
    public String title;
    public String description;
    public String pictureURL;
    public String url;
    public int releaseYear;
    public String duration;
    public int likeCount;
    public int dislikeCount;
    public UUID categoryId;
    public UUID subCategoryId;


    public Movie() {}

    public Movie(String jsonString) throws JSONException {
        {
            // Преобразуем строку в объект JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);
            // Получаем массив категорий из JSON
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            JSONObject movieJson = dataArray.getJSONObject(0);

            // Заполняем поля объекта Category
            this.id = UUID.fromString(movieJson.getString("id"));
            this.title = movieJson.getString("title");
            this.description = movieJson.getString("description");
            this.pictureURL = movieJson.getString("picture");
            this.url = movieJson.getString("url");
            this.releaseYear = movieJson.getInt("releaseYear");
            if (this.duration == "null") this.duration = "";
            this.likeCount = movieJson.getInt("likeCount");
            if (!movieJson.isNull("dislikeCount"))
                this.dislikeCount = movieJson.getInt("dislikeCount");
            else
                this.dislikeCount = 0;
            this.categoryId = UUID.fromString(movieJson.getString("categoryId"));
            this.subCategoryId = UUID.fromString(movieJson.getString("subCategoryId"));
        }
    }


    public static List<Movie> getFromJSON(String jsonString, String arrayName) throws JSONException {
        {
            List<Movie> movies = new ArrayList<>();

            // Преобразуем строку в объект JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);
            // Получаем массив категорий из JSON
            JSONArray dataArray = jsonResponse.getJSONArray(arrayName);

            // Перебираем каждый элемент в массиве
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject movieJson = dataArray.getJSONObject(i);

                // Создаем новый объект Category
                Movie movie = new Movie();

                // Заполняем поля объекта Category
                movie.id = UUID.fromString(movieJson.getString("id"));
                movie.title = movieJson.getString("title");
                movie.description = movieJson.getString("description");
                movie.pictureURL = movieJson.getString("picture");
                movie.url = movieJson.getString("url");
                movie.releaseYear = movieJson.getInt("releaseYear");
                movie.duration = movieJson.getString("duration");
                if (movie.duration == "null") movie.duration = "";
                movie.likeCount = movieJson.getInt("likeCount");
                if (!movieJson.isNull("dislikeCount"))
                    movie.dislikeCount = movieJson.getInt("dislikeCount");
                else
                    movie.dislikeCount = 0;
                movie.categoryId = UUID.fromString(movieJson.getString("categoryId"));
                movie.subCategoryId = UUID.fromString(movieJson.getString("subCategoryId"));

                // Добавляем категорию в список
                movies.add(movie);
            }

            return movies;
        }
    }
}
