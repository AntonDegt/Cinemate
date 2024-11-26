package com.step.cinemate;

import com.step.cinemate.Services.BackendService;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;



public class LoginActivity extends AppCompatActivity {

    private enum LoginActivityState {CHOOSE, LOGIN, REGISTRATION}

    // Layouts
    private LinearLayout chooseMethodLayout, loginLayout, registrationLayout;

    // Log In
    private EditText emailEditText, passwordEditText;
    private TextView errorMessageView;

    // Sign Up
    private EditText userNameEditText, firstNameEditText, surnameEditText, registerEmailEditText, phoneNumberEditText, registerPasswordEditText;
    private TextView errorMessageLogin, errorMessageFirstName, errorMessageSurname, errorMessageEmail, errorMessagePhoneNumber, errorMessagePassword, errorMessageAvatar;
    private ImageView avatarSelectedImage;
    private static final int PICK_IMAGE = 100;
    private String selectedAvatarImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация Layout-ов
        chooseMethodLayout = findViewById(R.id.chooseMethodLayout);
        loginLayout = findViewById(R.id.loginLayout);
        registrationLayout = findViewById(R.id.registrationLayout);

        switchState (LoginActivityState.CHOOSE);

        // Log In
        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        errorMessageView = findViewById(R.id.errorMessage);

        // Registration
        userNameEditText = findViewById(R.id.loginInputRegister);
        firstNameEditText = findViewById(R.id.firstNameInputRegister);
        surnameEditText = findViewById(R.id.surnameInputRegister);
        registerEmailEditText = findViewById(R.id.emailInputRegister);
        phoneNumberEditText = findViewById(R.id.phoneInputRegister);
        registerPasswordEditText = findViewById(R.id.passwordInputRegister);

        errorMessageLogin = findViewById(R.id.errorMessageLogin);
        errorMessageFirstName = findViewById(R.id.errorMessageFirstName);
        errorMessageSurname = findViewById(R.id.errorMessageSurname);
        errorMessageEmail = findViewById(R.id.errorMessageEmail);
        errorMessagePhoneNumber = findViewById(R.id.errorMessagePhoneNumber);
        errorMessagePassword = findViewById(R.id.errorMessagePassword);
        errorMessageAvatar = findViewById(R.id.errorMessageAvatar);

        avatarSelectedImage = findViewById(R.id.avatarPreviewImage);
        avatarSelectedImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // Отображаем изображение в ImageView
            avatarSelectedImage.setImageURI(imageUri);
        }
    }

    private void switchState (LoginActivityState state) {
        switch (state) {
            case CHOOSE:
                chooseMethodLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
                registrationLayout.setVisibility(View.GONE);
                break;

            case LOGIN:
                chooseMethodLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                registrationLayout.setVisibility(View.GONE);
                break;

            case REGISTRATION:
                chooseMethodLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.GONE);
                registrationLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Обработчик нажатия на ссылку "Sign up"
    public void onRegisterLinkClicked(View view) {
        switchState(LoginActivityState.REGISTRATION);
    }
    // Обработчик нажатия на ссылку "Back to login"
    public void onBackToLoginClicked(View view) {
        switchState(LoginActivityState.LOGIN);
    }





    // Обработчик нажатия на "Continue with Google"
    public void onGoogleSignInClicked(View view) {
    }
    // Обработчик нажатия на "Continue with Facebook"
    public void onFacebookSignInClicked(View view) {
    }
    // Обработчик нажатия на "Continue with Email"
    public void onEmailSignInClicked(View view) {
        switchState(LoginActivityState.REGISTRATION);
    }


    // Лаунчер для обработки результата выбора изображения
    public void avatarAddButton(View view) {
        // Запускаем Intent для выбора изображения
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }




    // Обработчик нажатия на "Log in"
    public void onLoginClicked(View view) {
        String email, password;

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        // Request
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

                    LogIn(response);
                });
    }

    // Обработчик нажатия на "Sign up"
    public void onRegisterClicked(View view) {
        String userName, firstName, surname, email, phoneNumber, password;

        userName = userNameEditText.getText().toString();
        firstName = firstNameEditText.getText().toString();
        surname = surnameEditText.getText().toString();
        email = registerEmailEditText.getText().toString();
        phoneNumber = phoneNumberEditText.getText().toString();
        password = registerPasswordEditText.getText().toString();


        // Request
        Map<String, String> params = new HashMap<>();
        params.put("userName", userName);
        params.put("firstName", firstName);
        params.put("surname", surname);
        params.put("email", email);
        params.put("phoneNumber", phoneNumber);
        params.put("password", password);

        Map<String, File> file_params = new HashMap<>();

        File avatar = new File(selectedAvatarImagePath);
        if (avatar.exists())
            file_params.put("avatar", avatar);


        BackendService.sendPostRequest(
                getResources().getString(R.string.signupuser),
                params,
                file_params,
                response -> {
                    // Обрабатываем результат в коллбэке
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    SignUp(response);
                });
    }

    public void LogIn (BackendService.Response response) throws JSONException {
        int code = response.getResponseCode();
        switch (code) {
            case -1:
            case 400:
            case 401:
                runOnUiThread(() -> errorMessageView.setVisibility(View.VISIBLE));
                break;

            case 200:
                runOnUiThread(() -> errorMessageView.setVisibility(View.GONE));

                JSONObject json = new JSONObject(response.getResponseBody());
                BackendService.token = json.getString("token");

                finish();
                break;
        }
    }

    public void SignUp (BackendService.Response response) throws JSONException {
        int code = response.getResponseCode();
        switch (code) {
            case -1:
            case 401:
                break;

            case 400:
                JSONObject json_error = new JSONObject(response.getResponseBody());


                if (json_error.getString("userNameErrorMessage").equals("null")) runOnUiThread(() -> errorMessageLogin.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessageLogin.setVisibility(View.VISIBLE);
                    try { errorMessageLogin.setText(json_error.getString("userNameErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("firstNameErrorMessage").equals("null")) runOnUiThread(() -> errorMessageFirstName.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessageFirstName.setVisibility(View.VISIBLE);
                    try { errorMessageFirstName.setText(json_error.getString("firstNameErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("surnameErrorMessage").equals("null")) runOnUiThread(() -> errorMessageSurname.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessageSurname.setVisibility(View.VISIBLE);
                    try { errorMessageSurname.setText(json_error.getString("surnameErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("emailErrorMessage").equals("null")) runOnUiThread(() -> errorMessageEmail.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessageEmail.setVisibility(View.VISIBLE);
                    try { errorMessageEmail.setText(json_error.getString("emailErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("phoneNumberErrorMessage").equals("null")) runOnUiThread(() -> errorMessagePhoneNumber.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessagePhoneNumber.setVisibility(View.VISIBLE);
                    try { errorMessagePhoneNumber.setText(json_error.getString("phoneNumberErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("passwordErrorMessage").equals("null")) runOnUiThread(() -> errorMessagePassword.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessagePassword.setVisibility(View.VISIBLE);
                    try { errorMessagePassword.setText(json_error.getString("passwordErrorMessage")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });

                if (json_error.getString("avataErrorMessager").equals("null")) runOnUiThread(() -> errorMessageAvatar.setVisibility(View.GONE));
                else runOnUiThread(() -> {
                    errorMessageAvatar.setVisibility(View.VISIBLE);
                    try { errorMessageAvatar.setText(json_error.getString("avataErrorMessager")); }
                    catch ( JSONException e) { throw new RuntimeException(e); }
                });


                break;

            case 200:
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "User registered successfully!", Toast.LENGTH_LONG).show());
                runOnUiThread(() -> switchState(LoginActivityState.LOGIN));
                break;
        }
    }
}