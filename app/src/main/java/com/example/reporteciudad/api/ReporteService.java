package com.example.reporteciudad.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReporteService {
    @POST("exec")
    Call<Void> enviarReporte(@Body ReporteRequest reporte);
} 