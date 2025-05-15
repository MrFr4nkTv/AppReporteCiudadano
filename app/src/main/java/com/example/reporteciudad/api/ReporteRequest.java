package com.example.reporteciudad.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Random;

/**
 * Esta clase representa un reporte ciudadano completo.
 * Contiene toda la información que proporciona el ciudadano
 * y se encarga de preparar los datos para enviarlos al servidor.
 */
public class ReporteRequest {
    // Usamos letras y números para generar códigos fáciles de recordar
    private static final String CARACTERES_CODIGO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    // Los códigos tienen 6 caracteres, como "ABC123"
    private static final int LONGITUD_CODIGO = 6;

    // Todos los campos tienen @SerializedName para que coincidan con Google Sheets
    
    /** Cada reporte tiene un código único para poder buscarlo después */
    @Expose
    @SerializedName("codigo_reporte")
    private final String codigoReporte;
    
    /** Nombre de la persona que hace el reporte */
    @Expose
    @SerializedName("nombre_interesado")
    private final String nombreInteresado;
    
    /** Colonia donde se ubica el problema reportado */
    @Expose
    @SerializedName("colonia")
    private final String colonia;
    
    /** Dirección específica del problema reportado */
    @Expose
    @SerializedName("direccion")
    private final String direccion;
    
    /** Número de celular para contacto */
    @Expose
    @SerializedName("celular")
    private final String celular;
    
    /** Correo electrónico para contacto y seguimiento */
    @Expose
    @SerializedName("correo")
    private final String correo;
    
    /** Categoría o tipo de problema reportado */
    @Expose
    @SerializedName("tipo_reporte")
    private final String tipoReporte;
    
    /** Descripción detallada del problema */
    @Expose
    @SerializedName("descripcion")
    private final String descripcion;
    
    /** Lista de URLs de las fotos subidas como evidencia */
    @Expose
    @SerializedName("fotos")
    private final List<String> fotos;

    /**
     * Cuando se crea un nuevo reporte:
     * - Se genera automáticamente un código único
     * - Se guardan todos los datos proporcionados
     * - Se prepara para enviarse al servidor
     */
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

    /**
     * Generamos un código aleatorio que sea:
     * - Fácil de leer (solo letras mayúsculas y números)
     * - Fácil de compartir (6 caracteres)
     * - Único para cada reporte
     */
    private String generarCodigoReporte() {
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        Random random = new Random();
        
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(CARACTERES_CODIGO.charAt(random.nextInt(CARACTERES_CODIGO.length())));
        }
        
        return codigo.toString();
    }
    
    // Getters para acceder a los campos del reporte
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