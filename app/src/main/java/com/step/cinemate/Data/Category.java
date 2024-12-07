package com.step.cinemate.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Category {
    public UUID id;
    public String name;
    public String description;
    public String pictureURL;
    public Category parentCategory;
    // Для категорий - null         Для подкатегорий - ссылка
    public List<Movie> movies = null;

    public Category() {}

    public Category(String jsonString) throws JSONException {
        {
            List<Category> categories = new ArrayList<>();

            // Преобразуем строку в объект JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);
            // Получаем массив категорий из JSON
            JSONObject categoryJson = jsonResponse.getJSONObject("data");

            // Заполняем поля объекта Category
            this.id = UUID.fromString(categoryJson.getString("id"));
            this.name = categoryJson.getString("name");
            this.description = categoryJson.getString("description");
            this.pictureURL = categoryJson.getString("picture");
        }
    }

    public static List<Category> getFromJSON(String jsonString) throws JSONException {
        {
            List<Category> categories = new ArrayList<>();

            // Преобразуем строку в объект JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);
            // Получаем массив категорий из JSON
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            // Перебираем каждый элемент в массиве
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject categoryJson = dataArray.getJSONObject(i);

                // Создаем новый объект Category
                Category category = new Category();

                // Заполняем поля объекта Category
                category.id = UUID.fromString(categoryJson.getString("id"));
                category.name = categoryJson.getString("name");
                category.description = categoryJson.getString("description");
                category.pictureURL = categoryJson.getString("picture");

                // Добавляем категорию в список
                categories.add(category);
            }

            return categories;
        }
    }
}