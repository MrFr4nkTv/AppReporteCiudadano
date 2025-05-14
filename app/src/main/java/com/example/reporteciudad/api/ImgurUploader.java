package com.example.reporteciudad.api;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Clase encargada de subir imágenes a Imgur y obtener sus URLs
 * Utiliza OkHttp para las peticiones HTTP y maneja la compresión de imágenes
 * 
 * Características principales:
 * - Compresión automática de imágenes
 * - Redimensionamiento inteligente
 * - Manejo de errores robusto
 * - Limpieza de recursos
 */
public class ImgurUploader {
    // Tag para logging
    private static final String TAG = "ImgurUploader";
    
    // Credenciales y configuración de Imgur
    private static final String IMGUR_CLIENT_ID = "5e9f6335eca8c4c";
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    
    // Configuración de optimización de imágenes
    private static final int MAX_IMAGE_DIMENSION = 2048; // Tamaño máximo en píxeles
    private static final int JPEG_QUALITY = 95; // Calidad de compresión (0-100)
    
    // Tipo de contenido para la petición HTTP
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
    
    // Cliente HTTP para realizar las peticiones
    private final OkHttpClient client;

    /**
     * Constructor de la clase
     * Inicializa el cliente HTTP que se usará para todas las peticiones
     */
    public ImgurUploader() {
        client = new OkHttpClient();
    }

    /**
     * Sube una imagen a Imgur y devuelve su URL
     * 
     * Proceso:
     * 1. Valida la imagen de entrada
     * 2. Redimensiona si es necesario
     * 3. Comprime la imagen
     * 4. Convierte a Base64
     * 5. Sube a Imgur
     * 6. Obtiene y devuelve la URL
     * 
     * @param bitmap La imagen a subir
     * @return URL de la imagen en Imgur
     * @throws IOException Si hay algún error en el proceso
     * @throws IllegalArgumentException Si el bitmap es nulo
     */
    public String uploadImage(Bitmap bitmap) throws IOException {
        if (bitmap == null) {
            throw new IllegalArgumentException("El bitmap no puede ser nulo");
        }

        Bitmap resizedBitmap = null;
        ByteArrayOutputStream baos = null;
        
        try {
            // Paso 1: Optimización de la imagen
            resizedBitmap = resizeImageIfNeeded(bitmap, MAX_IMAGE_DIMENSION);
            baos = new ByteArrayOutputStream();
            
            // Paso 2: Compresión de la imagen
            if (!resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)) {
                throw new IOException("Error al comprimir la imagen");
            }
            
            // Paso 3: Conversión a Base64
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Paso 4: Subida a Imgur
            return subirImagenAImgur(base64Image);
        } finally {
            // Limpieza de recursos para evitar fugas de memoria
            if (resizedBitmap != null && resizedBitmap != bitmap) {
                resizedBitmap.recycle();
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error al cerrar ByteArrayOutputStream", e);
                }
            }
        }
    }

    /**
     * Realiza la petición HTTP para subir la imagen a Imgur
     * 
     * Proceso:
     * 1. Prepara la petición con la imagen en Base64
     * 2. Envía la petición a la API de Imgur
     * 3. Procesa la respuesta para obtener la URL
     * 
     * @param base64Image Imagen en formato Base64
     * @return URL de la imagen subida
     * @throws IOException Si hay algún error en la petición o procesamiento
     */
    private String subirImagenAImgur(String base64Image) throws IOException {
        // Preparación de la petición HTTP
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TEXT, base64Image);
        Request request = new Request.Builder()
            .url(IMGUR_API_URL)
            .post(requestBody)
            .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
            .build();

        try (Response response = client.newCall(request).execute()) {
            // Verificación de la respuesta
            if (!response.isSuccessful()) {
                throw new IOException("Error al subir la imagen: " + response.code());
            }

            // Procesamiento de la respuesta JSON
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            return data.getString("link");
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar la respuesta de Imgur", e);
            throw new IOException("Error al procesar la respuesta: " + e.getMessage());
        }
    }
    
    /**
     * Redimensiona una imagen manteniendo su proporción
     * 
     * Características:
     * - Mantiene la relación de aspecto
     * - Solo redimensiona si es necesario
     * - Optimiza para el tamaño máximo permitido
     * 
     * @param image Imagen a redimensionar
     * @param maxDimension Tamaño máximo permitido (ancho o alto)
     * @return Imagen redimensionada o la original si no necesita cambios
     */
    private Bitmap resizeImageIfNeeded(Bitmap image, int maxDimension) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Verificación rápida: si la imagen ya es más pequeña, no la redimensionamos
        if (width <= maxDimension && height <= maxDimension) {
            return image;
        }
        
        // Cálculo de nuevas dimensiones manteniendo la proporción
        float bitmapRatio = (float) width / (float) height;
        int newWidth, newHeight;
        
        if (bitmapRatio > 1) {
            // Imagen horizontal: ajustamos el ancho al máximo
            newWidth = maxDimension;
            newHeight = (int) (newWidth / bitmapRatio);
        } else {
            // Imagen vertical o cuadrada: ajustamos el alto al máximo
            newHeight = maxDimension;
            newWidth = (int) (newHeight * bitmapRatio);
        }
        
        // Creación de la nueva imagen redimensionada
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }
} 