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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Actividad para consultar los detalles de un reporte existente
 * Permite buscar reportes por ID y muestra toda la información asociada
 */
public class ConsultarReporteActivity extends AppCompatActivity {
    private static final String TAG = "ConsultarReporteActivity";
    private ActivityConsultarReporteBinding binding;
    private FotosAdapter fotosAdapter;
    private android.widget.TextView tvEstado, tvMensajeAdmin;
    // URL del script de Google Apps que maneja los reportes (igual al de CrearReporteActivity)
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbyoruW0RIy-Y84ffDhv5T58VKOCmWSCLCVrRk25RE36sWB2PVwqcrYFFpRrVt0kihZCDQ/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializamos el ViewBinding para acceder a las vistas
        binding = ActivityConsultarReporteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos todos los componentes necesarios
        setupActionBar();
        setupRecyclerView();
        setupButtons();
        // Inicializo los nuevos TextView
        tvEstado = binding.tvEstado;
        tvMensajeAdmin = binding.tvMensajeAdmin;
    }

    private void setupActionBar() {
        // Configuramos la barra de acción con el botón de retroceso
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Consultar Reporte");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Manejamos el botón de retroceso en la barra de acción
        onBackPressed();
        return true;
    }

    private void setupRecyclerView() {
        // Configuramos el RecyclerView para mostrar las fotos horizontalmente
        fotosAdapter = new FotosAdapter(new java.util.ArrayList<>(), null);
        binding.rvFotos.setAdapter(fotosAdapter);
        binding.rvFotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupButtons() {
        // Configuramos el botón de búsqueda
        binding.btnBuscar.setOnClickListener(v -> {
            // Obtenemos el ID del reporte y eliminamos espacios en blanco
            String idReporte = binding.etIdReporte.getText().toString().trim();
            if (!idReporte.isEmpty()) {
                // Cambiamos el texto del botón y lo deshabilitamos durante la búsqueda
                binding.btnBuscar.setText("Consultando...");
                binding.btnBuscar.setEnabled(false);
                buscarReporte(idReporte);
            } else {
                Toast.makeText(this, "Por favor ingrese un ID de reporte", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarReporte(String idReporte) {
        new Thread(() -> {
            try {
                // Usamos la URL real del Google Apps Script
                String url = GOOGLE_SHEETS_URL + "?action=consultar&codigo_reporte=" + idReporte;
                Log.d(TAG, "URL de consulta: " + url);
                
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // Incrementamos el timeout
                connection.setReadTimeout(15000);
                
                // Añadimos headers para evitar caché
                connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setRequestProperty("Expires", "0");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Código de respuesta: " + responseCode);
                
                if (responseCode == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String responseStr = response.toString();
                    Log.d(TAG, "Respuesta: " + responseStr);
                    
                    // Si la respuesta comienza con <!DOCTYPE, es un error HTML, no un JSON
                    if (responseStr.trim().startsWith("<!DOCTYPE") || responseStr.trim().startsWith("<")) {
                        Log.e(TAG, "Respuesta no es JSON válido: " + responseStr.substring(0, Math.min(100, responseStr.length())));
                        runOnUiThread(() -> {
                            restaurarBoton();
                            Toast.makeText(this, "Error en el servidor. Contacte al administrador.", Toast.LENGTH_LONG).show();
                            limpiarCampos();
                        });
                        return;
                    }

                    org.json.JSONObject jsonResponse = new org.json.JSONObject(responseStr);
                    if (jsonResponse.optString("result").equals("success")) {
                        String titulo = jsonResponse.optString("titulo", "");
                        String descripcion = jsonResponse.optString("descripcion", "");
                        String nombreContacto = jsonResponse.optString("nombreContacto", "");
                        String telefonoContacto = jsonResponse.optString("telefonoContacto", "");
                        String direccionContacto = jsonResponse.optString("direccionContacto", "");
                        String estado = jsonResponse.optString("estado", "Pendiente");
                        String mensaje = jsonResponse.optString("mensaje", "Sin mensaje");
                        String fotosStr = jsonResponse.optString("fotos", "");
                        String fechaHora = jsonResponse.optString("fechaHora", "");

                        // Procesar las URLs de las fotos (separadas por coma)
                        java.util.List<String> fotosUrls = new java.util.ArrayList<>();
                        if (!fotosStr.isEmpty()) {
                            for (String urlFoto : fotosStr.split(",")) {
                                fotosUrls.add(urlFoto.trim());
                            }
                        }

                        runOnUiThread(() -> {
                            binding.tvTitulo.setText(titulo);
                            binding.tvDescripcion.setText(descripcion);
                            binding.tvNombreContacto.setText(nombreContacto);
                            binding.tvTelefonoContacto.setText(telefonoContacto);
                            binding.tvDireccionContacto.setText(direccionContacto);
                            binding.tvFecha.setText(formatearFecha(fechaHora));
                            tvEstado.setText(estado);
                            tvMensajeAdmin.setText(mensaje);
                            cargarFotosDesdeUrls(fotosUrls);
                            restaurarBoton();
                        });
                    } else {
                        runOnUiThread(() -> {
                            restaurarBoton();
                            limpiarCampos();
                            Toast.makeText(this, "Reporte no encontrado", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        restaurarBoton();
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al consultar reporte: " + e.getMessage());
                runOnUiThread(() -> {
                    restaurarBoton();
                    Toast.makeText(this, "Error al consultar el reporte", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void restaurarBoton() {
        binding.btnBuscar.setText("Buscar Reporte");
        binding.btnBuscar.setEnabled(true);
    }

    private void cargarFotosDesdeUrls(java.util.List<String> fotosUrls) {
        fotosAdapter.eliminarTodasLasFotos();
        for (String urlFoto : fotosUrls) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(urlFoto);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(() -> fotosAdapter.agregarFoto(bitmap));
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen: " + e.getMessage());
                    // Ignorar errores de carga de imagen individual
                }
            }).start();
        }
    }

    private void limpiarCampos() {
        // Limpiamos todos los campos de la interfaz
        binding.tvTitulo.setText("");
        binding.tvDescripcion.setText("");
        binding.tvNombreContacto.setText("");
        binding.tvTelefonoContacto.setText("");
        binding.tvDireccionContacto.setText("");
        binding.tvFecha.setText("");
        fotosAdapter.eliminarTodasLasFotos();
        // Limpiamos los nuevos campos
        tvEstado.setText("");
        tvMensajeAdmin.setText("");
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.isEmpty()) return "";
        // Intentar parsear formato ISO 8601
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            formatoEntrada.setLenient(true);
            java.util.Date fecha = formatoEntrada.parse(fechaOriginal);
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return formatoSalida.format(fecha);
        } catch (ParseException e) {
            // Si falla, devolver la original
            return fechaOriginal;
        }
    }
} 