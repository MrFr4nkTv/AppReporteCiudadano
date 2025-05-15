package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
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
 * Esta es la pantalla donde los ciudadanos crean sus reportes.
 * Aquí pueden ingresar toda la información necesaria y subir fotos
 * del problema que quieren reportar.
 */
public class CrearReporteActivity extends AppCompatActivity implements FotosAdapter.OnFotoClickListener {
    // Constantes para el manejo de permisos y configuración
    private static final String TAG = "CrearReporteActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    // URL del script de Google Apps que maneja los reportes
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbyoruW0RIy-Y84ffDhv5T58VKOCmWSCLCVrRk25RE36sWB2PVwqcrYFFpRrVt0kihZCDQ/exec";
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

    // Estas son las categorías que puede elegir el ciudadano para su reporte
    private final String[] tiposReporte = {
        "ALUMBRADO PÚBLICO",
        "ANIMALES CALLEJEROS O EN SITUACIÓN DE ABANDONO",
        "BACHES",
        "BASURA O ESCOMBRO",
        "FUMIGACIÓN O PLAGAS",
        "FUGAS O DRENAJE",
        "OTRO ASUNTO"
    };

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
        setupDropdowns();
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
     * Configura los dropdowns para colonias y tipos de reporte
     * - Colonias: carga desde archivo local con autocompletado
     * - Tipos de reporte: lista predefinida de opciones
     */
    private void setupDropdowns() {
        // Configurar el dropdown de tipos de reporte
        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tiposReporte
        );
        binding.actvTipoReporte.setAdapter(tiposAdapter);

        // Configurar el dropdown de colonias
        List<String> colonias = cargarColonias();
        ArrayAdapter<String> coloniasAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            colonias
        );
        binding.actvColonia.setAdapter(coloniasAdapter);
        
        // Configuración adicional para el autocompletado de colonias
        binding.actvColonia.setThreshold(1); // Muestra sugerencias después de 1 carácter
        binding.actvColonia.setOnItemClickListener((parent, view, position, id) -> {
            // Oculta el teclado cuando se selecciona una opción
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.actvColonia.getWindowToken(), 0);
        });
    }

    /**
     * Carga la lista de colonias desde el archivo assets/Listado de colonias.txt
     * El archivo contiene un select HTML con options
     * @return Lista de colonias disponibles
     */
    private List<String> cargarColonias() {
        List<String> colonias = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Listado de colonias.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                // Busca líneas que contengan la etiqueta option
                if (line.contains("<option value=\"")) {
                    // Extrae el valor entre las comillas del atributo value
                    int startIndex = line.indexOf("value=\"") + 7;
                    int endIndex = line.indexOf("\"", startIndex);
                    if (startIndex >= 7 && endIndex > startIndex) {
                        String colonia = line.substring(startIndex, endIndex).trim();
                        if (!colonia.isEmpty()) {
                            colonias.add(colonia);
                        }
                    }
                }
            }
            reader.close();

            if (colonias.isEmpty()) {
                Log.e(TAG, "No se encontraron colonias en el archivo");
                colonias.add("Error al cargar colonias");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al cargar colonias: " + e.getMessage());
            colonias.add("Error al cargar colonias");
        }
        return colonias;
    }

    /**
     * Cuando el ciudadano presiona "Enviar Reporte":
     * 1. Verificamos que llenó todos los campos necesarios
     * 2. Subimos sus fotos a Imgur para tener URLs permanentes
     * 3. Enviamos toda la información a Google Sheets
     * 4. Le mostramos su número de reporte para seguimiento
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
                ReporteRequest reporteRequest = new ReporteRequest(
                    binding.etNombreInteresado.getText().toString(),
                    binding.actvColonia.getText().toString(),
                    binding.etDireccion.getText().toString(),
                    binding.etCelular.getText().toString(),
                    binding.etCorreo.getText().toString(),
                    binding.actvTipoReporte.getText().toString(),
                    binding.etDescripcion.getText().toString(),
                    imageLinks
                );
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
     * Antes de enviar el reporte, nos aseguramos que:
     * - Ingresó su nombre y datos de contacto
     * - Seleccionó la colonia y escribió la dirección
     * - Eligió un tipo de reporte y lo describió
     * - Subió al menos una foto como evidencia
     */
    private boolean validarCampos() {
        if (binding.etNombreInteresado.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese el nombre del interesado", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.actvColonia.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor seleccione una colonia", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etDireccion.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese la dirección", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etCelular.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un número de celular", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etCorreo.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un correo electrónico", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.actvTipoReporte.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor seleccione un tipo de reporte", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etDescripcion.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show();
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
        binding.etNombreInteresado.setText("");
        binding.actvColonia.setText("");
        binding.etDireccion.setText("");
        binding.etCelular.setText("");
        binding.etCorreo.setText("");
        binding.actvTipoReporte.setText("");
        binding.etDescripcion.setText("");
        fotos.clear();
        fotosAdapter.notifyDataSetChanged();
    }

    /**
     * Las fotos se procesan antes de subirlas:
     * - Se comprimen para que no sean muy pesadas
     * - Se suben a Imgur para tener un link permanente
     * - Si algo falla, le avisamos al ciudadano
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
     * La información se guarda en Google Sheets para que:
     * - Los administradores puedan ver todos los reportes
     * - Puedan actualizar el estado del reporte
     * - El ciudadano pueda consultar su reporte después
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
                
                // Añadimos headers para evitar caché
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setRequestProperty("Expires", "0");
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Código de respuesta: " + responseCode);
                
                if (responseCode >= 200 && responseCode < 300) {
                    // Intentamos leer la respuesta para verificar que sea JSON válido
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String responseStr = response.toString();
                    Log.d(TAG, "Respuesta: " + responseStr);
                    
                    // Verificamos si la respuesta es HTML (error) o JSON (éxito)
                    if (responseStr.trim().startsWith("<!DOCTYPE") || responseStr.trim().startsWith("<")) {
                        Log.e(TAG, "Respuesta no es JSON válido: " + responseStr.substring(0, Math.min(100, responseStr.length())));
                        retryCount++;
                        continue;
                    }
                    
                    try {
                        // Intentamos parsear la respuesta como JSON
                        JSONObject jsonResponse = new JSONObject(responseStr);
                        if (jsonResponse.optString("result").equals("success")) {
                            return true;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear JSON: " + e.getMessage());
                        retryCount++;
                        continue;
                    }
                    
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
        // Agregamos la acción para el script de Google Apps
        urlBuilder.append("?action=crear");
        // Agregamos todos los parámetros del reporte a la URL
        urlBuilder.append("&codigo_reporte=").append(Uri.encode(reporteRequest.getCodigoReporte()));
        urlBuilder.append("&nombre_interesado=").append(Uri.encode(reporteRequest.getNombreInteresado()));
        urlBuilder.append("&colonia=").append(Uri.encode(reporteRequest.getColonia()));
        urlBuilder.append("&direccion=").append(Uri.encode(reporteRequest.getDireccion()));
        urlBuilder.append("&celular=").append(Uri.encode(reporteRequest.getCelular()));
        urlBuilder.append("&correo=").append(Uri.encode(reporteRequest.getCorreo()));
        urlBuilder.append("&tipo_reporte=").append(Uri.encode(reporteRequest.getTipoReporte()));
        urlBuilder.append("&descripcion=").append(Uri.encode(reporteRequest.getDescripcion()));
        
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