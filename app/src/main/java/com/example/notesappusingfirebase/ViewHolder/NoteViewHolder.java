package com.example.notesappusingfirebase.ViewHolder;

import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Locale;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    public TextView noteItem_title, noteItem_note, noteItem_time,reminderTimeTv,noteReminderExpired;
    ImageView noteIv;
    //   public ImageButton itemNote_more;
    CardView imageCardView;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setNote(FragmentActivity fragmentActivity, String title, String notes, String search, String url, String delete, String type) {

        noteItem_title = itemView.findViewById(R.id.noteItem_title);
        noteItem_note = itemView.findViewById(R.id.noteItem_note);
        noteIv = itemView.findViewById(R.id.noteItem_Iv);
        imageCardView = itemView.findViewById(R.id.imageCardView);
        noteItem_time = itemView.findViewById(R.id.item_NoteTimeTv);
        reminderTimeTv = itemView.findViewById(R.id.item_NoteReminderTimeTv);
        noteReminderExpired = itemView.findViewById(R.id.item_NoteReminderExpiredTv);
//        itemNote_more = itemView.findViewById(R.id.itemNote_more);


        if (type.equals("NOTETEXT")) {
            noteItem_title.setText(title);
            noteItem_note.setText(notes);
        } else {
            noteItem_title.setText(title);
            noteItem_note.setText(notes);
            imageCardView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(url)
                    .fit()
                    .centerInside()
                    .into(noteIv);

        }

    }
}
