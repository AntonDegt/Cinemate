package com.step.cinemate;

import android.os.Bundle;

import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.step.cinemate.Services.BackendService;
import com.step.cinemate.Services.LoginService;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Проверяем, есть ли сохраненные данные
        if (LoginService.token == null)
            if (LoginService.hasSavedLoginData(this)) {
                // Автологин
                Pair<String, String> loginData = LoginService.getSavedLoginData(this);
                String email = loginData.first;
                String password = loginData.second;

                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                Map<String, File> file_params = new HashMap<>();

                BackendService.sendPostRequest(
                        getResources().getString(R.string.auth),
                        params,
                        file_params,
                        response -> {
                            // Обрабатываем результат в коллбэке
                            System.out.println("Response Code: " + response.getResponseCode());
                            System.out.println("Response Body: " + response.getResponseBody());

                            int code = response.getResponseCode();
                            switch (code) {
                                case -1:
                                case 400:
                                case 401:
                                    break;

                                case 200:
                                    JSONObject json = new JSONObject(response.getResponseBody());
                                    LoginService.token = json.getString("token");

                                    startCinemateActivity();
                                    break;
                            }
                        },
                        "multipart/form-data");
                return;
            }
    }

    @Override
    protected void onStart(){
        super.onStart();
        System.out.println("MainActivity:" + "onStart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("MainActivity:" + "onPause()");
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MainActivity:" + "onResume()");

        if (LoginService.token != null)
            if (!LoginService.token.isEmpty())
                startCinemateActivity();
    }

    public void getStatedButton(View view) {
        if (LoginService.token == null) startLoginActivity();
        else if (LoginService.token.isEmpty()) startLoginActivity();
        else startCinemateActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void startCinemateActivity() {
        Intent intent = new Intent(this, LibraryActivity.class);
        startActivity(intent);
        finish();
    }
}