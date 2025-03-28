package com.example.blog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blog.Activity.MainActivity;
import com.example.blog.Activity.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Handler handler = new Handler();
        mAuth = FirebaseAuth.getInstance();

        handler.postDelayed(() -> {
            Intent intent = null;
            if(mAuth.getCurrentUser() != null){
                intent = new Intent(SplashScreen.this, MainActivity.class);
            }else {
                intent = new Intent(SplashScreen.this, RegisterActivity.class);
            }
            startActivity(intent);
            finish();
        },3000);
    }
}