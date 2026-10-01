package com.example.notesappusingfirebase.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.example.notesappusingfirebase.ViewHolder.AddRoomMembersViewHolder;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityShowMembersBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ShowMembersActivity extends AppCompatActivity {

    ActivityShowMembersBinding binding;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference roomRef, membersRef,adminReqRef;
    String currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    String address, adminId;

    FirebaseRecyclerAdapter<MembersModel, AddRoomMembersViewHolder> adminAdapter;
    FirebaseRecyclerAdapter<MembersModel, AddRoomMembersViewHolder> usersAdapter;

    boolean isSelectionMode = false;
    ArrayList<String> selectedUsers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityShowMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            address = bundle.getString("address");
            adminId = bundle.getString("adminId");
            Log.e("VALUES", address + adminId);
        } else {
            Toast.makeText(this, "Room Address Missing", Toast.LENGTH_SHORT).show();
        }

        adminReqRef = firebaseDatabase.getReference("AdminRequests").child(adminId).child(address);
        roomRef = firebaseDatabase.getReference("Rooms");
        membersRef = firebaseDatabase.getReference("RoomMembers").child(address);

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));


        Log.e("ADMIN_REF", adminId+"\n"+address+"\n"+currentUid);


        if (adminId.equals(currentUid)){
            //changing start icon of TextInputLayout, when password recovery email sent successfully
            binding.searchTil.setEndIconDrawable(R.drawable.baseline_group_remove_24);
            binding.searchTil.setEndIconCheckable(true);
            //now visible TextInputLayout Icon
            binding.searchTil.setEndIconVisible(true);

            binding.searchTil.setEndIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(ShowMembersActivity.this, "Delete Room", Toast.LENGTH_SHORT).show();
                }
            });

        }


        loadRoomMembers();
        setupSearch();


    }

    private void loadRoomMembers() {

        // Load ADMIN first
        Query adminQuery = membersRef.orderByKey().equalTo(adminId);

        FirebaseRecyclerOptions<MembersModel> adminOptions =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(adminQuery, MembersModel.class)
                        .build();

        adminAdapter = new FirebaseRecyclerAdapter<MembersModel, AddRoomMembersViewHolder>(adminOptions) {
            @NonNull
            @Override
            public AddRoomMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_member_item, parent, false);
                return new AddRoomMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AddRoomMembersViewHolder holder, int position, @NonNull MembersModel model) {

                holder.adminTv.setVisibility(View.VISIBLE);// admin doesn't get invited
                holder.setShowMembersProfile(ShowMembersActivity.this, model.getName(), model.getDate(), model.getProfile());
                holder.showMemberJoinDate.setText("Created On: " + model.getDate());
            }
        };

        // Load OTHER USERS except admin
        Query allUsersExceptAdmin = membersRef.orderByKey();

        FirebaseRecyclerOptions<MembersModel> userOptions =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(allUsersExceptAdmin, MembersModel.class)
                        .build();


        usersAdapter = new FirebaseRecyclerAdapter<MembersModel, AddRoomMembersViewHolder>(userOptions) {
            @NonNull
            @Override
            public AddRoomMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_member_item, parent, false);
                return new AddRoomMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AddRoomMembersViewHolder holder, int position, @NonNull MembersModel model) {

                String userId = getRef(position).getKey();

                // hide duplicate admin row in the users list
                if (userId.equals(adminId)) {

                    holder.adminTv.setVisibility(View.VISIBLE);  // admin doesn't get invited
                    holder.itemView.setVisibility(View.GONE); // prevent duplicate admin row
                    holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
                    return;
                }

                holder.setShowMembersProfile(ShowMembersActivity.this, model.getName(), model.getDate(), model.getProfile());
                holder.showMemberJoinDate.setText("Joined On: " + model.getDate());



                if (adminId.equals(currentUid)) {
//              apply selection state
                    holder.setSelection(selectedUsers.contains(userId));

                    holder.itemView.setOnLongClickListener(v -> {
                        if (!isSelectionMode) {
                            isSelectionMode = true;
                            selectedUsers.add(userId);
                            holder.setSelection(true);
                            showDeleteToolbar();
                        }
                        return true;
                    });

                    holder.itemView.setOnClickListener(v -> {
                        if (isSelectionMode) {
                            if (selectedUsers.contains(userId)) {
                                selectedUsers.remove(userId);
                                holder.setSelection(false);

                                if (selectedUsers.isEmpty()) {
                                    exitSelectionMode();
                                }

                            } else {
                                selectedUsers.add(userId);
                                holder.setSelection(true);
                            }
                        }
                    });
                }
            }
        };

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();   // 🔥 member count

                if (count == 1) {
                    binding.totoalMembers.setText("People: " + count);
                } else {
                    binding.totoalMembers.setText("People: " + count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Attach BOTH adapters to RecyclerView using ConcatAdapter
        androidx.recyclerview.widget.ConcatAdapter concatAdapter = new androidx.recyclerview.widget.ConcatAdapter(adminAdapter, usersAdapter);

        binding.recyclerview.setAdapter(concatAdapter);
        adminAdapter.startListening();
        usersAdapter.startListening();
    }

    private void setupSearch() {

        binding.searchEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Do nothing, just keep displaying notes
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
                    loadRoomMembers();
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

        Query query = membersRef.orderByChild("search")
                .startAt(searchQuery.toLowerCase())
                .endAt(searchQuery.toLowerCase() + "\uf8ff");


        FirebaseRecyclerOptions<MembersModel> userOptions =
                new FirebaseRecyclerOptions.Builder<MembersModel>()
                        .setQuery(query, MembersModel.class)
                        .build();


        usersAdapter = new FirebaseRecyclerAdapter<MembersModel, AddRoomMembersViewHolder>(userOptions) {
            @NonNull
            @Override
            public AddRoomMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_member_item, parent, false);
                return new AddRoomMembersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AddRoomMembersViewHolder holder, int position, @NonNull MembersModel model) {

                String userId = getRef(position).getKey();

                if (adminId.equals(currentUid)) {
//              apply selection state
                    holder.setSelection(selectedUsers.contains(userId));

                    holder.itemView.setOnLongClickListener(v -> {
                        if (!isSelectionMode) {
                            isSelectionMode = true;
                            selectedUsers.add(userId);
                            holder.setSelection(true);
                            showDeleteToolbar();
                        }
                        return true;
                    });

                    holder.itemView.setOnClickListener(v -> {
                        if (isSelectionMode) {
                            if (selectedUsers.contains(userId)) {
                                selectedUsers.remove(userId);
                                holder.setSelection(false);

                                if (selectedUsers.isEmpty()) {
                                    exitSelectionMode();
                                }

                            } else {
                                selectedUsers.add(userId);
                                holder.setSelection(true);
                            }
                        }
                    });

                }


                holder.setShowMembersProfile(ShowMembersActivity.this, model.getName(), model.getDate(), model.getProfile());


                if (model.getStatus().equals("Admin")) {
                    holder.adminTv.setVisibility(View.VISIBLE);  // admin doesn't get invited
                    holder.showMemberJoinDate.setText("Created On: " + model.getDate());
                } else {
                    holder.showMemberJoinDate.setText("Joined On: " + model.getDate());
                }

            }
        };

        membersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();   // 🔥 member count

                if (count == 1) {
                    binding.totoalMembers.setText("People: " + count);
                } else {
                    binding.totoalMembers.setText("People: " + count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.recyclerview.setAdapter(usersAdapter);
        usersAdapter.startListening();
    }


    private void showDeleteToolbar() {

        binding.searchTil.setEndIconCheckable(true);
        binding.searchTil.setEndIconVisible(true);
        binding.searchTil.setEndIconDrawable(R.drawable.baseline_delete_24);


        binding.searchTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (String uid : selectedUsers) {
                    membersRef.child(uid).removeValue();
                    adminReqRef.child(uid).child("status").setValue("Removed");
                    adminReqRef.child(uid).child("timestamp").setValue("Remove On: " + new android.icu.text.SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                }

                Toast.makeText(ShowMembersActivity.this, "Members removed", Toast.LENGTH_SHORT).show();
                exitSelectionMode();

            }
        });
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectedUsers.clear();
        usersAdapter.notifyDataSetChanged();
        binding.searchTil.setEndIconDrawable(R.drawable.baseline_group_remove_24);
    }

}