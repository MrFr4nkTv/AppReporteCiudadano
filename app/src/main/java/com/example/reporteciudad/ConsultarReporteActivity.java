package com.example.reporteciudad;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.reporteciudad.databinding.ActivityConsultarReporteBinding;
import java.util.Base64;

public class ConsultarReporteActivity extends AppCompatActivity {
    private ActivityConsultarReporteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultarReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupButtons();
    }

    private void setupButtons() {
        binding.btnBuscar.setOnClickListener(v -> {
            String idReporte = binding.etIdReporte.getText().toString();
            if (idReporte.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese un ID de reporte", Toast.LENGTH_SHORT).show();
                return;
            }
            buscarReporte(idReporte);
        });
    }

    private void buscarReporte(String idReporte) {
        // Aquí iría la lógica para buscar el reporte en el servidor
        // Por ahora simulamos una respuesta
        mostrarReporteSimulado();
    }

    private void mostrarReporteSimulado() {
        binding.cardViewReporte.setVisibility(View.VISIBLE);
        binding.tvTitulo.setText("Reporte de Bache en Calle Principal");
        binding.tvDescripcion.setText("Se ha detectado un bache de aproximadamente 1 metro de diámetro en la calle principal, cerca del parque central.");
        binding.tvEstado.setText("En Proceso");

        // Aquí iría la lógica para cargar la imagen desde el servidor
        // Por ahora usamos una imagen de ejemplo
        Glide.with(this)
                .load("https://via.placeholder.com/300")
                .into(binding.ivFotoReporte);
    }
} 