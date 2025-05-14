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
import android.util.Log;

/**
 * Actividad para consultar los detalles de un reporte existente
 * Permite buscar reportes por ID y muestra toda la información asociada
 */
public class ConsultarReporteActivity extends AppCompatActivity {
    private static final String TAG = "ConsultarReporteActivity";
    private ActivityConsultarReporteBinding binding;
    private ReporteManager reporteManager;
    private FotosAdapter fotosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializamos el ViewBinding para acceder a las vistas
        binding = ActivityConsultarReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos todos los componentes necesarios
        setupActionBar();
        initializeComponents();
        setupRecyclerView();
        setupButtons();
    }

    private void setupActionBar() {
        // Configuramos la barra de acción con el botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consultar Reporte");
        }
    }

    private void initializeComponents() {
        // Inicializamos el gestor de reportes que se encarga de la base de datos
        reporteManager = new ReporteManager(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Manejamos el botón de retroceso en la barra de acción
        onBackPressed();
        return true;
    }

    private void setupRecyclerView() {
        // Configuramos el RecyclerView para mostrar las fotos horizontalmente
        fotosAdapter = new FotosAdapter(null, null);
        binding.rvFotos.setAdapter(fotosAdapter);
        binding.rvFotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupButtons() {
        // Configuramos el botón de búsqueda
        binding.btnBuscar.setOnClickListener(v -> {
            // Obtenemos el ID del reporte y eliminamos espacios en blanco
            String idReporte = binding.etIdReporte.getText().toString().trim();
            if (!idReporte.isEmpty()) {
                buscarReporte(idReporte);
            } else {
                Toast.makeText(this, "Por favor ingrese un ID de reporte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarReporte(String idReporte) {
        // Buscamos el reporte en la base de datos local
        Reporte reporte = reporteManager.obtenerReporte(idReporte);
        if (reporte != null) {
            mostrarReporte(reporte);
        } else {
            Toast.makeText(this, "No se encontró el reporte", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        }
    }

    private void mostrarReporte(Reporte reporte) {
        try {
            // Mostramos todos los datos del reporte en la interfaz
            binding.tvTitulo.setText(reporte.getTitulo());
            binding.tvDescripcion.setText(reporte.getDescripcion());
            binding.tvNombreContacto.setText(reporte.getNombreContacto());
            binding.tvTelefonoContacto.setText(reporte.getTelefonoContacto());
            binding.tvDireccionContacto.setText(reporte.getDireccionContacto());

            // Cargamos las fotos del reporte desde Base64
            cargarFotos(reporte.getFotosBase64());
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar reporte", e);
            Toast.makeText(this, "Error al mostrar el reporte", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFotos(List<String> fotosBase64) {
        // Verificamos si hay fotos para cargar
        if (fotosBase64 == null || fotosBase64.isEmpty()) {
            return;
        }

        // Limpiamos las fotos anteriores y cargamos las nuevas
        fotosAdapter.eliminarTodasLasFotos();
        for (String fotoBase64 : fotosBase64) {
            try {
                // Convertimos la imagen de Base64 a Bitmap
                byte[] imageBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    fotosAdapter.agregarFoto(bitmap);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar foto", e);
            }
        }
    }

    private void limpiarCampos() {
        // Limpiamos todos los campos de la interfaz
        binding.tvTitulo.setText("");
        binding.tvDescripcion.setText("");
        binding.tvNombreContacto.setText("");
        binding.tvTelefonoContacto.setText("");
        binding.tvDireccionContacto.setText("");
        fotosAdapter.eliminarTodasLasFotos();
    }
} 