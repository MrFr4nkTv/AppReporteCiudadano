package com.example.reporteciudad;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReporteManager {
    private static final String PREF_NAME = "ReportesPrefs";
    private static final String KEY_REPORTES = "reportes";
    private SharedPreferences prefs;
    private Gson gson;

    public ReporteManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void guardarReporte(Reporte reporte) {
        List<Reporte> reportes = obtenerReportes();
        reportes.add(reporte);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_REPORTES, gson.toJson(reportes));
        editor.apply();
    }

    public List<Reporte> obtenerReportes() {
        String json = prefs.getString(KEY_REPORTES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Reporte>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public Reporte buscarReporte(String id) {
        List<Reporte> reportes = obtenerReportes();
        for (Reporte reporte : reportes) {
            if (reporte.getId().equals(id)) {
                return reporte;
            }
        }
        return null;
    }
} 