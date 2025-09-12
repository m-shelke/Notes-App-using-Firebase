package com.example.notesappusingfirebase.Model;

import android.media.Image;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;
import com.squareup.picasso.Picasso;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    TextView noteItem_title,noteItem_note;
    ImageView noteIv;
   public ImageButton itemNote_more;
    CardView imageCardView;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setNote(FragmentActivity fragmentActivity, String title, String notes, String search, String url, String delete, String type){

        noteItem_title = itemView.findViewById(R.id.noteItem_title);
        noteItem_note = itemView.findViewById(R.id.noteItem_note);
        noteIv = itemView.findViewById(R.id.noteItem_Iv);
        imageCardView = itemView.findViewById(R.id.imageCardView);
        itemNote_more = itemView.findViewById(R.id.itemNote_more);

        if (type.equals("text")){
            noteItem_title.setText(title);
            noteItem_note.setText(notes);
        }else {
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
