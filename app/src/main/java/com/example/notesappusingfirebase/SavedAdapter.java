package com.example.notesappusingfirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.example.notesappusingfirebase.Model.RoomHeader;
import com.example.notesappusingfirebase.Model.SavedItemModel;
import com.example.notesappusingfirebase.Model.SavedListItem;
import com.example.notesappusingfirebase.Model.SavedRow;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

//public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.VH> {
//
//    private final Context ctx;
//
//    private final List<SavedItemModel> pinned = new ArrayList<>();
//    private final List<SavedItemModel> normal = new ArrayList<>();
//    private final List<SavedItemModel> filtered = new ArrayList<>();
//
//    public SavedAdapter(Context ctx) {
//        this.ctx = ctx;
//    }
//
//    public void setData(List<SavedItemModel> pinnedList, List<SavedItemModel> normalList) {
//
//        pinned.clear();
//        normal.clear();
//        filtered.clear();
//
//        pinned.addAll(pinnedList);
//        normal.addAll(normalList);
//
//        filtered.addAll(pinned);
//        filtered.addAll(normal);
//
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(ctx).inflate(R.layout.item_saved, parent, false);
//        return new VH(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull VH h, int position) {
//
//            SavedItemModel m = filtered.get(position);
//
//            // TITLE
//            if ("TEXT".equals(m.type)) {
//                h.titleTv.setText(m.note);
//                h.fileIcon.setVisibility(View.GONE);
//                h.fileIcon.setImageResource(R.drawable.outline_text_fields_24);
//            }
//            else if ("IMAGE".equals(m.type)) {
//                h.titleTv.setText("Image");
//                h.fileIcon.setImageResource(R.drawable.image);
//            }
//            else {
//                h.titleTv.setText(m.fileName);
//                h.fileIcon.setImageResource(getFileIcon(m.fileName));
//            }
//
//            // META (Sender + Date)
//            String date = DateFormat.format("dd MMM, hh:mm a", m.timestamp).toString();
//            h.metaTv.setText(m.senderName + " • " + date);
//
//            // PIN
//            h.pinIcon.setVisibility(m.pinned ? View.VISIBLE : View.GONE);
//
//            // CLICK → OPEN
//            h.itemView.setOnClickListener(v -> openSavedItem(m));
//
//            // LONG CLICK → OPTIONS
//            h.itemView.setOnLongClickListener(v -> {
//                showOptions(m);
//                return true;
//            });
//
//
//    }
//
//    private void openSavedItem(SavedItemModel m) {
//
//        if ("TEXT".equals(m.type)) {
//            Toast.makeText(ctx, m.note, Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(m.fileUrl));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        try {
//            ctx.startActivity(intent);
//        } catch (Exception e) {
//            Toast.makeText(ctx, "No app found to open this file", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private int getFileIcon(String name) {
//        if (name == null) return R.drawable.baseline_insert_drive_file_24;
//
//        name = name.toLowerCase();
//        if (name.endsWith(".pdf")) return R.drawable.pdf;
//        if (name.endsWith(".doc") || name.endsWith(".docx")) return R.drawable.doc;
//        if (name.endsWith(".ppt") || name.endsWith(".pptx")) return R.drawable.ppt;
//        if (name.endsWith(".jpg") || name.endsWith("jpeg") || name.endsWith(".png")) return R.drawable.image;
//
//        return R.drawable.baseline_insert_drive_file_24;
//    }
//
//    private void showOptions(SavedItemModel m) {
//
//        String[] options = {"Unsave", m.pinned ? "Unpin" : "Pin"};
//
//        new AlertDialog.Builder(ctx)
//                .setItems(options, (d, i) -> {
//
//                    if (i == 0) removeSaved(m);
//                    else togglePin(m);
//
//                }).show();
//    }
//
//    private void removeSaved(SavedItemModel m) {
//
//        FirebaseDatabase.getInstance()
//                .getReference("SavedItems")
//                .child(FirebaseAuth.getInstance().getUid())
//                .child(m.savedId)
//                .removeValue();
//
//        Toast.makeText(ctx, "Removed", Toast.LENGTH_SHORT).show();
//    }
//
//    private void togglePin(SavedItemModel m) {
//
//        FirebaseDatabase.getInstance()
//                .getReference("SavedItems")
//                .child(FirebaseAuth.getInstance().getUid())
//                .child(m.savedId)
//                .child("pinned")
//                .setValue(!m.pinned);
//    }
//
//    @Override
//    public int getItemCount() {
//        return filtered.size();
//    }
//
//    public void filter(String q) {
//        q = q.toLowerCase();
//        filtered.clear();
//
//        for (SavedItemModel m : pinned) {
//            if (match(m, q)) filtered.add(m);
//        }
//        for (SavedItemModel m : normal) {
//            if (match(m, q)) filtered.add(m);
//        }
//        notifyDataSetChanged();
//    }
//
//    private boolean match(SavedItemModel m, String q) {
//        return (m.note != null && m.note.toLowerCase().contains(q)) ||
//                (m.fileName != null && m.fileName.toLowerCase().contains(q));
//    }
//
//    static class VH extends RecyclerView.ViewHolder {
//
//        ImageView fileIcon, pinIcon;
//        TextView titleTv, metaTv;
//
//        VH(@NonNull View v) {
//            super(v);
//            fileIcon = v.findViewById(R.id.item_fileIcon);
//            pinIcon = v.findViewById(R.id.pinIcon);
//            titleTv = v.findViewById(R.id.savedTitleTv);
//            metaTv = v.findViewById(R.id.savedMetaTv);
//        }
//    }
//}




public class SavedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final Context context;
    private final List<SavedListItem> items = new ArrayList<>();

    public interface Callback {
        void onItemClicked(SavedItemModel item);
    }

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

    // ---------------- HEADER VIEW HOLDER ----------------

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

    // ---------------- ITEM VIEW HOLDER ----------------

    class ItemVH extends RecyclerView.ViewHolder {

        ImageView fileIcon, pinIcon;
        TextView savedNoteTv, savedSenderTv;

        ItemVH(View v) {
            super(v);
            fileIcon = v.findViewById(R.id.item_fileIcon);
            pinIcon = v.findViewById(R.id.pinIcon);
            savedNoteTv = v.findViewById(R.id.savedTitleTv);
            savedSenderTv = v.findViewById(R.id.savedMetaTv);
        }

        void bind(SavedItemModel m) {

            if (m.getType().equals("TEXT")){
                savedNoteTv.setText(m.getNote());
                fileIcon.setVisibility(View.GONE);
            }else {
                fileIcon.setVisibility(View.VISIBLE);
                savedNoteTv.setText(m.getFileName());
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

        private int getFileIcon(SavedItemModel m) {
            if ("IMAGE".equals(m.getType())) return R.drawable.image;
            if ("PDF".equals(m.getType())) return R.drawable.pdf;
            if ("DOC".equals(m.getType())) return R.drawable.doc;
            if ("PPT".equals(m.getType())) return R.drawable.ppt;
            return R.drawable.outline_text_fields_24;
        }
    }







        private void showOptions(SavedItemModel m) {

        String[] options = {"Unsave", m.pinned ? "Unpin" : "Pin"};

        new AlertDialog.Builder(context)
                .setItems(options, (d, i) -> {

                    if (i == 0) removeSaved(m);
                    else togglePin(m);

                }).show();
    }

        private void removeSaved(SavedItemModel m) {

            Log.e("SAVE_ID",m.getSavedId() );

        FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(FirebaseAuth.getInstance().getUid())
                .child(m.getSavedId())
                .removeValue();

        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
    }

        private void togglePin(SavedItemModel m) {

        FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(FirebaseAuth.getInstance().getUid())
                .child(m.getSavedId())
                .child("pinned")
                .setValue(!m.pinned);
    }
}


