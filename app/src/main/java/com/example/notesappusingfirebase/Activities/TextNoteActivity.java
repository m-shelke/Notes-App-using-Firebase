package com.example.notesappusingfirebase.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notesappusingfirebase.Model.NotesModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.Reminder.ReminderReceiver;
import com.example.notesappusingfirebase.databinding.ActivityEditFabBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TextNoteActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    //ViewBinding class initialization
    ActivityEditFabBinding binding;
    String currentUId;
    NotesModel notesModel = new NotesModel();
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String titleEd, notesEd;
    String formattedDate;
    private boolean reminderEnabled = false;
    private long reminderTime = 0L;
    //init/setup progressDialog to show, while adding/updating Ads
    private ProgressDialog progressDialog;

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleReminder(Context context, long reminderTime, String noteId, String title, String note, String currentUId) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Build intent for reminder
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("noteId", noteId);
        intent.putExtra("currentUId", currentUId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ✅ Check permission for exact alarms (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // Exact alarm allowed
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                // ⚠️ Fallback: inexact alarm
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);

                Toast.makeText(context,
                        "Exact alarms not allowed. Reminder may not be precise.",
                        Toast.LENGTH_LONG).show();

                // Optionally guide user to settings
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                context.startActivity(settingsIntent);
            }
        } else {
            // ✅ Pre-Android 12 → exact alarms are fine
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }


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

        binding.uploadBtn.setOnClickListener(v -> {
            validateData();
        });


        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotificationPermission();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    showDateTimePicker(); // ✅ Only allow if permission granted
                } else {
                    binding.switchReminder.setChecked(false); // force off
                }
            } else {
                binding.reminderTimeTv.setVisibility(View.GONE);
                reminderEnabled = false;
                reminderTime = 0L;
            }
        });


    }

    private void showDateTimePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(TextNoteActivity.this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    @SuppressLint("SetTextI18n") TimePickerDialog timePicker = new TimePickerDialog(TextNoteActivity.this,
                            (timeView, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                // ✅ Check if selected time is before now
                                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                    Toast.makeText(TextNoteActivity.this, "Please select a future time", Toast.LENGTH_SHORT).show();
                                    binding.switchReminder.setChecked(false);
                                    reminderEnabled = false;
                                    reminderTime = 0L;
                                    return;
                                }

                                reminderEnabled = true;
                                reminderTime = calendar.getTimeInMillis();

                                Date date = new Date(reminderTime);
                                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                                formattedDate = formatter.format(date);

                                binding.reminderTimeTv.setVisibility(View.VISIBLE);
                                binding.reminderTimeTv.setText("Remind On: " + formattedDate);

                                binding.switchReminder.setChecked(true); // force checked
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false);

                    timePicker.setOnCancelListener(dialog -> {
                        binding.switchReminder.setChecked(false);
                        reminderEnabled = false;
                        reminderTime = 0L;
                    });

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // ✅ Prevent selecting past dates
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());

        datePicker.setOnCancelListener(dialog -> {
            binding.switchReminder.setChecked(false);
            reminderEnabled = false;
            reminderTime = 0L;
        });

        datePicker.show();
    }


    public void validateData() {

        titleEd = binding.titleEt.getText().toString().trim();
        notesEd = binding.noteEt.getText().toString().trim();

        if (titleEd.isEmpty()) {
            binding.titleEt.setError("Enter Title");
            binding.titleEt.requestFocus();
        } else if (notesEd.isEmpty()) {
            binding.noteEt.setError("Enter Note");
            binding.noteEt.requestFocus();
        } else {
            uploadNote();
        }

    }

    private void uploadNote() {

        //show progressDialog
        progressDialog.setMessage("We Adding Your Note");
        progressDialog.show();

        databaseReference = firebaseDatabase.getReference("Notes").child(currentUId);

        notesModel.setDelete(String.valueOf(System.currentTimeMillis()));
        notesModel.setNotes(notesEd);
        notesModel.setSearch(titleEd.toLowerCase());
        notesModel.setTitle(titleEd);
        notesModel.setType("NOTETEXT");
        // save this
        notesModel.setReminderTime(reminderTime);
        notesModel.setReminderEnabled(reminderEnabled);

        Log.e("REMINDER_TIME", String.valueOf(reminderTime));

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Enable exact alarms in settings to use reminders", Toast.LENGTH_LONG).show();
            return;
        }

        String randomKey = databaseReference.push().getKey();

        assert randomKey != null;
        databaseReference.child(randomKey).setValue(notesModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(TextNoteActivity.this, "Text Note Added", Toast.LENGTH_SHORT).show();

                            if (reminderEnabled) {
                                scheduleReminder(TextNoteActivity.this, reminderTime, randomKey, titleEd, notesEd, currentUId);
                            }

                            Intent intent = new Intent(TextNoteActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(binding.getRoot(), "Notification permission granted", Snackbar.LENGTH_SHORT).show();
            } else {
                // 🚫 User denied → block reminder setup
                Snackbar.make(binding.getRoot(), "Allow Notifications For Reminders", Snackbar.LENGTH_LONG)
                        .setAction("Settings", v -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(intent);
                        })
                        .show();

                binding.switchReminder.setChecked(false); // force off
            }
        }
    }


}