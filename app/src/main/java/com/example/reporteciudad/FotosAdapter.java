package com.example.reporteciudad;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;

/**
 * Adaptador para mostrar las fotos en un RecyclerView
 * Permite agregar, eliminar y mostrar fotos en una lista horizontal
 */
public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {
    // Lista de fotos que se mostrarán en el RecyclerView
    private List<Bitmap> fotos;
    // Listener para manejar la eliminación de fotos
    private OnFotoClickListener listener;

    /**
     * Interfaz para manejar el evento de eliminación de fotos
     */
    public interface OnFotoClickListener {
        void onEliminarClick(int position);
    }

    /**
     * Constructor del adaptador
     * @param fotos Lista inicial de fotos
     * @param listener Listener para manejar la eliminación de fotos
     */
    public FotosAdapter(List<Bitmap> fotos, OnFotoClickListener listener) {
        this.fotos = fotos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout para cada item de la lista
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        // Mostramos la foto en el ImageView
        holder.ivFoto.setImageBitmap(fotos.get(position));
        // Configuramos el botón de eliminar solo si el listener no es null y el botón existe
        if (listener != null && holder.btnEliminarFoto != null) {
            holder.btnEliminarFoto.setVisibility(View.VISIBLE);
            holder.btnEliminarFoto.setOnClickListener(v -> {
                listener.onEliminarClick(position);
            });
        } else if (holder.btnEliminarFoto != null) {
            holder.btnEliminarFoto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        // Devolvemos el número de fotos en la lista
        return fotos != null ? fotos.size() : 0;
    }

    /**
     * Agrega una nueva foto a la lista
     * @param foto Bitmap de la foto a agregar
     */
    public void agregarFoto(Bitmap foto) {
        if (fotos == null) {
            fotos = new ArrayList<>();
        }
        fotos.add(foto);
        // Notificamos que se agregó un nuevo item
        notifyItemInserted(fotos.size() - 1);
    }

    /**
     * Elimina una foto de la lista
     * @param position Posición de la foto a eliminar
     */
    public void eliminarFoto(int position) {
        if (fotos != null && position < fotos.size()) {
            fotos.remove(position);
            // Notificamos que se eliminó un item
            notifyItemRemoved(position);
        }
    }

    /**
     * Elimina todas las fotos de la lista
     */
    public void eliminarTodasLasFotos() {
        if (fotos != null) {
            int size = fotos.size();
            fotos.clear();
            // Notificamos que se eliminaron varios items
            notifyItemRangeRemoved(0, size);
        }
    }

    /**
     * ViewHolder que contiene las vistas de cada item de la lista
     */
    static class FotoViewHolder extends RecyclerView.ViewHolder {
        // ImageView para mostrar la foto
        ImageView ivFoto;
        // Botón para eliminar la foto (puede ser null si no existe en el layout)
        ImageButton btnEliminarFoto;

        /**
         * Constructor del ViewHolder
         * @param itemView Vista del item
         */
        FotoViewHolder(View itemView) {
            super(itemView);
            // Inicializamos las vistas
            ivFoto = itemView.findViewById(R.id.ivFoto);
            // Buscamos el botón de eliminar solo si existe en el layout
            int resId = itemView.getContext().getResources().getIdentifier("btnEliminarFoto", "id", itemView.getContext().getPackageName());
            if (resId != 0) {
                btnEliminarFoto = itemView.findViewById(resId);
            } else {
                btnEliminarFoto = null;
            }
        }
    }
} 