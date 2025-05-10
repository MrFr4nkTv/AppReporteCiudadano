package com.example.reporteciudad.api;

import java.util.List;

public class ReporteRequest {
    private String id;
    private String titulo;
    private String descripcion;
    private List<String> fotos;
    private String nombreContacto;
    private String telefonoContacto;
    private String direccionContacto;

    public ReporteRequest(String titulo, String descripcion, List<String> fotos,
                         String nombreContacto, String telefonoContacto, String direccionContacto) {
        this.id = "REP-" + System.currentTimeMillis();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotos = fotos;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccionContacto = direccionContacto;
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public List<String> getFotos() { return fotos; }
    public String getNombreContacto() { return nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public String getDireccionContacto() { return direccionContacto; }
} 