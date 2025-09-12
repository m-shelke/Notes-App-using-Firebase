package com.example.notesappusingfirebase.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.Model.NotesModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityImageBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {

    ActivityImageBinding binding;

    private Uri imageUri;

    String imageUrl;

    String currentUId;

    //ProgressDialog:to show while Profile Update
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //init/setup progressDialog to show, while adding/updating Ads
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        currentUId = user.getUid();

        //getting Uri form the External Storage or onActivityResult method
//        Intent intent = getIntent();
//        imageUri = intent.getData();

        //setting got Uri to XML VideoView i.e videoViewMain
//        binding.noteIv.setImageURI(imageUri);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            imageUrl = bundle.getString("imageUrl");
            imageUri = Uri.parse(imageUrl);
        }else {
            Toast.makeText(this, "Unable To Fetch Url", Toast.LENGTH_SHORT).show();
        }

        //print videoUri in log cat
        Log.e("URI", "onCreate: "+imageUrl);

        Picasso.get().load(imageUri).noPlaceholder().into(binding.noteIv);

        //Handle toolbarBackBtn click, to go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageOnStorage();
            }
        });

    }

    private void uploadImageOnStorage() {

        String imageTitle = binding.imageTitleNameEt.getText().toString().trim();
        String imageNote = binding.imageNoteEt.getText().toString().trim();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference("notesImage");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Notes").child(currentUId);

        NotesModel notesModel = new NotesModel();

        if (imageUri == null){
            binding.noImageviewTv.setVisibility(View.VISIBLE);
            Animation shake = AnimationUtils.loadAnimation(ImageActivity.this,R.anim.shake);
            binding.noImageviewTv.startAnimation(shake);
        }else if(imageTitle.isEmpty()) {
            binding.imageTitleNameEt.setError("Provide Title");
            binding.imageTitleNameEt.requestFocus();
        } else if (imageNote.isEmpty()) {
            binding.imageNoteEt.setError("Provide Note");
            binding.imageNoteEt.requestFocus();
        }else {

            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "jpg");
            UploadTask uploadTask = reference.putFile(imageUri);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    //calculate the current progress of the image being uploaded
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
                    String message = "Uploading Canvas...\nProgress " + (int) progress + "%";

                    //show progress
                    progressDialog.setMessage(message);
                    progressDialog.show();
                }
            });

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            //Continue with the task to get the download url
                            return reference.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();

                                notesModel.setDelete(String.valueOf(System.currentTimeMillis()));
                                notesModel.setNotes(imageNote);
                                notesModel.setSearch(imageTitle.toLowerCase());
                                notesModel.setTitle(imageTitle);
                                notesModel.setUrl(downloadUri.toString());
                                notesModel.setType("image");

                                String randomKey = databaseReference.push().getKey();

                                assert randomKey != null;
                                databaseReference.child(randomKey).setValue(notesModel);

                                Toast.makeText(ImageActivity.this, "Image Note Uploaded", Toast.LENGTH_SHORT).show();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(ImageActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                        finish();
                                    }
                                }, 1000);

                            } else {
                                //handle failures
                                //....
                            }
                        }
                    });
        }
    }
}