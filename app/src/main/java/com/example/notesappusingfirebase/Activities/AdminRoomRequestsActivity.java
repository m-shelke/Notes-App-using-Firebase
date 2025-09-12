package com.example.notesappusingfirebase.Activities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Model.AdminRequestStatusViewHolder;
import com.example.notesappusingfirebase.Model.RequestStatusModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityAdminRoomRequestsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AdminRoomRequestsActivity extends AppCompatActivity {

    ActivityAdminRoomRequestsBinding binding;
    String roomAddress, roomName, adminId;
    DatabaseReference adminRef, requestsRef;
    FirebaseRecyclerAdapter<RequestStatusModel, AdminRequestStatusViewHolder> adminAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminRoomRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        roomAddress = getIntent().getStringExtra("address");
        roomName = getIntent().getStringExtra("roomName");
        adminId = getIntent().getStringExtra("adminId");

        requestsRef = FirebaseDatabase.getInstance().getReference("RoomJoinRequests");
        adminRef = FirebaseDatabase.getInstance().getReference("AdminRequests").child(adminId).child(roomAddress);

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            Paint paint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int pos = viewHolder.getAdapterPosition();
                String key = adminAdapter.getRef(pos).getKey();

                // Get deleted model
                RequestStatusModel deletedModel = adminAdapter.getItem(pos);

                // Soft delete: remove temporarily from admin panel list
                assert key != null;
                adminRef.child(key).removeValue();

                // Also temporarily remove from receiver request list
                requestsRef.child(key).child(roomAddress).removeValue();

                Snackbar.make(binding.recyclerview, "Undo Before Time-Out", Snackbar.LENGTH_LONG).setAction("UNDO", v -> {

                            // 🔥 Restore data
                            adminRef.child(key).setValue(deletedModel);
                            requestsRef.child(key).child(roomAddress).setValue(deletedModel);

                            Toast.makeText(AdminRoomRequestsActivity.this, "Request restored", Toast.LENGTH_SHORT).show();
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {

                                // If NOT undone → perform final delete
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE || event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                    // No need to delete again because soft delete already removed them
                                }
                            }
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                if (dX < 0) {

                    Drawable bg = ContextCompat.getDrawable(AdminRoomRequestsActivity.this, R.drawable.swipe_cancel_bg);
                    bg.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    bg.draw(c);

                    Drawable icon = ContextCompat.getDrawable(AdminRoomRequestsActivity.this, R.drawable.outline_delete_sweep_24);

                    int iconWidth = icon.getIntrinsicWidth();
                    int iconHeight = icon.getIntrinsicHeight();

                    int iconLeft = itemView.getRight() - 60 - iconWidth;
                    int iconTop = itemView.getTop() + (itemHeight - iconHeight) / 2;

                    icon.setBounds(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
                    icon.draw(c);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(40f);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

                    String text = "Delete..";
                    float textX = iconLeft - 20 - paint.measureText(text);
                    float textY = itemView.getTop() + (itemHeight / 2f) + 15;

                    c.drawText(text, textX, textY, paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerview);

        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);

                if (chip.isChecked()) {
                    chip.animate().scaleX(1.02f).scaleY(1.02f).setDuration(180).start();
                    chip.setTypeface(null, Typeface.BOLD);
                    chip.setTypeface(null, chip.isChecked() ? Typeface.BOLD : Typeface.NORMAL);
                } else {
                    chip.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
                    chip.setTypeface(null, Typeface.NORMAL);
                }
            }

            if (checkedIds.isEmpty()) {
                // Nothing selected → load ALL
                loadData(null);
                return;
            }

            int chipId = checkedIds.get(0);

            if (chipId == R.id.chipPending) {
                loadData("Pending");
            } else if (chipId == R.id.chipAccepted) {
                loadData("Accepted");
            } else if (chipId == R.id.chipRejected) {
                loadData("Rejected");
            } else if (chipId == R.id.chipSelfQuit) {
                loadData("Self Quit");
            } else if (chipId == R.id.chipRemoved) {
                loadData("Removed");
            }
        });

        loadData(null);
        updateChipCounts();
    }


    private void loadData(String filterStatus) {

        // Start loading
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.recyclerview.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);

        Query query;

        if (filterStatus == null) {
            // Load ALL requests
            query = adminRef;
        } else {
            // Load filtered results
            query = adminRef.orderByChild("status").equalTo(filterStatus);
        }

        FirebaseRecyclerOptions<RequestStatusModel> options =
                new FirebaseRecyclerOptions.Builder<RequestStatusModel>().setQuery(query, RequestStatusModel.class).build();

        if (adminAdapter != null) {
            adminAdapter.stopListening();  // stop old adapter
        }

        adminAdapter = new FirebaseRecyclerAdapter<RequestStatusModel, AdminRequestStatusViewHolder>(options) {
            @NonNull
            @Override
            public AdminRequestStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_status_item, parent, false);
                return new AdminRequestStatusViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                binding.recyclerview.setVisibility(View.GONE);

                if (getItemCount() == 0) {

                    binding.emptyView.setVisibility(View.VISIBLE);

                    // Dynamic empty text
                    if (filterStatus == null) {
                        binding.emptyView.setText("No Requests Found");
                    } else {
                        binding.emptyView.setText("No " + filterStatus + " Requests");
                    }

                    // Fade animation
                    binding.emptyView.setAlpha(0f);
                    binding.emptyView.animate().alpha(1f).setDuration(300).start();

                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.recyclerview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull AdminRequestStatusViewHolder holder, int position, @NonNull RequestStatusModel model) {

                holder.setAdminRequestStatus(AdminRoomRequestsActivity.this, model.getName(), model.getTimestamp(), model.getEmail(), model.getProfile(), model.getStatus());

                binding.shimmerLayout.stopShimmer();
                binding.shimmerLayout.hideShimmer();

                String status = model.getStatus();

                if (status.equals("Pending")) {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_chip_bg_pending);
                    holder.tvStatus.setTextColor(Color.parseColor("#8A6D00"));
                } else if (status.equals("Accepted")) {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_chip_bg_accepted);
                    holder.tvStatus.setTextColor(Color.parseColor("#0A6E0A"));
                } else if (status.equals("Rejected")) {
                    holder.tvStatus.setBackgroundResource(R.drawable.status_chip_bg_rejected);
                    holder.tvStatus.setTextColor(Color.parseColor("#9F0000"));
                }

                holder.itemView.setAlpha(0f);
                holder.itemView.animate().alpha(1f).setDuration(300).start();
            }
        };
        binding.recyclerview.setAdapter(adminAdapter);
        adminAdapter.startListening();
    }

    private void updateChipCounts() {
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int pendingCount = 0;
                int acceptedCount = 0;
                int rejectedCount = 0;
                int selfQuitCount = 0;
                int removedCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if (status == null) continue;

                    switch (status) {
                        case "Pending":
                            pendingCount++;
                            break;
                        case "Accepted":
                            acceptedCount++;
                            break;
                        case "Rejected":
                            rejectedCount++;
                            break;
                        case "Self Quit":
                            selfQuitCount++;
                            break;
                        case "Removed":
                            removedCount++;
                            break;
                    }
                }
                binding.chipPending.setText("Pending (" + pendingCount + ")");
                binding.chipAccepted.setText("Accepted (" + acceptedCount + ")");
                binding.chipRejected.setText("Rejected (" + rejectedCount + ")");
                binding.chipSelfQuit.setText("Leaved (" + selfQuitCount + ")");
                binding.chipRemoved.setText("Removed (" + removedCount + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adminAdapter != null) adminAdapter.stopListening();
    }
}