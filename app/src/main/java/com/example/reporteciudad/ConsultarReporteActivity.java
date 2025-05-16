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
    // Declaramos todos los TextView para los campos del reporte
    private android.widget.TextView tvEstado, tvMensajeAdmin, tvColonia, tvCorreo;
    // URL del script de Google Apps que maneja los reportes (igual al de CrearReporteActivity)
    private static final String GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbxgngYX6Sn_UaoR1rHx4RWt4BTvUap5s8hzM29niwHwrtJKBizK2nV-n24mQg4SDLvoUA/exec";

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
        // Inicializamos todos los TextView
        tvEstado = binding.tvEstado;
        tvMensajeAdmin = binding.tvMensajeAdmin;
        tvColonia = binding.tvColonia;
        tvCorreo = binding.tvCorreo;
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
                    Log.d(TAG, "Respuesta completa del servidor: " + responseStr);
                    
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
                        // Obtenemos todos los campos del reporte con los nombres exactos del AppScript
                        String codigoReporte = jsonResponse.optString("codigo_reporte", "");
                        String nombreInteresado = jsonResponse.optString("nombre_interesado", "");
                        String colonia = jsonResponse.optString("colonia", "");
                        String direccion = jsonResponse.optString("direccion", "");
                        String celular = jsonResponse.optString("celular", "");
                        String correo = jsonResponse.optString("correo", "");
                        String tipoReporte = jsonResponse.optString("tipo_reporte", "");
                        String descripcion = jsonResponse.optString("descripcion", "");
                        String fotosStr = jsonResponse.optString("fotos", "");
                        String fechaHora = jsonResponse.optString("fechaHora", "");
                        String estado = jsonResponse.optString("estado", "Pendiente");
                        String mensaje = jsonResponse.optString("mensaje", "Sin mensaje");

                        // Procesar las URLs de las fotos 
                        java.util.List<String> fotosUrls = new java.util.ArrayList<>();
                        if (!fotosStr.isEmpty()) {
                            Log.d(TAG, "Datos de fotos recibidos (sin procesar): " + fotosStr);
                            
                            // Probemos diferentes estrategias para extraer URLs válidas
                            
                            // 1. Primero, intentamos procesar como URLs separadas por comas
                            if (fotosStr.contains("http")) {
                                String[] urls = fotosStr.split(",");
                                for (String urlString : urls) {
                                    urlString = urlString.trim();
                                    if (!urlString.isEmpty() && urlString.startsWith("http")) {
                                        fotosUrls.add(urlString);
                                        Log.d(TAG, "URL directa añadida: " + urlString);
                                    }
                                }
                            }
                            // 2. Si no se encontraron URLs válidas, intentamos extraer de fórmulas HYPERLINK
                            else if (fotosStr.contains("HYPERLINK")) {
                                Log.d(TAG, "Intentando extraer URLs de fórmulas HYPERLINK");
                                // Dividimos por líneas en caso de múltiples fórmulas
                                String[] formulas = fotosStr.split("\n");
                                for (String formula : formulas) {
                                    Log.d(TAG, "Procesando fórmula: " + formula);
                                    try {
                                        // Patrón para extraer URL de HYPERLINK("URL";
                                        int startIndex = formula.indexOf("HYPERLINK(\"") + 11; // +11 para omitir 'HYPERLINK("'
                                        if (startIndex < 11) continue; // Si no encontramos 'HYPERLINK("'
                                        
                                        int endIndex = formula.indexOf("\"", startIndex);
                                        if (endIndex > startIndex) {
                                            String extractedUrl = formula.substring(startIndex, endIndex);
                                            if (!extractedUrl.isEmpty() && extractedUrl.startsWith("http")) {
                                                fotosUrls.add(extractedUrl);
                                                Log.d(TAG, "URL extraída correctamente: " + extractedUrl);
                                            } else {
                                                Log.e(TAG, "URL extraída no válida: " + extractedUrl);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error al extraer URL de fórmula: " + e.getMessage());
                                    }
                                }
                            }
                            // 3. Si las dos estrategias anteriores fallaron, revisamos si hay errores específicos
                            else {
                                Log.e(TAG, "No se pudieron extraer URLs válidas del campo fotos: " + fotosStr);
                                if (fotosStr.equals("Ver foto 1") || fotosStr.contains("Ver foto")) {
                                    Log.e(TAG, "Detectada etiqueta 'Ver foto' en lugar de URL - Problema en el script del servidor");
                                }
                            }
                        } else {
                            Log.d(TAG, "Campo de fotos vacío");
                        }
                        
                        // Registramos cuántas fotos se encontraron
                        Log.d(TAG, "Número de fotos válidas encontradas: " + fotosUrls.size());

                        runOnUiThread(() -> {
                            binding.tvTitulo.setText(tipoReporte);
                            binding.tvDescripcion.setText(descripcion);
                            binding.tvNombreContacto.setText(nombreInteresado);
                            binding.tvTelefonoContacto.setText(celular);
                            binding.tvDireccionContacto.setText(direccion);
                            binding.tvFecha.setText(formatearFecha(fechaHora));
                            tvEstado.setText(estado);
                            tvMensajeAdmin.setText(mensaje);
                            // Mostramos los nuevos campos
                            tvColonia.setText(colonia);
                            tvCorreo.setText(correo);
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
        
        // Si no hay fotos para cargar, mostramos mensaje
        if (fotosUrls.isEmpty()) {
            Log.d(TAG, "No hay fotos para cargar");
            runOnUiThread(() -> {
                Toast.makeText(this, "No hay fotos disponibles para este reporte", Toast.LENGTH_SHORT).show();
            });
            return;
        }
        
        Log.d(TAG, "Cantidad de fotos a cargar: " + fotosUrls.size());
        Log.d(TAG, "Lista completa de URLs: " + fotosUrls.toString());
        
        // Intentamos cargar cada foto en un hilo separado
        for (String urlFoto : fotosUrls) {
            if (urlFoto.isEmpty()) continue;
            
            Log.d(TAG, "Intentando cargar foto desde URL: " + urlFoto);
            // Verificamos si la URL parece válida
            if (!urlFoto.startsWith("http")) {
                Log.e(TAG, "URL no válida (no tiene protocolo): " + urlFoto);
                continue;
            }
            
            new Thread(() -> {
                try {
                    // Configuramos la conexión con timeout
                    java.net.URL url = new java.net.URL(urlFoto);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    
                    // Leemos la imagen
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(connection.getInputStream());
                    
                    if (bitmap != null) {
                        // Si se pudo cargar la imagen, la agregamos al adaptador
                        runOnUiThread(() -> {
                            fotosAdapter.agregarFoto(bitmap);
                            Log.d(TAG, "Foto cargada correctamente desde: " + urlFoto);
                        });
                    } else {
                        Log.e(TAG, "No se pudo decodificar la imagen: " + urlFoto);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen desde " + urlFoto + ": " + e.getMessage(), e);
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
        // Limpiamos los campos de estado y mensaje
        tvEstado.setText("");
        tvMensajeAdmin.setText("");
        // Limpiamos los nuevos campos
        tvColonia.setText("");
        tvCorreo.setText("");
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