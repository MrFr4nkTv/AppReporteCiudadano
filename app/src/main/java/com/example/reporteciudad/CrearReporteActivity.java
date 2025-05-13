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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.example.reporteciudad.api.ReporteRequest;
import com.example.reporteciudad.api.ImgurUploader;
import android.util.Log;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;

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
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbxn8CPKL_b4roQ-9GyanMkzPdtZyGCfmCVgABqlMs4u0TJlnX1MVvrvgxe6B8IVUYib6g/exec";
    private Uri photoURI;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int TIMEOUT_MS = 30000; // Aumentado a 30 segundos
    private static final int MAX_RETRIES = 3; // Número máximo de reintentos

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
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                        fotosAdapter.agregarFoto(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.reporteciudad.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
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
        // Deshabilitar el botón y cambiar el texto
        binding.btnEnviarReporte.setEnabled(false);
        binding.btnEnviarReporte.setText("Enviando...");

        // Subir imágenes a Imgur en un hilo separado
        new Thread(() -> {
            try {
                ImgurUploader imgurUploader = new ImgurUploader();
                List<String> imageLinks = new ArrayList<>();
                
                for (Bitmap foto : fotos) {
                    String imageLink = imgurUploader.uploadImage(foto);
                    imageLinks.add(imageLink);
                }

                // Crear el objeto ReporteRequest con el nuevo código de reporte
                ReporteRequest reporteRequest = new ReporteRequest(
                    binding.etTitulo.getText().toString(),
                    binding.etDescripcion.getText().toString(),
                    imageLinks,
                    binding.etNombreContacto.getText().toString(),
                    binding.etTelefonoContacto.getText().toString(),
                    binding.etDireccionContacto.getText().toString()
                );
                
                // Obtener el código único generado
                final String codigoReporte = reporteRequest.getCodigoReporte();
                Log.d("ReporteCiudad", "Código de reporte generado: " + codigoReporte);
                
                // Construir URL con parámetros
                StringBuilder urlBuilder = new StringBuilder(GOOGLE_SHEETS_URL);
                urlBuilder.append("?codigo_reporte=").append(Uri.encode(codigoReporte));
                urlBuilder.append("&titulo=").append(Uri.encode(reporteRequest.getTitulo()));
                urlBuilder.append("&descripcion=").append(Uri.encode(reporteRequest.getDescripcion()));
                urlBuilder.append("&nombreContacto=").append(Uri.encode(reporteRequest.getNombreContacto()));
                urlBuilder.append("&telefonoContacto=").append(Uri.encode(reporteRequest.getTelefonoContacto()));
                urlBuilder.append("&direccionContacto=").append(Uri.encode(reporteRequest.getDireccionContacto()));
                
                // Concatenar fotos
                StringBuilder fotosStr = new StringBuilder();
                for (int i = 0; i < reporteRequest.getFotos().size(); i++) {
                    fotosStr.append(reporteRequest.getFotos().get(i));
                    if (i < reporteRequest.getFotos().size() - 1) {
                        fotosStr.append(",");
                    }
                }
                urlBuilder.append("&fotos=").append(Uri.encode(fotosStr.toString()));
                
                String finalUrl = urlBuilder.toString();
                Log.d("ReporteCiudad", "URL: " + finalUrl);

                // Intentar enviar el reporte con reintentos
                int retryCount = 0;
                boolean success = false;
                Exception lastException = null;

                while (retryCount < MAX_RETRIES && !success) {
                    try {
                        if (retryCount > 0) {
                            Log.d("ReporteCiudad", "Reintentando envío (intento " + (retryCount + 1) + " de " + MAX_RETRIES + ")");
                            Thread.sleep(1000); // Esperar 1 segundo entre reintentos
                        }

                        URL url = new URL(finalUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(TIMEOUT_MS);
                        connection.setReadTimeout(TIMEOUT_MS);
                        connection.setDoInput(true);
                        
                        try {
                            int responseCode = connection.getResponseCode();
                            Log.d("ReporteCiudad", "Código de respuesta: " + responseCode);
                            
                            BufferedReader in;
                            if (responseCode >= 200 && responseCode < 300) {
                                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            } else {
                                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            }
                            
                            String inputLine;
                            StringBuilder response = new StringBuilder();
                            
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            
                            String responseString = response.toString();
                            Log.d("ReporteCiudad", "Respuesta: " + responseString);
                            
                            final int finalResponseCode = responseCode;
                            final String finalResponse = responseString;
                            
                            runOnUiThread(() -> {
                                if (finalResponseCode >= 200 && finalResponseCode < 300) {
                                    Toast.makeText(CrearReporteActivity.this, 
                                        "Reporte enviado correctamente", 
                                        Toast.LENGTH_SHORT).show();
                                    mostrarDialogoIdReporte(codigoReporte);
                                    limpiarCampos();
                                } else {
                                    String mensajeError = "Error al enviar reporte";
                                    try {
                                        JSONObject jsonError = new JSONObject(finalResponse);
                                        if (jsonError.has("message")) {
                                            mensajeError += ": " + jsonError.getString("message");
                                        }
                                    } catch (JSONException e) {
                                        mensajeError += " (Código: " + finalResponseCode + ")";
                                    }
                                    Toast.makeText(CrearReporteActivity.this, 
                                        mensajeError, 
                                        Toast.LENGTH_LONG).show();
                                    Log.e("ReporteCiudad", "Error HTTP: " + finalResponseCode + " - " + finalResponse);
                                }
                                binding.btnEnviarReporte.setEnabled(true);
                                binding.btnEnviarReporte.setText("Enviar Reporte");
                            });
                            
                            success = true;
                        } finally {
                            connection.disconnect();
                        }
                    } catch (Exception e) {
                        lastException = e;
                        Log.e("ReporteCiudad", "Error en intento " + (retryCount + 1) + ": " + e.getMessage());
                        retryCount++;
                    }
                }

                if (!success) {
                    final Exception finalException = lastException;
                    runOnUiThread(() -> {
                        Toast.makeText(CrearReporteActivity.this, 
                            "Error al enviar reporte después de " + MAX_RETRIES + " intentos: " + finalException.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        binding.btnEnviarReporte.setEnabled(true);
                        binding.btnEnviarReporte.setText("Enviar Reporte");
                    });
                }
            } catch (IOException e) {
                Log.e("ReporteCiudad", "Error al subir imágenes", e);
                runOnUiThread(() -> {
                    Toast.makeText(CrearReporteActivity.this, 
                        "Error al subir las imágenes: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    binding.btnEnviarReporte.setEnabled(true);
                    binding.btnEnviarReporte.setText("Enviar Reporte");
                });
            }
        }).start();
    }
} 