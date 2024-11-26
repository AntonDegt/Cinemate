package com.step.cinemate.Services;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BackendService {

    public static String token;
    // Класс для возврата кода и тела ответа
    public static class Response {
        private final int responseCode;
        private final String responseBody;

        public Response(int responseCode, String responseBody) {
            this.responseCode = responseCode;
            this.responseBody = responseBody;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }

    // Функциональный интерфейс для обработки результата
    @FunctionalInterface
    public interface ResponseCallback {
        void onResponse(Response response) throws JSONException;
    }


    // Универсальный метод для отправки POST запроса с callback
    // Метод для отправки POST запроса с несколькими файлами
    public static void sendPostRequest(String urlString, Map<String, String> params, Map<String, File> files, ResponseCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            try {
                // Устанавливаем URL
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                // Настройка соединения
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setDoOutput(true);

                // Открываем поток для записи данных
                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

                // Добавляем текстовые параметры
                for (Map.Entry<String, String> param : params.entrySet()) {
                    writer.write("--" + boundary + "\r\n");
                    writer.write("Content-Disposition: form-data; name=\"" + param.getKey() + "\"\r\n\r\n");
                    writer.write(param.getValue() + "\r\n");
                }



                // Завершаем запрос
                writer.write("--" + boundary + "--\r\n");
                writer.flush();
                writer.close();

                // Чтение ответа от сервера
                int responseCode = connection.getResponseCode();
                StringBuilder responseBody = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line.trim());
                }
                reader.close();

                // Вызов callback с результатом
                callback.onResponse(new Response(responseCode, responseBody.toString()));

            } catch (Exception e) {
                // Обработка ошибок
                try {
                    callback.onResponse(new Response(-1, e.getMessage()));
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }




    public static void sendGetRequest(String urlString, Map<String, String> params, ResponseCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Добавляем параметры в URL
                StringBuilder urlWithParams = new StringBuilder(urlString);
                if (params != null && !params.isEmpty()) {
                    urlWithParams.append("?");
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        urlWithParams.append(param.getKey())
                                .append("=")
                                .append(param.getValue())
                                .append("&");
                    }
                    // Убираем последний "&"
                    urlWithParams.setLength(urlWithParams.length() - 1);
                }

                // Открываем соединение
                URL url = new URL(urlWithParams.toString());
                connection = (HttpURLConnection) url.openConnection();

                // Настройка соединения
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                // Получаем код ответа
                int responseCode = connection.getResponseCode();

                // Чтение ответа от сервера
                BufferedReader reader = null;
                try {
                    InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                            ? connection.getInputStream()
                            : connection.getErrorStream();

                    // Считываем поток с помощью BufferedReader
                    reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }

                    // Передаем данные через callback
                    callback.onResponse(new Response(responseCode, responseBody.toString()));
                } catch (Exception e) {
                    callback.onResponse(new Response(-1, e.getMessage()));
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                // Обработка ошибок
                try {
                    callback.onResponse(new Response(-1, e.getMessage()));
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
