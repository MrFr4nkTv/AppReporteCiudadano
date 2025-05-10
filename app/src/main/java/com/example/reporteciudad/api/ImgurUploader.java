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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
} 