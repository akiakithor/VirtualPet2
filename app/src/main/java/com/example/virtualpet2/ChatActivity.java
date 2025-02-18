package com.example.virtualpet2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button logoutButton;
    private ImageView sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference chatRef;
    private DatabaseReference petRef;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Intent serviceIntent = new Intent(this, PetBackgroundService.class);
        startService(serviceIntent);

        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userName = preferences.getString("userName", "Anonymous");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        logoutButton = findViewById(R.id.logoutButton);

        chatRef = FirebaseDatabase.getInstance().getReference("messages");
        petRef = FirebaseDatabase.getInstance().getReference("pet");

        chatAdapter = new ChatAdapter(messageList, userName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                Message message = new Message(userName, messageText, timestamp);
                message.setSeenBy(new ArrayList<>());  // Initialize seenBy list as ArrayList


                chatRef.push().setValue(message);
                messageInput.setText("");

                DatabaseReference taskRef = petRef.child("tasks/messagesSent");
                taskRef.get().addOnSuccessListener(snapshot -> {
                    int currentMessagesSent = snapshot.exists() && snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                    taskRef.setValue(currentMessagesSent + 1);
                });

                showPetStatusNotification(userName, messageText);
            }
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        // Ensure seenBy is initialized
                        if (message.getSeenBy() == null) {
                            message.setSeenBy(new ArrayList<>());  // Initialize if null
                        }

                        // Mark message as seen if not already marked
                        if (!message.getSeenBy().contains(userName)) {
                            message.getSeenBy().add(userName);
                            chatRef.child(dataSnapshot.getKey()).setValue(message);  // Update in Firebase
                        }

                        messageList.add(message);
                        if (!message.getSender().equals(userName)) {
                            showMessageNotification(message.getSender(), message.getMessage());
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });

    }

    private void showPetStatusNotification(String sender, String latestMessage) {
        petRef.child("pet").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                double health = snapshot.child("health").getValue(Double.class) != null ? snapshot.child("health").getValue(Double.class) : 100.0;
                double happiness = snapshot.child("happiness").getValue(Double.class) != null ? snapshot.child("happiness").getValue(Double.class) : 100.0;

                String channelId = "pet_status_channel";
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId, "Pet Status", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                Intent intent = new Intent(this, TaskActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_pet_status)
                        .setContentTitle("New message from " + sender)
                        .setContentText("Message: " + latestMessage + "\nHealth: " + health + ", Happiness: " + happiness)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                notificationManager.notify(1, builder.build());
            }
        });
    }

    private void showMessageNotification(String sender, String message) {
        String channelId = "message_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Message Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("New message from " + sender)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(2, builder.build());
    }
}
