package com.example.reporteciudad;

import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Reporte {
    private String id;
    private String titulo;
    private String descripcion;
    private String fotoBase64;
    private String fecha;
    private String nombreContacto;
    private String telefonoContacto;
    private String direccionContacto;

    public Reporte(String titulo, String descripcion, String fotoBase64, 
                  String nombreContacto, String telefonoContacto, String direccionContacto) {
        this.id = UUID.randomUUID().toString().substring(0, 8); // Genera un ID de 8 caracteres
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotoBase64 = fotoBase64;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccionContacto = direccionContacto;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.fecha = sdf.format(new Date());
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFotoBase64() { return fotoBase64; }
    public String getFecha() { return fecha; }
    public String getNombreContacto() { return nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public String getDireccionContacto() { return direccionContacto; }
} 