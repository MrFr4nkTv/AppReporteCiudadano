package com.example.reporteciudad.api;

import java.util.List;

public class ReporteRequest {
    private String titulo;
    private String descripcion;
    private List<String> fotos;
    private String nombreContacto;
    private String telefonoContacto;
    private String direccionContacto;

    public ReporteRequest(String titulo, String descripcion, List<String> fotos,
                         String nombreContacto, String telefonoContacto, String direccionContacto) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotos = fotos;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccionContacto = direccionContacto;
    }
} 