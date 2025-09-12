package com.example.notesappusingfirebase.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.Model.NotesModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityEditFabBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditFabActivity extends AppCompatActivity {

    //ViewBinding class initialization
    ActivityEditFabBinding binding;

    //init/setup progressDialog to show, while adding/updating Ads
    private ProgressDialog progressDialog;
    private static final String TAG = "ADS_CREATED_TAG";

    String currentUId;

    NotesModel notesModel;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //inflating corresponding layout (Activity Layout)
        binding = ActivityEditFabBinding.inflate(getLayoutInflater());
        //Attaching root
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


        //Handle toolbarBackBtn click, to go back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.uploadBtn.setOnClickListener(v->{
            validateData();
        });


    }

    String titleEd,notesEd;

    public void validateData(){

        titleEd = binding.titleEt.getText().toString().trim();
        notesEd = binding.noteEt.getText().toString().trim();

        if (titleEd.isEmpty()){
            binding.titleEt.setError("Enter Title");
            binding.titleEt.requestFocus();
        } else if (notesEd.isEmpty()) {
            binding.noteEt.setError("Enter Note");
            binding.noteEt.requestFocus();
        }else {
            uploadNote();
        }

    }

    private void uploadNote() {

        Log.d(TAG, "NotesModel: ");

        //show progressDialog
        progressDialog.setMessage("We Adding Your Note");
        progressDialog.show();

        databaseReference = firebaseDatabase.getReference("Notes").child(currentUId);

        notesModel = new NotesModel();

        notesModel.setDelete(String.valueOf(System.currentTimeMillis()));
        notesModel.setNotes(notesEd);
        notesModel.setSearch(titleEd.toLowerCase());
        notesModel.setTitle(titleEd);
        notesModel.setType("text");

        String randomKey = databaseReference.push().getKey();

        assert randomKey != null;
        databaseReference.child(randomKey).setValue(notesModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(EditFabActivity.this, "Text Note Added", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(EditFabActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });


    }
}