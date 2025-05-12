package com.example.reporteciudad.api;

import com.google.gson.annotations.SerializedName;

public class ReporteResponse {
    @SerializedName("result")
    private String result;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("id")
    private String id;

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
    
    public String getId() {
        return id;
    }
} 