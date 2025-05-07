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
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public ReporteManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void guardarReporte(Reporte reporte) {
        List<Reporte> reportes = obtenerReportes();
        reportes.add(reporte);
        guardarReportes(reportes);
    }

    public Reporte obtenerReporte(String id) {
        List<Reporte> reportes = obtenerReportes();
        for (Reporte reporte : reportes) {
            if (reporte.getId().equals(id)) {
                return reporte;
            }
        }
        return null;
    }

    private List<Reporte> obtenerReportes() {
        String json = sharedPreferences.getString(KEY_REPORTES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Reporte>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void guardarReportes(List<Reporte> reportes) {
        String json = gson.toJson(reportes);
        sharedPreferences.edit().putString(KEY_REPORTES, json).apply();
    }
} 