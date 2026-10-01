package com.example.notesappusingfirebase.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        String note = intent.getStringExtra("note");
        String noteId = intent.getStringExtra("noteId");
        String currentUId = intent.getStringExtra("currentUId");

        Log.e("RECEIVER_VALUES:",title+"\n"+note+"\n" );

        NotificationHelper.showNotification(context, title, note);

        // 🔥 Mark expired in Firebase
        assert currentUId != null;
        assert noteId != null;
        FirebaseDatabase.getInstance()
                .getReference("Notes")
                .child(currentUId)
                .child(noteId)
                .child("reminderExpired")
                .setValue(true);
    }
}

