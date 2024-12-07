package com.step.cinemate.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.UiThread;

import com.step.cinemate.R;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoginService {

    public static String userName;
    public static String userFirstName;
    public static String userSurname;
    public static String userEmail;
    public static String userPhoneNumber;
    public static String userAvatar;

    public static String token = null;


    public interface LoginDataCallback {
        void onLoginDataReady();
    }
    public static void getLoginDataFromBackend(Context context, LoginDataCallback callback) {
        Map<String, String> params = new HashMap<>();
        Map<String, File> file_params = new HashMap<>();

        BackendService.sendGetRequest(
                context.getString(R.string.signupuser),
                params,
                response -> {
                    try {
                        // Обрабатываем результат в коллбэке
                        System.out.println("Response Code: " + response.getResponseCode());
                        System.out.println("Response Body: " + response.getResponseBody());

                        JSONObject responseObject = new JSONObject(response.getResponseBody());
                        JSONObject userObject = responseObject.getJSONObject("user");

                        // Извлекаем значения и присваиваем статическим переменным
                        userName = userObject.getString("name");
                        userFirstName = userObject.getString("firstName");
                        userSurname = userObject.getString("surname");
                        userEmail = userObject.getString("email");
                        userPhoneNumber = userObject.getString("phoneNumber");
                        userAvatar = context.getString(R.string.avatars) + userObject.getString("avatar");

                        // Вызываем callback, если он задан
                        if (callback != null) {
                            callback.onLoginDataReady();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void saveLoginData(Context context, String email, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply(); // Сохранение данных
    }

    public static boolean hasSavedLoginData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);

        String username = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        return username != null && password != null; // Если данные есть, возвращает true
    }

    public static Pair<String, String> getSavedLoginData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);

        String username = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);

        return new Pair<>(username, password); // Возвращает имя пользователя и пароль
    }

    public static void clearLoginData(Context context) {
        token = null;
        userName = null;
        userFirstName = null;
        userSurname = null;
        userEmail = null;
        userPhoneNumber = null;
        userAvatar = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
