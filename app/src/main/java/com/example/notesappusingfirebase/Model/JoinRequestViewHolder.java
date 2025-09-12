package com.example.notesappusingfirebase.Model;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;

public class JoinRequestViewHolder extends RecyclerView.ViewHolder {
    public TextView roomNameTv, adminNameTv, timeTv;
    public Button acceptBtn, rejectBtn;

    public JoinRequestViewHolder(View itemView) {
        super(itemView);
        roomNameTv = itemView.findViewById(R.id.roomNameTv);
        adminNameTv = itemView.findViewById(R.id.adminNameTv);
        timeTv = itemView.findViewById(R.id.timeTv);
        acceptBtn = itemView.findViewById(R.id.acceptBtn);
        rejectBtn = itemView.findViewById(R.id.rejectBtn);
    }
}
