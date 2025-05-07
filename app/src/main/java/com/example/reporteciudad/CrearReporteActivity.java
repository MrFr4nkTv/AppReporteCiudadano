package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.reporteciudad.databinding.ActivityCrearReporteBinding;
import com.example.reporteciudad.databinding.DialogReporteIdBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrearReporteActivity extends AppCompatActivity implements FotosAdapter.OnFotoClickListener {
    private ActivityCrearReporteBinding binding;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private List<Bitmap> fotos;
    private FotosAdapter fotosAdapter;
    private ReporteManager reporteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Habilitar el botón de retroceso en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crear Reporte");
        }

        reporteManager = new ReporteManager(this);
        fotos = new ArrayList<>();
        setupRecyclerView();
        setupLaunchers();
        setupButtons();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupRecyclerView() {
        fotosAdapter = new FotosAdapter(fotos, this);
        binding.rvFotos.setAdapter(fotosAdapter);
    }

    private void setupLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    abrirGaleria();
                } else {
                    Toast.makeText(this, "Se necesita permiso para acceder a la galería", Toast.LENGTH_SHORT).show();
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap foto = (Bitmap) extras.get("data");
                    fotosAdapter.agregarFoto(foto);
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        fotosAdapter.agregarFoto(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
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

        binding.btnSeleccionarFoto.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    abrirGaleria();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    abrirGaleria();
                }
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

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
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
        if (binding.etNombreContacto.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etTelefonoContacto.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etDireccionContacto.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su dirección", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fotos.isEmpty()) {
            Toast.makeText(this, "Por favor agregue al menos una foto", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onEliminarClick(int position) {
        fotosAdapter.eliminarFoto(position);
    }

    private void mostrarDialogoIdReporte(String idReporte) {
        DialogReporteIdBinding dialogBinding = DialogReporteIdBinding.inflate(LayoutInflater.from(this));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        
        dialogBinding.tvIdReporte.setText("ID del Reporte: " + idReporte);
        dialogBinding.tvMensaje.setText("Por favor, anote este ID para poder consultar su reporte posteriormente.");
        
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        
        dialogBinding.btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            // Regresar al menú principal
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        dialog.show();
    }

    private void limpiarCampos() {
        binding.etTitulo.setText("");
        binding.etDescripcion.setText("");
        binding.etNombreContacto.setText("");
        binding.etTelefonoContacto.setText("");
        binding.etDireccionContacto.setText("");
        fotos.clear();
        fotosAdapter.notifyDataSetChanged();
    }

    private void enviarReporte() {
        List<String> fotosBase64 = new ArrayList<>();
        for (Bitmap foto : fotos) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            foto.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String fotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            fotosBase64.add(fotoBase64);
        }

        Reporte reporte = new Reporte(
            binding.etTitulo.getText().toString(),
            binding.etDescripcion.getText().toString(),
            fotosBase64,
            binding.etNombreContacto.getText().toString(),
            binding.etTelefonoContacto.getText().toString(),
            binding.etDireccionContacto.getText().toString()
        );
        
        reporteManager.guardarReporte(reporte);

        // Mostrar el diálogo con el ID del reporte
        mostrarDialogoIdReporte(reporte.getId());
    }
} 