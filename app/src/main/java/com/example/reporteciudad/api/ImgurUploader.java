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

/*
 * Proceso completo:
 * 1. Optimización de imágenes
 * 2. Compresión inteligente
 * 3. Subida a la API de Imgur
 * 4. Obtención de URLs públicas
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
     * Esta clase se encarga de subir las fotos a Imgur.
     * Las procesa para que no sean muy pesadas y obtiene
     * un link permanente que podemos guardar en la base de datos.
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
     * Hace la petición a Imgur para subir la imagen:
     * - Usa nuestra clave de API para identificarnos
     * - Envía la imagen en formato Base64
     * - Espera la respuesta con el link
     * - Maneja cualquier error que pueda ocurrir
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
     * Ajusta el tamaño de la imagen si es necesario:
     * - Mantiene la proporción para que no se deforme
     * - La hace más pequeña si es muy grande
     * - La deja igual si ya tiene buen tamaño
     * - Ahorra datos móviles del ciudadano
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