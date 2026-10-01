package com.example.notesappusingfirebase.Fragments;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.notesappusingfirebase.Activities.CanvasNoteActivity;
import com.example.notesappusingfirebase.Activities.ForwardRoomActivity;
import com.example.notesappusingfirebase.Model.NotesModel;
import com.example.notesappusingfirebase.Model.RoomChatMemberModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.Reminder.ReminderReceiver;
import com.example.notesappusingfirebase.ViewHolder.NoteViewHolder;
import com.example.notesappusingfirebase.databinding.FragmentHomeBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final String TAG = "IMAGE_CHANGE_TAG";
    DatabaseReference databaseReference, userAccountRef;
    String currentUid, name, email, imageUrl, userId;
    FirebaseRecyclerAdapter<NotesModel, NoteViewHolder> adapter;
    ImageView noteImageView;
    TextView changeNoteImageTv;
    //View Binding
    private FragmentHomeBinding binding;
    //Context for this fragment class
    private Context mContext;
    private String currentFilterType = null;
    private ProgressDialog progressDialog;
    private Uri imageUri;

    private BottomSheetDialog bottomSheetDialog;
    private TextView progressTitle, progressMessage;
    private LinearProgressIndicator progressBar;

    private boolean isCancelled = false;

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check, if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get Uri of image picked
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Image Picked from Gallery " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(mContext)
                                    .load(imageUri)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .into(noteImageView);

                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }

                    } else {
                        //Canceled
                        Toast.makeText(mContext, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher<String> requestsStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);

                    //Let's check if permission is granted or not
                    if (isGranted) {
                        //  Storage Permission granted, we can now launch Gallery to pick Image
                        pickImageGallery();
                    } else {
                        //Storage Permission denied, we can't launch  Gallery to picked Image
                        Toast.makeText(getContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    //check if image capture or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as assigned in PickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Capture " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(mContext)
                                    .load(imageUri)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .into(noteImageView);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //canceled
                        Toast.makeText(mContext, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    Log.d(TAG, "onActivityResult: " + result.toString());

                    //Let's check if permission granted or not
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        //Camera or Storage or both permission granted, we can now launch camera to capture image
                        Log.d(TAG, "onActivityResult: All Granted e.g Cameta, Storage");
                        pickImageCamera();
                    } else {
                        //Camera or Storage or both permission denied, can not camera to capture image
                        Log.d(TAG, "onActivityResult: All or Either one is denied");
                        Toast.makeText(mContext, "Both Camera and Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        //get and init the context for this fragment class
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mContext), container, false);
        //attaching root of xml file
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
        binding.recyclerview.setHasFixedSize(true);
        //set layout to RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(mContext));


        //changing start icon of TextInputLayout, when password recovery email sent successfully
        binding.searchTil.setEndIconDrawable(R.drawable.baseline_sort_24);
        binding.searchTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        binding.searchTil.setEndIconVisible(true);

        binding.searchTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.sortingLinearLayout.getVisibility() == View.VISIBLE) {
                    binding.sortingLinearLayout.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> binding.sortingLinearLayout.setVisibility(View.GONE))
                            .start();
                } else {
                    binding.sortingLinearLayout.setAlpha(0f);
                    binding.sortingLinearLayout.setVisibility(View.VISIBLE);
                    binding.sortingLinearLayout.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                }
                hideKeyboard(v);
            }
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        currentUid = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Notes").child(currentUid);

        // Load all notes initially
        loadNotes("IMAGE");
        highlightSelectedButton(binding.btnImageNotes);

        binding.btnTextNotes.setOnClickListener(v -> {
            currentFilterType = "NOTETEXT";
            loadNotes(currentFilterType);
            highlightSelectedButton(binding.btnTextNotes);
        });
        binding.btnImageNotes.setOnClickListener(v -> {
            currentFilterType = "IMAGE";
            loadNotes(currentFilterType);
            highlightSelectedButton(binding.btnImageNotes);
        });
        binding.btnCanvasNotes.setOnClickListener(v -> {
            currentFilterType = "CANVAS";
            loadNotes(currentFilterType);
            highlightSelectedButton(binding.btnCanvasNotes);
        });

        setupSearch();
        userAccountRef = FirebaseDatabase.getInstance().getReference().child("NotesUserAccounts");
        userAccountRef.child(Objects.requireNonNull(currentUid)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    //get User Info, spelling should be as in Firebase realtime database
                    email = "" + snapshot.child("email").getValue();
                    name = "" + snapshot.child("name").getValue();
                    imageUrl = (String) snapshot.child("profile").getValue();
                    userId = (String) snapshot.child("userId").getValue();

//                    binding.nameTv.setText(name);
//                    binding.emailTv.setText(email);
//                    Glide.with(mContext).load(imageUrl).into(binding.userProfileIv);

//                    try {
//
//                        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
//
//                        Glide.with(mContext)
//                                .load(imageUrl)
//                                .placeholder(R.drawable.person)
//                                .apply(requestOptions)
//                                .listener(new RequestListener<Drawable>() {
//                                    @Override
//                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
//                                        Log.e("GlideError", "Load failed", e);
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
//                                        return false;
//                                    }
//                                })
//                                .into(binding.userProfileIv);
//
//                    } catch (Exception e) {
//                        Log.e("TAG", Objects.requireNonNull(e.getMessage()));
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Fetching Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightSelectedButton(Button selectedButton) {
        // Reset all buttons to default
        binding.btnTextNotes.setBackgroundColor(Color.LTGRAY);
        binding.btnTextNotes.setTextColor(Color.BLACK);

        binding.btnImageNotes.setBackgroundColor(Color.LTGRAY);
        binding.btnImageNotes.setTextColor(Color.BLACK);

        binding.btnCanvasNotes.setBackgroundColor(Color.LTGRAY);
        binding.btnCanvasNotes.setTextColor(Color.BLACK);

        // Highlight selected button (if not null)
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
            selectedButton.setTextColor(Color.WHITE);
        }
    }

    private void loadNotes(String typeFilter) {

        Query query;
        if (typeFilter == null) {
            query = databaseReference;
        } else {
            query = databaseReference.orderByChild("type").equalTo(typeFilter);
        }

        FirebaseRecyclerOptions<NotesModel> options =
                new FirebaseRecyclerOptions.Builder<NotesModel>()
                        .setQuery(query, NotesModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<NotesModel, NoteViewHolder>(options) {
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
                return new NoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull NotesModel model) {

                holder.setNote(getActivity(), model.getTitle(), model.getNotes(), model.getSearch(), model.getUrl(), model.getDelete(), model.getType());

                String postKey = getRef(position).getKey();
                String title = getItem(position).getTitle();
                String note = getItem(position).getNotes();
                String url = getItem(position).getUrl();
                String delete = getItem(position).getDelete();
                String type = getItem(position).getType();

                long deleteLong = Long.parseLong(model.getDelete());

                String reminder_time = String.valueOf(model.getReminderTime());
                Log.e("REMINDER_TIME_TELL", reminder_time);

                Date date = new Date(deleteLong);
                android.icu.text.SimpleDateFormat formatter = new android.icu.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String formattedDate = formatter.format(date);

                Date date1 = new Date(model.getReminderTime());
                android.icu.text.SimpleDateFormat formatter1 = new android.icu.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String noteReminderTime = formatter1.format(date1);


                holder.noteItem_time.setText(formattedDate);
                Log.e("FORMATTED_DATE", String.valueOf(formattedDate));

                if (model.getReminderTime() == 0L) {
                    holder.reminderTimeTv.setVisibility(View.GONE);
                    holder.noteReminderExpired.setVisibility(View.GONE);
                } else {
                    holder.reminderTimeTv.setText(noteReminderTime);
                }

                if (model.isReminderEnabled() && model.isReminderExpired()) {
                    holder.noteReminderExpired.setVisibility(View.VISIBLE);
                    holder.reminderTimeTv.setVisibility(View.GONE);
                    holder.noteReminderExpired.setText("Reminder Expired");
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moreOptions(postKey, title, note, url, delete, type, model.getReminderTime());
                    }
                });
            }
        };
        binding.recyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    private void moreOptions(String postKey, String title, String note, String url, String delete, String type, long reminderTime) {

        // Inflate your custom layout
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet, null);

        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        // Initialize views
        TextView editTv = sheetView.findViewById(R.id.noteEdit);
        TextView forwardTv = sheetView.findViewById(R.id.noteForward);
        TextView downloadTv = sheetView.findViewById(R.id.noteDownload);
        TextView deleteTv = sheetView.findViewById(R.id.noteDelete);
        TextView dismissTv = sheetView.findViewById(R.id.noteReminderDismiss);

        if (reminderTime == 0L) {
            dismissTv.setVisibility(View.GONE);
        } else {
            dismissTv.setVisibility(View.VISIBLE);
        }

        // Show download only for image/text/canvas types
        if (type.equals("NOTETEXT") || type.equals("IMAGE") || type.equals("CANVAS")) {
            downloadTv.setVisibility(View.VISIBLE);
        } else {
            downloadTv.setVisibility(View.GONE);
        }

        // Edit option
        editTv.setOnClickListener(v -> {
            editDialog(postKey, title, note, type, url, delete);
            bottomSheetDialog.dismiss();
        });

        forwardTv.setOnClickListener(v -> {

            Intent intent = new Intent(mContext, ForwardRoomActivity.class);

            // 🔥 convert note → forwarder payload
            ArrayList<RoomChatMemberModel> list = new ArrayList<>();
            list.add(convertNoteToRoomMessage(postKey, title, note, url, type));

            intent.putParcelableArrayListExtra("messages", list);
            intent.putExtra("address", "HOME_NOTE"); // dummy / optional

            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        // Download / Save as PDF
        downloadTv.setOnClickListener(v -> {

            showProgressBottomSheet();


            saveNoteAsPDF(title, note, url, type);
            bottomSheetDialog.dismiss();
        });

        deleteTv.setOnClickListener(v -> {

            BottomSheetDialog confirmDeleteSheet = new BottomSheetDialog(mContext, R.style.BottomSheetDialogTheme);
            View sheetView2 = LayoutInflater.from(mContext)
                    .inflate(R.layout.bottom_sheet_confirm_delete, null);

            confirmDeleteSheet.setContentView(sheetView2);
            confirmDeleteSheet.show();

            TextView titleTv = sheetView2.findViewById(R.id.deleteTitleTv);
            TextView messageTv = sheetView2.findViewById(R.id.deleteMessageTv);
            Button yesBtn = sheetView2.findViewById(R.id.yesBtn);
            Button noBtn = sheetView2.findViewById(R.id.noBtn);

            titleTv.setText("Delete Note");
            messageTv.setText("Are you sure you want to permanently delete this note?");

            yesBtn.setOnClickListener(btn -> {

                Query query = databaseReference.orderByChild("delete").equalTo(delete);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }

                        // Delete from Firebase Storage (if image or canvas)
                        if (url != null && !url.isEmpty() &&
                                (type != null && (type.equals("IMAGE") || type.equals("CANVAS")))) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            storageReference.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mContext, "Note and Image Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, "Failed to Delete Image Note", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(mContext, "Note Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(mContext, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                confirmDeleteSheet.dismiss();
                bottomSheetDialog.dismiss();
            });

            noBtn.setOnClickListener(btn -> confirmDeleteSheet.dismiss());
        });

        dismissTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelReminder(postKey);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes").child(currentUid).child(postKey);
                ref.child("reminderTime").setValue(0);
                ref.child("reminderEnabled").setValue(false);
                bottomSheetDialog.dismiss();
            }
        });
    }

    private RoomChatMemberModel convertNoteToRoomMessage(String noteId, String title, String note, String url, String type) {

        RoomChatMemberModel model = new RoomChatMemberModel();

        model.setMessageId(noteId);
        model.setSenderId(userId);
        model.setSenderName(name);

        model.setType(type);
        model.setNote(note);
        model.setTitle(title);

        if ("IMAGE".equals(type) || "CANVAS".equals(type)) {
            model.setImageUrl(url);
        }

        model.setTimestamp(System.currentTimeMillis());
        model.setSeenBy(new HashMap<>());

        // ⭐ FORWARD FLAGS
        model.setForwarded(true);
        model.setForwardedFrom(name);


        return model;
    }

    private void editDialog(String postKey, String title, String note, String type, String url, String delete) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.activity_image, null);

        TextView toolbarTv = view.findViewById(R.id.toolbarTitleTv);
        toolbarTv.setText("Your Note Going To Update");

        ImageButton toolBarBackBtn = view.findViewById(R.id.toolbarBackBtn);
        EditText noteTitleEd = view.findViewById(R.id.imageTitleNameEt);
        EditText noteBodyEd = view.findViewById(R.id.imageNoteEt);
        Button updateBtn = view.findViewById(R.id.uploadBtn);
        updateBtn.setText("Update");
        CardView cardNoteIv = view.findViewById(R.id.cardNoteIv);
        noteImageView = view.findViewById(R.id.noteIv);
        changeNoteImageTv = view.findViewById(R.id.changeNoteImgTv);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        if (type.equals("NOTETEXT")) {
            cardNoteIv.setVisibility(View.GONE);
        } else if (type.equals("IMAGE")) {
            cardNoteIv.setVisibility(View.VISIBLE);
            changeNoteImageTv.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(url);
            Picasso.get().load(uri).into(noteImageView);
            Log.e("URI", String.valueOf(Uri.parse(url)));
        } else {
            cardNoteIv.setVisibility(View.VISIBLE);
            changeNoteImageTv.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(url);
            Picasso.get().load(uri).into(noteImageView);
            Log.e("URI", String.valueOf(Uri.parse(url)));
        }

        toolBarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        noteTitleEd.setText(title);
        noteBodyEd.setText(note);

        String newNoteTitleStr = noteTitleEd.getText().toString().trim();
        String newNoteBodyStr = noteBodyEd.getText().toString().trim();

        changeNoteImageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (type.equals("CANVAS")) {
                    Intent intent = new Intent(mContext, CanvasNoteActivity.class);
                    intent.putExtra("postKey", postKey);
                    intent.putExtra("title", title);
                    intent.putExtra("note", note);
                    intent.putExtra("url", url);
                    intent.putExtra("delete", delete);
                    intent.putExtra("type", type); // "canvas"
                    mContext.startActivity(intent);

                    Log.e(TAG, String.valueOf(type.equals("CANVAS")));
                } else {
                    imagePickDialog();
                    Log.e("DELETE", delete);
                }
            }
        });

        dialog.show();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (noteTitleEd.getText().toString().trim().isEmpty()) {
                    noteTitleEd.setError("Title Required");
                    noteTitleEd.requestFocus();
                } else if (noteBodyEd.getText().toString().trim().isEmpty()) {
                    noteBodyEd.setError("Note Required");
                    noteBodyEd.requestFocus();
                } else {
                    if (type.equals("NOTETEXT")) {
                        updateTextNote(postKey, noteTitleEd.getText().toString().trim(), noteBodyEd.getText().toString().trim());
                        dialog.dismiss();
                    } else if (type.equals("IMAGE") || type.equals("CANVAS")) {
                        if (imageUri != null) {
                            uploadNewImageAndUpdate(postKey, newNoteTitleStr, newNoteBodyStr, imageUri, delete, dialog);
                        } else {
                            updateImageNote(postKey, noteTitleEd.getText().toString().trim(), noteBodyEd.getText().toString().trim(), url);
                            dialog.dismiss();
                        }
                    }
                }
            }
        });
    }

    // Upload new image to Firebase Storage and update note
    private void uploadNewImageAndUpdate(String postKey, String title, String note, Uri imageUri, String delete, AlertDialog dialog) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("notesImage/" + delete + ".jpg");
        storageRef.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        //calculate the current progress of the image being uploaded
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
                        String message = "Uploading Updates...\nProgress " + (int) progress + "%";

                        //show progress
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    }
                })
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageUrl = uri.toString();
                    updateImageNote(postKey, title, note, newImageUrl);
                    progressDialog.dismiss();
                    dialog.dismiss();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Update image note in Firebase
    private void updateImageNote(String postKey, String title, String note, String imageUrl) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes").child(currentUid).child(postKey);
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("title", title);
        updateMap.put("search", title.toLowerCase());
        updateMap.put("notes", note);
        updateMap.put("url", imageUrl);
        ref.updateChildren(updateMap).addOnSuccessListener(aVoid ->
                Toast.makeText(getContext(), "Note updated", Toast.LENGTH_SHORT).show());
    }

    // Update text note in Firebase
    private void updateTextNote(String postKey, String title, String note) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes").child(currentUid).child(postKey);
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("title", title);
        updateMap.put("search", title.toLowerCase());
        updateMap.put("notes", note);
        ref.updateChildren(updateMap).addOnSuccessListener(aVoid ->
                Toast.makeText(getContext(), "Note updated", Toast.LENGTH_SHORT).show());
    }

    private void imagePickDialog() {

        //init popup menu param#1  is context and Param#2 is the UI View (profileImagePickFab) to above or below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(mContext, changeNoteImageTv);
        //add menu items to our popup menu Param#1 is GroupId,Param#2 is ItemId,Param#3 is OrderID,Param#4 Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.setGravity(Gravity.BOTTOM);

        //show popup menu
        popupMenu.show();

        //Handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                //get id of the menu item clicked
                int itemId = menuItem.getItemId();

                if (itemId == 1) {
                    //Camera is clicked we need to check if we have permission of Camera,Storage before launching Camera to capture Image
                    Log.d(TAG, "onMenuItemClick: Camera clicked, check if camera permission granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. We only need Camera Permission
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        //Device version is below TIRAMISU. We need Camera and Storage Permission
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    Log.d(TAG, "onMenuItemClick: Check if Storage permission is granted or not");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestsStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return true;
            }
        });
    }

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");

        //setup Content Values,MediaStore to capture high quality image using Camera Intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Intent to launch Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    // ✅ When search box empty, reload current type’s notes
                    Button selectedButton = getSelectedButton();
                    if (selectedButton != null) {
                        if (selectedButton == binding.btnTextNotes)
                            loadNotes("NOTETEXT");
                        else if (selectedButton == binding.btnImageNotes)
                            loadNotes("IMAGE");
                        else if (selectedButton == binding.btnCanvasNotes)
                            loadNotes("CANVAS");
                    } else {
                        loadNotes(null);
                    }
                } else {
                    // ✅ Only run search if user typed something
                    search(searchQuery);
                }

                if (searchQuery.isBlank()) {
                    loadNotes(null);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private Button getSelectedButton() {
        if (binding.btnTextNotes.getCurrentTextColor() == Color.WHITE) return binding.btnTextNotes;
        if (binding.btnImageNotes.getCurrentTextColor() == Color.WHITE)
            return binding.btnImageNotes;
        if (binding.btnCanvasNotes.getCurrentTextColor() == Color.WHITE)
            return binding.btnCanvasNotes;
        return null;
    }

    private void search(String searchQuery) {

        Query query;
        if (currentFilterType == null) {
            // Search across all notes
            query = databaseReference.orderByChild("search")
                    .startAt(searchQuery.toLowerCase())
                    .endAt(searchQuery.toLowerCase() + "\uf8ff");

        } else {
            // Search only inside the selected type
            query = databaseReference.orderByChild("type")
                    .equalTo(currentFilterType);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Rebuild the filtered search query by title/text
                FirebaseRecyclerOptions<NotesModel> options =
                        new FirebaseRecyclerOptions.Builder<NotesModel>()
                                .setQuery(databaseReference.orderByChild("search")
                                        .startAt(searchQuery.toLowerCase())
                                        .endAt(searchQuery.toLowerCase() + "\uf8ff"), NotesModel.class)
                                .build();

                adapter = new FirebaseRecyclerAdapter<NotesModel, NoteViewHolder>(options) {
                    @NonNull
                    @Override
                    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
                        return new NoteViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull NotesModel model) {

                        // Only show notes matching both search text and current filter type
                        if (currentFilterType == null || model.getType().equals(currentFilterType)) {
                            holder.setNote(getActivity(), model.getTitle(), model.getNotes(), model.getSearch(), model.getUrl(), model.getDelete(), model.getType());
                            holder.itemView.setVisibility(View.VISIBLE);
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                        }
                    }
                };

                binding.recyclerview.setAdapter(adapter);
                adapter.startListening();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveNoteAsPDF(String title, String noteText, String imageUrl, String noteType) {

        new Thread(() -> {
            try {

                // ----------- UPDATE: Start Progress 0% -----------
                updateDownloadProgress(5, "Starting...");

                // ======= 1. LOAD IMAGE (0–20%) =======
                Bitmap imageBitmap = null;
                if ((noteType.equalsIgnoreCase("IMAGE") || noteType.equalsIgnoreCase("CANVAS"))
                        && imageUrl != null && !imageUrl.trim().isEmpty()) {

                    updateDownloadProgress(10, "Loading image...");

                    try {
                        URL url = new URL(imageUrl);
                        imageBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    updateDownloadProgress(20, "Image loaded");
                }

                // ======= 2. GENERATE PDF (20–80%) =======
                updateDownloadProgress(30, "Generating PDF...");

                PdfDocument pdfDocument = new PdfDocument();
                int pageWidth = 595, pageHeight = 842, margin = 40;

                Paint titlePaint = new Paint();
                titlePaint.setColor(Color.BLACK);
                titlePaint.setTextSize(18f);
                titlePaint.setFakeBoldText(true);

                TextPaint bodyPaint = new TextPaint();
                bodyPaint.setColor(Color.BLACK);
                bodyPaint.setTextSize(14f);

                Paint headerPaint = new Paint();
                headerPaint.setColor(Color.DKGRAY);
                headerPaint.setTextSize(12f);

                Paint footerPaint = new Paint();
                footerPaint.setColor(Color.GRAY);
                footerPaint.setTextSize(10f);

                int currentPageNumber = 1;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                float y = margin + 40;
                String headerText = String.valueOf(R.string.app_name);
                String dateText = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

                canvas.drawText(headerText, margin, 30, headerPaint);
                canvas.drawText(dateText, pageWidth - margin - 130, 30, headerPaint);

                // TITLE
                if (title != null && !title.trim().isEmpty()) {
                    canvas.drawText("Title: " + title, margin, y, titlePaint);
                    y += 40;
                }

                updateDownloadProgress(50, "Adding text...");

                // TEXT
                if (noteText != null && !noteText.trim().isEmpty()) {
                    int textWidth = pageWidth - (2 * margin);
                    int lineHeight = 18;

                    String[] paragraphs = noteText.split("\n");

                    int totalPara = paragraphs.length;
                    int donePara = 0;

                    for (String para : paragraphs) {

                        // progress inside text rendering
                        int percent = 50 + (donePara * 20 / totalPara);
                        updateDownloadProgress(percent, "Formatting text...");

                        StaticLayout staticLayout;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            staticLayout = StaticLayout.Builder.obtain(para, 0, para.length(), bodyPaint, textWidth)
                                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                    .setLineSpacing(0, 1.3f)
                                    .setIncludePad(false)
                                    .build();
                        } else {
                            staticLayout = new StaticLayout(para, bodyPaint, textWidth,
                                    Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
                        }

                        int paraHeight = staticLayout.getHeight();
                        if (y + paraHeight > pageHeight - 80) {
                            canvas.drawText("Page " + currentPageNumber, pageWidth / 2f, pageHeight - 30, footerPaint);
                            pdfDocument.finishPage(page);

                            currentPageNumber++;
                            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create();
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();

                            canvas.drawText(headerText, margin, 30, headerPaint);
                            canvas.drawText(dateText, pageWidth - margin - 130, 30, headerPaint);
                            y = margin + 40;
                        }

                        canvas.save();
                        canvas.translate(margin, y);
                        staticLayout.draw(canvas);
                        canvas.restore();

                        y += paraHeight + lineHeight;
                        donePara++;
                    }
                }

                // IMAGE
                updateDownloadProgress(80, "Inserting image...");

                if (imageBitmap != null) {
                    int maxWidth = pageWidth - (2 * margin);
                    int maxHeight = pageHeight / 2;

                    float scale = Math.min((float) maxWidth / imageBitmap.getWidth(),
                            (float) maxHeight / imageBitmap.getHeight());

                    Bitmap scaled = Bitmap.createScaledBitmap(
                            imageBitmap,
                            (int) (imageBitmap.getWidth() * scale),
                            (int) (imageBitmap.getHeight() * scale),
                            true
                    );

                    canvas.drawBitmap(scaled, margin, y, null);
                }

                canvas.drawText("Page " + currentPageNumber, pageWidth / 2f, pageHeight - 30, footerPaint);
                pdfDocument.finishPage(page);

                // ======= 3. SAVE FILE (80–100%) =======
                updateDownloadProgress(90, "Saving file...");

                String safeTitle = (title == null || title.isEmpty()) ? "Note" : title.replaceAll("[^a-zA-Z0-9]", "_");
                String fileName = safeTitle + "_" + System.currentTimeMillis() + ".pdf";

                Uri pdfUri;
                OutputStream outputStream;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyNotes");

                    pdfUri = requireContext().getContentResolver()
                            .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    outputStream = requireContext().getContentResolver().openOutputStream(pdfUri);

                } else {
                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyNotes");
                    if (!dir.exists()) dir.mkdirs();

                    File file = new File(dir, fileName);
                    pdfUri = Uri.fromFile(file);
                    outputStream = new FileOutputStream(file);
                }

                pdfDocument.writeTo(outputStream);
                pdfDocument.close();
                outputStream.close();

                updateDownloadProgress(100, "Completed");

                requireActivity().runOnUiThread(() -> {
                    bottomSheetDialog.dismiss();
                    Toast.makeText(requireContext(), "PDF saved successfully!", Toast.LENGTH_SHORT).show();
                });


            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            }

        }).start();
    }

    private void showProgressBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.progress_bottom_sheet, null);

        progressTitle = view.findViewById(R.id.progressTitle);
        progressMessage = view.findViewById(R.id.progressMessage);
        progressBar = view.findViewById(R.id.progressBar);

        Button cancelBtn = view.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            isCancelled = true; // flag you can check
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
    }

    private void updateDownloadProgress(int progress, String msg) {
        requireActivity().runOnUiThread(() -> {
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                progressBar.setProgress(progress);
                progressMessage.setText(msg);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadNotes(null);
    }


    private void cancelReminder(String noteId) {
        Intent intent = new Intent(mContext, ReminderReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
                mContext,
                noteId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        am.cancel(pi);
    }

}
