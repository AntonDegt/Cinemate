package com.step.cinemate;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.step.cinemate.Data.Movie;
import com.step.cinemate.Services.BackendService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private EditText userNameEditText, firstNameEditText, surnameEditText, registerEmailEditText, phoneNumberEditText, registerPasswordEditText;
    private TextView errorMessageLogin, errorMessageFirstName, errorMessageSurname, errorMessageEmail, errorMessagePhoneNumber, errorMessagePassword, errorMessageAvatar;
    private ImageView avatarSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Registration
        userNameEditText = findViewById(R.id.loginInputRegister);
        firstNameEditText = findViewById(R.id.firstNameInputRegister);
        surnameEditText = findViewById(R.id.surnameInputRegister);
        registerEmailEditText = findViewById(R.id.emailInputRegister);
        phoneNumberEditText = findViewById(R.id.phoneInputRegister);
        registerPasswordEditText = findViewById(R.id.passwordInputRegister);
        loadFields();

        errorMessageLogin = findViewById(R.id.errorMessageLogin);
        errorMessageFirstName = findViewById(R.id.errorMessageFirstName);
        errorMessageSurname = findViewById(R.id.errorMessageSurname);
        errorMessageEmail = findViewById(R.id.errorMessageEmail);
        errorMessagePhoneNumber = findViewById(R.id.errorMessagePhoneNumber);
        errorMessagePassword = findViewById(R.id.errorMessagePassword);
        errorMessageAvatar = findViewById(R.id.errorMessageAvatar);
    }

    private void loadFields(){
        BackendService.sendGetRequest(
                getResources().getString(R.string.signupuser),
                new HashMap<>(),
                response -> {
                    // Обрабатываем результат в коллбэке
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    if (response.getResponseCode() == 200){
                        List<Movie> movies = new ArrayList<>();

                        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
                        JSONObject dataObj = jsonResponse.getJSONObject("user");

                        userNameEditText.setText(dataObj.getString("name"));
                        firstNameEditText.setText(dataObj.getString("firstName"));
                        surnameEditText.setText(dataObj.getString("surname"));
                        registerEmailEditText.setText(dataObj.getString("email"));
                        phoneNumberEditText.setText(dataObj.getString("phoneNumber"));
                        }
                });

    }

    public void onEditClicked(View view) {
        String userName, firstName, surname, email, phoneNumber, password;

        userName = userNameEditText.getText().toString();
        firstName = firstNameEditText.getText().toString();
        surname = surnameEditText.getText().toString();
        email = registerEmailEditText.getText().toString();
        phoneNumber = phoneNumberEditText.getText().toString();


        // Request
        Map<String, String> par = new HashMap<>();
        par.put("userName", userName);
        par.put("firstName", firstName);
        par.put("surname", surname);
        par.put("email", email);
        par.put("phoneNumber", phoneNumber);


        BackendService.sendPatchRequest(
                getResources().getString(R.string.signupuser),
                par,
                new HashMap<>(),
                response -> {
                    // Обрабатываем результат в коллбэке
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    SignUp(response);
                });
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
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "User edit successfully!", Toast.LENGTH_LONG).show());
                break;
        }
    }

    public void onCancelClicked(View view) {
        finish();
    }
}