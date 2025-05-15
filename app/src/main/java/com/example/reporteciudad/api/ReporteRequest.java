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
    @SerializedName("nombre_interesado")
    private final String nombreInteresado;
    
    @Expose
    @SerializedName("colonia")
    private final String colonia;
    
    @Expose
    @SerializedName("direccion")
    private final String direccion;
    
    @Expose
    @SerializedName("celular")
    private final String celular;
    
    @Expose
    @SerializedName("correo")
    private final String correo;
    
    @Expose
    @SerializedName("tipo_reporte")
    private final String tipoReporte;
    
    @Expose
    @SerializedName("descripcion")
    private final String descripcion;
    
    @Expose
    @SerializedName("fotos")
    private final List<String> fotos;

    public ReporteRequest(String nombreInteresado, String colonia, String direccion,
                         String celular, String correo, String tipoReporte,
                         String descripcion, List<String> fotos) {
        this.codigoReporte = generarCodigoReporte();
        this.nombreInteresado = nombreInteresado;
        this.colonia = colonia;
        this.direccion = direccion;
        this.celular = celular;
        this.correo = correo;
        this.tipoReporte = tipoReporte;
        this.descripcion = descripcion;
        this.fotos = fotos;
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
    public String getNombreInteresado() { return nombreInteresado; }
    public String getColonia() { return colonia; }
    public String getDireccion() { return direccion; }
    public String getCelular() { return celular; }
    public String getCorreo() { return correo; }
    public String getTipoReporte() { return tipoReporte; }
    public String getDescripcion() { return descripcion; }
    public List<String> getFotos() { return fotos; }
} 