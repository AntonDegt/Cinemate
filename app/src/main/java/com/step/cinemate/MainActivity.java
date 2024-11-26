package com.step.cinemate;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.step.cinemate.Services.BackendService;

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

        if (BackendService.token != null)
            if (!BackendService.token.isEmpty())
                startCinemateActivity();
    }

    public void getStatedButton(View view) {
        if (BackendService.token == null) startLoginActivity();
        else if (BackendService.token.isEmpty()) startLoginActivity();
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