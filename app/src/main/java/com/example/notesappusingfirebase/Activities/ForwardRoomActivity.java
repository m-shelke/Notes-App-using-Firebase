package com.example.notesappusingfirebase.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notesappusingfirebase.Model.RoomChatMemberModel;
import com.example.notesappusingfirebase.Model.RoomModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityForwardRoomBinding;
import com.example.notesappusingfirebase.Adapter.ForwardRoomAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardRoomActivity extends AppCompatActivity {

    ActivityForwardRoomBinding binding;

    private ArrayList<RoomChatMemberModel> messagesToForward;

    // Track forwarded messages for UNDO
    private final HashMap<String, List<String>> forwardedIdsMap = new HashMap<>();



    private ForwardRoomAdapter adapter;
    private ArrayList<RoomModel> rooms = new ArrayList<>();

    private String currentUid, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForwardRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        currentUid = FirebaseAuth.getInstance().getUid();

        binding.roomRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForwardRoomAdapter(rooms, address, room -> forwardToRoom(room.roomId), count -> {
                    // Enable if at least one selected
                    binding.floatingForward.setEnabled(count > 0);
                }
        );


        binding.roomRecycler.setAdapter(adapter);

        binding.floatingForward.setEnabled(false); // disabled by default

        messagesToForward = getIntent().getParcelableArrayListExtra("messages");

        if (messagesToForward == null || messagesToForward.isEmpty()) {
            Toast.makeText(this, "No messages to forward", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.floatingForward.setOnClickListener(v -> {

            List<String> rooms = adapter.getSelectedRoomIds();

            if (rooms.isEmpty()) {
                Toast.makeText(this, "Select at least one room", Toast.LENGTH_SHORT).show();
                return;
            }

            forwardToMultipleRooms(rooms);
        });


        binding.searchEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRooms(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        loadMyRooms();
    }

    private void forwardToRoom(String targetRoomId) {

        DatabaseReference targetRef = FirebaseDatabase.getInstance().getReference("NoteList").child(targetRoomId);

        for (RoomChatMemberModel m : messagesToForward) {

            String newId = targetRef.push().getKey();

            RoomChatMemberModel copy = createForwardCopy(m, newId);

            targetRef.child(newId).setValue(copy);
        }
        finish();
    }

    private void forwardToMultipleRooms(List<String> roomIds) {

        DatabaseReference root = FirebaseDatabase.getInstance().getReference("NoteList");

        forwardedIdsMap.clear();

        for (String roomId : roomIds) {

            List<String> ids = new ArrayList<>();

            for (RoomChatMemberModel m : messagesToForward) {

                String newId = root.child(roomId).push().getKey();
                if (newId == null) continue;

                RoomChatMemberModel copy = createForwardCopy(m, newId);
                root.child(roomId).child(newId).setValue(copy);

                ids.add(newId);
            }

            forwardedIdsMap.put(roomId, ids);
        }

        showUndoSnackbar();
    }

    private void showUndoSnackbar() {

        com.google.android.material.snackbar.Snackbar.make(
                        binding.getRoot(),
                        "Messages forwarded",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("UNDO", v -> undoForward())
                .addCallback(new com.google.android.material.snackbar.Snackbar.Callback() {
                    @Override
                    public void onDismissed(com.google.android.material.snackbar.Snackbar transientBottomBar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }).show();
    }


    private void undoForward() {

        DatabaseReference root = FirebaseDatabase.getInstance()
                .getReference("NoteList");

        for (String roomId : forwardedIdsMap.keySet()) {
            for (String msgId : forwardedIdsMap.get(roomId)) {
                root.child(roomId).child(msgId).removeValue();
            }
        }

        Toast.makeText(this, "Forward undone", Toast.LENGTH_SHORT).show();
        finish();
    }


    private RoomChatMemberModel createForwardCopy(RoomChatMemberModel m, String newId) {

        RoomChatMemberModel copy = new RoomChatMemberModel();

        copy.setMessageId(newId);
        copy.setSenderId(FirebaseAuth.getInstance().getUid());
        copy.setSenderName(m.getSenderName());


        copy.setType(m.getType());
        copy.setNote(m.getNote());
        copy.setTitle(m.getTitle());

        copy.setImageUrl(m.getImageUrl());
        copy.setFileUrl(m.getFileUrl());
        copy.setFileName(m.getFileName());
        copy.setFileSize(m.getFileSize());

        copy.setTimestamp(System.currentTimeMillis());
        copy.setSeenBy(new HashMap<>());

        // ⭐ FORWARDED FLAGS
        copy.setForwarded(true);
        copy.setForwardedFrom(m.getSenderName()); // optional

        return copy;
    }

    private void loadMyRooms() {

        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("Rooms").child(currentUid);

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("RoomMembers");

        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rooms.clear();

                for (DataSnapshot roomSnap : snapshot.getChildren()) {

                    RoomModel room = roomSnap.getValue(RoomModel.class);
                    if (room == null) continue;

                    String roomId = roomSnap.getKey();
                    room.setRoomId(roomId);

                    // 🔥 Load member count
                    membersRef.child(roomId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot memberSnap) {
                                    room.setMembers(String.valueOf((int) memberSnap.getChildrenCount()));
                                    if (!room.getRoomId().equals(address)) {
                                        rooms.add(room);
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void filterRooms(String query) {
        ArrayList<RoomModel> filteredList = new ArrayList<>();

        for (RoomModel room : rooms) {
            if (room.getRoomName() != null && room.getRoomName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(room);
            }
        }

        adapter.updateList(filteredList);
    }

}