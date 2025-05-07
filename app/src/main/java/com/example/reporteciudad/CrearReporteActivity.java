package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.reporteciudad.databinding.ActivityCrearReporteBinding;
import java.io.ByteArrayOutputStream;

public class CrearReporteActivity extends AppCompatActivity {
    private ActivityCrearReporteBinding binding;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Bitmap fotoCapturada;
    private ReporteManager reporteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reporteManager = new ReporteManager(this);
        setupCameraLauncher();
        setupButtons();
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    fotoCapturada = (Bitmap) extras.get("data");
                    binding.ivFoto.setImageBitmap(fotoCapturada);
                }
            }
        );
    }

    private void setupButtons() {
        binding.btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                abrirCamara();
            }
        });

        binding.btnEnviarReporte.setOnClickListener(v -> {
            if (validarCampos()) {
                enviarReporte();
            }
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private boolean validarCampos() {
        if (binding.etTitulo.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un título", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etDescripcion.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fotoCapturada == null) {
            Toast.makeText(this, "Por favor tome una foto", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void enviarReporte() {
        // Convertir la foto a base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fotoCapturada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String fotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Crear y guardar el reporte
        Reporte reporte = new Reporte(
            binding.etTitulo.getText().toString(),
            binding.etDescripcion.getText().toString(),
            fotoBase64
        );
        
        reporteManager.guardarReporte(reporte);

        // Mostrar el ID del reporte
        String mensaje = "Reporte guardado correctamente\nID del reporte: " + reporte.getId();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        
        // Limpiar campos
        binding.etTitulo.setText("");
        binding.etDescripcion.setText("");
        binding.ivFoto.setImageBitmap(null);
        fotoCapturada = null;
    }
} 