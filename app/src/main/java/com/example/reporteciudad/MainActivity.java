package com.example.reporteciudad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reporteciudad.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupButtons();
    }

    private void setupButtons() {
        binding.btnCrearReporte.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearReporteActivity.class);
            startActivity(intent);
        });

        binding.btnConsultarReporte.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConsultarReporteActivity.class);
            startActivity(intent);
        });

        binding.btnContacto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactoActivity.class);
            startActivity(intent);
        });
    }
} 