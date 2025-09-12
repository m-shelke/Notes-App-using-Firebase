package com.example.notesappusingfirebase.Activities;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notesappusingfirebase.Model.NotesModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityDrawFabBinding;
import com.example.notesappusingfirebase.text.VerticalSeekBar;
import com.example.notesappusingfirebase.text.WritingView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;
import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawFabActivity extends AppCompatActivity {

    ActivityDrawFabBinding binding;

    Float translationYaxis = 100f;
    boolean isMenuOpen = false;
    OvershootInterpolator interpolator = new OvershootInterpolator();

    WritingView writingView;

    String currentUId;
    String postKey, delete, type, title, note, url;
    //init/setup progressDialog to show, while adding/updating Ads
    private ProgressDialog progressDialog;
    private boolean isMoveMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDrawFabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        Intent intent = getIntent();
        postKey = intent.getStringExtra("postKey");
        title = intent.getStringExtra("title");
        note = intent.getStringExtra("note");
        url = intent.getStringExtra("url");
        delete = intent.getStringExtra("delete");
        type = intent.getStringExtra("type");

        writingView = binding.writingView;

        if (type != null && type.equals("canvas") && url != null && !url.isEmpty()) {
            loadCanvasFromUrl(url);
        }

        // Default mode = Draw Mode
        writingView.setMoveMode(false);
        writingView.setShapeType(WritingView.ShapeType.FREE_DRAW);

        binding.moveToggleBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_purple));

        // Toggle behavior
        binding.moveToggleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                // Move Mode ON
//                writingView.setMoveMode(true);
//                writingView.setShapeType(WritingView.ShapeType.FREE_DRAW);
//            } else {
//                // Drawing Mode ON
//                writingView.setMoveMode(false);
//                writingView.setShapeType(WritingView.ShapeType.FREE_DRAW);
//            }
            toggleMode();
        });

        //init/setup progressDialog to show, while adding/updating Ads
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        currentUId = user.getUid();

        binding.verticalSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                writingView.setLineWidth(progress);
                Log.e("VerticalSeekBar", "Progress: " + progress);
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar seekBar) {
                writingView.setLineWidth(seekBar.getProgress());
                Log.e("VerticalSeekBar", "Start tracking");
            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar seekBar) {
                writingView.setLineWidth(seekBar.getProgress());
                Log.e("VerticalSeekBar", "Stop tracking");
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        showMenu();

        binding.fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = Bitmap.createBitmap(binding.main.getWidth(), binding.main.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                binding.writingView.draw(canvas);

                LayoutInflater inflater = LayoutInflater.from(DrawFabActivity.this);
                View view = inflater.inflate(R.layout.draw_preview, null);

                ImageView drawPreviewIv = view.findViewById(R.id.drawPreviewIv);
                EditText drawTitleEd = view.findViewById(R.id.drawTitleNameEt);
                EditText drawNoteEd = view.findViewById(R.id.drawNoteEt);
                CardView closeCv = view.findViewById(R.id.closeCv);
                Button uploadBtn = view.findViewById(R.id.uploadBtn);
                Button updateBtn = view.findViewById(R.id.updateBtn);
                GifImageView noCanvas_gif = view.findViewById(R.id.noCanvas_gif);
                TextView noCanvasTv = view.findViewById(R.id.noCanvasTv);

                if (writingView.drawnStrokes.isEmpty() && url.isEmpty()) {
                    noCanvas_gif.setVisibility(VISIBLE);
                    noCanvasTv.setVisibility(VISIBLE);
                }

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageReference = firebaseStorage.getReference("notesImage");
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("Notes").child(currentUId);

                AlertDialog.Builder builder = new AlertDialog.Builder(DrawFabActivity.this);
                builder.setCancelable(true);
                builder.setView(view);

                AlertDialog dialog = builder.create();

//                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();

                drawPreviewIv.setImageBitmap(bitmap);

                Animation fadeOut = AnimationUtils.loadAnimation(DrawFabActivity.this, R.anim.dialog_exist);
                closeCv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        v.startAnimation(fadeOut);

                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                animation.start();
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                dialog.dismiss();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                });

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (dialog.isShowing()) dialog.dismiss();
                }, 500000);


                if (Objects.equals(type, "canvas")) {
                    uploadBtn.setVisibility(View.GONE);
                    updateBtn.setVisibility(VISIBLE);

                    drawTitleEd.setText(title);
                    drawNoteEd.setText(note);

                    updateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String drawTitle = drawTitleEd.getText().toString().trim();
                            String drawNote = drawNoteEd.getText().toString().trim();
                            drawPreviewIv.setDrawingCacheEnabled(true);
                            drawPreviewIv.buildDrawingCache();

                            if (drawTitle.isEmpty()) {
                                drawTitleEd.setError("Provide Title");
                                drawTitleEd.requestFocus();
                            } else if (drawNote.isEmpty()) {
                                drawNoteEd.setError("Provide Note");
                                drawNoteEd.requestFocus();
                            } else {

                                // Capture updated drawing as bitmap
                                Bitmap updatedBitmap = Bitmap.createBitmap(binding.main.getWidth(), binding.main.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas updatedCanvas = new Canvas(updatedBitmap);
                                binding.writingView.draw(updatedCanvas);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                updatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("Notes").child(currentUId);

                                StorageReference storageRef = FirebaseStorage.getInstance().getReference("notesImage/" + delete + ".jpg");
                                storageRef.putBytes(data)
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
                                        }).continueWithTask(task -> {
                                            if (!task.isSuccessful()) {
                                                throw task.getException();
                                            }
                                            return storageRef.getDownloadUrl();
                                        }).addOnCompleteListener(task -> {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Uri downloadUri = task.getResult();

                                                HashMap<String, Object> updateMap = new HashMap<>();
                                                updateMap.put("title", drawTitle);
                                                updateMap.put("notesModel", drawNote);
                                                updateMap.put("url", downloadUri.toString());
                                                updateMap.put("search", drawTitle.toLowerCase());
                                                updateMap.put("type", "canvas");
                                                updateMap.put("delete", delete); // keep same delete timestamp or update if you prefer

                                                databaseReference.child(postKey).updateChildren(updateMap)
                                                        .addOnSuccessListener(unused -> {
                                                            Toast.makeText(DrawFabActivity.this, "Canvas Updated Successfully!", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                            Intent intent = new Intent(DrawFabActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(DrawFabActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(DrawFabActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });


                                Log.e("TITLEED", drawTitle + drawNote);
                            }
                        }
                    });
                }

                uploadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String drawTitle = drawTitleEd.getText().toString().trim();
                        String drawNote = drawNoteEd.getText().toString().trim();
                        drawPreviewIv.setDrawingCacheEnabled(true);
                        drawPreviewIv.buildDrawingCache();

                        if (writingView.drawnStrokes.isEmpty()) {
                            Animation shake = AnimationUtils.loadAnimation(DrawFabActivity.this, R.anim.shake);
                            noCanvasTv.startAnimation(shake);
                        } else if (drawTitle.isEmpty()) {
                            drawTitleEd.setError("Provide Title");
                            drawTitleEd.requestFocus();
                        } else if (drawNote.isEmpty()) {
                            drawNoteEd.setError("Provide Note");
                            drawNoteEd.requestFocus();
                        } else {

                            Bitmap bitmap1 = ((BitmapDrawable) drawPreviewIv.getDrawable()).getBitmap();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] data = byteArrayOutputStream.toByteArray();

                            NotesModel notesModel = new NotesModel();

                            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + "jpg");
                            UploadTask uploadTask = reference.putBytes(data);

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

//                                                drawProgressBar.setVisibility(View.GONE);

                                                notesModel.setDelete(String.valueOf(System.currentTimeMillis()));
                                                notesModel.setNotes(drawNote);
                                                notesModel.setSearch(drawTitle.toLowerCase());
                                                notesModel.setTitle(drawTitle);
                                                notesModel.setUrl(downloadUri.toString());
                                                notesModel.setType("canvas");

                                                String randomKey = databaseReference.push().getKey();

                                                databaseReference.child(randomKey).setValue(notesModel);

                                                Toast.makeText(DrawFabActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(DrawFabActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        dialog.dismiss();
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
                });
            }
        });
    }


    private void loadCanvasFromUrl(String url) {
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (bitmap != null) {
                        // Set it as background so user can see and draw on it
                        binding.writingView.setBackground(new BitmapDrawable(getResources(), bitmap));
                        Toast.makeText(this, "Canvas loaded from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Failed to load canvas image", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    //All Button onClick(); methods
    public void yellowClick(View view) {
        writingView.setLineColor(Color.YELLOW);
    }

    public void magentaClick(View view) {
        writingView.setLineColor(Color.MAGENTA);
    }

    public void redClick(View view) {
        writingView.setLineColor(Color.RED);
    }

    public void greenClick(View view) {
        writingView.setLineColor(Color.GREEN);
    }

    public void blueClick(View view) {
        writingView.setLineColor(Color.BLUE);
    }

    public void triangleClick(View view) {
        writingView.setShapeType(WritingView.ShapeType.TRIANGLE);
    }

    public void circleClick(View view) {
        writingView.setShapeType(WritingView.ShapeType.CIRCLE);
    }

    public void linesClick(View view) {
        writingView.setShapeType(WritingView.ShapeType.LINE);
    }

    public void rectangleClick(View view) {
        writingView.setShapeType(WritingView.ShapeType.RECTANGLE);
    }

    public void freeDrawClick(View view) {
        writingView.setShapeType(WritingView.ShapeType.FREE_DRAW);
    }


    private void showMenu() {

        binding.fabClear.setAlpha(0f);
        binding.fabUndo.setAlpha(0f);
        binding.fabRedo.setAlpha(0f);
        binding.fabSave.setAlpha(0f);
        binding.fabColor.setAlpha(0f);
        binding.fabUpload.setAlpha(0f);
        binding.colorSetLinear.setAlpha(0f);
        binding.shapeSetLinear.setAlpha(0f);

        binding.fabClear.setTranslationY(translationYaxis);
        binding.fabUndo.setTranslationY(translationYaxis);
        binding.fabRedo.setTranslationY(translationYaxis);
        binding.fabSave.setTranslationY(translationYaxis);
        binding.fabColor.setTranslationY(translationYaxis);
        binding.fabUpload.setTranslationY(translationYaxis);
        binding.colorSetLinear.setTranslationY(translationYaxis);
        binding.shapeSetLinear.setTranslationY(translationYaxis);

        binding.expandFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMenuOpen) {
                    closeMenu();
                } else {
                    openMenu();
                }
            }
        });


        binding.fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writingView.clearCanvas();
            }
        });

        binding.fabUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writingView.undo();
            }
        });

        binding.fabRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writingView.redo();
            }
        });

        binding.fabColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(DrawFabActivity.this, Color.BLACK, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        writingView.setLineColor(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // do nothing
                    }
                });
                colorPicker.show();
            }
        });

        binding.fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
    }

    @SuppressLint("NewApi")
    private void saveImage() {

        Bitmap bitmap = writingView.getDrawingBitmap();

        String filename = "drawing_" + System.currentTimeMillis() + ".png";

        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above - use MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyDrawings");

            ContentResolver resolver = getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                assert imageUri != null;
                fos = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "Saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            And request permissions at runtime (for Android 6–9 only):
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Android 9 and below
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDrawings");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, filename);

            try {
                fos = Files.newOutputStream(file.toPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Toast.makeText(this, "Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                // Refresh gallery
                MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openMenu() {

        isMenuOpen = !isMenuOpen;
        binding.expandFab.setImageResource(R.drawable.stars);

        binding.fabClear.setVisibility(View.VISIBLE);
        binding.fabUndo.setVisibility(View.VISIBLE);
        binding.fabRedo.setVisibility(View.VISIBLE);
        binding.fabSave.setVisibility(View.VISIBLE);
        binding.fabColor.setVisibility(View.VISIBLE);
        binding.fabUpload.setVisibility(View.VISIBLE);
        binding.colorSetLinear.setVisibility(View.VISIBLE);
        binding.shapeSetLinear.setVisibility(View.VISIBLE);

        binding.fabClear.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabUndo.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabRedo.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabSave.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabColor.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabUpload.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.colorSetLinear.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        binding.shapeSetLinear.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();

    }

    private void closeMenu() {

        isMenuOpen = !isMenuOpen;
        binding.expandFab.setImageResource(R.drawable.baseline_keyboard_double_arrow_up_24);

        binding.fabClear.setVisibility(View.GONE);
        binding.fabUndo.setVisibility(View.GONE);
        binding.fabRedo.setVisibility(View.GONE);
        binding.fabSave.setVisibility(View.GONE);
        binding.fabColor.setVisibility(View.GONE);
        binding.fabUpload.setVisibility(View.GONE);
        binding.colorSetLinear.setVisibility(View.GONE);
        binding.shapeSetLinear.setVisibility(View.GONE);

        binding.fabClear.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabUndo.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabRedo.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabSave.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabColor.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.fabUpload.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.colorSetLinear.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        binding.shapeSetLinear.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();

    }

    private void toggleMode() {
        isMoveMode = !isMoveMode;

        writingView.setMoveMode(isMoveMode);
        writingView.setShapeType(WritingView.ShapeType.FREE_DRAW);

        // Animate the button
        ScaleAnimation scale = new ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(200);
        binding.moveToggleBtn.startAnimation(scale);

        // Change icon & color
        if (isMoveMode) {
            //   binding.moveToggleBtn.setButtonDrawable(R.drawable.mail);
            binding.moveToggleBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_dark));
        } else {
            //   binding.moveToggleBtn.setButtonDrawable(R.drawable.draw);
            binding.moveToggleBtn.setBackgroundTintList(getColorStateList(android.R.color.holo_purple));
        }
    }

}