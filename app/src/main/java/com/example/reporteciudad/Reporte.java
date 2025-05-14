package com.example.reporteciudad;

import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Reporte {
    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss";
    private static final int LONGITUD_ID = 8;

    private final String id;
    private final String titulo;
    private final String descripcion;
    private final List<String> fotosBase64;
    private final String fecha;
    private final String nombreContacto;
    private final String telefonoContacto;
    private final String direccionContacto;

    public Reporte(String titulo, String descripcion, List<String> fotosBase64, 
                  String nombreContacto, String telefonoContacto, String direccionContacto) {
        this.id = UUID.randomUUID().toString().substring(0, LONGITUD_ID);
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotosBase64 = fotosBase64;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccionContacto = direccionContacto;
        this.fecha = new SimpleDateFormat(FORMATO_FECHA, Locale.getDefault()).format(new Date());
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public List<String> getFotosBase64() { return fotosBase64; }
    public String getFecha() { return fecha; }
    public String getNombreContacto() { return nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public String getDireccionContacto() { return direccionContacto; }
} 