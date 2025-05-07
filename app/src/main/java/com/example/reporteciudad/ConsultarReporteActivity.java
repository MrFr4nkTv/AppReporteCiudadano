package com.example.reporteciudad;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.reporteciudad.databinding.ActivityConsultarReporteBinding;
import java.util.List;

public class ConsultarReporteActivity extends AppCompatActivity {
    private ActivityConsultarReporteBinding binding;
    private ReporteManager reporteManager;
    private FotosAdapter fotosAdapter;

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
        setupRecyclerView();
        setupButtons();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupRecyclerView() {
        fotosAdapter = new FotosAdapter(null, null);
        binding.rvFotos.setAdapter(fotosAdapter);
        binding.rvFotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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

        // Convertir las fotos de base64 a Bitmap
        List<String> fotosBase64 = reporte.getFotosBase64();
        if (fotosBase64 != null && !fotosBase64.isEmpty()) {
            for (String fotoBase64 : fotosBase64) {
                byte[] imageBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                fotosAdapter.agregarFoto(bitmap);
            }
        }
    }

    private void limpiarCampos() {
        binding.tvTitulo.setText("");
        binding.tvDescripcion.setText("");
        binding.tvNombreContacto.setText("");
        binding.tvTelefonoContacto.setText("");
        binding.tvDireccionContacto.setText("");
        fotosAdapter.eliminarTodasLasFotos();
    }
} 