package com.example.reporteciudad.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Random;

public class ReporteRequest {
    private static final String CARACTERES_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_CODIGO = 6;

    @Expose
    @SerializedName("codigo_reporte")
    private final String codigoReporte;
    
    @Expose
    @SerializedName("titulo")
    private final String titulo;
    
    @Expose
    @SerializedName("descripcion")
    private final String descripcion;
    
    @Expose
    @SerializedName("fotos")
    private final List<String> fotos;
    
    @Expose
    @SerializedName("nombreContacto")
    private final String nombreContacto;
    
    @Expose
    @SerializedName("telefonoContacto")
    private final String telefonoContacto;
    
    @Expose
    @SerializedName("direccionContacto")
    private final String direccionContacto;

    public ReporteRequest(String titulo, String descripcion, List<String> fotos,
                         String nombreContacto, String telefonoContacto, String direccionContacto) {
        this.codigoReporte = generarCodigoReporte();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotos = fotos;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.direccionContacto = direccionContacto;
    }

    private String generarCodigoReporte() {
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        Random random = new Random();
        
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
        }
        
        return codigo.toString();
    }
    
    // Getters
    public String getCodigoReporte() { return codigoReporte; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public List<String> getFotos() { return fotos; }
    public String getNombreContacto() { return nombreContacto; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public String getDireccionContacto() { return direccionContacto; }
} 