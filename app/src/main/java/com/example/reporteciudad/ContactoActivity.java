package com.example.reporteciudad;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reporteciudad.databinding.ActivityContactoBinding;

public class ContactoActivity extends AppCompatActivity {
    private ActivityContactoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Habilitar el botón de retroceso en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportar Problema");
        }

        // Auto-rellenar información del sistema
        autoRellenarInformacionSistema();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void autoRellenarInformacionSistema() {
        try {
            // Versión de la App
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.etVersionApp.setText(versionName);

            // Dispositivo
            String dispositivo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
            binding.etDispositivo.setText(dispositivo);

            // Versión de Android
            String versionAndroid = "Android " + android.os.Build.VERSION.RELEASE;
            binding.etVersionAndroid.setText(versionAndroid);

        } catch (Exception e) {
            Toast.makeText(this, "Error al obtener información del sistema", Toast.LENGTH_SHORT).show();
        }
    }
} 