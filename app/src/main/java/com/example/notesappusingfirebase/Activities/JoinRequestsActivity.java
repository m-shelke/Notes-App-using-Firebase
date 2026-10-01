package com.example.notesappusingfirebase.Activities;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notesappusingfirebase.ViewHolder.JoinRequestViewHolder;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.Model.RoomModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityJoinRequestsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Locale;

public class JoinRequestsActivity extends AppCompatActivity {

    ActivityJoinRequestsBinding binding;

    DatabaseReference requestsRef, membersRef, roomsRef, userAccountRef,adminReqRef;
    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String currentUserName,currentUserProfile,currentUserEmail;
    FirebaseRecyclerAdapter<Object, JoinRequestViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityJoinRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestsRef = FirebaseDatabase.getInstance().getReference("RoomJoinRequests").child(currentUid);
        membersRef = FirebaseDatabase.getInstance().getReference("RoomMembers");
        roomsRef = FirebaseDatabase.getInstance().getReference("Rooms");
        userAccountRef = FirebaseDatabase.getInstance().getReference("NotesUserAccounts").child(currentUid);
        adminReqRef = FirebaseDatabase.getInstance().getReference("AdminRequests");


        userAccountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    currentUserName = (String) snapshot.child("name").getValue();
                    currentUserProfile = (String) snapshot.child("profile").getValue();
                    currentUserEmail = (String) snapshot.child("email").getValue();

                    Log.e("NAME_EMAIL ",currentUserName + currentUserEmail + currentUserProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        loadRequests();

    }

    private void loadRequests() {

        Query q = requestsRef.orderByChild("timestamp");
        FirebaseRecyclerOptions<Object> options =
                new FirebaseRecyclerOptions.Builder<Object>()
                        .setQuery(q, Object.class)
                        .build();

        // We'll use a simple map approach because request objects are dynamic; view holder should read map values.
        adapter = new FirebaseRecyclerAdapter<Object, JoinRequestViewHolder>(options) {
            @NonNull
            @Override
            public JoinRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.join_request_item, parent, false);
                return new JoinRequestViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull JoinRequestViewHolder holder, int position, @NonNull Object model) {

                // read as DataSnapshot style (FirebaseRecyclerAdapter gives you model; simpler: fetch snapshot via getRef(pos).getKey())
                String requestKey = getRef(position).getKey(); // roomAddress
                assert requestKey != null;
                DatabaseReference singleRef = requestsRef.child(requestKey);

                singleRef.get().addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    String roomName = String.valueOf(snapshot.child("roomName").getValue());
                    String roomAddress = String.valueOf(snapshot.child("roomAddress").getValue());
                    String adminId = String.valueOf(snapshot.child("adminId").getValue());
                    String adminName = String.valueOf(snapshot.child("adminName").getValue());
                    String adminProfile = String.valueOf(snapshot.child("adminProfile").getValue());
                    long timestamp = snapshot.child("timestamp").getValue() != null ? (long) snapshot.child("timestamp").getValue() : System.currentTimeMillis();

                    holder.roomNameTv.setText(roomName);
                    holder.adminNameTv.setText("From: " + adminName);
                    holder.timeTv.setText("On: "+new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(timestamp)));

                    holder.acceptBtn.setOnClickListener(v -> {
                        acceptRequest(roomAddress, adminId, roomName,adminName);
                        getOnBackPressedDispatcher().onBackPressed();
                        // remove request inside acceptRequest after successful add
                    });

                    holder.rejectBtn.setOnClickListener(v -> {
                        // simply remove the request
                        requestsRef.child(roomAddress).removeValue().addOnSuccessListener(aVoid ->
                                adminReqRef.child(adminId).child(roomAddress).child(currentUid).child("status").setValue("Rejected"));
                        adminReqRef.child(adminId).child(roomAddress).child(currentUid).child("timestamp").setValue("Rejected On: " + new android.icu.text.SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));

                        Toast.makeText(JoinRequestsActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();

                        getOnBackPressedDispatcher().onBackPressed();
                    });

                }).addOnFailureListener(e -> Log.e("JoinRequests", "read failed", e));
            }
        };

        binding.recyclerview.setAdapter(adapter);
        adapter.startListening();
    }


    private void acceptRequest(String roomAddress, String adminId, String roomName, String adminName) {

        String userId = currentUid;

        // Build member model using minimal info (you may fetch more data if needed)
        MembersModel membersModel = new MembersModel();

        membersModel.setDate(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
        membersModel.setName(currentUserName); // or fetch real name from userAccountRef if you want
        membersModel.setSearch(currentUserName.toLowerCase());
        membersModel.setStatus("Member");
        membersModel.setEmail(currentUserEmail); // optional
        membersModel.setProfile(currentUserProfile);
        membersModel.setUserId(userId);

        // add to RoomMembers/{roomAddress}/{userId}
        membersRef.child(roomAddress).child(userId).setValue(membersModel).addOnSuccessListener(aVoid -> {

            // Also add to Rooms/{userId}/{roomAddress} so it appears in their Rooms list
            RoomModel roomModel = new RoomModel();
            roomModel.setAddress(roomAddress);
            roomModel.setAdminId(adminId);
            roomModel.setRoomCreatedBy(adminName); // optional — you could read adminName before
            roomModel.setRoomName(roomName);
            roomModel.setSearch(roomName.toLowerCase());
            roomModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a").format(new Date()));
            roomModel.setMembers("0");

            roomsRef.child(userId).child(roomAddress).setValue(roomModel).addOnSuccessListener(aVoid1 -> {

                // update status in user side
             //   requestsRef.child(roomAddress).child("status").setValue("accepted");
                adminReqRef.child(adminId).child(roomAddress).child(userId).child("status").setValue("Accepted");
                adminReqRef.child(adminId).child(roomAddress).child(userId).child("timestamp").setValue("Accepted On: " + new android.icu.text.SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));

                // remove request
                requestsRef.child(roomAddress).removeValue().addOnSuccessListener(aVoid2 ->
                        Toast.makeText(JoinRequestsActivity.this, "You joined: " + roomName, Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e ->
                    Toast.makeText(JoinRequestsActivity.this, "Failed to add room", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e ->
                Toast.makeText(JoinRequestsActivity.this, "Failed to add member", Toast.LENGTH_SHORT).show());


        Log.e("RECEIVER_ID", userId);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) adapter.stopListening();
    }
}