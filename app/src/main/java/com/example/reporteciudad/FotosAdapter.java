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

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {
    private List<Bitmap> fotos;
    private OnFotoClickListener listener;

    public interface OnFotoClickListener {
        void onEliminarClick(int position);
    }

    public FotosAdapter(List<Bitmap> fotos, OnFotoClickListener listener) {
        this.fotos = fotos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        holder.ivFoto.setImageBitmap(fotos.get(position));
        holder.btnEliminarFoto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fotos != null ? fotos.size() : 0;
    }

    public void agregarFoto(Bitmap foto) {
        if (fotos == null) {
            fotos = new ArrayList<>();
        }
        fotos.add(foto);
        notifyItemInserted(fotos.size() - 1);
    }

    public void eliminarFoto(int position) {
        if (fotos != null && position < fotos.size()) {
            fotos.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void eliminarTodasLasFotos() {
        if (fotos != null) {
            int size = fotos.size();
            fotos.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        ImageButton btnEliminarFoto;

        FotoViewHolder(View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            btnEliminarFoto = itemView.findViewById(R.id.btnEliminarFoto);
        }
    }
} 