package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.reporteciudad.databinding.ActivityContactoBinding;
import com.example.reporteciudad.databinding.DialogAgradecimientoBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactoActivity extends AppCompatActivity implements FotosAdapter.OnFotoClickListener {
    private ActivityContactoBinding binding;
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private List<Bitmap> fotos;
    private FotosAdapter fotosAdapter;

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

        // Inicializar lista de fotos y adapter
        fotos = new ArrayList<>();
        setupRecyclerView();
        setupTipoProblema();
        setupLaunchers();
        setupButtons();

        // Auto-rellenar información del sistema
        autoRellenarInformacionSistema();
    }

    private void setupRecyclerView() {
        fotosAdapter = new FotosAdapter(fotos, this);
        binding.rvImagenes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImagenes.setAdapter(fotosAdapter);
    }

    private void setupTipoProblema() {
        String[] tiposProblema = {
            "Error de la aplicación",
            "Sugerencia de mejora",
            "Problema de rendimiento",
            "Problema de interfaz",
            "Otro"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tiposProblema
        );

        binding.actvTipoProblema.setAdapter(adapter);
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
        binding.btnAgregarImagen.setOnClickListener(v -> {
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
                mostrarDialogoAgradecimiento();
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private boolean validarCampos() {
        if (binding.etNombre.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etCorreo.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.actvTipoProblema.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor seleccione el tipo de problema", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etDescripcionProblema.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una descripción del problema", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void mostrarDialogoAgradecimiento() {
        DialogAgradecimientoBinding dialogBinding = DialogAgradecimientoBinding.inflate(getLayoutInflater());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        
        dialogBinding.btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onEliminarClick(int position) {
        fotosAdapter.eliminarFoto(position);
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