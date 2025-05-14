package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.reporteciudad.databinding.ActivityCrearReporteBinding;
import com.example.reporteciudad.databinding.DialogReporteIdBinding;
import com.example.reporteciudad.api.ReporteRequest;
import com.example.reporteciudad.api.ImgurUploader;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Actividad principal para la creación de reportes ciudadanos
 * Permite tomar fotos, seleccionar de la galería y enviar reportes
 * Implementa la interfaz OnFotoClickListener para manejar la eliminación de fotos
 */
public class CrearReporteActivity extends AppCompatActivity implements FotosAdapter.OnFotoClickListener {
    // Constantes para el manejo de permisos y configuración
    private static final String TAG = "CrearReporteActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    // URL del script de Google Apps que maneja los reportes
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbxn8CPKL_b4roQ-9GyanMkzPdtZyGCfmCVgABqlMs4u0TJlnX1MVvrvgxe6B8IVUYib6g/exec";
    // Tiempo máximo de espera para las peticiones al servidor
    private static final int TIMEOUT_MS = 30000;
    // Número máximo de intentos si falla la conexión
    private static final int MAX_RETRIES = 3;

    // Variables para el manejo de la UI y datos
    private ActivityCrearReporteBinding binding;
    // Launchers para manejar los resultados de la cámara y galería
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    // Lista de fotos seleccionadas y adaptador para mostrarlas
    private List<Bitmap> fotos;
    private FotosAdapter fotosAdapter;
    // URI de la foto temporal cuando se usa la cámara
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializamos el ViewBinding para acceder a las vistas
        binding = ActivityCrearReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuramos todos los componentes necesarios
        setupActionBar();
        initializeComponents();
        setupRecyclerView();
        setupLaunchers();
        setupButtons();
    }

    /**
     * Configura la barra de acción con el botón de retroceso
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crear Reporte");
        }
    }

    /**
     * Inicializa los componentes básicos de la actividad
     */
    private void initializeComponents() {
        fotos = new ArrayList<>();
    }

    /**
     * Configura el RecyclerView para mostrar las fotos seleccionadas
     */
    private void setupRecyclerView() {
        fotosAdapter = new FotosAdapter(fotos, this);
        binding.rvFotos.setAdapter(fotosAdapter);
    }

    /**
     * Configura los launchers para manejar permisos, cámara y galería
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

        // Launcher para manejar el resultado de la cámara
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        // Obtenemos la imagen capturada y la agregamos al adaptador
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                        fotosAdapter.agregarFoto(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
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
        // Botón para tomar fotos con la cámara
        binding.btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitamos permiso de cámara si no lo tenemos
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                abrirCamara();
            }
        });

        // Botón para seleccionar fotos de la galería
        binding.btnSeleccionarFoto.setOnClickListener(v -> {
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
                enviarReporte();
            }
        });
    }

    /**
     * Abre la cámara para tomar una foto
     */
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // Creamos un archivo temporal para la foto
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                // Obtenemos la URI del archivo usando FileProvider
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.reporteciudad.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    /**
     * Crea un archivo temporal para guardar la foto
     */
    private File createImageFile() throws IOException {
        // Creamos un nombre único para el archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
        return image;
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
        // Eliminamos la foto seleccionada del adaptador
        fotosAdapter.eliminarFoto(position);
    }

    /**
     * Muestra un diálogo con el ID del reporte generado
     */
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
            // Regresamos al menú principal
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        dialog.show();
    }

    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarCampos() {
        binding.etTitulo.setText("");
        binding.etDescripcion.setText("");
        binding.etNombreContacto.setText("");
        binding.etTelefonoContacto.setText("");
        binding.etDireccionContacto.setText("");
        fotos.clear();
        fotosAdapter.notifyDataSetChanged();
    }

    /**
     * Proceso principal para enviar el reporte
     */
    private void enviarReporte() {
        if (!validarCampos()) {
            return;
        }

        // Deshabilitamos el botón mientras se envía para evitar envíos duplicados
        binding.btnEnviarReporte.setEnabled(false);
        binding.btnEnviarReporte.setText("Enviando...");

        // Ejecutamos el envío en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Primero subimos todas las imágenes a Imgur
                List<String> imageLinks = subirImagenes();
                if (imageLinks == null) {
                    mostrarErrorEnUI("Error al subir las imágenes");
                    return;
                }

                // Creamos el objeto con todos los datos del reporte
                ReporteRequest reporteRequest = crearReporteRequest(imageLinks);
                String codigoReporte = reporteRequest.getCodigoReporte();
                
                // Intentamos enviar el reporte al servidor
                if (enviarReporteAlServidor(reporteRequest)) {
                    mostrarExitoEnUI(codigoReporte);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al enviar reporte", e);
                mostrarErrorEnUI("Error al enviar el reporte: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Sube todas las imágenes a Imgur y devuelve sus URLs
     */
    private List<String> subirImagenes() {
        try {
            // Subimos cada imagen a Imgur y obtenemos los links
            ImgurUploader imgurUploader = new ImgurUploader();
            List<String> imageLinks = new ArrayList<>();
            
            for (Bitmap foto : fotos) {
                String imageLink = imgurUploader.uploadImage(foto);
                if (imageLink != null) {
                    imageLinks.add(imageLink);
                }
            }
            return imageLinks;
        } catch (Exception e) {
            Log.e(TAG, "Error al subir imágenes", e);
            return null;
        }
    }

    /**
     * Crea el objeto ReporteRequest con los datos del formulario
     */
    private ReporteRequest crearReporteRequest(List<String> imageLinks) {
        return new ReporteRequest(
            binding.etTitulo.getText().toString(),
            binding.etDescripcion.getText().toString(),
            imageLinks,
            binding.etNombreContacto.getText().toString(),
            binding.etTelefonoContacto.getText().toString(),
            binding.etDireccionContacto.getText().toString()
        );
    }

    /**
     * Envía el reporte al servidor con reintentos en caso de fallo
     */
    private boolean enviarReporteAlServidor(ReporteRequest reporteRequest) {
        // Construimos la URL con todos los parámetros del reporte
        String url = construirUrl(reporteRequest);
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            try {
                // Esperamos un poco entre intentos para no saturar el servidor
                if (retryCount > 0) {
                    Thread.sleep(1000);
                }
                
                // Configuramos la conexión con timeout para evitar bloqueos
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                
                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    return true;
                }
                
                retryCount++;
            } catch (Exception e) {
                Log.e(TAG, "Error en intento " + (retryCount + 1), e);
                retryCount++;
            }
        }
        return false;
    }

    /**
     * Construye la URL para enviar el reporte al servidor
     */
    private String construirUrl(ReporteRequest reporteRequest) {
        StringBuilder urlBuilder = new StringBuilder(GOOGLE_SHEETS_URL);
        // Agregamos todos los parámetros del reporte a la URL
        urlBuilder.append("?codigo_reporte=").append(Uri.encode(reporteRequest.getCodigoReporte()));
        urlBuilder.append("&titulo=").append(Uri.encode(reporteRequest.getTitulo()));
        urlBuilder.append("&descripcion=").append(Uri.encode(reporteRequest.getDescripcion()));
        urlBuilder.append("&nombreContacto=").append(Uri.encode(reporteRequest.getNombreContacto()));
        urlBuilder.append("&telefonoContacto=").append(Uri.encode(reporteRequest.getTelefonoContacto()));
        urlBuilder.append("&direccionContacto=").append(Uri.encode(reporteRequest.getDireccionContacto()));
        
        // Concatenamos todas las URLs de las fotos
        StringBuilder fotosStr = new StringBuilder();
        for (int i = 0; i < reporteRequest.getFotos().size(); i++) {
            fotosStr.append(reporteRequest.getFotos().get(i));
            if (i < reporteRequest.getFotos().size() - 1) {
                fotosStr.append(",");
            }
        }
        urlBuilder.append("&fotos=").append(Uri.encode(fotosStr.toString()));
        
        return urlBuilder.toString();
    }

    /**
     * Muestra un mensaje de error en la UI
     */
    private void mostrarErrorEnUI(String mensaje) {
        runOnUiThread(() -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            binding.btnEnviarReporte.setEnabled(true);
            binding.btnEnviarReporte.setText("Enviar Reporte");
        });
    }

    /**
     * Muestra el mensaje de éxito y el ID del reporte
     */
    private void mostrarExitoEnUI(String codigoReporte) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show();
            mostrarDialogoIdReporte(codigoReporte);
            limpiarCampos();
            binding.btnEnviarReporte.setEnabled(true);
            binding.btnEnviarReporte.setText("Enviar Reporte");
        });
    }
} 