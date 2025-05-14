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

/**
 * Actividad para reportar problemas o sugerencias sobre la aplicación
 * Permite enviar reportes con imágenes y detalles del sistema
 */
public class ContactoActivity extends AppCompatActivity implements FotosAdapter.OnFotoClickListener {
    // Binding para acceder a las vistas
    private ActivityContactoBinding binding;
    // Constante para el permiso de galería
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    // Launchers para manejar permisos y galería
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    // Lista de fotos y adaptador para mostrarlas
    private List<Bitmap> fotos;
    private FotosAdapter fotosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializamos el ViewBinding
        binding = ActivityContactoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuramos la barra de acción
        setupActionBar();
        // Inicializamos los componentes
        initializeComponents();
        // Configuramos los componentes de la UI
        setupRecyclerView();
        setupTipoProblema();
        setupLaunchers();
        setupButtons();
        // Obtenemos información del sistema
        autoRellenarInformacionSistema();
    }

    /**
     * Configura la barra de acción con el botón de retroceso
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reportar Problema");
        }
    }

    /**
     * Inicializa los componentes básicos
     */
    private void initializeComponents() {
        fotos = new ArrayList<>();
    }

    /**
     * Configura el RecyclerView para mostrar las imágenes
     */
    private void setupRecyclerView() {
        fotosAdapter = new FotosAdapter(fotos, this);
        binding.rvImagenes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImagenes.setAdapter(fotosAdapter);
    }

    /**
     * Configura el AutoCompleteTextView con los tipos de problema
     */
    private void setupTipoProblema() {
        // Lista de tipos de problemas predefinidos
        String[] tiposProblema = {
            "Error de la aplicación",
            "Sugerencia de mejora",
            "Problema de rendimiento",
            "Problema de interfaz",
            "Otro"
        };

        // Creamos el adaptador para el AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tiposProblema
        );

        binding.actvTipoProblema.setAdapter(adapter);
    }

    /**
     * Configura los launchers para permisos y galería
     */
    private void setupLaunchers() {
        // Launcher para solicitar permisos de galería
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

        // Launcher para manejar el resultado de la galería
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        // Obtenemos la imagen seleccionada y la agregamos al adaptador
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        fotosAdapter.agregarFoto(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    /**
     * Configura los botones y sus listeners
     */
    private void setupButtons() {
        // Botón para agregar imágenes
        binding.btnAgregarImagen.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Para Android 13 y superior, usamos el permiso READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    abrirGaleria();
                }
            } else {
                // Para versiones anteriores, usamos READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    abrirGaleria();
                }
            }
        });

        // Botón para enviar el reporte
        binding.btnEnviarReporte.setOnClickListener(v -> {
            if (validarCampos()) {
                mostrarDialogoAgradecimiento();
            }
        });
    }

    /**
     * Abre la galería para seleccionar una imagen
     */
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * Valida que todos los campos requeridos estén llenos
     */
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

    /**
     * Muestra un diálogo de agradecimiento al enviar el reporte
     */
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
        // Manejamos el botón de retroceso en la barra de acción
        onBackPressed();
        return true;
    }

    @Override
    public void onEliminarClick(int position) {
        // Eliminamos la foto seleccionada del adaptador
        fotosAdapter.eliminarFoto(position);
    }

    /**
     * Obtiene y muestra información del sistema automáticamente
     */
    private void autoRellenarInformacionSistema() {
        try {
            // Obtenemos la versión de la aplicación
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.etVersionApp.setText(versionName);

            // Obtenemos información del dispositivo
            String dispositivo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
            binding.etDispositivo.setText(dispositivo);

            // Obtenemos la versión de Android
            String versionAndroid = "Android " + android.os.Build.VERSION.RELEASE;
            binding.etVersionAndroid.setText(versionAndroid);

        } catch (Exception e) {
            Toast.makeText(this, "Error al obtener información del sistema", Toast.LENGTH_SHORT).show();
        }
    }
} 