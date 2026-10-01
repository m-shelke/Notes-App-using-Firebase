package com.example.notesappusingfirebase.Reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.notesappusingfirebase.R;

//public class NotificationHelper {
//
//    private static final String CHANNEL_ID = "note_reminder";
//
//    public static void showNotification(Context context, String title, String message) {
//
//        NotificationManager manager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Note Reminders",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            manager.createNotificationChannel(channel);
//        }
//
//        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.baseline_timer_24)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setColor(context.getColor(R.color.blue))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .build();
//
//        manager.notify((int) System.currentTimeMillis(), notification);
//    }
//}



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.notesappusingfirebase.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "note_reminder";
    private static final String CHANNEL_NAME = "Note Reminders";

    public static void showNotification(Context context, String title, String message) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ✅ Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setDescription("Notifications for note reminders");
            manager.createNotificationChannel(channel);
        }

        // ✅ Build notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_timer_24) // must exist in drawable
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.blue)) // safe for all versions
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // sound, vibration, lights
                .build();

        // ✅ Use a stable unique ID (hash of title+message)
        int notificationId = (title + message).hashCode();
        manager.notify(notificationId, notification);
    }
}


