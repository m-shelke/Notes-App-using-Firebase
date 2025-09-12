package com.example.notesappusingfirebase.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notesappusingfirebase.Model.RoomNoteMemberModel;
import com.example.notesappusingfirebase.Package.ImagesUploadAdapter;
import com.example.notesappusingfirebase.Package.MultipleImageModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityMultipImageUploadBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MultipImageUploadActivity extends AppCompatActivity {

    ActivityMultipImageUploadBinding binding;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    DatabaseReference roomListRef;
    StorageReference storageReference;
    String currentUid, address, roomName, senderName;
    RoomNoteMemberModel roomNoteMemberModel;

    private List<MultipleImageModel> selectedImages = new ArrayList<>();
    private ImagesUploadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMultipImageUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra("imageUris");

        if (imageUris != null) {
            for (Uri uri : imageUris) {
                Log.d("MULTI_IMAGE", "Uri: " + uri.toString());
                addSelectedImage(uri);    // <-- Loads into RecyclerView
            }
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            roomName = bundle.getString("roomName");
            senderName = bundle.getString("name");
            address = bundle.getString("address");
            Log.e("VALUES", roomName + "\n" + senderName + "\n" + address);
        } else {
            Toast.makeText(this, "Room Value Missing", Toast.LENGTH_SHORT).show();
        }

        roomNoteMemberModel = new RoomNoteMemberModel();
        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        storageReference = firebaseStorage.getReference("NoteFiles");
        roomListRef = firebaseDatabase.getReference("NoteList").child(address);


        //changing start icon of TextInputLayout, when password recovery email sent successfully
        binding.imageCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
        binding.imageCaptionTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        binding.imageCaptionTil.setEndIconVisible(true);

        binding.imageCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAllImages();
            }
        });

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setHasFixedSize(true);

        adapter = new ImagesUploadAdapter(this, selectedImages, position -> {
            if (position >= 0 && position < selectedImages.size()) {
                selectedImages.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();

            }
        });

        binding.recyclerview.setAdapter(adapter);

    }

    private void addSelectedImage(Uri uri) {
        String name = getFileNameFromUri(uri);
        long size = getFileSizeFromUri(uri);
        MultipleImageModel si = new MultipleImageModel(uri, name, size);
        selectedImages.add(si);
    }


    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private long getFileSizeFromUri(Uri uri) {
        long size = 0;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex))
                    size = cursor.getLong(sizeIndex);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return size;
    }


    private void uploadAllImages() {

        for (int i = 0; i < selectedImages.size(); i++) {
            final int pos = i;
            MultipleImageModel sel = selectedImages.get(pos);
            if (sel.uploaded) continue; // skip already uploaded

            // create child path: notesFiles/<timestamp>_<filename>
            String ext = getFileExtension(sel.fileName); // helper below
            String storageName = sel.fileName;
            StorageReference imageRef = storageReference.child(storageName);

            UploadTask uploadTask = imageRef.putFile(sel.uri);
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                int p = (int) progress;
                // update adapter progress
                adapter.updateProgress(pos, p);
            }).addOnSuccessListener(taskSnapshot -> {
                // get download url
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    adapter.markUploaded(pos, url);

                    String imageSetName = binding.imageCaptionEt.getText().toString().trim();
                    if (imageSetName.isEmpty()) {
                        imageSetName = "IMAGE";
                    }

                    long ts = new Date().getTime();

                    // create RoomNoteMemberModel and push to Realtime DB
                    RoomNoteMemberModel model = new RoomNoteMemberModel();
                    model.setSenderName(senderName);           // your existing name var
                    model.setSenderId(currentUid);         // your existing userId var
                    model.setFileName(sel.fileName);
                    model.setNote(imageSetName);                  // add caption if you want
                    model.setSearch(imageSetName.toLowerCase());
                    model.setTimestamp(ts);
                    model.setFileUrl(url);
                    model.setType("IMAGE");

                    String key = roomListRef.push().getKey();
                    if (key != null) roomListRef.child(key).setValue(model)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Toast.makeText(MultipImageUploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
            }).addOnFailureListener(e -> {
                // mark as failed or show toast
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) return "";
        return fileName.substring(dot + 1);
    }

}