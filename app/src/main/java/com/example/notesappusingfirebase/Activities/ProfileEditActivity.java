package com.example.notesappusingfirebase.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class ProfileEditActivity extends AppCompatActivity {

    ActivityProfileEditBinding binding;

    private static final String TAG = "PROFILE_EDIT_TAG";

    //FirebaseAuth for Auth related task
    private FirebaseAuth firebaseAuth;

    //ProgressDialog:to show while Profile Update
    private ProgressDialog progressDialog;
    private Uri imageUri;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get instance of the Firebase fo auth related task
        firebaseAuth = FirebaseAuth.getInstance();

        //progressDialog: to show while profile update
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        //Handle profileImagePickFab click, show image pick popup menu
        binding.updateProfileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageGallery();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });


    }

    private void validateData() {

        name = binding.updateNameEd.getText().toString().trim();
//         email=binding.emailEt.getText().toString().trim();

        //Validate data
        if (imageUri==null){
            //no image to upload to storage, just update Database
            updateProfileDb();

            Toast.makeText(this, "imageUri is null", Toast.LENGTH_SHORT).show();
        }else {
            //image need to upload storage, first upload image then update Database
            uploadProfileImageStorage();
        }

    }


    private void updateProfileDb() {

        //show progressDialog
        progressDialog.setMessage("Updating user info");
        progressDialog.show();

        //setup data in hashMap to update to Firebase Database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name",name);
//        hashMap.put("email",""+email);

//        if (imageUrl != null) {
//            //update profileImageUrl in the Database only if uploaded image url is not null
//            hashMap.put("imageUrl", imageUrl);
//        }


        //DatabaseReference of user to update info
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("NotesUserAccounts");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid()))
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //update Successfully
                        Log.d(TAG, "onSuccess: Info Updated");
                        progressDialog.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Name Updated", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //update failed
                        Log.e(TAG, "onFailure: ",e);
                        progressDialog.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Profile Update Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void uploadProfileImageStorage() {

        //show progress
        progressDialog.setMessage("Uploading User Image..");
        progressDialog.show();

        //setup image and path e.g UserImage/profile_userid
        String filePathAndName = "NotesModel/"+"profile"+firebaseAuth.getUid();

        //StorageReference to upload image
        StorageReference reference= FirebaseStorage.getInstance().getReference().child(filePathAndName);
        reference.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress=(100.0*snapshot.getBytesTransferred()) /snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Process: "+progress);

                        progressDialog.setMessage("Uploading Profile Image.\nProgress: "+(int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //image upload successfully, get Url of upload image
                        Log.d(TAG, "onSuccess: Uploaded..!!");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl=uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
//                            updateProfileDb(uploadedImageUrl);

                            //change progress dialog message
                            progressDialog.setMessage("Saving user Info..");

                            //getting uid of the Registered user to save in Realtime database
                            String registerUserId = firebaseAuth.getUid();

                            //setup data to save in firebase realtime db. most of the data will be empty and will set in edit profile
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("name",name);
                            hashMap.put("profile",uploadedImageUrl);

                            //set data to firebase db
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NotesUserAccounts");
                            assert registerUserId != null;
                            reference.child(registerUserId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            //update Successfully
                                            Log.d(TAG, "onSuccess: Info Updated");
                                            progressDialog.dismiss();

                                            Toast.makeText(ProfileEditActivity.this, "Profile Name+Image Updated", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //update failed
                                            Log.e(TAG, "onFailure: ",e);
                                            progressDialog.dismiss();

                                            Toast.makeText(ProfileEditActivity.this, "Profile Update Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to upload image
                        Log.e(TAG, "onFailure: ",e);
                        progressDialog.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Failed to Update Profile Image: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

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
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.baseline_person_24)
                                    .into(binding.updateProfileImg);

                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }

                    } else {
                        //Canceled
                        Toast.makeText(ProfileEditActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ProfileEditActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageGallery() {

        Log.d(TAG, "pickImageGallery: ");

        //Intent to launch Image Picker e.g.Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to picked Image
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void loadMyInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NotesUserAccounts");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get User Info, spelling should be as in Firebase realtime database
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String imageUrl = (String) snapshot.child("profile").getValue();

                binding.updateNameEd.setText(name);
                binding.updateEmailEd.setText(email);
                binding.updateEmailEd.setEnabled(false);

                try {

                    RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

                    Glide.with(ProfileEditActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.person)
                            .apply(requestOptions)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Load failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(binding.updateProfileImg);

                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileEditActivity.this, "Fetching Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}