package com.example.notesappusingfirebase.Fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Activities.ImagePreviewActivity;
import com.example.notesappusingfirebase.Helper.RoomHeader;
import com.example.notesappusingfirebase.Model.SavedINoteModel;
import com.example.notesappusingfirebase.Helper.SavedListItem;
import com.example.notesappusingfirebase.Helper.SavedRow;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.Adapter.SavedAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SavedFragment extends Fragment {

    private final List<SavedINoteModel> allItems = new ArrayList<>();
    EditText searchSavedEt;
    ChipGroup filterChipGroup;
    String filterType = null;
    private RecyclerView recyclerView;
    private View emptyView;
    private SavedAdapter adapter;
    private DatabaseReference savedRef;
    private String currentUid;

    public SavedFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_saved, container, false);

        recyclerView = v.findViewById(R.id.savedRecyclerView);
        emptyView = v.findViewById(R.id.emptyView);
        searchSavedEt = v.findViewById(R.id.searchSavedEt);
        filterChipGroup = v.findViewById(R.id.filterChipGroup);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        currentUid = FirebaseAuth.getInstance().getUid();

        savedRef = FirebaseDatabase.getInstance().getReference("SavedItems").child(currentUid);

        setupAdapter();
        attachSavedListener();
        setupSearch();
        setupChipFilter();

        return v;
    }

    // ---------------- ADAPTER ----------------

    private void setupAdapter() {

        adapter = new SavedAdapter(requireContext(), new SavedAdapter.Callback() {

            @Override
            public void onItemClicked(SavedINoteModel item) {
                openSavedItem(item);
            }

        });

        recyclerView.setAdapter(adapter);
    }

    // ---------------- FIREBASE ----------------

    private void attachSavedListener() {

        savedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allItems.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    SavedINoteModel m = s.getValue(SavedINoteModel.class);
                    if (m != null) {
                        allItems.add(m);
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // ---------------- UI LOGIC ----------------

    private void updateUI() {

        if (allItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        adapter.submitGroupedList(buildGroupedList(allItems));
    }

    // ---------------- GROUPING ----------------
    private List<SavedListItem> buildGroupedList(List<SavedINoteModel> items) {

        Map<String, List<SavedINoteModel>> roomMap = new LinkedHashMap<>();
        Map<String, String> roomNameMap = new HashMap<>();

        for (SavedINoteModel m : items) {
            if (!roomMap.containsKey(m.getRoomId())) {
                roomMap.put(m.getRoomId(), new ArrayList<>());
                roomNameMap.put(m.getRoomId(), m.getRoomName());
            }
            roomMap.get(m.getRoomId()).add(m);
        }

        List<SavedListItem> grouped = new ArrayList<>();

        for (String roomId : roomMap.keySet()) {
            grouped.add(new RoomHeader(roomId, roomNameMap.get(roomId)));

            // Sort pinned first
            List<SavedINoteModel> roomItems = roomMap.get(roomId);
            Collections.sort(roomItems, (a, b) -> Boolean.compare(b.isPinned(), a.isPinned()));

            for (SavedINoteModel m : roomItems) {
                grouped.add(new SavedRow(m));
            }
        }

        return grouped;
    }


    // ---------------- ACTIONS ----------------

    private void openSavedItem(SavedINoteModel item) {

        if ("IMAGE".equals(item.getType()) && "CANVAS".equals(item.getType())) {

            Intent i = new Intent(requireContext(), ImagePreviewActivity.class);
            i.putExtra("imageUrl", item.getImageUrl());
            startActivity(i);

        } else if ("TEXT".equals(item.getType())) {
            Toast.makeText(requireContext(), item.getNote(), Toast.LENGTH_LONG).show();
        } else if ("NOTETEXT".equals(item.getType())) {
            Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(item.getImageUrl()));
            startActivity(i);
        }
    }

    private void setupSearch() {
        searchSavedEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(filterType, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void applyFilters(String typeFilter, String query) {

        List<SavedINoteModel> filtered = new ArrayList<>();

        for (SavedINoteModel m : allItems) {
            boolean matchesType = (typeFilter == null || m.getType().equals(typeFilter));
            boolean matchesQuery = query.isEmpty() || (m.getType().equals("TEXT") && m.getNote().toLowerCase().contains(query.toLowerCase())) || (!m.getType().equals("TEXT") && m.getNote().toLowerCase().contains(query.toLowerCase()));

            if (matchesType && matchesQuery) {
                filtered.add(m);
            }
        }

        adapter.submitGroupedList(buildGroupedList(filtered));
    }


    private void setupChipFilter() {

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

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
                filterType = null; // show all
                applyFilters(filterType, searchSavedEt.getText().toString());
                return;
            }

            int chipId = checkedIds.get(0);

            if (chipId == R.id.chipPdf) {
                filterType = "PDF";
            } else if (chipId == R.id.chipDoc) {
                filterType = "DOC";
            } else if (chipId == R.id.chipPpt) {
                filterType = "PPT";
            } else if (chipId == R.id.chipText) {
                filterType = "TEXT";
            } else if (chipId == R.id.chipImage) {
                filterType = "IMAGE";
            } else if (chipId == R.id.chipCanvas) {
                filterType = "CANVAS";
            } else if (chipId == R.id.chipNotes) {
                filterType = "NOTETEXT";
            }

            applyFilters(filterType, searchSavedEt.getText().toString());
        });
    }


}
