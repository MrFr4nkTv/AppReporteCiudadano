package com.example.reporteciudad;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reporteciudad.databinding.ActivityConsultarReporteBinding;

public class ConsultarReporteActivity extends AppCompatActivity {
    private ActivityConsultarReporteBinding binding;
    private ReporteManager reporteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultarReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Habilitar el botón de retroceso en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consultar Reporte");
        }

        reporteManager = new ReporteManager(this);
        setupButtons();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupButtons() {
        binding.btnBuscar.setOnClickListener(v -> {
            String idReporte = binding.etIdReporte.getText().toString();
            if (!idReporte.isEmpty()) {
                buscarReporte(idReporte);
            } else {
                Toast.makeText(this, "Por favor ingrese un ID de reporte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarReporte(String idReporte) {
        Reporte reporte = reporteManager.obtenerReporte(idReporte);
        if (reporte != null) {
            mostrarReporte(reporte);
        } else {
            Toast.makeText(this, "No se encontró el reporte", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        }
    }

    private void mostrarReporte(Reporte reporte) {
        binding.tvTitulo.setText(reporte.getTitulo());
        binding.tvDescripcion.setText(reporte.getDescripcion());
        binding.tvNombreContacto.setText(reporte.getNombreContacto());
        binding.tvTelefonoContacto.setText(reporte.getTelefonoContacto());
        binding.tvDireccionContacto.setText(reporte.getDireccionContacto());

        // Convertir la foto de base64 a Bitmap
        byte[] imageBytes = Base64.decode(reporte.getFotoBase64(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        binding.ivFoto.setImageBitmap(bitmap);
    }

    private void limpiarCampos() {
        binding.tvTitulo.setText("");
        binding.tvDescripcion.setText("");
        binding.tvNombreContacto.setText("");
        binding.tvTelefonoContacto.setText("");
        binding.tvDireccionContacto.setText("");
        binding.ivFoto.setImageBitmap(null);
    }
} 