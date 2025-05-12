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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.reporteciudad.api.ReporteService;
import com.example.reporteciudad.api.ReporteRequest;
import com.example.reporteciudad.api.ImgurUploader;
import com.example.reporteciudad.api.ReporteResponse;
import android.util.Log;

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
    private ReporteService reporteService;
    // Reemplaza esta URL con la que obtengas después de desplegar el script
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbzkWDtTvdSn61rNKROflTXRm5fMAkm2mpoKe6xcEk5ullHGj4wleX5YIxyZyLAAnDiA/exec/";
    private Uri photoURI;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

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

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(GOOGLE_SHEETS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        reporteService = retrofit.create(ReporteService.class);
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

                // Crear y enviar el reporte con los enlaces de las imágenes
                ReporteRequest reporteRequest = new ReporteRequest(
                    binding.etTitulo.getText().toString(),
                    binding.etDescripcion.getText().toString(),
                    imageLinks,
                    binding.etNombreContacto.getText().toString(),
                    binding.etTelefonoContacto.getText().toString(),
                    binding.etDireccionContacto.getText().toString()
                );
                
                // Log para verificar que el ID se generó correctamente
                Log.d("ReporteCiudad", "ID del reporte generado: " + reporteRequest.getId());

                runOnUiThread(() -> {
                    reporteService.enviarReporte(reporteRequest).enqueue(new Callback<ReporteResponse>() {
                        @Override
                        public void onResponse(Call<ReporteResponse> call, Response<ReporteResponse> response) {
                            String mensaje;
                            if (response.isSuccessful() && response.body() != null) {
                                ReporteResponse reporteResponse = response.body();
                                mensaje = "Reporte enviado exitosamente";
                                // Log para verificar la respuesta
                                Log.d("ReporteCiudad", "Respuesta del servidor: result=" + reporteResponse.getResult() + ", message=" + reporteResponse.getMessage());
                                mostrarDialogoIdReporte(reporteRequest.getId());
                                limpiarCampos();
                            } else {
                                mensaje = "Error al enviar el reporte. Código: " + response.code();
                                Log.e("ReporteCiudad", "Error en la respuesta: " + response.code() + " " + response.message());
                                try {
                                    if (response.errorBody() != null) {
                                        Log.e("ReporteCiudad", "Error body: " + response.errorBody().string());
                                    }
                                } catch (IOException e) {
                                    Log.e("ReporteCiudad", "Error al leer error body", e);
                                }
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(CrearReporteActivity.this, mensaje, Toast.LENGTH_LONG).show();
                                binding.btnEnviarReporte.setEnabled(true);
                                binding.btnEnviarReporte.setText("Enviar Reporte");
                            });
                        }

                        @Override
                        public void onFailure(Call<ReporteResponse> call, Throwable t) {
                            String mensaje = "Error de conexión: " + t.getMessage();
                            Log.e("ReporteCiudad", "Error de conexión", t);
                            runOnUiThread(() -> {
                                Toast.makeText(CrearReporteActivity.this, mensaje, Toast.LENGTH_LONG).show();
                                binding.btnEnviarReporte.setEnabled(true);
                                binding.btnEnviarReporte.setText("Enviar Reporte");
                            });
                        }
                    });
                });
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