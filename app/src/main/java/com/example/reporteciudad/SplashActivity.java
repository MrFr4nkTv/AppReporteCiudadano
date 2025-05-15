package com.example.reporteciudad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_TIME = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animar el logo con fade-in
        ImageView logoImageView = findViewById(R.id.ivLogo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoImageView.startAnimation(fadeIn);

        // Esperar 2 segundos y luego ir a MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cierra el SplashActivity para que no vuelva cuando presione atrás
        }, SPLASH_DISPLAY_TIME);
    }
} 