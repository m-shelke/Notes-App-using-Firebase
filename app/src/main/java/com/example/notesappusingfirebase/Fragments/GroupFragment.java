package com.example.notesappusingfirebase.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notesappusingfirebase.Activities.AdminRequestStatusActivity;
import com.example.notesappusingfirebase.Activities.JoinRequestsActivity;
import com.example.notesappusingfirebase.Activities.OpenRoomActivity;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.Model.RoomModel;
import com.example.notesappusingfirebase.ViewHolder.RoomViewHolder;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.FragmentGroupBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class GroupFragment extends Fragment {

    FragmentGroupBinding binding;
    DatabaseReference roomRef, userAccountRef, membersRef;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String currentUid, name, email, profile;
    RoomModel roomModel;
    MembersModel membersModel;

    FirebaseRecyclerAdapter<RoomModel, RoomViewHolder> adapter;

    //Context for this fragment class
    private Context mContext;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroupBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomModel = new RoomModel();
        membersModel = new MembersModel();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        currentUid = user.getUid();

        roomRef = firebaseDatabase.getReference("Rooms").child(currentUid);
        userAccountRef = firebaseDatabase.getReference("NotesUserAccounts").child(currentUid);
        membersRef = firebaseDatabase.getReference("RoomMembers");

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(mContext));

        //changing start icon of TextInputLayout, when password recovery email sent successfully
        //   binding.searchTil.setEndIconDrawable(R.drawable.request);
        binding.searchTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        binding.searchTil.setEndIconVisible(true);

        binding.searchTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(mContext, JoinRequestsActivity.class));
            }
        });

        binding.createCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRooms();
            }
        });

        userAccountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    name = (String) snapshot.child("name").getValue();
                    profile = (String) snapshot.child("profile").getValue();
                    email = (String) snapshot.child("email").getValue();

                    Log.e("NAME_EMAIL ", name + email + profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadRooms();
        setupSearch();
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
                    loadRooms();
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

        Query query = roomRef.orderByChild("search")
                .startAt(searchQuery.toLowerCase())
                .endAt(searchQuery.toLowerCase() + "\uf8ff");

        FirebaseRecyclerOptions<RoomModel> options =
                new FirebaseRecyclerOptions.Builder<RoomModel>()
                        .setQuery(query, RoomModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<RoomModel, RoomViewHolder>(options) {
            @NonNull
            @Override
            public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent, false);
                return new RoomViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RoomViewHolder holder, int position, @NonNull RoomModel model) {


                String createdByText;

                if (model.getAdminId().equals(currentUid)) {
                    createdByText = "You";
                    holder.txtCheckRequestStatus.setVisibility(View.VISIBLE);
                } else {
                    createdByText = model.getRoomCreatedBy(); // actual admin name
                    holder.txtCheckRequestStatus.setVisibility(View.INVISIBLE);
                }

                holder.setRoom(getActivity(), model.getRoomName(), model.getAdminId(), model.getAddress(), model.getTime(), model.getMembers(), createdByText, model.getSearch());

                // 🔥 Count total members in this room
                DatabaseReference membersCountRef =
                        FirebaseDatabase.getInstance().getReference("RoomMembers")
                                .child(model.getAddress());

                membersCountRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();   // 🔥 member count
                        holder.txtMembers.setText("Members: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                holder.txtCheckRequestStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String roomName = model.getRoomName();
                        String adminId = model.getAdminId();
                        String address = model.getAddress();

                        Intent intent = new Intent(mContext, AdminRequestStatusActivity.class);
                        intent.putExtra("roomName", roomName);
                        intent.putExtra("adminId", adminId);
                        intent.putExtra("address", address);
                        startActivity(intent);

                    }
                });

                holder.btnOpenRoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String roomName = model.getRoomName();
                        String adminId = model.getAdminId();
                        String address = model.getAddress();

                        Intent intent = new Intent(mContext, OpenRoomActivity.class);
                        intent.putExtra("roomName", roomName);
                        intent.putExtra("adminId", adminId);
                        intent.putExtra("address", address);
                        intent.putExtra("name", name);
                        startActivity(intent);
                    }
                });
            }
        };
        binding.recyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    private void createRooms() {

        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_bs);

        EditText roomNameEd = dialog.findViewById(R.id.roomNameEd);
        Button createRoomBtn = dialog.findViewById(R.id.createRoomBtn);

//        Calendar date = Calendar.getInstance();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
//        final String saveDate = simpleDateFormat.format(date.getTime());
//
//        Calendar calendarTime = Calendar.getInstance();
//        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
//        final String saveTime = simpleTimeFormat.format(calendarTime.getTime());
//
//        time = saveDate + saveTime;

        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String roomName = roomNameEd.getText().toString().trim();

                if (roomName.isEmpty()) {
                    roomNameEd.setError("Provide Room Name");
                    roomNameEd.requestFocus();
                    return;
                }

                String address = roomName + currentUid + System.currentTimeMillis();

                roomModel.setAddress(address);
                roomModel.setAdminId(currentUid);
                roomModel.setRoomCreatedBy(name);
                roomModel.setSearch(roomName.toLowerCase());
                roomModel.setRoomName(roomName);
                roomModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                roomModel.setMembers("0");

                roomRef.child(address).setValue(roomModel);

                membersModel.setDate(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
                membersModel.setName(name);
                membersModel.setSearch(name.toLowerCase());
                membersModel.setStatus("Admin");
                membersModel.setEmail(email);
                membersModel.setProfile(profile);
                membersModel.setUserId(currentUid);

                membersRef.child(address).child(currentUid).setValue(membersModel)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(mContext, "Room Created!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(mContext, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void loadRooms() {

        FirebaseRecyclerOptions<RoomModel> options =
                new FirebaseRecyclerOptions.Builder<RoomModel>()
                        .setQuery(roomRef, RoomModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<RoomModel, RoomViewHolder>(options) {
            @NonNull
            @Override
            public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent, false);
                return new RoomViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RoomViewHolder holder, int position, @NonNull RoomModel model) {

                String createdByText;

                if (model.getAdminId().equals(currentUid)) {
                    createdByText = "You";
                    holder.txtCheckRequestStatus.setVisibility(View.VISIBLE);
                } else {
                    createdByText = model.getRoomCreatedBy(); // actual admin name
                    holder.txtCheckRequestStatus.setVisibility(View.INVISIBLE);
                }

                holder.setRoom(getActivity(), model.getRoomName(), model.getAdminId(), model.getAddress(), model.getTime(), model.getMembers(), createdByText, model.getSearch());

                // 🔥 Count total members in this room
                DatabaseReference membersCountRef =
                        FirebaseDatabase.getInstance().getReference("RoomMembers")
                                .child(model.getAddress());

                membersCountRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();   // 🔥 member count
                        holder.txtMembers.setText("Members: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                holder.txtCheckRequestStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String roomName = model.getRoomName();
                        String adminId = model.getAdminId();
                        String address = model.getAddress();

                        Intent intent = new Intent(mContext, AdminRequestStatusActivity.class);
                        intent.putExtra("roomName", roomName);
                        intent.putExtra("adminId", adminId);
                        intent.putExtra("address", address);
                        startActivity(intent);

                    }
                });

                holder.btnOpenRoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String roomName = model.getRoomName();
                        String adminId = model.getAdminId();
                        String address = model.getAddress();

                        Intent intent = new Intent(mContext, OpenRoomActivity.class);
                        intent.putExtra("roomName", roomName);
                        intent.putExtra("adminId", adminId);
                        intent.putExtra("address", address);
                        intent.putExtra("name", name);
                        startActivity(intent);
                    }
                });
            }
        };
        binding.recyclerview.setAdapter(adapter);
        adapter.startListening();
    }
}