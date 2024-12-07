package com.step.cinemate.Services;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BackendService {
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

    // Очередь для запросов
    private static final Queue<Runnable> requestQueue = new LinkedList<>();
    private static boolean isProcessing = false;

    // Метод для выполнения следующего запроса
    private static synchronized void processNext() {
        Runnable nextRequest = requestQueue.poll();
        if (nextRequest != null) {
            isProcessing = true;
            new Thread(() -> {
                try {
                    nextRequest.run();
                } finally {
                    synchronized (BackendService.class) {
                        isProcessing = false;
                        processNext();
                    }
                }
            }).start();
        }
    }
    public static void sendPostRequest(String urlString, Map<String, String> params, Map<String, File> files, ResponseCallback callback, String contentType) {
        sendPostRequest(urlString, params, files, new HashMap<>(), callback, contentType);
    }
    public static void sendPostRequest(String urlString, Map<String, String> params, Map<String, File> files, Map<String, Boolean> paramsBoolean, ResponseCallback callback, String contentType) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // Устанавливаем Content-Type
                if ("application/json".equals(contentType)) {
                    connection.setRequestProperty("Content-Type", "application/json");
                } else if ("multipart/form-data".equals(contentType)) {
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                }

                if (LoginService.token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + LoginService.token);
                }

                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();

                if ("application/json".equals(contentType)) {
                    // JSON тело
                    JSONObject json = new JSONObject(params);
                    outputStream.write(json.toString().getBytes(StandardCharsets.UTF_8));
                } else if ("multipart/form-data".equals(contentType)) {
                    // Multipart тело
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    for (Map.Entry<String, String> param : params.entrySet()) {
                        writer.write("--" + boundary + "\r\n");
                        writer.write("Content-Disposition: form-data; name=\"" + param.getKey() + "\"\r\n\r\n");
                        writer.write(param.getValue() + "\r\n");
                    }

                    for (Map.Entry<String, File> fileEntry : files.entrySet()) {
                        writer.write("--" + boundary + "\r\n");
                        writer.write("Content-Disposition: form-data; name=\"" + fileEntry.getKey() + "\"; filename=\"" + fileEntry.getValue().getName() + "\"\r\n");
                        writer.write("Content-Type: application/octet-stream\r\n\r\n");
                        writer.flush();

                        FileInputStream fileInputStream = new FileInputStream(fileEntry.getValue());
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        fileInputStream.close();
                        writer.write("\r\n");
                    }
                    writer.write("--" + boundary + "--\r\n");
                    writer.flush();
                    writer.close();
                }

                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                StringBuilder responseBody = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line.trim());
                }
                reader.close();

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(responseCode, responseBody.toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(-1, e.getMessage()));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }






    // Универсальный метод для отправки GET запроса с callback
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
                    urlWithParams.setLength(urlWithParams.length() - 1); // Убираем последний "&"
                }

                // Открываем соединение
                URL url = new URL(urlWithParams.toString());
                connection = (HttpURLConnection) url.openConnection();

                // Настройка соединения
                connection.setRequestMethod("GET");

                // Если токен доступен, добавляем его в заголовок
                if (LoginService.token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + LoginService.token);
                }

                connection.setRequestProperty("Accept", "application/json");

                // Чтение ответа
                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line.trim());
                }
                reader.close();

                // Передача результата в UI-поток
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(responseCode, responseBody.toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                // Обработка ошибки в UI-потоке
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(-1, e.getMessage()));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    public static void sendDeleteRequest(String urlString, Map<String, String> params, ResponseCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                // Настройка соединения
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Content-Type", "application/json");

                // Если токен доступен, добавляем его в заголовок
                if (LoginService.token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + LoginService.token);
                }

                connection.setDoOutput(true);

                // Параметры передаём в теле запроса в формате JSON
                if (params != null && !params.isEmpty()) {
                    JSONObject jsonBody = new JSONObject(params);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    outputStream.close();
                }

                // Чтение ответа от сервера
                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line.trim());
                }
                reader.close();

                // Передача результата в UI-поток
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(responseCode, responseBody.toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(-1, e.getMessage()));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    public static void sendPatchRequest(String urlString, Map<String, String> params, Map<String, Boolean> paramsB, ResponseCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();

                // Настройка соединения
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");

                if (LoginService.token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + LoginService.token);
                }

                connection.setDoOutput(true);

                // Формирование JSON
                JSONObject jsonBody = new JSONObject();
                if (params != null) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        jsonBody.put(entry.getKey(), entry.getValue());
                    }
                }
                if (paramsB != null) {
                    for (Map.Entry<String, Boolean> entry : paramsB.entrySet()) {
                        jsonBody.put(entry.getKey(), entry.getValue());
                    }
                }

                System.out.println("JSON Body: " + jsonBody.toString());

                // Отправка тела запроса
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                // Чтение ответа
                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line.trim());
                }
                reader.close();

                System.out.println("Response Code: " + responseCode);
                System.out.println("Response Body: " + responseBody.toString());

                // Передача результата в UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(responseCode, responseBody.toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        callback.onResponse(new Response(-1, e.getMessage()));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
