package com.example.reporteciudad.api;

import android.graphics.Bitmap;
import android.util.Base64;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgurUploader {
    // NO uses el Client Secret en apps móviles, solo el Client ID
    private static final String IMGUR_CLIENT_ID = "5e9f6335eca8c4c"; // Client ID proporcionado
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private final OkHttpClient client;

    public ImgurUploader() {
        client = new OkHttpClient();
    }

    public String uploadImage(Bitmap bitmap) throws IOException {
        // Escalar la imagen si es demasiado grande
        Bitmap resizedBitmap = resizeImageIfNeeded(bitmap, 2048); // Máximo 2048px (mantiene buena calidad)
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Usar calidad alta (95) en lugar de 100 para reducir un poco el tamaño sin perder mucha calidad
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos);
        
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle(); // Liberar memoria si creamos un nuevo bitmap
        }
        
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        RequestBody requestBody = RequestBody.create(
            MediaType.parse("text/plain"),
            base64Image
        );

        Request request = new Request.Builder()
            .url(IMGUR_API_URL)
            .post(requestBody)
            .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error al subir la imagen: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            return data.getString("link");
        } catch (Exception e) {
            throw new IOException("Error al procesar la respuesta: " + e.getMessage());
        }
    }
    
    // Método para redimensionar la imagen si es demasiado grande, manteniendo la relación de aspecto
    private Bitmap resizeImageIfNeeded(Bitmap image, int maxDimension) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Si la imagen ya es lo suficientemente pequeña, no la redimensionamos
        if (width <= maxDimension && height <= maxDimension) {
            return image;
        }
        
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            // El ancho es mayor
            width = maxDimension;
            height = (int) (width / bitmapRatio);
        } else {
            // El alto es mayor o son iguales
            height = maxDimension;
            width = (int) (height * bitmapRatio);
        }
        
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
} 