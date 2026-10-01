package com.example.notesappusingfirebase.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomViewHolder extends RecyclerView.ViewHolder {

    public TextView txtRoomName, txtCreatedBy, txtMembers,txtCheckRequestStatus;
    public Button btnOpenRoom;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference membersRef;
    int membersCount;

    public RoomViewHolder(@NonNull View itemView) {
        super(itemView);

        txtMembers = itemView.findViewById(R.id.txtMembers);
        txtCheckRequestStatus = itemView.findViewById(R.id.txtCheckRequestStatus);
    }

    public void setRoom(FragmentActivity fragmentActivity, String roomName, String adminId, String address, String time, String members, String roomCreatedBy, String search) {

        txtRoomName = itemView.findViewById(R.id.txtRoomName);
        txtCreatedBy = itemView.findViewById(R.id.txtCreatedBy);
        txtMembers = itemView.findViewById(R.id.txtMembers);
        btnOpenRoom = itemView.findViewById(R.id.btnOpenRoom);

        txtRoomName.setText(roomName);
        txtCreatedBy.setText("Created by: " + roomCreatedBy);
        txtMembers.setText("Total Members: " + membersCount);
    }

    public void showMembers(String address) {

        membersRef = firebaseDatabase.getReference("RoomMembers");

        membersRef.child(address).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    membersCount = (int) snapshot.getChildrenCount();
                    txtMembers.setText("Total Members: " + membersCount);
                } else {
                    membersCount = (int) snapshot.getChildrenCount();
                    txtMembers.setText("Total Members: " + membersCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
