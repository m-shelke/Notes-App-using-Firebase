package com.example.notesappusingfirebase.Activities;

import android.animation.ValueAnimator;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Model.AddMembersViewHolder;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.Model.RoomModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityAddMembesBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddMembersActivity extends AppCompatActivity {

    ActivityAddMembesBinding binding;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference userAccountRef,adminReqRef,roomRef,membersRef,requestsRef;
    String roomName, adminId, address,currentUserName,currentUserProfile,currentUserEmail;
    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder> adapter;

    FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder> adminAdapter;
    FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder> usersAdapter;

    private final HashSet<String> hiddenUserIds = new HashSet<>();

    private final HashSet<String> pendingOrAcceptedUsers = new HashSet<>();
    private int totalRequestsSent = 0;
    private int remainingUsers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddMembesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userAccountRef = firebaseDatabase.getReference("NotesUserAccounts");
        requestsRef = firebaseDatabase.getReference("RoomJoinRequests");
        adminReqRef = firebaseDatabase.getReference("AdminRequests");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            roomName = bundle.getString("roomName");
            adminId = bundle.getString("adminId");
            address = bundle.getString("address");

            Log.e("VALUES", roomName + address + adminId);
        } else {
            Toast.makeText(this, "Value Fetch Error", Toast.LENGTH_SHORT).show();
        }

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        userAccountRef.child(currentUid).addValueEventListener(new ValueEventListener() {
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

        loadHiddenUsers();
        setupSearch();
    }

    public void loadAllMembers() {

        // Load ADMIN first
        Query adminQuery = userAccountRef.orderByKey().equalTo(adminId);

        FirebaseRecyclerOptions<MembersModel> adminOptions =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(adminQuery, MembersModel.class)
                        .build();

       adminAdapter = new FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder>(adminOptions) {
                    @NonNull
                    @Override
                    public AddMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_members_item, parent, false);
                        return new AddMembersViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull AddMembersViewHolder holder, int position, @NonNull MembersModel model) {

                        holder.setAddMembersProfile(AddMembersActivity.this, model.getName(), model.getEmail(), model.getProfile());

                        holder.adminTv.setVisibility(View.VISIBLE);  // admin doesn't get invited
                        holder.txtInvite.setVisibility(View.GONE);
                        holder.checkBox.setVisibility(View.GONE);
                    }
                };

        // Load OTHER USERS except admin
        Query allUsersExceptAdmin = userAccountRef.orderByKey();

        FirebaseRecyclerOptions<MembersModel> userOptions =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(allUsersExceptAdmin, MembersModel.class)
                        .build();

    usersAdapter = new FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder>(userOptions) {
                    @NonNull
                    @Override
                    public AddMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_members_item, parent, false);
                        return new AddMembersViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull AddMembersViewHolder holder, int position, @NonNull MembersModel model) {

                        String userId = getRef(position).getKey();

                        // hide duplicate admin row in the users list
                        if (userId.equals(adminId)) {

                            holder.adminTv.setVisibility(View.VISIBLE);  // admin doesn't get invited
                            holder.txtInvite.setVisibility(View.GONE);
                            holder.checkBox.setVisibility(View.GONE);
                            holder.itemView.setVisibility(View.GONE); // prevent duplicate admin row
                            holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                            return;
                        }

                        // Hide ADMIN
                        if (userId.equals(adminId)) {
                            hideRow(holder);
                            return;
                        }

                        // Hide users with Pending / Accepted requests
                        if (hiddenUserIds.contains(userId)) {
                            hideRow(holder);
                            return;
                        }

                        holder.setAddMembersProfile(AddMembersActivity.this, model.getName(), model.getEmail(), model.getProfile());

                        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                if (isChecked){
                                    holder.checkBox.setVisibility(View.GONE);
                                    holder.txtInvite.setVisibility(View.VISIBLE);

                                    sendRoomJoinRequest(userId, model.getName(), model.getEmail(), model.getProfile());

                                    Log.e("CLICK", model.getUserId() + model.getName() + model.getEmail() + model.getProfile());
                                }else {

                                }
                            }
                        });
                    }
                };

        // Attach BOTH adapters to RecyclerView using ConcatAdapter
        androidx.recyclerview.widget.ConcatAdapter concatAdapter = new androidx.recyclerview.widget.ConcatAdapter(adminAdapter, usersAdapter);

        binding.recyclerview.setAdapter(concatAdapter);
        adminAdapter.startListening();
        usersAdapter.startListening();
    }

    private void sendRoomJoinRequest(String receiverId, String receiverName, String receiverEmail, String receiverProfile) {

        if (receiverId == null) return;

        // request id - use room address
        String requestId = address;

        DatabaseReference singleReqRef = requestsRef.child(receiverId).child(requestId);

        // check if request already pending
        singleReqRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(AddMembersActivity.this, "Failed to check request", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot snap = task.getResult();
            if (snap.exists()) {
                // there is already a request (pending or other)
                String status = String.valueOf(snap.child("status").getValue());
                Toast.makeText(AddMembersActivity.this, "Request already " + status, Toast.LENGTH_SHORT).show();
                return;
            }

            // build request map
            Map<String, Object> req = new HashMap<>();
            req.put("roomName", roomName);
            req.put("roomAddress", address);
            req.put("adminId", currentUid);
            // we can set adminName and adminProfile by reading current user info or reuse variables
            req.put("adminName", currentUserName != null ? currentUserName : "Admin");
            req.put("adminProfile", currentUserProfile != null ? currentUserProfile : "");
            req.put("timestamp", System.currentTimeMillis());
            req.put("status", "Pending");

            singleReqRef.setValue(req).addOnCompleteListener(setTask -> {
                if (setTask.isSuccessful()) {
                    Toast.makeText(AddMembersActivity.this, "Request sent to user", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddMembersActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show();
                }
            });

            // --- STORE FOR ADMIN TRACKING ---
            HashMap<String, Object> adminMap = new HashMap<>();
            adminMap.put("userId", receiverId);
            adminMap.put("name", receiverName);
            adminMap.put("email", receiverEmail);
            adminMap.put("profile", receiverProfile);
            adminMap.put("timestamp","On Hold: " + new android.icu.text.SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
            adminMap.put("status", "Pending");

            adminReqRef.child(adminId)
                    .child(requestId)
                    .child(receiverId)
                    .setValue(adminMap).addOnCompleteListener(setTask -> {
                if (setTask.isSuccessful()) {
                    Toast.makeText(AddMembersActivity.this, "STORE FOR ADMIN TRACKING", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddMembersActivity.this, "Failed STORE FOR ADMIN TRACKING", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupSearch() {

        binding.searchEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Do nothing, just keep displaying notesModel
            }
        });

        binding.searchEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();

                if (searchQuery.isEmpty()) {
                    loadAllMembers();
                } else {
                    // ✅ Only run search if user typed something
                    search(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void search(String searchQuery) {

        Query query = userAccountRef.orderByChild("search")
                .startAt(searchQuery.toLowerCase())
                .endAt(searchQuery.toLowerCase() + "\uf8ff");

        FirebaseRecyclerOptions<MembersModel> options =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(query, MembersModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<MembersModel, AddMembersViewHolder>(options) {
            @NonNull
            @Override
            public AddMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_members_item, parent, false);
                return new AddMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AddMembersViewHolder holder, int position, @NonNull MembersModel model) {

                String userId = getRef(position).getKey();

                // hide duplicate admin row in the users list
                if (userId.equals(adminId)) {

                    holder.adminTv.setVisibility(View.VISIBLE);  // admin doesn't get invited
                    holder.txtInvite.setVisibility(View.GONE);
                    holder.checkBox.setVisibility(View.GONE);
                    holder.itemView.setVisibility(View.GONE); // prevent duplicate admin row
                    holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                    return;
                }

                // Hide ADMIN
                if (userId.equals(adminId)) {
                    hideRow(holder);
                    return;
                }

                // Hide users with Pending / Accepted requests
                if (hiddenUserIds.contains(userId)) {
                    hideRow(holder);
                    return;
                }

                holder.setAddMembersProfile(AddMembersActivity.this, model.getName(), model.getEmail(), model.getProfile());

                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked){
                            holder.checkBox.setVisibility(View.GONE);
                            holder.txtInvite.setVisibility(View.VISIBLE);

                            sendRoomJoinRequest(userId, model.getName(), model.getEmail(), model.getProfile());

                            Log.e("CLICK", model.getUserId() + model.getName() + model.getEmail() + model.getProfile());
                        }else {

                        }
                    }
                });
            }
        };
        binding.recyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (adminAdapter != null) adminAdapter.stopListening();
        if (usersAdapter != null) usersAdapter.stopListening();
    }

    private void loadHiddenUsers() {

        adminReqRef.child(adminId).child(address)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        hiddenUserIds.clear();
                        pendingOrAcceptedUsers.clear();
                        totalRequestsSent = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            totalRequestsSent++;  // count all requests made

                            String userId = snap.getKey();
                            String status = "" + snap.child("status").getValue();

                            // hide users with pending OR accepted
                            if (status.equals("Pending") || status.equals("Accepted")) {
                                hiddenUserIds.add(userId);
                                pendingOrAcceptedUsers.add(userId);
                            }
                        }

                        binding.txtTotalSent.post(() ->
                                animateCounter(binding.txtTotalSent,
                                        Integer.parseInt(binding.txtTotalSent.getText().toString().replace("Sent: ", "")),
                                        totalRequestsSent,
                                        "Sent"
                                )
                        );

                        calculateRemainingUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void calculateRemainingUsers() {

        userAccountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                remainingUsers = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String userId = snap.getKey();

                    if (userId.equals(adminId)) continue;   // skip admin
                    if (pendingOrAcceptedUsers.contains(userId)) continue; // skip blocked

                    remainingUsers++;
                }

                binding.txtRemaining.post(() ->
                        animateCounter(binding.txtRemaining,
                                Integer.parseInt(binding.txtRemaining.getText().toString().replace("Left: ", "")),
                                remainingUsers,
                                "Left"
                        )
                );


                // after counters updated → load full list
                loadAllMembers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void hideRow(AddMembersViewHolder holder) {
        holder.itemView.setVisibility(View.GONE);
        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
    }

    private void animateCounter(TextView textView, int start, int end, String label) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(600);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(label + ": " + value);
            textView.setScaleX(1.0f + (value % 2) * 0.01f);
            textView.setScaleY(1.0f + (value % 2) * 0.01f);
        });
        animator.start();
    }
}