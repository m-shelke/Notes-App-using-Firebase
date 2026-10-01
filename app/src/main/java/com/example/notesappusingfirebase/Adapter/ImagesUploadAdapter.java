package com.example.notesappusingfirebase.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Model.MultipleImageModel;
import com.example.notesappusingfirebase.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagesUploadAdapter extends RecyclerView.Adapter<ImagesUploadAdapter.Holder> {

    public interface Callback {
        void onRemove(int position);
    }

    private final List<MultipleImageModel> list;
    private final Context ctx;
    private final Callback callback;

    public ImagesUploadAdapter(Context ctx, List<MultipleImageModel> list, Callback callback) {
        this.ctx = ctx;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_image_upload, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        MultipleImageModel item = list.get(position);

        holder.tvFileName.setText(item.fileName);
        holder.tvFileSize.setText(android.text.format.Formatter.formatFileSize(ctx, item.fileSize));
        holder.progressBar.setProgress(item.progress);
        holder.tvProgressPercent.setText(item.progress + "%");

        // show uploaded state
        if (item.uploaded) {
            holder.tvProgressPercent.setText("Uploaded");
            holder.progressBar.setProgress(100);
        }

        // load thumbnail (use Glide or native, but we assume no external libs)
        try {
//            holder.ivPreview.setImageURI(item.uri);

            Picasso.get().load(item.uri).into(holder.ivPreview);

        } catch (Exception e) {
            holder.ivPreview.setImageResource(R.drawable.person);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (callback != null) callback.onRemove(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateProgress(int position, int percent) {
        if (position < 0 || position >= list.size()) return;
        list.get(position).progress = percent;
        notifyItemChanged(position);
    }

    public void markUploaded(int position, String downloadUrl) {
        if (position < 0 || position >= list.size()) return;
        MultipleImageModel it = list.get(position);
        it.uploaded = true;
        it.downloadUrl = downloadUrl;
        it.progress = 100;
        notifyItemChanged(position);
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        TextView tvFileName, tvFileSize, tvProgressPercent;
        ProgressBar progressBar;
        ImageButton btnRemove;

        Holder(@NonNull View v) {
            super(v);
            ivPreview = v.findViewById(R.id.ivPreview);
            tvFileName = v.findViewById(R.id.tvFileName);
            tvFileSize = v.findViewById(R.id.tvFileSize);
            progressBar = v.findViewById(R.id.progressBar);
            tvProgressPercent = v.findViewById(R.id.tvProgressPercent);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}

