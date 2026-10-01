package com.example.notesappusingfirebase.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Model.RoomModel;
import com.example.notesappusingfirebase.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForwardRoomAdapter extends RecyclerView.Adapter<ForwardRoomAdapter.RoomVH> {

    private final Set<String> selectedRoomIds = new HashSet<>();
    public String currentRoomId;
    private List<RoomModel> rooms;
    private OnRoomClick listener;
    private OnSelectionChanged selectionListener;
    public ForwardRoomAdapter(List<RoomModel> rooms, String currentRoomId, OnRoomClick listener, OnSelectionChanged selectionListener) {
        this.rooms = rooms;
        this.currentRoomId = currentRoomId;
        this.listener = listener;
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent, false);
        return new RoomVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomVH holder, int position) {
        RoomModel r = rooms.get(position);
        holder.txtRoomName.setText(r.getRoomName());
        holder.txtMembers.setText("Members: " + r.getMembers());
        holder.txtCreatedBy.setText("Created By: " + r.getRoomCreatedBy());
        holder.itemView.setOnClickListener(v -> listener.onClick(r));

        if (r.getRoomId().equals(currentRoomId)) {
            holder.itemView.setAlpha(0.4f);
            holder.itemView.setEnabled(false);
            holder.itemView.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setEnabled(true);
            holder.itemView.setOnClickListener(v -> listener.onClick(r));
        }

          holder.itemView.setBackgroundColor(0xFFE3F2FD);

        boolean selected = selectedRoomIds.contains(r.getRoomId());
        //  holder.itemView.setBackgroundColor(selected ? 0xFFE3F2FD : 0x00000000);
        if (selected) {
            holder.itemView.setBackgroundResource(R.drawable.preview_bgg);
        } else if (!selected) {
//            holder.itemView.setBackgroundResource(R.drawable.bg_warning_box);
            holder.itemView.setBackgroundColor(0xFFE3F2FD);
        }

        holder.itemView.setOnClickListener(v -> {
            toggleSelection(r.getRoomId());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    private void toggleSelection(String roomId) {
        if (selectedRoomIds.contains(roomId)) {
            selectedRoomIds.remove(roomId);
            selectionListener.onSelectionChanged(selectedRoomIds.size());
        } else {
            selectedRoomIds.add(roomId);
            selectionListener.onSelectionChanged(selectedRoomIds.size());
        }



    }

    public List<String> getSelectedRoomIds() {
        return new ArrayList<>(selectedRoomIds);
    }

    public interface OnRoomClick {
        void onClick(RoomModel room);
    }

    static class RoomVH extends RecyclerView.ViewHolder {

        TextView txtRoomName, txtCreatedBy, txtMembers;
        LinearLayout linearLayout;

        RoomVH(View v) {
            super(v);
            txtRoomName = v.findViewById(R.id.txtRoomName);
            txtCreatedBy = v.findViewById(R.id.txtCreatedBy);
            txtMembers = v.findViewById(R.id.txtMembers);
            linearLayout = v.findViewById(R.id.linear);
            linearLayout.setVisibility(View.GONE);

        }
    }

    public void updateList(ArrayList<RoomModel> newList) {
        this.rooms = newList;
        notifyDataSetChanged();
    }

    public interface OnSelectionChanged {
        void onSelectionChanged(int count);
    }

}

