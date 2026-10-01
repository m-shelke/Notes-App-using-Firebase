package com.example.notesappusingfirebase.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Helper.RoomHeader;
import com.example.notesappusingfirebase.Model.SavedINoteModel;
import com.example.notesappusingfirebase.Helper.SavedListItem;
import com.example.notesappusingfirebase.Helper.SavedRow;
import com.example.notesappusingfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final Context context;
    private final List<SavedListItem> items = new ArrayList<>();
    private final Callback callback;

    public SavedAdapter(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    // ✅ THIS FIXES submitGroupedList RED ERROR
    public void submitGroupedList(List<SavedListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof RoomHeader
                ? TYPE_HEADER
                : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_room_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_saved, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {


        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind((RoomHeader) items.get(pos));
        } else {
            ((ItemVH) holder).bind(((SavedRow) items.get(pos)).getItem());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void showOptions(SavedINoteModel m) {

        String[] options = {"Unsave", m.pinned ? "Unpin" : "Pin"};

        new AlertDialog.Builder(context)
                .setItems(options, (d, i) -> {

                    if (i == 0) removeSaved(m);
                    else togglePin(m);

                }).show();
    }

    // ---------------- HEADER VIEW HOLDER ----------------

    private void removeSaved(SavedINoteModel m) {

        Log.e("SAVE_ID", m.getSavedId());

        FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(FirebaseAuth.getInstance().getUid())
                .child(m.getSavedId())
                .removeValue();

        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
    }

    // ---------------- ITEM VIEW HOLDER ----------------

    private void togglePin(SavedINoteModel m) {

        FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(FirebaseAuth.getInstance().getUid())
                .child(m.getSavedId())
                .child("pinned")
                .setValue(!m.pinned);
    }


    public interface Callback {
        void onItemClicked(SavedINoteModel item);
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView roomNameTv;

        HeaderVH(View v) {
            super(v);
            roomNameTv = v.findViewById(R.id.roomNameTv);
        }

        void bind(RoomHeader header) {
            roomNameTv.setText(header.getRoomName());
        }
    }

    class ItemVH extends RecyclerView.ViewHolder {

        ImageView fileIcon, pinIcon;
        TextView savedNoteTv, savedSenderTv, saveMessageTv;

        ItemVH(View v) {
            super(v);
            fileIcon = v.findViewById(R.id.item_fileIcon);
            pinIcon = v.findViewById(R.id.pinIcon);
            savedNoteTv = v.findViewById(R.id.savedTitleTv);
            saveMessageTv = v.findViewById(R.id.saveMessageTv);
            savedSenderTv = v.findViewById(R.id.savedMetaTv);
        }

        void bind(SavedINoteModel m) {

            if (m.getType().equals("TEXT")) {
                savedNoteTv.setVisibility(View.GONE);
                saveMessageTv.setVisibility(View.VISIBLE);
                saveMessageTv.setText(m.getNote());
                fileIcon.setVisibility(View.GONE);

            } else if (m.getType().equals("NOTETEXT")) {
                savedNoteTv.setVisibility(View.VISIBLE);
                savedNoteTv.setText(m.getTitle());
                saveMessageTv.setText(m.getNote());
                fileIcon.setVisibility(View.GONE);
            } else {
                fileIcon.setVisibility(View.VISIBLE);
                savedNoteTv.setVisibility(View.VISIBLE);
                savedNoteTv.setText(m.getType());
                saveMessageTv.setText(m.getNote());
            }

            // META (Sender + Date)
            String date = DateFormat.format("dd MMM, hh:mm a", m.getTimestamp()).toString();
            savedSenderTv.setText(m.getSenderName() + " • " + date);
//

            // 🔥 File icon detection
            fileIcon.setImageResource(getFileIcon(m));

            // 📌 Pin state
            pinIcon.setVisibility(m.isPinned() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (callback != null) callback.onItemClicked(m);
            });

            itemView.setOnLongClickListener(v -> {
                //     if (callback != null) callback.onPinToggle(m);
                showOptions(m);
                return true;
            });
        }

        private int getFileIcon(SavedINoteModel m) {
            if ("IMAGE".equals(m.getType())) return R.drawable.image;
            if ("CANVAS".equals(m.getType())) return R.drawable.canvas;
            if ("PDF".equals(m.getType())) return R.drawable.pdf;
            if ("DOC".equals(m.getType())) return R.drawable.doc;
            if ("PPT".equals(m.getType())) return R.drawable.ppt;
            return R.drawable.outline_text_fields_24;
        }
    }
}


