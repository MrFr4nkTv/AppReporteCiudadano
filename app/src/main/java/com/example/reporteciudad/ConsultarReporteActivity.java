package com.example.reporteciudad;

import android.os.Bundle;
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

        reporteManager = new ReporteManager(this);
        setupButtons();
    }

    private void setupButtons() {
        binding.btnBuscar.setOnClickListener(v -> {
            String id = binding.etIdReporte.getText().toString().trim();
            if (id.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese un ID", Toast.LENGTH_SHORT).show();
                return;
            }
            buscarReporte(id);
        });
    }

    private void buscarReporte(String id) {
        Reporte reporte = reporteManager.buscarReporte(id);
        if (reporte != null) {
            binding.tvTitulo.setText("Título: " + reporte.getTitulo());
            binding.tvDescripcion.setText("Descripción: " + reporte.getDescripcion());
            binding.tvFecha.setText("Fecha: " + reporte.getFecha());
            // Aquí podrías mostrar la imagen si lo deseas
        } else {
            Toast.makeText(this, "No se encontró ningún reporte con ese ID", Toast.LENGTH_SHORT).show();
            binding.tvTitulo.setText("");
            binding.tvDescripcion.setText("");
            binding.tvFecha.setText("");
        }
    }
} 