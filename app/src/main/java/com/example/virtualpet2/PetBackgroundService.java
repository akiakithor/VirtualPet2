package com.example.virtualpet2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PetBackgroundService extends Service {
    private static final String CHANNEL_ID = "PetServiceChannel";
    private DatabaseReference petStatsRef;

    @Override
    public void onCreate() {
        super.onCreate();
        petStatsRef = FirebaseDatabase.getInstance().getReference("pet/stats");
        listenForPetStatUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Virtual Pet")
                .setContentText("Your pet's stats are being updated in real-time.")
                .setSmallIcon(R.drawable.ic_pet_notification) // Use an appropriate icon
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    private void listenForPetStatUpdates() {
        petStatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Update the notification with the latest pet stats
                    String petStatus = "Pet health: " + snapshot.child("health").getValue(Integer.class)
                            + " | Hunger: " + snapshot.child("hunger").getValue(Integer.class);

                    Notification notification = new NotificationCompat.Builder(PetBackgroundService.this, CHANNEL_ID)
                            .setContentTitle("Virtual Pet Stats")
                            .setContentText(petStatus)
                            .setSmallIcon(R.drawable.ic_pet_notification)
                            .build();
                    startForeground(1, notification);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database errors if needed
            }
        });
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Pet Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
