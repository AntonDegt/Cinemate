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



    public static List<Movie> getFromJSON(String jsonString) throws JSONException {
        {
            List<Movie> movies = new ArrayList<>();

            // Преобразуем строку в объект JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);
            // Получаем массив категорий из JSON
            JSONArray dataArray = jsonResponse.getJSONArray("data");

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
                movie.likeCount = movieJson.getInt("likeCount");
                movie.dislikeCount = movieJson.getInt("dislikeCount");
                movie.categoryId = UUID.fromString(movieJson.getString("categoryId"));
                movie.subCategoryId = UUID.fromString(movieJson.getString("subCategoryId"));

                // Добавляем категорию в список
                movies.add(movie);
            }

            return movies;
        }
    }
}
