package com.example.reporteciudad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.cardview.widget.CardView;
import com.example.reporteciudad.databinding.ActivityContactoBinding;
import com.example.reporteciudad.databinding.DialogAgradecimientoBinding;

/**
 * Actividad que muestra la información de contacto y permite:
 * - Abrir el mapa con la ubicación
 * - Enviar correo electrónico
 * - Realizar llamada telefónica
 */
public class ContactoActivity extends AppCompatActivity {

    // Constantes para la información de contacto
    private static final String DIRECCION = "Avenida Serdán, Centro, Guaymas, Sonora";
    private static final String CORREO = "francisco.ortega240522@potros.itson.edu.mx";
    private static final String TELEFONO = "6221253841";
    
    // Constante para las coordenadas de la ubicación
    private static final double LATITUD = 27.9205;
    private static final double LONGITUD = -110.8975;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        // Configuramos la barra de acción
        setupActionBar();
        
        // Configuramos los listeners de los CardViews
        setupCardListeners();
    }

    /**
     * Configura la barra de acción con el botón de retroceso
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contacto");
        }
    }

    /**
     * Configura los listeners para cada tarjeta de contacto
     */
    private void setupCardListeners() {
        // CardView para la dirección
        CardView cardDireccion = findViewById(R.id.cardDireccion);
        cardDireccion.setOnClickListener(v -> abrirMapa());

        // CardView para el correo electrónico
        CardView cardCorreo = findViewById(R.id.cardCorreo);
        cardCorreo.setOnClickListener(v -> enviarCorreo());

        // CardView para el teléfono
        CardView cardTelefono = findViewById(R.id.cardTelefono);
        cardTelefono.setOnClickListener(v -> realizarLlamada());
    }

    /**
     * Abre el mapa mostrando la ubicación definida
     */
    private void abrirMapa() {
        // Creamos la URI con las coordenadas para abrir el mapa
        Uri gmmIntentUri = Uri.parse("geo:" + LATITUD + "," + LONGITUD + "?q=" + Uri.encode(DIRECCION));
        
        // Creamos el intent para abrir el mapa
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Especificamos Google Maps
        
        // Verificamos si hay alguna aplicación que pueda manejar el intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Si no hay Google Maps, intentamos abrir con cualquier app de mapas
            Uri mapsUri = Uri.parse("geo:" + LATITUD + "," + LONGITUD + "?q=" + Uri.encode(DIRECCION));
            Intent alternativeMapIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
            startActivity(alternativeMapIntent);
        }
    }

    /**
     * Abre la aplicación de correo para enviar un email
     */
    private void enviarCorreo() {
        try {
            // Creamos el intent para enviar un correo
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + CORREO));
            
            // Añadimos asunto y cuerpo del correo (opcionales)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contacto desde la App Reporte Ciudad");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola, me comunico desde la aplicación Reporte Ciudad...");
            
            // Lanzamos el intent directamente sin verificar resolveActivity
            startActivity(emailIntent);
        } catch (Exception e) {
            // Si ocurre algún error, informamos al usuario
            Toast.makeText(this, "No se pudo abrir la aplicación de correo", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Abre la aplicación de teléfono para realizar una llamada
     */
    private void realizarLlamada() {
        try {
            // Formateamos el número para asegurarnos que no tiene caracteres no válidos
            String numeroLimpio = TELEFONO.replaceAll("[^0-9]", "");
            
            // Creamos el intent para el marcador telefónico
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + numeroLimpio));
            
            // Lanzamos el intent directamente sin verificar resolveActivity
            startActivity(callIntent);
        } catch (Exception e) {
            // Si ocurre algún error, informamos al usuario
            Toast.makeText(this, "No se pudo abrir la aplicación de teléfono", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Manejamos el botón de retroceso en la barra de acción
        onBackPressed();
        return true;
    }
} 